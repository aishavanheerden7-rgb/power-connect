package com.powerconnect.controllers;

import com.powerconnect.entities.Incident;
import com.powerconnect.entities.IncidentFollower;
import com.powerconnect.entities.IncidentUpdate;
import com.powerconnect.entities.User;
import com.powerconnect.repositories.IncidentFollowerRepository;
import com.powerconnect.repositories.IncidentRepository;
import com.powerconnect.repositories.IncidentUpdateRepository;
import com.powerconnect.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Everything to do with incidents.
//
// Status flow:
//   REPORTED -> ASSIGNED -> DISPATCHED -> IN_PROGRESS -> COMPLETED or NOT_COMPLETED -> CLOSED
//   (community member)  (admin)   (technician ...........................)   (community member or admin)
//
//   If the work is COMPLETED but the problem is still there, the community member
//   reopens it (REOPENED) and the admin assigns a technician again.
@CrossOrigin   // lets the web version of the app (a different address) call this API
@RestController
public class IncidentController {

    // Smart alerts: this many open incidents of one service in one area within 24 hours
    // probably have one cause (e.g. a broken transformer), so we warn the admin.
    public static final int HOTSPOT_MINIMUM = 3;
    public static final int HOTSPOT_HOURS = 24;

    // What the app sends in the request body
    public record AssignRequest(Integer technicianId) { }
    public record StatusRequest(String status, String note) { }
    public record CloseRequest(String note, Integer rating) { }
    public record ReopenRequest(String note) { }

    private final IncidentRepository incidentRepository;
    private final IncidentUpdateRepository updateRepository;
    private final IncidentFollowerRepository followerRepository;
    private final UserRepository userRepository;
    private final AlertSender alerts;
    private final LoginCheck loginCheck;

    public IncidentController(IncidentRepository incidentRepository,
                              IncidentUpdateRepository updateRepository,
                              IncidentFollowerRepository followerRepository,
                              UserRepository userRepository,
                              AlertSender alerts,
                              LoginCheck loginCheck) {
        this.incidentRepository = incidentRepository;
        this.updateRepository = updateRepository;
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
        this.alerts = alerts;
        this.loginCheck = loginCheck;
    }

    // The list each role sees:
    //   ADMIN      -> every incident
    //   TECHNICIAN -> incidents assigned to them
    //   COUNCILLOR -> incidents in their ward (area)
    //   COMMUNITY  -> incidents they reported or follow
    @GetMapping("/incidents")
    public List<Incident> list(@RequestHeader(value = "Token", required = false) String token) {
        User user = loginCheck.user(token);
        if (user.getRole().equals("ADMIN")) {
            return incidentRepository.findAllByOrderByCreatedAtDesc();
        }
        if (user.getRole().equals("TECHNICIAN")) {
            return incidentRepository.findByTechnicianIdOrderByCreatedAtDesc(user.getUserId());
        }
        if (user.getRole().equals("COUNCILLOR")) {
            return incidentRepository.findByAreaOrderByCreatedAtDesc(user.getArea());
        }
        return incidentRepository.findMine(user.getUserId());
    }

    // One incident, with its latest status
    @GetMapping("/incidents/{id}")
    public Incident getOne(@RequestHeader(value = "Token", required = false) String token,
                           @PathVariable Integer id) {
        loginCheck.user(token);
        return findIncident(id);
    }

    // COMMUNITY, before reporting: is the same problem already reported in this area?
    // e.g. GET /incidents/similar?area=Matero&category=Power outage
    @GetMapping("/incidents/similar")
    public List<Incident> similar(@RequestHeader(value = "Token", required = false) String token,
                                  @RequestParam String area,
                                  @RequestParam String category) {
        loginCheck.user(token);
        return incidentRepository.findOpenSimilar(area, category);
    }

    // COMMUNITY: "Yes, I have the same problem" - follow that incident instead of reporting a new one
    @PostMapping("/incidents/{id}/follow")
    public Incident follow(@RequestHeader(value = "Token", required = false) String token,
                           @PathVariable Integer id) {
        User user = loginCheck.userWithRole(token, "COMMUNITY");
        Incident incident = findIncident(id);
        boolean alreadyFollowing = followerRepository.existsByIncidentIdAndUserId(id, user.getUserId());
        if (!user.getUserId().equals(incident.getReportedBy()) && !alreadyFollowing) {
            followerRepository.save(new IncidentFollower(id, user.getUserId()));
            updateRepository.save(new IncidentUpdate(id, incident.getStatus(),
                    user.getFullName() + " has the same problem", user.getUserId()));
        }
        return incident;
    }

