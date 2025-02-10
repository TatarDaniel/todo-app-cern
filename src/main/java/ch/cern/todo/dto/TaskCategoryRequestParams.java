package ch.cern.todo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCategoryRequestParams {
    private String createdBy;
    private String name;
    private String description;
}
