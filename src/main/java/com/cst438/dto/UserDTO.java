package com.cst438.dto;

import com.cst438.domain.User;

/*
 * Data Transfer Object for user data
 * A user can be a STUDENT, ADMIN, or INSTRUCTOR type
 */
public record UserDTO(
    int id,
    String name,
    String email,
    String type)
{
    public static UserDTO fromEntity(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
    }
}
