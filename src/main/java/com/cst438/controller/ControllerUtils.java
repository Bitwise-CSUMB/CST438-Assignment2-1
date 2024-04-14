
package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

public class ControllerUtils {

    public static User validateStudent(final UserRepository userRepository, final Principal principal) {

        final User user = userRepository.findByEmail(principal.getName());

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown user");
        }

        if (!user.getType().equals("STUDENT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user (not a STUDENT)");
        }

        return user;
    }
}
