package com.powerconnect.repositories;

import com.powerconnect.entities.IncidentUpdate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentUpdateRepository extends JpaRepository<IncidentUpdate, Integer> {

    List<IncidentUpdate> findByIncidentIdOrderByCreatedAtAscUpdateIdAsc(Integer incidentId);  // oldest first = timeline
}
