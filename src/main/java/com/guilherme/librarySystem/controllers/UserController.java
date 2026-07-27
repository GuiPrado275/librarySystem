package com.guilherme.librarySystem.controllers;

import com.guilherme.librarySystem.models.User;
import com.guilherme.librarySystem.models.dto.UserCreateDTO;
import com.guilherme.librarySystem.models.dto.UserUpdateDTO;
import com.guilherme.librarySystem.repositories.UserRepository;
import com.guilherme.librarySystem.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> findByID(@PathVariable Long id) {
        User user = this.userService.findById(id);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        User user = this.userService.fromDTO(userCreateDTO);
        User newUser = this.userService.create(user);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newUser.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        User user = this.userService.fromDTO(userUpdateDTO);
        user.setId(id);
        this.userService.update(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = this.userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

}
