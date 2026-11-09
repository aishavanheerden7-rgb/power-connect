package com.powerconnect.controllers;

import com.powerconnect.entities.Contact;
import com.powerconnect.repositories.ContactRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// "Who to contact": hotlines and councillors.
// Anyone can see them (even before logging in). Only the admin can add, change or delete them.
@CrossOrigin
@RestController
public class ContactController {

    private final ContactRepository contactRepository;
    private final LoginCheck loginCheck;

    public ContactController(ContactRepository contactRepository, LoginCheck loginCheck) { //DI
        this.contactRepository = contactRepository;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/contacts")
    public List<Contact> contacts() {
        return contactRepository.findAllByOrderByContactIdAsc();
    }
    
    //GET - read
    //Post - create
    //Put - update
    //Delete - delete

    @PostMapping("/contacts")
    public Contact add(@RequestHeader(value = "Token", required = false) String token,
                       @RequestBody Contact contact) {
        loginCheck.userWithRole(token, "ADMIN");
        checkRequired(contact);
        return contactRepository.save(contact);
    }

    @PutMapping("/contacts/{id}")
    public Contact change(@RequestHeader(value = "Token", required = false) String token,
                          @PathVariable Integer id,
                          @RequestBody Contact changes) {
        loginCheck.userWithRole(token, "ADMIN");
        checkRequired(changes);
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        contact.setName(changes.getName());
        contact.setTitle(changes.getTitle());
        contact.setServiceType(changes.getServiceType());
        contact.setArea(changes.getArea());
        contact.setPhone(changes.getPhone());
        contact.setEmail(changes.getEmail());
        return contactRepository.save(contact);
    }

    @DeleteMapping("/contacts/{id}")
    public void delete(@RequestHeader(value = "Token", required = false) String token,
                       @PathVariable Integer id) {
        loginCheck.userWithRole(token, "ADMIN");
        contactRepository.deleteById(id);
    }

    //validation
    private void checkRequired(Contact contact) {
        if (contact.getName() == null || contact.getName().isBlank()
                || contact.getTitle() == null || contact.getTitle().isBlank()
                || contact.getPhone() == null || contact.getPhone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, title and phone are required");
        }
    }
}
