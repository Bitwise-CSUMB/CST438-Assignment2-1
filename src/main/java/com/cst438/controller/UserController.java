package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/*
 * CRUD apis for User entity
 *     List all users,
 *     post new user,
 *     update user - only selected fields name, email, type
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private GradebookServiceProxy gradebookServiceProxy;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {
        final List<User> users = userRepository.findAllByOrderByIdAsc();
        return users.stream().map(UserDTO::fromEntity).collect(Collectors.toList());
    }

    @PostMapping("/users")
    public UserDTO createUser(@RequestBody final UserDTO userDTO) {

        final User user = new User();
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());

        // create password and encrypt it
        final String password = userDTO.name() + "2024";
        final String encPassword = encoder.encode(password);
        user.setPassword(encPassword);

        validateUserType(userDTO);
        user.setType(userDTO.type());

        final UserDTO newDTO = UserDTO.fromEntity(userRepository.save(user));
        gradebookServiceProxy.addUser(newDTO);
        return newDTO;
    }

    @PutMapping("/users")
    public UserDTO updateUser(@RequestBody final UserDTO userDTO) {

        final User user = userRepository.findById(userDTO.id()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user id not found"));

        user.setName(userDTO.name());
        user.setEmail(userDTO.email());

        validateUserType(userDTO);
        user.setType(userDTO.type());

        final UserDTO newDTO = UserDTO.fromEntity(userRepository.save(user));
        gradebookServiceProxy.updateUser(newDTO);
        return newDTO;
    }

    @DeleteMapping("/users/{id}")
    public void updateUser(@PathVariable("id") final int id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        gradebookServiceProxy.deleteUser(id);
    }

    private void validateUserType(UserDTO userDTO) throws ResponseStatusException {
        final String type = userDTO.type();
        if (!type.equals("STUDENT") && !type.equals("INSTRUCTOR") && !type.equals("ADMIN")) {
            // invalid type
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid user type");
        }
    }
}
