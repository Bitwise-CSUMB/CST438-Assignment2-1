
package com.cst438.controller;

import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.function.Supplier;

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

    public static User validateInstructor(final UserRepository userRepository, final Principal principal) {

        final User user = userRepository.findByEmail(principal.getName());

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown user");
        }

        if (!user.getType().equals("INSTRUCTOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user (not an INSTRUCTOR)");
        }

        return user;
    }

    public static void validateInstructorForSection(final User user, final Section section) {
        if (!section.getInstructorEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instructor does not teach sectionNo");
        }
    }

    public record ValidateInstructorRet(
        User user,
        Section section)
    {
    }

    public static ValidateInstructorRet validateInstructorAndInstructorForSection(
        final UserRepository userRepository,
        final Principal principal,
        final Section section)
    {
        if (section == null) {
            throw unknownSectionErr();
        }
        return validateInstructorAndInstructorForSection(userRepository, principal, () -> section);
    }

    public static ValidateInstructorRet validateInstructorAndInstructorForSection(
        final UserRepository userRepository,
        final Principal principal,
        final SectionRepository sectionRepository,
        final int sectionNo)
    {
        return validateInstructorAndInstructorForSection(userRepository, principal,
            sectionRepository.findById(sectionNo).orElseThrow(ControllerUtils::unknownSectionErr));
    }

    private static ValidateInstructorRet validateInstructorAndInstructorForSection(
        final UserRepository userRepository,
        final Principal principal,
        final Supplier<Section> sectionSupplier)
    {
        final User user = validateInstructor(userRepository, principal);
        final Section section = sectionSupplier.get();
        validateInstructorForSection(user, section);
        return new ValidateInstructorRet(user, section);
    }

    private static ResponseStatusException unknownSectionErr() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown sectionNo");
    }
}
