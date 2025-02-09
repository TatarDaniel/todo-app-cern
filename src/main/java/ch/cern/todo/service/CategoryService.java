package ch.cern.todo.service;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.TaskCategoryRequestParam;
import ch.cern.todo.mapper.ConvertorCategory;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public Page<CategoryResponse> getAllCategories(final TaskCategoryRequestParam requestParams, final Pageable pageable) {

        final Specification<Category> spec = createSpecification(requestParams);
        final Page<Category> categories = categoryRepository.findAll(spec, pageable);

        final List<CategoryResponse> categoryResponses = categories.getContent().stream().map(convertorCategory::convertToCategoryResponse).toList();

        return new PageImpl<>(categoryResponses, pageable, categories.getTotalElements());
    }

    public CategoryResponse createCategory(final CategoryRequest categoryRequest, final String userName) throws Exception {
        final Optional<User> user = userRepository.findByUsername(userName);

        if(user.isPresent()) {
            final Category category = convertorCategory.convertToCategoryEntity(categoryRequest, user.get());

            return convertorCategory.convertToCategoryResponse(categoryRepository.save(category));
        }
        else {
            throw new Exception();
        }
    }

    public CategoryResponse updateCategory(final CategoryRequest categoryRequest, final String userName, final Long id) {
        final Category category = findById(id).get();

        if (isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new RuntimeException("Unauthorized to update this category!");
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        final Category updatedCategory = categoryRepository.save(category);

        return convertorCategory.convertToCategoryResponse(updatedCategory);
    }

    public void deleteCategory(final String userName, final Long id) {


        final Category category = findById(id).get();

        if (isNotAdmin() && !category.getCreatedBy().getUsername().equals(userName)) {
            throw new RuntimeException("Unauthorized to update this category!");
        }

        categoryRepository.delete(category);
    }

    private Specification<Category> createSpecification(final TaskCategoryRequestParam params) {
        return (root, query, criteriaBuilder) -> {
            Specification<Category> spec = Specification.where(null);

            if (Objects.nonNull(params.getCreatedBy())) {
                spec = spec.and((root1, query1, cb) -> cb.equal(root.get("createdBy").get("username"), params.getCreatedBy()));
            }

            if (Objects.nonNull(params.getName())) {
                spec = spec.and((root1, query1, cb) -> cb.like(cb.lower(root.get("name")), "%" + params.getName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(params.getDescription())) {
                spec = spec.and((root1, query1, cb) -> cb.like(cb.lower(root.get("description")), "%" + params.getDescription().toLowerCase() + "%"));
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    private boolean isNotAdmin() {
        return SecurityUtil.getAuthentication().getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private Optional<Category> findById(final Long id) {
        final Optional<Category> optionalCategory = categoryRepository.findCategoryById(id);

        if(optionalCategory.isEmpty()) {
            throw new RuntimeException("Category not found");
        }
        else {
            return optionalCategory;
        }
    }

}
