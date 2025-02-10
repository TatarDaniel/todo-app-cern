package ch.cern.todo.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class DuplicateResourceFoundException extends RuntimeException {
  public DuplicateResourceFoundException(String resourceType, String field, String value) {
    super(resourceType + " with " + field + " '" + value + "' already exists.");
  }
}