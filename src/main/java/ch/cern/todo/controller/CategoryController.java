package ch.cern.todo.controller;

import ch.cern.todo.dto.CategoryCreateRequest;
import ch.cern.todo.util.SecurityUtil;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.dto.CategoryQueryParams;
import ch.cern.todo.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(final CategoryQueryParams requestParam) {
        final List<CategoryResponse> categoryResponses = categoryService.getAllCategories(requestParam);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponses);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody final CategoryCreateRequest categoryCreateRequest) {
        final CategoryResponse category = categoryService.createCategory(categoryCreateRequest, SecurityUtil.getLoggedInUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@Valid @RequestBody final CategoryRequest categoryRequest,
                                                           @PathVariable @NotNull final Long categoryId) {
        final CategoryResponse category = categoryService.updateCategory(categoryRequest, SecurityUtil.getLoggedInUsername(), categoryId);

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable @NotNull final Long categoryId) {
        categoryService.deleteCategory(SecurityUtil.getLoggedInUsername(), categoryId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
