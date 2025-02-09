package ch.cern.todo.controller;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.dto.TaskCategoryRequestParam;
import ch.cern.todo.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(final TaskCategoryRequestParam requestParam, final Pageable pageable) {

        final String loggedInUser = SecurityUtil.getLoggedInUsername();

        if (SecurityUtil.getAuthentication().getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            requestParam.setCreatedBy(loggedInUser);
        }

        final Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(requestParam, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponses.getContent());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody final CategoryRequest categoryRequest) throws Exception {

        final String loggedInUser = SecurityUtil.getLoggedInUsername();

        final CategoryResponse category = categoryService.createCategory(categoryRequest, loggedInUser);

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@RequestBody final CategoryRequest categoryRequest, @PathVariable final Long id) {

        final String loggedInUser = SecurityUtil.getLoggedInUsername();

        final CategoryResponse category = categoryService.updateCategory(categoryRequest, loggedInUser, id);

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable final Long id) {

        final String loggedInUser = SecurityUtil.getLoggedInUsername();

        categoryService.deleteCategory(loggedInUser, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Category with id " + id + " was deleted successfully.");
    }
}
