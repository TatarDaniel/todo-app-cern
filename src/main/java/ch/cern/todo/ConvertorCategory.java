package ch.cern.todo;

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

    public Category convertToCategoryEntity(final CategoryRequest categoryDto, final User user) {

        Category category = Category.builder()
                .name(categoryDto.getName()).
                description(categoryDto.getDescription())
                .createdBy(user)
                .build();

        return category;
    }

    public List<CategoryResponse> convertToCategories(final List<Category> categoryList) {
        if(!CollectionUtils.isEmpty(categoryList)) {
            return categoryList.stream().map(this::convertToCategoryDto).toList();
        }

        return Collections.emptyList();
    }

    public CategoryResponse convertToCategoryDto(final Category category) {
        return CategoryResponse.builder().name(category.getName()).description(category.getDescription()).createdBy(category.getCreatedBy().getUsername()).build();
    }
}
