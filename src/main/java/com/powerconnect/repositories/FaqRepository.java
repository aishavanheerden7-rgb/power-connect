package com.powerconnect.repositories;

import com.powerconnect.entities.Faq;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Integer> {

    List<Faq> findAllByOrderByFaqIdAsc();    // in the order they were added: the first match wins
}
