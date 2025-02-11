package ch.cern.todo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TaskQueryParams {
    private String createdBy;
    private String name;
    private String description;
    private String category;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
}
