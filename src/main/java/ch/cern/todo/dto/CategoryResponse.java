package ch.cern.todo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

@RequiredArgsConstructor
@SuperBuilder
@Getter
public class CategoryResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String createdBy;
}
