package com.powerconnect.repositories;

import com.powerconnect.entities.IncidentFollower;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentFollowerRepository extends JpaRepository<IncidentFollower, Integer> {

    List<IncidentFollower> findByIncidentId(Integer incidentId);             // everyone following one incident

    boolean existsByIncidentIdAndUserId(Integer incidentId, Integer userId); // already following?
}
