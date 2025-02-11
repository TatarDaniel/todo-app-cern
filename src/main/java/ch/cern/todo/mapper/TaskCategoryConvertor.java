package ch.cern.todo.mapper;

import ch.cern.todo.dto.CategoryCreateRequest;

import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskCategoryConvertor {

    public Category convertToCategoryEntity(final CategoryCreateRequest categoryCreateRequest, final User user) {

        return Category.builder()
                .name(categoryCreateRequest.getName())
                .description(categoryCreateRequest.getDescription() != null ? categoryCreateRequest.getDescription() : "")
                .createdBy(user)
                .build();
    }

    public CategoryResponse convertToCategoryResponse(final Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdBy(category.getCreatedBy().getUsername())
                .build();
    }
}
