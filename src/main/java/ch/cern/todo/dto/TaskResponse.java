package ch.cern.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private String category;
}
