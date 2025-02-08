package ch.cern.todo.service;

import ch.cern.todo.ConvertorCategory;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ConvertorCategory convertorCategory;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, final ConvertorCategory convertorCategory, final UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.convertorCategory = convertorCategory;
        this.userRepository = userRepository;
    }

    public List<CategoryResponse> getAllCategories() {

        List<Category> cats = categoryRepository.findAll();

        return convertorCategory.convertToCategories(cats);
    }

    public CategoryResponse createCategory(CategoryRequest category) throws Exception {

        Optional<User> user = userRepository.findByUsername("userTest");

        if(user.isPresent()) {
            Category cat = convertorCategory.convertToCategoryEntity(category, user.get());

            cat = categoryRepository.save(cat);

            CategoryResponse dto = convertorCategory.convertToCategoryDto(cat);

            return dto;
        }
        else {
            throw new Exception();
        }


    }

}
