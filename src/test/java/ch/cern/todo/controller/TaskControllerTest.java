package ch.cern.todo.controller;

import ch.cern.todo.config.JacksonConfig;
import ch.cern.todo.config.SecurityUtil;
import ch.cern.todo.dto.*;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.service.TaskService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private static final String TASK_URL = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void testGetAllTasks_FilteredByLoggedInUser() throws Exception {
        try (final var securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            final TaskResponse response = TaskResponse.builder()
                    .id(1L)
                    .name("New task")
                    .description("Description")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .category("Work")
                    .build();
            final List<TaskResponse> mockTasks = List.of(response);

            when(taskService.getAllTasks(any(TaskRequestParams.class))).thenReturn(mockTasks);

            mockMvc.perform(get("/api/v1/tasks")
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].name").value("New task"))
                    .andExpect(jsonPath("$[0].description").value("Description"))
                    .andExpect(jsonPath("$[0].category").value("Work"));

            verify(taskService, times(1)).getAllTasks(any());
        }
    }

    @Test
    void testUserCannotAccessOthersTasks() throws Exception {
        try (final var securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doThrow(new UnauthorizedException("You are not allowed to see task created by other user!"))
                    .when(taskService).getAllTasks(any(TaskRequestParams.class));

            mockMvc.perform(get("/api/v1/tasks")
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());


            verify(taskService, times(1)).getAllTasks(any());
        }
    }

    @Test
    void shouldCreateTask_whenValidRequest() throws Exception {
        final TaskRequest taskRequest = new TaskRequest("Task", "Description", LocalDateTime.now().plusDays(1), "Personal");
        final TaskResponse taskResponse = new TaskResponse(1L, "Task", "Description", "user", LocalDateTime.now().plusDays(1), "Personal");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");
            when(taskService.createTask(any(TaskRequest.class), eq("user")))
                    .thenReturn(taskResponse);

            mockMvc.perform(post(TASK_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .content(JacksonConfig.getObjectMapper().writeValueAsString(taskRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Task"))
                    .andExpect(jsonPath("$.description").value("Description"));

            verify(taskService, times(1)).createTask(any(TaskRequest.class), eq("user"));
        }
    }

    @Test
    void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        final TaskRequest taskRequest = new TaskRequest("Task", "Description", LocalDateTime.now().plusDays(1), "NotExistingCategory");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");
            when(taskService.createTask(any(TaskRequest.class), eq("user")))
                    .thenThrow(new ResourceNotFoundException("Category", "NotExistingCategory"));

            mockMvc.perform(post(TASK_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .content(JacksonConfig.getObjectMapper().writeValueAsString(taskRequest)))
                    .andExpect(status().isNotFound());

            verify(taskService, times(1)).createTask(any(TaskRequest.class), eq("user"));
        }
    }

    @Test
    void shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        final TaskRequest invalidRequest = new TaskRequest();

        mockMvc.perform(post(TASK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                        .content(new ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }


    @Test
    void shouldUpdateTask_whenUserOwnsIt() throws Exception {
        final LocalDateTime expectedDeadline = LocalDateTime.now().plusDays(1);
        final Long taskId = 1L;
        final TaskRequest taskRequest = new TaskRequest("Task", "Description", expectedDeadline, "Personal");
        final TaskResponse taskResponse = new TaskResponse(1L, "Updated Task", "This is an updated task", "user", expectedDeadline, "Personal");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            when(taskService.updateTask(any(TaskRequest.class), eq("user"), eq(taskId)))
                    .thenReturn(taskResponse);

            mockMvc.perform(put(TASK_URL + "/" + taskId)
                            .header("Authorization", "Basic dXNlcjp0ZXN0MTIz")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JacksonConfig.getObjectMapper().writeValueAsString(taskRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.name").value("Updated Task"))
                    .andExpect(jsonPath("$.description").value("This is an updated task"))
                    .andExpect(jsonPath("$.createdBy").value("user"));

            verify(taskService, times(1)).updateTask(any(TaskRequest.class), eq("user"), eq(taskId));
        }
    }

    @Test
    void shouldReturnForbidden_whenUserTriesToUpdateAnotherUsersTask() throws Exception {
        final Long taskId = 1L;
        final LocalDateTime expectedDeadline = LocalDateTime.now().plusDays(1);
        final TaskRequest taskRequest = new TaskRequest("Task", "Description", expectedDeadline, "Personal");

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            when(taskService.updateTask(any(TaskRequest.class), eq("user"), eq(taskId)))
                    .thenThrow(new UnauthorizedException("You cannot update another user's task"));

            // When & Then
            mockMvc.perform(put(TASK_URL + "/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JacksonConfig.getObjectMapper().writeValueAsString(taskRequest)))
                    .andExpect(status().isForbidden());

            verify(taskService, times(1)).updateTask(any(TaskRequest.class), eq("user"), eq(taskId));
        }
    }

    @Test
    void shouldDeleteTask_whenUserOwnsIt() throws Exception {
        final Long categoryId = 1L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doNothing().when(taskService).deleteTask("user", categoryId);

            mockMvc.perform(delete(TASK_URL + "/" + categoryId))
                    .andExpect(status().isNoContent());

            verify(taskService, times(1)).deleteTask("user", categoryId);
        }
    }

    @Test
    void shouldReturnForbidden_whenUserTriesToDeleteAnotherUsersTask() throws Exception {
        final Long categoryId = 1L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doThrow(new UnauthorizedException("You cannot delete another user's category"))
                    .when(taskService).deleteTask("user", categoryId);

            mockMvc.perform(delete(TASK_URL + "/" + categoryId))
                    .andExpect(status().isForbidden());

            verify(taskService, times(1)).deleteTask("user", categoryId);
        }
    }

    @Test
    void shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        final Long categoryId = 999L;

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn("user");

            doThrow(new ResourceNotFoundException("Category", "999"))
                    .when(taskService).deleteTask("user", categoryId);

            mockMvc.perform(delete(TASK_URL + "/" + categoryId))
                    .andExpect(status().isNotFound());

            verify(taskService, times(1)).deleteTask("user", categoryId);
        }
    }

}
