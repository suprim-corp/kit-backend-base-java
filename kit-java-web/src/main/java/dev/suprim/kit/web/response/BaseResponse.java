package dev.suprim.kit.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import dev.suprim.kit.exception.ApiStatus;

@Builder
public record BaseResponse<T>(
		@Schema(description = "Response code", example = "1")
		int code,

		@Schema(description = "Response message", example = "Success!")
		String message,

		@Schema(description = "Response data")
		T data
) {
	public BaseResponse() {
		this(ApiStatus.SUCCESS.getCode(), ApiStatus.SUCCESS.getMessage(), null);
	}

	public BaseResponse(T data) {
		this(ApiStatus.SUCCESS.getCode(), ApiStatus.SUCCESS.getMessage(), data);
	}

	public BaseResponse(ApiStatus code) {
		this(code.getCode(), code.getMessage(), null);
	}

	public BaseResponse(ApiStatus code, String message) {
		this(code.getCode(), message, null);
	}

	public BaseResponse(ApiStatus code, T data) {
		this(code.getCode(), code.getMessage(), data);
	}

	public BaseResponse(ApiStatus code, String message, T data) {
		this(code.getCode(), message, data);
	}
}