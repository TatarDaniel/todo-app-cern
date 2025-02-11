package ch.cern.todo.service;

import ch.cern.todo.dto.TaskCreateRequest;
import ch.cern.todo.util.SecurityUtil;
import ch.cern.todo.dto.TaskRequest;
import ch.cern.todo.dto.TaskQueryParams;
import ch.cern.todo.dto.TaskResponse;
import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import ch.cern.todo.exceptions.DuplicateResourceFoundException;
import ch.cern.todo.exceptions.ResourceNotFoundException;
import ch.cern.todo.exceptions.UnauthorizedException;
import ch.cern.todo.exceptions.UserNotFoundException;
import ch.cern.todo.mapper.TaskConvertor;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import ch.cern.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskConvertor taskConvertor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnTasks_whenUserIsAuthorized() {
        final String username = "user";
        final TaskQueryParams requestParams = new TaskQueryParams();
        requestParams.setCreatedBy(username);

        final Task task = Task.builder()
                .id(1L)
                .createdBy(
                        User.builder()
                                .id(1L)
                                .username(username)
                                .build())
                .build();
        final TaskResponse taskResponse = new TaskResponse(1L, "Task 1", "Description", username, LocalDate.now(), "Personal");

        final Authentication mockAuth = mock(Authentication.class);
        lenient().when(mockAuth.getName()).thenReturn(username);
        lenient().when(mockAuth.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_USER")));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getAuthentication).thenReturn(mockAuth);
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn(username);

            when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of(task));
            when(taskConvertor.convertToTaskResponse(any(Task.class))).thenReturn(taskResponse);

            final List<TaskResponse> result = taskService.getAllTasks(requestParams);

            assertEquals(1, result.size());
            assertEquals("Task 1", result.get(0).getName());

            verify(taskRepository, times(1)).findAll(any(Specification.class));
            verify(taskConvertor, times(1)).convertToTaskResponse(any(Task.class));
        }
    }

    @Test
    void shouldThrowUnauthorizedException_whenUserTriesToAccessOthersTasks() {
        final String loggedInUser = "user";
        final String otherUser = "admin";
        final TaskQueryParams requestParams = new TaskQueryParams();
        requestParams.setCreatedBy(otherUser);

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getLoggedInUsername).thenReturn(loggedInUser);

            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final UnauthorizedException thrownException = assertThrows(
                    UnauthorizedException.class,
                    () -> taskService.getAllTasks(requestParams)
            );

            assertEquals("You are not allowed to see tasks created by " + otherUser + "!", thrownException.getMessage());
        }
    }

    @Test
    void shouldCreateTaskSuccessfully_whenValidRequest() {
        final String username = "user";
        final TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", "Description", LocalDate.now(),"Personal");

        final User user = User.builder().username(username).build();
        final Category category = Category.builder().name("Personal").build();
        final Task taskToSave = Task.builder().id(1L).name("Task 1").description("Description").createdBy(user).category(category).build();
        final TaskResponse expectedResponse = new TaskResponse(1L, "Task 1", "Description", username, LocalDate.now(), "Personal");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(categoryRepository.findByName(taskRequest.getCategory())).thenReturn(Optional.of(category));
        when(taskRepository.findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user)).thenReturn(Optional.empty());
        when(taskConvertor.convertToTaskEntity(taskRequest, user, category)).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(taskToSave);
        when(taskConvertor.convertToTaskResponse(taskToSave)).thenReturn(expectedResponse);

        final TaskResponse actualResponse = taskService.createTask(taskRequest, username);

        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findByUsername(username);
        verify(categoryRepository, times(1)).findByName(taskRequest.getCategory());
        verify(taskRepository, times(1)).findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user);
        verify(taskRepository, times(1)).save(taskToSave);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        final String username = "user";
        final TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", "Description", LocalDate.now(),"Personal");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> taskService.createTask(taskRequest, username)
        );

        assertEquals(username + " not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist_whenCreating() {
        final String username = "user";
        final TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", "Description", LocalDate.now(),"Personal");

        final User user = User.builder().username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(categoryRepository.findByName(taskRequest.getCategory())).thenReturn(Optional.empty());

        final ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskRequest, username)
        );

        assertEquals("Category with id : Personal not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(categoryRepository, times(1)).findByName(taskRequest.getCategory());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowDuplicateResourceFoundException_whenTaskAlreadyExists() {
        final String username = "user";
        final TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", "Description", LocalDate.now(),"Personal");

        final User user = User.builder().username(username).build();
        final Category category = Category.builder().name("Personal").build();
        final Task existingTask = Task.builder().id(1L).name("Task 1").description("Description").createdBy(user).category(category).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(categoryRepository.findByName(taskRequest.getCategory())).thenReturn(Optional.of(category));
        when(taskRepository.findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user))
                .thenReturn(Optional.of(existingTask));

        final DuplicateResourceFoundException exception = assertThrows(
                DuplicateResourceFoundException.class,
                () -> taskService.createTask(taskRequest, username)
        );

        assertEquals("Task with name 'Task 1' already exists.", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(categoryRepository, times(1)).findByName(taskRequest.getCategory());
        verify(taskRepository, times(1)).findByNameAndCategoryAndCreatedBy(taskRequest.getName(), category, user);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTaskSuccessfully_whenUserIsAuthorized() {
        final Long taskId = 1L;
        final String username = "user";
        final TaskRequest taskRequest = new TaskRequest("New task", "new description", LocalDate.now(),"Personal");

        final User user = User.builder().username(username).build();
        final Category category = Category.builder().name("Category").build();
        final Task existingTask = Task.builder().id(1L).name("Old task").description("old description").createdBy(user).category(category).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(categoryRepository.findByName(taskRequest.getCategory())).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskConvertor.convertToTaskResponse(any(Task.class)))
                .thenReturn(new TaskResponse(taskId, taskRequest.getName(), taskRequest.getDescription(), username, LocalDate.now(), taskRequest.getCategory()));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final TaskResponse updatedTask = taskService.updateTask(taskRequest, username, taskId);

            assertEquals(taskRequest.getName(), updatedTask.getName());
            assertEquals(taskRequest.getDescription(), updatedTask.getDescription());
            assertEquals(taskRequest.getCategory(), updatedTask.getCategory());

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(existingTask);
            verify(categoryRepository, times(1)).findByName(taskRequest.getCategory());
        }
    }

    @Test
    void shouldThrowUnauthorizedException_whenUserTriesToUpdateOthersTask() {
        final Long taskId = 1L;
        final String username = "user1";
        final String anotherUser = "user2";
        final TaskRequest taskRequest = new TaskRequest("New task", "new description", LocalDate.now(),"Personal");

        final User taskOwner = User.builder().username(anotherUser).build();
        final Category category = Category.builder().name("Category").build();
        final Task existingTask = Task.builder().id(1L).name("Old task").description("old description").createdBy(taskOwner).category(category).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final UnauthorizedException thrownException = assertThrows(
                    UnauthorizedException.class,
                    () -> taskService.updateTask(taskRequest, username, taskId)
            );

            assertEquals("You are not allowed to update this task!", thrownException.getMessage());

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist_whenUpdating() {
        final Long taskId = 1L;
        final String username = "user";
        final TaskRequest taskRequest = new TaskRequest("New task", "new description", LocalDate.now(),"NonExisting Category");

        final User user = User.builder().username(username).build();
        final Category category = Category.builder().name("Category").build();
        final Task existingTask = Task.builder().id(1L).name("Old task").description("old description").createdBy(user).category(category).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(categoryRepository.findByName(taskRequest.getCategory())).thenReturn(Optional.empty());

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final ResourceNotFoundException thrownException = assertThrows(
                    ResourceNotFoundException.class,
                    () -> taskService.updateTask(taskRequest, username, taskId)
            );

            assertEquals("Category with id : NonExisting Category not found", thrownException.getMessage());

            verify(taskRepository, times(1)).findById(taskId);
            verify(categoryRepository, times(1)).findByName(taskRequest.getCategory());
            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void shouldThrowEntityNotFoundException_whenTaskDoesNotExist_whenUpdating() {
        final Long taskId = 999L;
        final String username = "user";
        final TaskRequest taskRequest = new TaskRequest("Updated task", "updated description", LocalDate.now(),"Personal");

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        final ResourceNotFoundException thrownException = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateTask(taskRequest, username, taskId)
        );

        assertEquals("Task with id : 999 not found", thrownException.getMessage());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any());
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldDeleteTaskSuccessfully_whenUserIsAuthorized() {
        final Long taskId = 1L;
        final String username = "user";
        final User user = User.builder().username(username).build();
        final Task existingTask = Task.builder().id(1L).name("Task").description("Description").createdBy(user).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            taskService.deleteTask(username, taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).delete(existingTask);
        }
    }

    @Test
    void shouldThrowUnauthorizedException_whenUserTriesToDeleteOthersTask() {
        final Long taskId = 1L;
        final String username = "user1";
        final String anotherUser = "user2";
        final User taskOwner = User.builder().username(anotherUser).build();
        final Task existingTask = Task.builder().id(1L).name("Task").description("Description").createdBy(taskOwner).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        try (final MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::isNotAdmin).thenReturn(true);

            final UnauthorizedException thrownException = assertThrows(
                    UnauthorizedException.class,
                    () -> taskService.deleteTask(username, taskId)
            );

            assertEquals("You are not allowed to delete this category!", thrownException.getMessage());

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).delete((Task) any());
        }
    }

    @Test
    void shouldThrowEntityNotFoundException_whenTaskDoesNotExist_whenDeleting() {
        final Long taskId = 99L;
        final String username = "user";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        final ResourceNotFoundException thrownException = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(username, taskId)
        );

        assertEquals("Task with id : 99 not found", thrownException.getMessage());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
