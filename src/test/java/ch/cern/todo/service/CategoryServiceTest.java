package ch.cern.todo.service;

import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.dto.TaskCategoryRequestParams;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.User;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.exceptions.UserNotFoundException;
import ch.cern.todo.mapper.ConvertorCategory;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ConvertorCategory convertorCategory;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategoriesSuccessfully() {
        final TaskCategoryRequestParams requestParams = new TaskCategoryRequestParams();
        final Category category = Category.builder().id(1L).name("Work").createdBy(User.builder().id(1L).build()).build();
        final CategoryResponse categoryResponse = CategoryResponse.builder().id(1L).name("Work").description("Work-related tasks").createdBy("user").build();

        when(categoryRepository.findAll(any(Specification.class))).thenReturn(List.of(category));
        when(convertorCategory.convertToCategoryResponse(category)).thenReturn(categoryResponse);

        final List<CategoryResponse> result = categoryService.getAllCategories(requestParams);

        assertEquals(1, result.size());
        assertEquals("Work", result.get(0).getName());
        verify(categoryRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void shouldReturnEmptyList_whenNoCategoriesExist() {
        final TaskCategoryRequestParams requestParams = new TaskCategoryRequestParams();
        when(categoryRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        final List<CategoryResponse> result = categoryService.getAllCategories(requestParams);

        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        final String username = "user";
        final CategoryRequest categoryRequest = new CategoryRequest("Work", "Work-related tasks");
        final User user = User.builder().id(1L).username(username).build();
        final Category category = Category.builder().id(1L).name("Work").createdBy(user).build();
        final CategoryResponse expectedResponse = CategoryResponse.builder().id(1L).name("Work").description("Work-related tasks").createdBy("user").build();


        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(convertorCategory.convertToCategoryEntity(categoryRequest, user)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(convertorCategory.convertToCategoryResponse(category)).thenReturn(expectedResponse);

        final CategoryResponse result = categoryService.createCategory(categoryRequest, username);

        assertEquals("Work", result.getName());
        verify(userRepository, times(1)).findByUsername(username);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        final String username = "user";
        final CategoryRequest categoryRequest = new CategoryRequest("Work", "Work-related tasks");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> categoryService.createCategory(categoryRequest, username));

        verify(userRepository, times(1)).findByUsername(username);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        final String username = "user";
        final Long categoryId = 1L;
        final CategoryRequest categoryRequest = new CategoryRequest("Updated Work", "Updated description");
        final User user = User.builder().id(1L).username(username).build();
        final Category category = Category.builder().id(1L).name("Work").createdBy(user).build();
        final CategoryResponse expectedResponse = CategoryResponse.builder().id(1L).name("Updated Work").description("Updated description").createdBy("user").build();


        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(convertorCategory.convertToCategoryResponse(category)).thenReturn(expectedResponse);

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final CategoryResponse result = categoryService.updateCategory(categoryRequest, username, categoryId);

            assertEquals("Updated Work", result.getName());
            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, times(1)).save(category);
        }
    }

    @Test
    void shouldThrowUnauthorizedException_whenUserNotAllowedToUpdateCategory() {
        final String username = "user1";
        final Long categoryId = 1L;
        final String otherUser = "user2";
        final CategoryRequest categoryRequest = new CategoryRequest("Updated Work", "Updated description");
        final User owner = User.builder().id(1L).username(otherUser).build();
        final Category category = Category.builder().id(1L).name("Work").createdBy(owner).build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            assertThrows(UnauthorizedException.class, () -> categoryService.updateCategory(categoryRequest, username, categoryId));

            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, never()).save(any());
        }
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        final String username = "user";
        final Long categoryId = 1L;
        final User user = User.builder().id(1L).username(username).build();
        final Category category = Category.builder().id(1L).name("Work").createdBy(user).build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            // When
            categoryService.deleteCategory(username, categoryId);

            // Then
            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, times(1)).delete(category);
        }
    }

    @Test
    void shouldThrowUnauthorizedException_whenUserNotAllowedToDeleteCategory() {
        final String username = "user1";
        final Long categoryId = 1L;
        final String otherUser = "user2";
        final User owner = User.builder().id(1L).username(otherUser).build();
        final Category category = Category.builder().id(1L).name("Work").createdBy(owner).build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            assertThrows(UnauthorizedException.class, () -> categoryService.deleteCategory(username, categoryId));

            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }
}
