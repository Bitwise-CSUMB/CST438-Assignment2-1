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

import java.util.ArrayList;
import java.util.List;

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
    GradebookServiceProxy gradebookServiceProxy;

    @Autowired
    UserRepository userRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {

        List<User> users = userRepository.findAllByOrderByIdAsc();
        List<UserDTO> userDTO_list = new ArrayList<>();
        for (User u: users) {
            userDTO_list.add(new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getType()));
        }
        return userDTO_list;
    }

    @PostMapping("/users")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());

        // create password and encrypt it
        String password = userDTO.name()+"2024";
        String enc_password = encoder.encode(password);
        user.setPassword(enc_password);

        user.setType(userDTO.type());
        if (!userDTO.type().equals("STUDENT") &&
                !userDTO.type().equals("INSTRUCTOR") &&
                !userDTO.type().equals("ADMIN")) {
            // invalid type
            throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "invalid user type");
        }
        userRepository.save(user);
        gradebookServiceProxy.addUser(UserDTO.fromEntity(user));
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
    }

    @PutMapping("/users")
    public UserDTO updateUser(@RequestBody UserDTO userDTO) {
        User user = userRepository.findById(userDTO.id()).orElse(null);
        if (user==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user id not found");
        }
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        user.setType(userDTO.type());
        if (!userDTO.type().equals("STUDENT") &&
                !userDTO.type().equals("INSTRUCTOR") &&
                !userDTO.type().equals("ADMIN")) {
            // invalid type
            throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "invalid user type");
        }
        userRepository.save(user);
        gradebookServiceProxy.updateUser(UserDTO.fromEntity(user));
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
    }

    @DeleteMapping("/users/{id}")
    public void  updateUser(@PathVariable("id") int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user!=null) {
            userRepository.delete(user);
        }
        gradebookServiceProxy.deleteUser(id);

    }
}
