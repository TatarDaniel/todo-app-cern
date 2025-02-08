package ch.cern.todo.dto;

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
public class CategoryRequest {
    private String name;
    private String description;
}
