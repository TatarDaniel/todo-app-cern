package ch.cern.todo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Data
@SuperBuilder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
}
