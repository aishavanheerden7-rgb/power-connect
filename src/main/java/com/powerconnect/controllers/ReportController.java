package com.powerconnect.controllers;

import com.powerconnect.entities.Incident;
import com.powerconnect.repositories.IncidentRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

// ADMIN: numbers for the Reports screen, including "smart alerts".
// ADMIN and COUNCILLOR: open incidents per area, for the map.
@CrossOrigin
@RestController
public class ReportController {

    private final IncidentRepository incidentRepository;
    private final LoginCheck loginCheck;

    public ReportController(IncidentRepository incidentRepository, LoginCheck loginCheck) {
        this.incidentRepository = incidentRepository;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/reports")
    public Map<String, Object> reports(@RequestHeader(value = "Token", required = false) String token) {
        loginCheck.userWithRole(token, "ADMIN");

        List<Incident> all = incidentRepository.findAll();

        // Average time from "reported" to "work finished", for the incidents that are finished
        long totalMinutes = 0;
        int finished = 0;
        for (Incident incident : all) {
            if (incident.getCompletedAt() != null) {
                totalMinutes += Duration.between(incident.getCreatedAt(), incident.getCompletedAt()).toMinutes();
                finished++;
            }
        }
        double averageHours = finished == 0 ? 0 : Math.round(totalMinutes / 60.0 / finished * 10) / 10.0;

        // Average star rating from community members (only incidents that were rated)
        int totalStars = 0;
        int rated = 0;
        for (Incident incident : all) {
            if (incident.getRating() != null) {
                totalStars += incident.getRating();
                rated++;
            }
        }
        double averageRating = rated == 0 ? 0 : Math.round(totalStars * 10.0 / rated) / 10.0;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total", all.size());
        report.put("averageHoursToComplete", averageHours);
        report.put("averageRating", averageRating);
        report.put("ratedCount", rated);
        report.put("byStatus", toMap(incidentRepository.countByStatus()));
        report.put("byServiceType", toMap(incidentRepository.countByServiceType()));
        report.put("byArea", toMap(incidentRepository.countByArea()));
        report.put("hotspots", hotspots());
        return report;
    }

    // Smart alerts (simple rules, no AI service needed):
    // 3 or more open incidents of the same service in the same area in the last 24 hours
    // probably have ONE cause, so it is better to send one team to fix that cause.
    private List<Map<String, Object>> hotspots() {
        LocalDateTime since = LocalDateTime.now().minusHours(IncidentController.HOTSPOT_HOURS);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : incidentRepository.findHotspots(since, IncidentController.HOTSPOT_MINIMUM)) {
            Map<String, Object> hotspot = new LinkedHashMap<>();
            hotspot.put("area", row[0]);
            hotspot.put("serviceType", row[1]);
            hotspot.put("count", row[2]);
            hotspot.put("likelyCause", likelyCause((String) row[1]));
            list.add(hotspot);
        }
        return list;
    }

    // What usually causes many problems of one service in one area at the same time
    public static String likelyCause(String serviceType) {
        if ("ELECTRICITY".equals(serviceType)) return "possible transformer or substation fault";
        if ("WATER".equals(serviceType)) return "possible burst main pipe or pump failure";
        return "possible blocked main sewer line";
    }

    // Open incidents per area, e.g. {"Matero": 3, "Kabulonga": 1}
    @GetMapping("/map")
    public Map<String, Long> map(@RequestHeader(value = "Token", required = false) String token) {
        String role = loginCheck.user(token).getRole();
        if (!role.equals("ADMIN") && !role.equals("COUNCILLOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins and councillors can see the map");
        }
        return toMap(incidentRepository.countOpenByArea());
    }

    // Turn query rows like ["WATER", 4] into JSON like {"WATER": 4}
    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
