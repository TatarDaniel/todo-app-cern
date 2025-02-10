package ch.cern.todo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime deadline;
    private String category;
}
