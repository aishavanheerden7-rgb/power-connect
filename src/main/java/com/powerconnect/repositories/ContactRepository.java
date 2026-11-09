package com.powerconnect.repositories;

import com.powerconnect.entities.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    List<Contact> findAllByOrderByContactIdAsc();
}
