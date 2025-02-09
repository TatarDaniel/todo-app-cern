package ch.cern.todo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(final String message) {
        super(String.format(message));
    }
}