    // COMMUNITY: report a new problem (water, electricity or sanitation)
    @PostMapping("/incidents")
    @Transactional   // everything below is saved together, or not at all
    public Incident create(@RequestHeader(value = "Token", required = false) String token,
                           @RequestBody Incident incident) {
        User user = loginCheck.userWithRole(token, "COMMUNITY");
        incident.setReportedBy(user.getUserId());
        incident.setTechnicianId(null);
        incident.setStatus("REPORTED");
        incident.setCompletedAt(null);   // the app may not set these, so clear anything that was sent
        incident.setClosedAt(null);
        incident.setRating(null);
        Incident saved = incidentRepository.save(incident);
        Integer id = saved.getIncidentId();

        updateRepository.save(new IncidentUpdate(id, "REPORTED", "Incident reported", user.getUserId()));
        String message = "New incident: " + saved.getCategory() + " in " + saved.getArea();
        alerts.toAdmins(id, "NEW_INCIDENT", message);
        alerts.toCouncillors(saved, "NEW_INCIDENT", message);
        checkForHotspot(saved);
        return saved;
    }

    // Smart alert: if this incident makes exactly HOTSPOT_MINIMUM open incidents of the same
    // service in the same area within HOTSPOT_HOURS, warn the admins (once, not for every new one).
    private void checkForHotspot(Incident incident) {
        LocalDateTime since = LocalDateTime.now().minusHours(HOTSPOT_HOURS);
        for (Object[] row : incidentRepository.findHotspots(since, HOTSPOT_MINIMUM)) {
            String area = (String) row[0];
            String service = (String) row[1];
            long count = (Long) row[2];
            if (area.equals(incident.getArea()) && service.equals(incident.getServiceType()) && count == HOTSPOT_MINIMUM) {
                alerts.toAdmins(incident.getIncidentId(), "HOTSPOT",
                        count + " " + service + " incidents in " + area + " - " + ReportController.likelyCause(service));
            }
        }
    }

    // Anyone logged in: the timeline of an incident (every status change with its time)
    @GetMapping("/incidents/{id}/updates")
    public List<IncidentUpdate> timeline(@RequestHeader(value = "Token", required = false) String token,
                                         @PathVariable Integer id) {
        loginCheck.user(token);
        return updateRepository.findByIncidentIdOrderByCreatedAtAscUpdateIdAsc(id);
    }

    // ADMIN: the list of technicians to choose from (the app shows the ones for the right service)
    @GetMapping("/technicians")
    public List<User> technicians(@RequestHeader(value = "Token", required = false) String token) {
        loginCheck.userWithRole(token, "ADMIN");
        return userRepository.findByRole("TECHNICIAN");
    }

    // ADMIN: give the incident to a technician.
    // Also used to re-assign an incident that was NOT_COMPLETED or REOPENED.
    @PutMapping("/incidents/{id}/assign")
    @Transactional
    public Incident assign(@RequestHeader(value = "Token", required = false) String token,
                           @PathVariable Integer id,
                           @RequestBody AssignRequest request) {
        User admin = loginCheck.userWithRole(token, "ADMIN");
        Incident incident = findIncident(id);
        if (incident.getStatus().equals("COMPLETED") || incident.getStatus().equals("CLOSED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This incident is already finished");
        }
        User technician = userRepository.findById(request.technicianId()).orElse(null);
        if (technician == null || !technician.getRole().equals("TECHNICIAN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That user is not a technician");
        }

        incident.setTechnicianId(technician.getUserId());
        incident.setStatus("ASSIGNED");
        incident.setCompletedAt(null);
        incidentRepository.save(incident);

        String message = "Technician " + technician.getFullName() + " has been assigned to your incident";
        updateRepository.save(new IncidentUpdate(id, "ASSIGNED", message, admin.getUserId()));
        alerts.toCommunity(incident, "ASSIGNED", message);
        alerts.toUser(technician.getUserId(), id, "NEW_JOB",
                "New job: " + incident.getCategory() + " in " + incident.getArea());
        return incident;
    }

