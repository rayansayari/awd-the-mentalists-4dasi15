package com.awd.provider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/** Format JSON unique des erreurs. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                            String path, Map<String, String> validationErrors) {}
