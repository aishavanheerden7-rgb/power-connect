package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One row of "faqs": some keywords and the answer in each language
@Entity
@Table(name = "faqs")
public class Faq {

    @Id
    @Column(name = "faq_id")
    private Integer faqId;

    private String keywords;    // comma-separated, e.g. "pipe,burst,leak"

    @Column(name = "answer_en")
    private String answerEn;

    @Column(name = "answer_bem")
    private String answerBem;

    @Column(name = "answer_ny")
    private String answerNy;

    public String getKeywords() { return keywords; }

    // Does the user's message contain one of our keywords?
    public boolean matches(String message) {
        for (String keyword : keywords.split(",")) {
            if (message.contains(keyword.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // Pick the answer in the user's language (English if we don't know the language)
    public String answerIn(String language) {
        if ("bem".equals(language)) return answerBem;
        if ("ny".equals(language)) return answerNy;
        return answerEn;
    }
}
