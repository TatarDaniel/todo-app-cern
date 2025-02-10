package ch.cern.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
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
