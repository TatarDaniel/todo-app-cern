# To-Do Application

## Overview
This is a Spring Boot-based To-Do application that allows users to create, manage tasks and tasks categories. 

## Technologies Used
- **Java 21**
- **Spring Boot**
- **Lombok** 
- **Spring Validation** 
- **Jackson** 
- **JUnit & Mockito** 

## Project Structure
```
ch.cern.todo
│── config          # Configuration classes
│── controller      # REST Controllers
│── dto            # Data Transfer Objects (DTOs)
│── entity         # Database entities 
│── exceptions     # Custom exception handling classes
│── mapper         # Mapping between entities and DTOs
│── repository     # Spring Data JPA Repositories
│── service        # Business logic layer
│── util           # Utility classes
│── resources      # Application properties and configurations
│── test           # Unit tests
```

## Database Schema
The application consists of three main tables:

### User Table (`users`)
| Column    | Type    | Constraints |
|-----------|--------|-------------|
| id        | Long   | Primary Key, Auto-generated |
| username  | String | Unique, Not Null, Max 50 chars |
| password  | String | Not Null |
| role      | String | Not Null |

### Category Table (`task_categories`)
| Column      | Type    | Constraints |
|------------|--------|-------------|
| id         | Long   | Primary Key, Auto-generated |
| name       | String | Unique, Not Null, Max 100 chars |
| description| String | Max 500 chars |
| createdBy  | User   | ManyToOne (User) |

### Task Table (`tasks`)
| Column      | Type    | Constraints |
|------------|--------|-------------|
| id         | Long   | Primary Key, Auto-generated |
| name       | String | Not Null, Max 100 chars |
| description| String | Max 500 chars |
| deadline   | Date   | Not Null |
| category   | Category | ManyToOne (Category) |
| createdBy  | User   | ManyToOne (User) |

## Features
- **User Authentication & Authorization** (Spring Security)
- **CRUD Operations for Tasks & Categories**
- **User-based task categorization**
- **Global Exception Handling**

## API Endpoints
### Base Path
All endpoints are prefixed with `/api/v1/`

### Authentication
- `POST /api/v1/auth/register` - Register a new user

### Categories
- `GET /api/v1/categories` - Get all categories
- `POST /api/v1/categories` - Create a new category
- `PUT /api/v1/categories/{categoryId}` - Update a category
- `DELETE /api/v1/categories/{categoryId}` - Delete a category

### Tasks
- `GET /api/v1/tasks` - Get all tasks
- `POST /api/v1/tasks` - Create a new task
- `PUT /api/v1/tasks/{taskId}` - Update a task
- `DELETE /api/v1/tasks/{taskId}` - Delete a task

### Default Admin Credentials
The default admin user has the following credentials:

- **Username:** admin
- **Password:** admin123
- **Role:** ROLE_ADMIN

### Build and Run the Application
1. Clone the repository:
   ```bash
   git clone https://github.com/TatarDaniel/todo-app-cern.git
   cd to-do-app
   ```
2. Configure the database in `application.properties`.
3. **Build the application**
   ```bash
   ./gradlew build
   ```
4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```
5. Access the application at `http://localhost:8080/`


## Running Tests

To run the tests, execute the following command:
```bash
./gradlew test
```


