package ch.cern.todo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequestParams {
    private String createdBy;
    private String name;
    private String description;
    private String category;
    private LocalDateTime deadline;
}
