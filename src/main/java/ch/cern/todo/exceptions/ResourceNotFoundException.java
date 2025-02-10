package ch.cern.todo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(final String resourceCategory, final String resourceName) {
        super(String.format("%s with id : %s not found", resourceCategory, resourceName));
    }
}
