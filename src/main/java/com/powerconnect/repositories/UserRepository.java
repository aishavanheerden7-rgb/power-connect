package com.powerconnect.repositories;

import com.powerconnect.entities.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring writes the SQL for these methods from their names
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);          // null if nobody has that email

    User findByToken(String token);          // null if the token is not valid

    List<User> findByRole(String role);      // e.g. all TECHNICIANs

    List<User> findByRoleAndArea(String role, String area);   // e.g. the COUNCILLOR for Matero

    List<User> findAllByOrderByRoleAscFullNameAsc();          // admin: everyone, grouped by role

    long countByRole(String role);           // e.g. how many COMMUNITY members (to make the next code, e.g. CM-005)
}
