package com.cst438.service;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class RegistrarServiceProxy {

    private final Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    public void sendUpdateEnrollment(EnrollmentDTO enrollmentDTO) {
        sendMessage("updateEnrollment " + asJsonString(enrollmentDTO));
    }

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        try {
            System.out.println("Receive from Registrar " + message);
            String[] parts = message.split(" ", 2);
            if (parts[0].contains("Course")) {
                switch (parts[0]) {
                    case "addCourse": {
                        // example "addCourse {"courseId":"cst238","title":"Data Structures","credits":5}"
                        newEntityFromDTO(
                            new Course(),                              // EntityType newEntity
                            fromJsonString(parts[1], CourseDTO.class), // DTOType dto
                            CourseDTO::courseId,                       // Function<DTOType, KeyType> idSupplier
                            Course::setCourseId,                       // BiConsumer<DTOType, KeyType> idConsumer
                            RegistrarServiceProxy::fillCourseFromDTO,  // BiFunction<EntityType, DTOType, Boolean> fillFunc
                            courseRepository                           // CrudRepository<EntityType, KeyType> repository
                        );
                        break;
                    }
                    case "updateCourse": {
                        // example "updateCourse {"courseId":"cst238","title":"Data Structures Updated","credits":10}"
                        updateEntityFromDTO(
                            "Course",                                  // String entityName,
                            fromJsonString(parts[1], CourseDTO.class), // DTOType dto,
                            CourseDTO::courseId,                       // Function<DTOType, KeyType> idSupplier,
                            courseRepository,                          // CrudRepository<EntityType, KeyType> repository,
                            RegistrarServiceProxy::fillCourseFromDTO   // BiFunction<EntityType, DTOType, Boolean> fillFunc
                        );
                        break;
                    }
                    case "deleteCourse": {
                        // example "deleteCourse cst238"
                        courseRepository.deleteById(parts[1]);
                        break;
                    }
                    default: {
                        System.out.println("Option not implemented: " + parts[0]);
                    }
                }
            } else if (parts[0].contains("Section")) {
                switch (parts[0]) {
                    case "addSection":
                        // example "addSection {"secNo":1000,"year":2023,"semester":"Fall","courseId":"cst363","secId":3
                        // ,"building":"052","room":"102","times":"M W 10:00-11:50","instructorName":"david wisneski","instructorEmail":"dwisneski@csumb.edu"}"
                        break;
                    case "updateSection":
                        // example "updateSection {"secNo":3,"year":2023,"semester":"Fall","courseId":"cst363","secId":3
                        // ,"building":"109","room":"102","times":"M W 10:00-11:50","instructorName":"david wisneski","instructorEmail":"jgross@csumb.edu"}"
                        break;
                    case "deleteSection":
                        // example "deleteSection 3"
                        break;
                    default:
                        System.out.println("Option not implemented: " + parts[0]);
                }
            } else if (parts[0].contains("Assignment")) {
                AssignmentDTO assignmentDTO = fromJsonString(parts[1], AssignmentDTO.class);
                Assignment a = assignmentRepository.findByAssignmentId(assignmentDTO.id());
                if (a == null) {
                    System.out.println("Error receiveFromRegistrar Assignment not found " + assignmentDTO.id());
                }
                switch (parts[0]) {
                    case "addAssignment":
                        assignmentRepository.save(a);
                        break;
                    case "updateAssignment":
                        a.setTitle(assignmentDTO.title());
                        java.sql.Date sqlDueDate = java.sql.Date.valueOf(assignmentDTO.dueDate());
                        a.setDueDate(sqlDueDate);
                        assignmentRepository.save(a);
                        break;
                    case "deleteAssignment":
                        assignmentRepository.delete(a);
                        break;
                    default:
                        System.out.println("Option not implemented: " + parts[0]);
                }
            } else if (parts[0].contains("Enrollment")) {
                switch (parts[0]) {
                    case "addEnrollment": {
                        newEntityFromDTO(
                            new Enrollment(),                              // EntityType newEntity
                            fromJsonString(parts[1], EnrollmentDTO.class), // DTOType dto
                            EnrollmentDTO::enrollmentId,                   // Function<DTOType, KeyType> idSupplier
                            Enrollment::setEnrollmentId,                   // BiConsumer<DTOType, KeyType> idConsumer
                            this::fillEnrollmentFromDTO,                   // BiFunction<EntityType, DTOType, Boolean> fillFunc
                            enrollmentRepository                           // CrudRepository<EntityType, KeyType> repository
                        );
                        break;
                    }
                    default: {
                        System.out.println("Option not implemented: " + parts[0]);
                    }
                }
            } else {
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

    private static boolean fillCourseFromDTO(Course course, CourseDTO enrollmentDTO) {
        course.setTitle(enrollmentDTO.title());
        course.setCredits(enrollmentDTO.credits());
        return true;
    }

    private boolean fillEnrollmentFromDTO(Enrollment enrollment, EnrollmentDTO enrollmentDTO) {

        // Update grade
        enrollment.setGrade(enrollmentDTO.grade());

        // Update user
        final User user = fetchEntity("User", userRepository::findById, enrollmentDTO, EnrollmentDTO::studentId);
        if (user == null) return false;
        enrollment.setUser(user);

        // Update section
        final Section section = fetchEntity("Section", sectionRepository::findById, enrollmentDTO,
            EnrollmentDTO::sectionId
        );
        if (section == null) return false;
        enrollment.setSection(section);

        return true;
    }

    private <EntityType, KeyType, DTOType> EntityType newEntityFromDTO(
        EntityType newEntity,
        DTOType dto,
        Function<DTOType, KeyType> idSupplier,
        BiConsumer<EntityType, KeyType> idConsumer,
        BiFunction<EntityType, DTOType, Boolean> fillFunc,
        CrudRepository<EntityType, KeyType> repository
    )
    {
        // Set new entity id
        idConsumer.accept(newEntity, idSupplier.apply(dto));

        // Fill new entity
        if (!fillFunc.apply(newEntity, dto)) return null;

        // Save to database
        return repository.save(newEntity);
    }

    private <EntityType, KeyType, DTOType> EntityType updateEntityFromDTO(
        String entityName,
        DTOType dto,
        Function<DTOType, KeyType> idSupplier,
        CrudRepository<EntityType, KeyType> repository,
        BiFunction<EntityType, DTOType, Boolean> fillFunc
    )
    {
        // Fetch and fill entity
        final EntityType entity = fillEntityFromDTO(entityName, repository::findById, idSupplier, dto, fillFunc);

        // Save to database
        if (entity != null) return repository.save(entity);
        return null;
    }

    private <EntityType, KeyType, DTOType> EntityType fillEntityFromDTO(
        String entityName,
        Function<KeyType, Optional<EntityType>> findFunc,
        Function<DTOType, KeyType> idSupplier,
        DTOType dto,
        BiFunction<EntityType, DTOType, Boolean> fillFunc
    )
    {
        // Fetch existing entity
        final EntityType entity = fetchEntity(entityName, findFunc, dto, idSupplier);
        if (entity == null) return null;

        // Fill entity
        if (!fillFunc.apply(entity, dto)) return null;
        return entity;
    }

    private <EntityType, KeyType, DTOType> EntityType fetchEntity(
        String name,
        Function<KeyType, Optional<EntityType>> findFunc,
        DTOType dto,
        Function<DTOType, KeyType> idSupplier
    )
    {
        // Fetch entity
        final KeyType id = idSupplier.apply(dto);
        final Optional<EntityType> optional = findFunc.apply(id);

        if (optional.isEmpty()) {
            System.out.println(name + " with id " + id + " not found");
            return null;
        }

        return optional.get();
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
