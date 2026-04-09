package dev.suprim.kit.web.response;

/**
 * Represents a validation error for a specific field.
 */
public record ValidationError(
		String field,
		String message
) {}
