package com.powerconnect.controllers;

import com.powerconnect.entities.IncidentPhoto;
import com.powerconnect.entities.User;
import com.powerconnect.repositories.IncidentPhotoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Photos of an incident, sent and stored as base64 text: "data:image/jpeg;base64,..."
@CrossOrigin
@RestController
public class PhotoController {

    public record PhotoRequest(String image) { }

    // About 3 MB of text = a photo of about 2 MB. The app shrinks photos before sending.
    private static final int MAX_LENGTH = 3_000_000;

    private final IncidentPhotoRepository photoRepository;
    private final LoginCheck loginCheck;

    public PhotoController(IncidentPhotoRepository photoRepository, LoginCheck loginCheck) {
        this.photoRepository = photoRepository;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/incidents/{id}/photos")
    public List<IncidentPhoto> list(@RequestHeader(value = "Token", required = false) String token,
                                    @PathVariable Integer id) {
        loginCheck.user(token);
        return photoRepository.findByIncidentIdOrderByPhotoIdAsc(id);
    }

    @PostMapping("/incidents/{id}/photos")
    public void add(@RequestHeader(value = "Token", required = false) String token,
                    @PathVariable Integer id,
                    @RequestBody PhotoRequest request) {
        User user = loginCheck.user(token);
        String image = request.image();
        if (image == null || !image.startsWith("data:image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That is not a picture");
        }
        if (image.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The picture is too big");
        }
        photoRepository.save(new IncidentPhoto(id, user.getUserId(), image));
    }

    // Remove a photo: only the person who added it, or an admin
    @DeleteMapping("/incidents/{id}/photos/{photoId}")
    public void remove(@RequestHeader(value = "Token", required = false) String token,
                       @PathVariable Integer id,
                       @PathVariable Integer photoId) {
        User user = loginCheck.user(token);
        IncidentPhoto photo = photoRepository.findById(photoId).orElse(null);
        if (photo == null || !photo.getIncidentId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }
        if (!user.getRole().equals("ADMIN") && !user.getUserId().equals(photo.getUploadedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only remove your own photos");
        }
        photoRepository.delete(photo);
    }
}