    // TECHNICIAN: move the job along: DISPATCHED, IN_PROGRESS, COMPLETED or NOT_COMPLETED
    @PutMapping("/incidents/{id}/status")
    @Transactional
    public Incident updateStatus(@RequestHeader(value = "Token", required = false) String token,
                                 @PathVariable Integer id,
                                 @RequestBody StatusRequest request) {
        User technician = loginCheck.userWithRole(token, "TECHNICIAN");
        Incident incident = findIncident(id);
        if (!technician.getUserId().equals(incident.getTechnicianId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This job is not assigned to you");
        }
        if (incident.getStatus().equals("CLOSED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This incident is closed");
        }
        String status = request.status();
        if (!List.of("DISPATCHED", "IN_PROGRESS", "COMPLETED", "NOT_COMPLETED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + status);
        }

        incident.setStatus(status);
        if (status.equals("COMPLETED") || status.equals("NOT_COMPLETED")) {
            incident.setCompletedAt(LocalDateTime.now());   // the "work was done" timestamp
        }
        incidentRepository.save(incident);

        updateRepository.save(new IncidentUpdate(id, status, request.note(), technician.getUserId()));
        alerts.toCommunity(incident, status, "Your incident is now " + status.replace('_', ' ').toLowerCase());

        // The admin only hears about the end of the job (not every step), because they may need to act:
        // close it, or assign someone again
        if (status.equals("COMPLETED")) {
            alerts.toAdmins(id, "JOB_COMPLETED", technician.getFullName() + " completed the work");
        }
        if (status.equals("NOT_COMPLETED")) {
            alerts.toAdmins(id, "JOB_NOT_COMPLETED", technician.getFullName() + " could not complete the work: "
                    + (request.note() == null ? "" : request.note()));
        }
        return incident;
    }

    // Close the incident once the work is finished:
    //   - the community member who reported it confirms it is fixed (status COMPLETED), or
    //   - the admin closes it (COMPLETED or NOT_COMPLETED)
    @PutMapping("/incidents/{id}/close")
    @Transactional
    public Incident close(@RequestHeader(value = "Token", required = false) String token,
                          @PathVariable Integer id,
                          @RequestBody CloseRequest request) {
        User user = loginCheck.user(token);
        Incident incident = findIncident(id);

        boolean isAdmin = user.getRole().equals("ADMIN");
        boolean isReporter = user.getUserId().equals(incident.getReportedBy());
        if (!isAdmin && !isReporter) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot close this incident");
        }
        boolean canClose = incident.getStatus().equals("COMPLETED")
                || (isAdmin && incident.getStatus().equals("NOT_COMPLETED"));
        if (!canClose) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The work must be finished before closing");
        }

        // The community member can give 1 to 5 stars when they close it
        if (isReporter && request.rating() != null) {
            if (request.rating() < 1 || request.rating() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be 1 to 5 stars");
            }
            incident.setRating(request.rating());
        }

        incident.setStatus("CLOSED");
        incident.setClosedAt(LocalDateTime.now());
        incidentRepository.save(incident);

        String note = request.note() != null && !request.note().isBlank() ? request.note() : "Incident closed";
        updateRepository.save(new IncidentUpdate(id, "CLOSED", note, user.getUserId()));
        if (isAdmin) {
            // tell the community member; they don't need an alert if they closed it themselves
            alerts.toCommunity(incident, "CLOSED", "Your incident has been closed");
        } else {
            // the community member confirmed it is fixed: tell the admin (with the stars, if given)
            String stars = incident.getRating() == null ? "" : " (" + incident.getRating() + " stars)";
            alerts.toAdmins(id, "CLOSED_BY_MEMBER", user.getFullName() + " confirmed it is fixed" + stars);
        }
        return incident;
    }

    // COMMUNITY: the technician said COMPLETED, but the problem is still there.
    // The incident goes back to the admin to assign a technician again.
    @PutMapping("/incidents/{id}/reopen")
    @Transactional
    public Incident reopen(@RequestHeader(value = "Token", required = false) String token,
                           @PathVariable Integer id,
                           @RequestBody ReopenRequest request) {
        User user = loginCheck.userWithRole(token, "COMMUNITY");
        Incident incident = findIncident(id);
        if (!user.getUserId().equals(incident.getReportedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your incident");
        }
        if (!incident.getStatus().equals("COMPLETED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed work can be reopened");
        }

        incident.setStatus("REOPENED");
        incident.setCompletedAt(null);
        incidentRepository.save(incident);

        String note = request.note() != null && !request.note().isBlank() ? request.note() : "Still not fixed";
        updateRepository.save(new IncidentUpdate(id, "REOPENED", note, user.getUserId()));
        alerts.toAdmins(id, "REOPENED", "Incident reopened - still broken: " + note);
        return incident;
    }

    private Incident findIncident(Integer id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }
}
