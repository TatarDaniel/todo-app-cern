package ch.cern.todo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequestParam {
    private String createdBy;
    private String name;
    private String description;
}
