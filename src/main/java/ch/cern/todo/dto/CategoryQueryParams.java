package ch.cern.todo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryQueryParams {
    private String createdBy;
    private String name;
    private String description;
}
