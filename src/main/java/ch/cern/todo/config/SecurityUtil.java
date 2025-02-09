package ch.cern.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class SecurityUtil {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static String getLoggedInUsername() {
        final Authentication auth = getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }
}
