package ch.cern.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CategoryRequest {

    @NotBlank
    private String name;

    private String description;
}
