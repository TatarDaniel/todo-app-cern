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

    public TaskService(final TaskRepository taskRepository, final UserRepository userRepository, final CategoryRepository categoryRepository, final TaskConvertor taskConvertor) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.taskConvertor = taskConvertor;
    }

    public List<TaskResponse> getAllTasks(final TaskRequestParams requestParams) {
        if(requestParams.getCreatedBy() == null && SecurityUtil.isNotAdmin()) {
            requestParams.setCreatedBy(SecurityUtil.getLoggedInUsername());
        }
        if (SecurityUtil.isNotAdmin() && !requestParams.getCreatedBy().equals(SecurityUtil.getLoggedInUsername())) {
            throw new UnauthorizedException("You are not allowed to see tasks created by " + requestParams.getCreatedBy() + "!");
        }

        final List<Task> tasks = taskRepository.findAll(createSpecification(requestParams));

        return tasks.stream().map(taskConvertor::convertToTaskResponse).toList();
    }

    public TaskResponse createTask(final TaskRequest taskRequest, final String userName) {
        final User user = userRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException(userName));
        final Category category = categoryRepository.findByName(taskRequest.getCategory()).orElseThrow(() -> new ResourceNotFoundException("Category", taskRequest.getCategory()));

        final Optional<Task> optionalTask = taskRepository.findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user);

        if(optionalTask.isPresent()) {
            throw new DuplicateResourceFoundException("Task", "name", taskRequest.getName());
        }

        final Task taskToSave = taskConvertor.convertToTaskEntity(taskRequest, user, category);

        return taskConvertor.convertToTaskResponse(taskRepository.save(taskToSave));
    }

    public TaskResponse updateTask(final TaskRequest taskRequest, final String userName, final Long id) {
        final Task task = findById(id);

        if (SecurityUtil.isNotAdmin() && !task.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to update this task!");
        }

        final Category category = categoryRepository.findByName(taskRequest.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category", taskRequest.getCategory()));

        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());
        task.setDeadline(taskRequest.getDeadline());
        task.setCategory(category);

        final Task updatedTask = taskRepository.save(task);

        return taskConvertor.convertToTaskResponse(updatedTask);
    }

    public void deleteTask(final String userName, final Long id) {
        final Task task = findById(id);

        if (SecurityUtil.isNotAdmin() && !task.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to delete this category!");
        }

        taskRepository.delete(task);
    }

    protected Specification<Task> createSpecification(final TaskRequestParams params) {
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


    private Task findById(final Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));
    }
}
