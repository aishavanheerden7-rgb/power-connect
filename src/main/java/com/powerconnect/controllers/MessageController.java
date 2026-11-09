package com.powerconnect.controllers;

import com.powerconnect.entities.Incident;
import com.powerconnect.entities.Message;
import com.powerconnect.entities.User;
import com.powerconnect.repositories.IncidentRepository;
import com.powerconnect.repositories.MessageRepository;
import com.powerconnect.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Messages on an incident, e.g. the technician asks "Which house is it?" and the community member answers
@CrossOrigin
@RestController
public class MessageController {

    public record MessageRequest(String body) { }

    // What the app gets back: the message plus who wrote it (so the app can show the name)
    public record MessageView(Integer messageId, String senderName, String senderRole,
                              String body, LocalDateTime createdAt) { }

    private final MessageRepository messageRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AlertSender alerts;
    private final LoginCheck loginCheck;

    public MessageController(MessageRepository messageRepository, IncidentRepository incidentRepository,
                             UserRepository userRepository, AlertSender alerts, LoginCheck loginCheck) {
        this.messageRepository = messageRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.alerts = alerts;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/incidents/{id}/messages")
    public List<MessageView> list(@RequestHeader(value = "Token", required = false) String token,
                                  @PathVariable Integer id) {
        loginCheck.user(token);
        List<MessageView> views = new ArrayList<>();
        for (Message message : messageRepository.findByIncidentIdOrderByMessageIdAsc(id)) {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            views.add(new MessageView(message.getMessageId(),
                    sender == null ? "?" : sender.getFullName(),
                    sender == null ? "" : sender.getRole(),
                    message.getBody(), message.getCreatedAt()));
        }
        return views;
    }

    @PostMapping("/incidents/{id}/messages")
    public void send(@RequestHeader(value = "Token", required = false) String token,
                     @PathVariable Integer id,
                     @RequestBody MessageRequest request) {
        User sender = loginCheck.user(token);
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
        if (request.body() == null || request.body().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The message is empty");
        }
        messageRepository.save(new Message(id, sender.getUserId(), request.body().trim()));

        // Alert the people on the incident (but not the person who wrote it)
        String alert = "New message from " + sender.getFullName();
        if (!sender.getRole().equals("COMMUNITY")) {
            alerts.toCommunity(incident, "MESSAGE", alert);
        }
        if (incident.getTechnicianId() != null && !incident.getTechnicianId().equals(sender.getUserId())) {
            alerts.toUser(incident.getTechnicianId(), id, "MESSAGE", alert);
        }
        if (!sender.getRole().equals("ADMIN")) {
            alerts.toAdmins(id, "MESSAGE", alert);
        }
    }
}
