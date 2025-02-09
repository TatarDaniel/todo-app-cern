package ch.cern.todo.controller;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.dto.TaskCategoryRequestParam;
import ch.cern.todo.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<List<CategoryResponse>> getAllCategories(final TaskCategoryRequestParam requestParam,
                                                                   final Pageable pageable) {
        final Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(requestParam, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponses.getContent());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody final CategoryRequest categoryRequest) throws Exception {
        final CategoryResponse category = categoryService.createCategory(categoryRequest, SecurityUtil.getLoggedInUsername());

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@Valid @RequestBody final CategoryRequest categoryRequest,
                                                           @PathVariable @NotNull final Long id) {
        final CategoryResponse category = categoryService.updateCategory(categoryRequest, SecurityUtil.getLoggedInUsername(), id);

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable @NotNull final Long id) {
        categoryService.deleteCategory(SecurityUtil.getLoggedInUsername(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
