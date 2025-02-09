package ch.cern.todo.controller;

import ch.cern.todo.dto.TaskResponse;
import ch.cern.todo.entity.Task;
import ch.cern.todo.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }
}
