package ch.cern.todo.service;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.*;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import ch.cern.todo.exceptions.DuplicateResourceFoundException;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.exceptions.UserNotFoundException;
import ch.cern.todo.mapper.TaskConvertor;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import ch.cern.todo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TaskService {


    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskConvertor taskConvertor;
    private final CategoryService categoryService;

    public TaskService(final TaskRepository taskRepository, final UserRepository userRepository, final CategoryRepository categoryRepository, final TaskConvertor taskConvertor, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.taskConvertor = taskConvertor;
        this.categoryService = categoryService;
    }

    public Page<TaskResponse> getAllCategories(final TaskRequestParams requestParams, final Pageable pageable) {
        if (isNotAdmin() && !requestParams.getCreatedBy().equals(SecurityUtil.getLoggedInUsername())) {
            throw new UnauthorizedException("You are not allowed to see task created by " + requestParams.getCreatedBy() + "!");
        }

        final Specification<Task> spec = createSpecification(requestParams);
        final Page<Task> tasks = taskRepository.findAll(spec, pageable);

        final List<TaskResponse> taskResponses = tasks.stream().map(taskConvertor::convertToTaskResponse).toList();

        return new PageImpl<>(taskResponses, pageable, tasks.getTotalElements());
    }

    public TaskResponse createTask(final TaskRequest taskRequest, final String userName) {
        final User user = userRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException(userName));
        final Category category = categoryRepository.findByName(taskRequest.getCategory()).orElseThrow(() -> new ResourceNotFoundException(taskRequest.getCategory()));

        final Optional<Task> optionalTask = taskRepository.findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user);

        if(optionalTask.isPresent()) {
            throw new DuplicateResourceFoundException("Task", "name", taskRequest.getName());
        }

        final Task taskToSave = taskConvertor.convertToTaskEntity(taskRequest, user, category);

        return taskConvertor.convertToTaskResponse(taskRepository.save(taskToSave));
    }

    public TaskResponse updateTask(final TaskRequest taskRequest, final String userName, final Long id) {
        final Task task = findById(id);

        if (isNotAdmin() && !task.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to update this task!");
        }

        final Category category = categoryRepository.findByName(taskRequest.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException(taskRequest.getCategory()));

        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());
        task.setDeadline(taskRequest.getDeadline());
        task.setCategory(category);

        final Task updatedTask = taskRepository.save(task);

        return taskConvertor.convertToTaskResponse(updatedTask);
    }

    public void deleteTask(final String userName, final Long id) {
        final Task task = findById(id);

        if (isNotAdmin() && !task.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to delete this category!");
        }

        taskRepository.delete(task);
    }

    private Specification<Task> createSpecification(final TaskRequestParams params) {
        return (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(params.getCreatedBy())) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy").get("username"), params.getCreatedBy()));
            }

            if (Objects.nonNull(params.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + params.getName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(params.getDescription())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + params.getDescription().toLowerCase() + "%"));
            }

            if (Objects.nonNull(params.getCategory())) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("name"), params.getCategory()));
            }

            if (Objects.nonNull(params.getDeadline())) {
                predicates.add(criteriaBuilder.lessThan(root.get("deadline"), params.getDeadline()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean isNotAdmin() {
        return SecurityUtil.getAuthentication().getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private Task findById(final Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id.toString()));
    }

}
