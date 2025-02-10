package ch.cern.todo.service;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.TaskCategoryRequestParams;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.exceptions.UserNotFoundException;
import ch.cern.todo.mapper.ConvertorCategory;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ConvertorCategory convertorCategory;
    private final UserRepository userRepository;

    public CategoryService(final CategoryRepository categoryRepository, final ConvertorCategory convertorCategory, final UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.convertorCategory = convertorCategory;
        this.userRepository = userRepository;
    }

    public Page<CategoryResponse> getAllCategories(final TaskCategoryRequestParams requestParams, final Pageable pageable) {
        // TODO change the conditions
        if (isNotAdmin() && !requestParams.getCreatedBy().equals(SecurityUtil.getLoggedInUsername())) {
            throw new UnauthorizedException("You are not allowed to view this category!");
        }

        final Specification<Category> spec = createSpecification(requestParams);
        final Page<Category> categories = categoryRepository.findAll(spec, pageable);

        final List<CategoryResponse> categoryResponses = categories.getContent().stream().map(convertorCategory::convertToCategoryResponse).toList();

        return new PageImpl<>(categoryResponses, pageable, categories.getTotalElements());
    }

    public CategoryResponse createCategory(final CategoryRequest categoryRequest, final String userName) {
        final User user = userRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException(userName));

        final Category category = convertorCategory.convertToCategoryEntity(categoryRequest, user);

        return convertorCategory.convertToCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse updateCategory(final CategoryRequest categoryRequest, final String userName, final Long categoryId) {
        final Category category = findById(categoryId);

        if (isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to update this category!");
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        final Category updatedCategory = categoryRepository.save(category);

        return convertorCategory.convertToCategoryResponse(updatedCategory);
    }

    public void deleteCategory(final String userName, final Long categoryId) {
        final Category category = findById(categoryId);

        if (isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new UnauthorizedException("You are not allowed to delete this category!");
        }

        categoryRepository.delete(category);
    }

    private Specification<Category> createSpecification(final TaskCategoryRequestParams params) {
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

    private boolean isNotAdmin() {
        return SecurityUtil.getAuthentication().getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private Category findById(final Long id) {
        return categoryRepository.findCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id.toString()));
    }

}
