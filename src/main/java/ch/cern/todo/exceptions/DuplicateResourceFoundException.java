package ch.cern.todo.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceFoundException extends RuntimeException {
  public DuplicateResourceFoundException(final String resourceType, final String field, final String value) {
    super(resourceType + " with " + field + " '" + value + "' already exists.");
  }

  public DuplicateResourceFoundException(final String userName) {
    super(userName + " already exists.");
  }
}