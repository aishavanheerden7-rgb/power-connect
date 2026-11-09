package com.powerconnect.repositories;

import com.powerconnect.entities.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByIncidentIdOrderByMessageIdAsc(Integer incidentId);   // oldest first, like a chat
}
