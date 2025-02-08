package ch.cern.todo.controller;

import ch.cern.todo.ConvertorCategory;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ConvertorCategory convertorCategory;

    public CategoryController(CategoryService categoryService, final ConvertorCategory convertorCategory) {
        this.categoryService = categoryService;
        this.convertorCategory = convertorCategory;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categoryResponses = categoryService.getAllCategories();

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponses);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest category) throws Exception {

        CategoryResponse cat = categoryService.createCategory(category);

        return ResponseEntity.status(HttpStatus.OK).body(cat);
    }
}
