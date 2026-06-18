package com.example.attendance.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

public final class ProblemDetailFactory {

    private ProblemDetailFactory() {
    }

    public static ProblemDetail create(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        return problem;
    }

    public static ProblemDetail notFound(String resourceName, Object id) {
        return create(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            "%s with id '%s' was not found".formatted(resourceName, id)
        );
    }

    public static ProblemDetail conflict(String detail) {
        return create(HttpStatus.CONFLICT, "Conflict", detail);
    }
}
