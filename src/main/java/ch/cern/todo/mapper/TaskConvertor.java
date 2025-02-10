package ch.cern.todo.mapper;

import ch.cern.todo.dto.TaskRequest;
import ch.cern.todo.dto.TaskResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskConvertor {

    public Task convertToTaskEntity(final TaskRequest taskRequest, final User user, final Category category) {
        return Task.builder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription().isEmpty() ? "" : taskRequest.getDescription())
                .deadline(taskRequest.getDeadline())
                .createdBy(user)
                .category(category)
                .build();
    }

    public TaskResponse convertToTaskResponse(final Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .deadline(LocalDateTime.parse(task.getDeadline().toString()))
                .category(task.getCategory().getName())
                .createdBy(task.getCreatedBy().getUsername())
                .build();
    }
}
