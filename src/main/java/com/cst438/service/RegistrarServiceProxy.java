package com.cst438.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void testMessage(String message) {
        sendMessage(message);
    }

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        try {
            System.out.println("Receive from Registrar " + message);
            String[] parts = message.split(" ", 2);
            switch (parts[0]) {
                case "addAssignment":
                    break;
                case "updateAssignment":
                    break;
                case "deleteAssignment":
                    break;
                case "addCourse":
                    break;
                case "updateCourse":
                    break;
                case "deleteCourse":
                    break;
                case "addEnrollment":
                    break;
                case "updateEnrollment":
                    break;
                case "deleteEnrollment":
                    break;
                default:
                    System.out.println("Option not implemented: " + parts[0]);
            }
        } catch (Exception e) {
            System.out.println("Exception in receivedFromRegistrar " + e.getMessage());
        }
    } 

    private void sendMessage(String s) {
        System.out.println("Gradebook to Registrar " + s);
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
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