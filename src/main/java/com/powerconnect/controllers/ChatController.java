package com.powerconnect.controllers;

import com.powerconnect.entities.Faq;
import com.powerconnect.repositories.FaqRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// The chat bot: a simple FAQ bot.
// It goes through the faqs table in order, and replies with the first answer whose keywords
// appear in the user's message. Emergencies are first in the table, so they always win.
@CrossOrigin
@RestController
public class ChatController {

    public record ChatRequest(String message, String language) { }

    private static final String HELPLINE = "Helpline: +260 211 000 000 | Email: help@powerconnect.co.zm";

    private final FaqRepository faqRepository;

    public ChatController(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        String message = request.message() == null ? "" : request.message().toLowerCase();
        List<Faq> faqs = faqRepository.findAllByOrderByFaqIdAsc();

        Faq fallback = null;
        for (Faq faq : faqs) {
            if (faq.getKeywords().equals("default")) {
                fallback = faq;                 // remember the "I don't know" answer for later
            } else if (faq.matches(message)) {
                return Map.of("reply", faq.answerIn(request.language()), "helpline", HELPLINE);
            }
        }

        String reply = fallback != null ? fallback.answerIn(request.language())
                                        : "Sorry, I don't know that one yet. Please call or email us.";
        return Map.of("reply", reply, "helpline", HELPLINE);
    }
}
