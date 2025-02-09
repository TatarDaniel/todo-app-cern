package ch.cern.todo.mapper;

import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Component
public class ConvertorCategory {

    public Category convertToCategoryEntity(final CategoryRequest categoryRequest, final User user) {

        return Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .createdBy(user)
                .build();
    }

    public List<CategoryResponse> convertToCategories(final List<Category> categoryList) {
        if(!CollectionUtils.isEmpty(categoryList)) {
            return categoryList.stream().map(this::convertToCategoryResponse).toList();
        }

        return Collections.emptyList();
    }

    public CategoryResponse convertToCategoryResponse(final Category category) {
        return CategoryResponse.builder().id(category.getId()).name(category.getName()).description(category.getDescription()).createdBy(category.getCreatedBy().getUsername()).build();
    }
}
