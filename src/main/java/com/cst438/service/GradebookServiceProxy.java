package com.cst438.service;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.User;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GradebookServiceProxy {

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Autowired
    EnrollmentRepository enrollmentRepository;

    public void addCourse(CourseDTO course) {
        sendMessage("addCourse " + asJsonString(course));
    }

    public void updateCourse(CourseDTO course) {
        sendMessage("updateCourse " + asJsonString(course));
    }

    public void deleteCourse(String courseId) {
        sendMessage("deleteCourse " + courseId);
    }

    public void addSection(SectionDTO section) {
        sendMessage("addSection " + asJsonString(section));
    }

    public void updateSection(SectionDTO section) {
        sendMessage("updateSection " + asJsonString(section));
    }

    public void deleteSection(int sectionId) {
        sendMessage("deleteSection " + sectionId);
    }

    public void enrollInCourse(EnrollmentDTO enrollment){
        sendMessage("addEnrollment "+ asJsonString(enrollment));
    }

    public void dropCourse(int enrollmentId){
        sendMessage("dropEnrollment " + enrollmentId);
    }

    public void addUser(User user){
        sendMessage("addUser " + asJsonString(user));
    }
    public void updateUser(User user){
        sendMessage("updateUser " + asJsonString(user));
    }

    public void deleteUser(int userId){
        sendMessage("deleteUser " + userId);
    }

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message)  {
        try {
            System.out.println("Receive from Gradebook " + message);
            String[] parts = message.split(" ", 2);
            switch (parts[0]) {
                case "updateEnrollment":
                    EnrollmentDTO enrollmentDTO = fromJsonString(parts[1], EnrollmentDTO.class);
                    Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(enrollmentDTO.enrollmentId());
                    if (optionalEnrollment.isPresent()) {
                        Enrollment enrollment = optionalEnrollment.get();
                        // update the grade and save back to database
                        enrollment.setGrade(enrollmentDTO.grade());
                        enrollmentRepository.save(enrollment);
                    } else {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Invalid enrollment " + enrollmentDTO.enrollmentId());
                    }
                    break;
                default:
                    System.out.println("Option not implemented: " + parts[0]);
            }
        } catch (Exception e) {
            System.out.println("Exception in receivedFromGradebook " + e.getMessage());
        }
    } 

    private void sendMessage(String s) {
        System.out.println("Registrar to Gradebook " + s);
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}