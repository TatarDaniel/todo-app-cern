package ch.cern.todo.controller;

import ch.cern.todo.dto.CategoryCreateRequest;
import ch.cern.todo.util.SecurityUtil;
import ch.cern.todo.dto.CategoryRequest;
import ch.cern.todo.dto.CategoryResponse;
import ch.cern.todo.dto.CategoryQueryParams;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.service.CategoryService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private static final String CATEGORY_URL = "/api/v1/categories";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    void shouldReturnListOfCategories_whenDataExists() throws Exception {
        final List<CategoryResponse> categoryResponses = List.of(
                CategoryResponse.builder()
                        .id(1L)
                        .name("Work")
                        .description("Work-related tasks")
                        .createdBy("user")
                        .build(),
                CategoryResponse.builder()
                        .id(2L)
                        .name("Personal")
                        .description("Personal-related tasks")
                        .createdBy("user")
                        .build()
        );

        when(categoryService.getAllCategories(any(CategoryQueryParams.class))).thenReturn(categoryResponses);

        mockMvc.perform(get(CATEGORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Work"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Personal"));

        verify(categoryService, times(1)).getAllCategories(any(CategoryQueryParams.class));
    }

    @Test
    void shouldReturnEmptyList_whenNoCategoriesExist() throws Exception {
        when(categoryService.getAllCategories(any(CategoryQueryParams.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get(CATEGORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService, times(1)).getAllCategories(any(CategoryQueryParams.class));
    }

    @Test
    void shouldCreateCategory_whenValidRequest() throws Exception {
        final CategoryCreateRequest categoryRequest = new CategoryCreateRequest("Gym", "Gym-related tasks");
        final CategoryResponse categoryResponse = new CategoryResponse(1L, "Gym", "Gym-related tasks", "user");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");
            when(categoryService.createCategory(any(CategoryCreateRequest.class), eq("user")))
                    .thenReturn(categoryResponse);

            mockMvc.perform(post(CATEGORY_URL)
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(categoryRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Gym"))
                    .andExpect(jsonPath("$.description").value("Gym-related tasks"))
                    .andExpect(jsonPath("$.createdBy").value("user"));
        }

        verify(categoryService, times(1)).createCategory(any(CategoryCreateRequest.class), eq("user"));
    }

    @Test
    void shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        final CategoryRequest invalidRequest = new CategoryRequest();

        mockMvc.perform(post(CATEGORY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldUpdateCategory_whenUserOwnsIt() throws Exception {
        final Long categoryId = 1L;
        final CategoryRequest categoryRequest = new CategoryRequest("Gym", "Gym-related tasks");
        final CategoryResponse categoryResponse = new CategoryResponse(categoryId, "Gym updated", "Description Updated", "user");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            when(categoryService.updateCategory(any(CategoryRequest.class), eq("user"), eq(categoryId)))
                    .thenReturn(categoryResponse);

            mockMvc.perform(put(CATEGORY_URL + "/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(categoryRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(categoryId))
                    .andExpect(jsonPath("$.name").value("Gym updated"))
                    .andExpect(jsonPath("$.description").value("Description Updated"))
                    .andExpect(jsonPath("$.createdBy").value("user"));

            verify(categoryService, times(1)).updateCategory(any(CategoryRequest.class), eq("user"), eq(categoryId));
        }
    }

    @Test
    void shouldReturnForbidden_whenUserTriesToModifyAnotherUsersCategory() throws Exception {
        final Long categoryId = 1L;
        final CategoryRequest categoryRequest = new CategoryRequest("Gym", "Gym-related tasks");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            when(categoryService.updateCategory(any(CategoryRequest.class), eq("user"), eq(categoryId)))
                    .thenThrow(new UnauthorizedException("You are not allowed to update this category!"));

            mockMvc.perform(put(CATEGORY_URL + "/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(categoryRequest)))
                    .andExpect(status().isForbidden());

            verify(categoryService, times(1)).updateCategory(any(CategoryRequest.class), eq("user"), eq(categoryId));
        }
    }

    @Test
    void shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        final Long categoryId = 1L;
        final CategoryRequest invalidRequest = new CategoryRequest();

        mockMvc.perform(put(CATEGORY_URL + "/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldDeleteCategory_whenUserOwnsIt() throws Exception {
        final Long categoryId = 1L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doNothing().when(categoryService).deleteCategory("user", categoryId);

            mockMvc.perform(delete(CATEGORY_URL + "/" + categoryId))
                    .andExpect(status().isNoContent());

            verify(categoryService, times(1)).deleteCategory("user", categoryId);
        }
    }

    @Test
    void shouldReturnForbidden_whenUserTriesToDeleteAnotherUsersCategory() throws Exception {
        final Long categoryId = 1L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doThrow(new UnauthorizedException("You cannot delete another user's category"))
                    .when(categoryService).deleteCategory("user", categoryId);

            mockMvc.perform(delete(CATEGORY_URL + "/" + categoryId))
                    .andExpect(status().isForbidden());

            verify(categoryService, times(1)).deleteCategory("user", categoryId);
        }
    }

    @Test
    void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        final Long categoryId = 999L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doThrow(new ResourceNotFoundException("Category", "999"))
                    .when(categoryService).deleteCategory("user", categoryId);

            mockMvc.perform(delete(CATEGORY_URL + "/" + categoryId))
                    .andExpect(status().isNotFound());

            verify(categoryService, times(1)).deleteCategory("user", categoryId);
        }
    }

}
