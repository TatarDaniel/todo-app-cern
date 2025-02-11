package ch.cern.todo.service;

import ch.cern.todo.dto.CategoryCreateRequest;
import ch.cern.todo.util.SecurityUtil;
import ch.cern.todo.dto.CategoryQueryParams;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.exceptions.UserNotFoundException;
import ch.cern.todo.mapper.TaskCategoryConvertor;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskCategoryConvertor taskCategoryConvertor;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAllCategories(final CategoryQueryParams requestParams) {
        final Specification<Category> spec = createSpecification(requestParams);

        return categoryRepository.findAll(spec)
                .stream()
                .map(taskCategoryConvertor::convertToCategoryResponse)
                .toList();
    }

    public CategoryResponse createCategory(final CategoryCreateRequest categoryCreateRequest, final String userName) {
        final User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        final Category category = taskCategoryConvertor.convertToCategoryEntity(categoryCreateRequest, user);

        return taskCategoryConvertor.convertToCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse updateCategory(final CategoryRequest categoryRequest, final String userName, final Long categoryId) {
        final Category category = findById(categoryId);

        if (SecurityUtil.isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to update this category!");
        }

        final Category updatedCategory = categoryRepository.save(updateCategory(category, categoryRequest));

        return taskCategoryConvertor.convertToCategoryResponse(updatedCategory);
    }

    public void deleteCategory(final String userName, final Long categoryId) {
        final Category category = findById(categoryId);

        if (SecurityUtil.isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to delete this category!");
        }

        categoryRepository.delete(category);
    }

    private Specification<Category> createSpecification(final CategoryQueryParams params) {
        return (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(params.getCreatedBy())) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy").get("username"), params.getCreatedBy()));
            }

            if (Objects.nonNull(params.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + params.getName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(params.getDescription())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + params.getDescription().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Category findById(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id.toString()));
    }

    private Category updateCategory(final Category existingCategory, final CategoryRequest categoryRequest) {

        if(categoryRequest.getDescription() != null) {
            existingCategory.setDescription(categoryRequest.getDescription());
        }

        existingCategory.setName(categoryRequest.getName());

        return existingCategory;
    }

}
