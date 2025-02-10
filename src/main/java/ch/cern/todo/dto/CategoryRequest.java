package ch.cern.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Task category name is required")
    private String name;

    private String description;
}
