package ch.cern.todo.config;

import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import ch.cern.todo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(final UserRepository userRepository, final TaskRepository taskRepository,
                           final CategoryRepository categoryRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        taskRepository.deleteAll();

        if (userRepository.count() == 0) {
            final User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .build();
            final User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("test123"))
                    .role("ROLE_USER")
                    .build();

            userRepository.saveAll(List.of(admin, user));

            final Category work = Category.builder().name("Work").description("Work related tasks").createdBy(admin).build();
            final Category personal = Category.builder().name("Personal").description("Personal tasks").createdBy(user).build();
            final Category personal1 = Category.builder().name("Personal1").description("Personal tasks").createdBy(user).build();
            final Category personal2 = Category.builder().name("Personal2").description("Personal tasks").createdBy(user).build();
            final Category personal3 = Category.builder().name("Personal3").description("Personal tasks").createdBy(user).build();
            final Category personal4 = Category.builder().name("Personal4").description("Personal tasks").createdBy(user).build();
            final Category personal5 = Category.builder().name("Personal5").description("Personal tasks").createdBy(user).build();
            categoryRepository.saveAll(List.of(work, personal, personal1, personal2, personal3, personal4, personal5));

            final Task task1 = Task.builder().name("Complete report").description("Finish project report").deadline(LocalDateTime.now().plusDays(2)).category(work).createdBy(admin).build();
            final Task task2 = Task.builder().name("Buy groceries").description("Milk, eggs, bread").deadline(LocalDateTime.now().plusDays(1)).category(personal).createdBy(user).build();
            final Task task3 = Task.builder().name("Buy groceries").description("Milk, eggs, bread").deadline(LocalDateTime.now().plusDays(1)).category(personal).createdBy(user).build();
            final Task task4 = Task.builder().name("Buy groceries").description("Milk, eggs, bread").deadline(LocalDateTime.now().plusDays(1)).category(personal).createdBy(user).build();
            final Task task5 = Task.builder().name("Buy groceries").description("Milk, eggs, bread").deadline(LocalDateTime.now().plusDays(1)).category(personal).createdBy(user).build();
            final Task task6 = Task.builder().name("Buy groceries").description("Milk, eggs, bread").deadline(LocalDateTime.now().plusDays(1)).category(personal).createdBy(user).build();
            taskRepository.saveAll(List.of(task1, task2, task3, task4, task5, task6));
        }
    }
}
