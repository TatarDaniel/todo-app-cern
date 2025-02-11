package ch.cern.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CategoryCreateRequest {
    @NotBlank(message = "Task category name is required")
    private String name;

    private String description;
}
