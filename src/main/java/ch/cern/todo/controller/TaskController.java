package ch.cern.todo.controller;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.*;
import ch.cern.todo.entity.Task;
import ch.cern.todo.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }


    // Get task by conditions
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTask(final TaskRequestParams requestParam,
                                                        final Pageable pageable) {
        final Page<TaskResponse> taskResponses = taskService.getAllCategories(requestParam, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponses.getContent());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid final TaskRequest categoryRequest) {
        final TaskResponse task = taskService.createTask(categoryRequest, SecurityUtil.getLoggedInUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@Valid @RequestBody final TaskRequest taskRequest,
                                                           @PathVariable @NotNull final Long id) {
        final TaskResponse updatedTask = taskService.updateTask(taskRequest, SecurityUtil.getLoggedInUsername(), id);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable @NotNull final Long id) {
        taskService.deleteTask(SecurityUtil.getLoggedInUsername(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
