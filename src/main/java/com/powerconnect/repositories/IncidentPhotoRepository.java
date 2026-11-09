package com.powerconnect.repositories;

import com.powerconnect.entities.IncidentPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentPhotoRepository extends JpaRepository<IncidentPhoto, Integer> {

    List<IncidentPhoto> findByIncidentIdOrderByPhotoIdAsc(Integer incidentId);
}
