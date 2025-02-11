package ch.cern.todo.mapper;

import ch.cern.todo.dto.TaskCreateRequest;
import ch.cern.todo.dto.TaskResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TaskConvertor {

    public Task convertToTaskEntity(final TaskCreateRequest taskCreateRequest, final User user, final Category category) {
        return Task.builder()
                .name(taskCreateRequest.getName())
                .description(taskCreateRequest.getDescription() != null ? taskCreateRequest.getDescription() : "")
                .deadline(LocalDate.from(taskCreateRequest.getDeadline()))
                .createdBy(user)
                .category(category)
                .build();
    }

    public TaskResponse convertToTaskResponse(final Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .category(task.getCategory().getName())
                .createdBy(task.getCreatedBy().getUsername())
                .build();
    }
}
