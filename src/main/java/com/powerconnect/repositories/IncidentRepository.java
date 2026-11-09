package com.powerconnect.repositories;

import com.powerconnect.entities.Incident;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    // Spring writes the SQL for these from the method names
    List<Incident> findAllByOrderByCreatedAtDesc();                                // admin: everything

    List<Incident> findByAreaOrderByCreatedAtDesc(String area);                    // councillor: their ward

    // Community member: incidents they reported, plus incidents they follow (same problem)
    @Query("SELECT i FROM Incident i WHERE i.reportedBy = :userId "
         + "OR i.incidentId IN (SELECT f.incidentId FROM IncidentFollower f WHERE f.userId = :userId) "
         + "ORDER BY i.createdAt DESC")
    List<Incident> findMine(@Param("userId") Integer userId);

    // Duplicate detection: open incidents with the same problem in the same area
    @Query("SELECT i FROM Incident i WHERE i.area = :area AND i.category = :category "
         + "AND i.status NOT IN ('COMPLETED', 'CLOSED') ORDER BY i.createdAt DESC")
    List<Incident> findOpenSimilar(@Param("area") String area, @Param("category") String category);

    // Smart alerts: areas with many open incidents of the same service since a time.
    // Each row is [area, serviceType, how many], e.g. ["Matero", "ELECTRICITY", 3]
    @Query("SELECT i.area, i.serviceType, COUNT(i) FROM Incident i "
         + "WHERE i.createdAt >= :since AND i.status <> 'CLOSED' "
         + "GROUP BY i.area, i.serviceType HAVING COUNT(i) >= :minimum ORDER BY COUNT(i) DESC")
    List<Object[]> findHotspots(@Param("since") LocalDateTime since, @Param("minimum") long minimum);

    // Map: open incidents per area
    @Query("SELECT i.area, COUNT(i) FROM Incident i WHERE i.status <> 'CLOSED' GROUP BY i.area")
    List<Object[]> countOpenByArea();

    List<Incident> findByTechnicianIdOrderByCreatedAtDesc(Integer technicianId);  // technician: assigned to them

    // For the reports. Each row is [the value, how many incidents], e.g. ["WATER", 4]
    // (JPQL: like SQL, but uses the Java class name "Incident" instead of the table name)
    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countByStatus();

    @Query("SELECT i.serviceType, COUNT(i) FROM Incident i GROUP BY i.serviceType")
    List<Object[]> countByServiceType();

    @Query("SELECT i.area, COUNT(i) FROM Incident i GROUP BY i.area ORDER BY COUNT(i) DESC")
    List<Object[]> countByArea();
}
