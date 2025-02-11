package ch.cern.todo.controller;

import ch.cern.todo.util.SecurityUtil;
import ch.cern.todo.dto.*;
import ch.cern.todo.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTask(final TaskQueryParams requestParam) {
        final List<TaskResponse> taskResponses = taskService.getAllTasks(requestParam);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponses);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid final TaskCreateRequest taskCreateRequest) {
        final TaskResponse task = taskService.createTask(taskCreateRequest, SecurityUtil.getLoggedInUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@Valid @RequestBody final TaskRequest taskRequest,
                                                           @PathVariable @NotNull final Long taskId) {
        final TaskResponse updatedTask = taskService.updateTask(taskRequest, SecurityUtil.getLoggedInUsername(), taskId);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteCategory(@PathVariable @NotNull final Long taskId) {
        taskService.deleteTask(SecurityUtil.getLoggedInUsername(), taskId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
