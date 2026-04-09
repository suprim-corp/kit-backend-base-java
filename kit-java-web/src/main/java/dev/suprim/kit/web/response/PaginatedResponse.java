package dev.suprim.kit.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Pageable;
import dev.suprim.kit.exception.ApiStatus;

import java.util.List;

public record PaginatedResponse<T>(
		@Schema(description = "Response code", example = "200") int code,

		@Schema(description = "Response message", example = "OK") String message,

		@Schema(description = "Response data") List<T> data,

		@Schema(description = "Pagination information") PaginationInfo pagination
) {
	public record PaginationInfo(
			@Schema(description = "Total number of items", example = "100") long total,

			@Schema(description = "Current page count", example = "10") @JsonProperty("current_page") long currentPage,

			@Schema(description = "Total number of pages", example = "10") @JsonProperty("total_page") int totalPages,

			@Schema(description = "Current page number", example = "1") int page,

			@Schema(description = "Number of items per page", example = "10") int size,

			@Schema(description = "Is last page", example = "false") boolean last,

			@Schema(description = "Is first page", example = "true") boolean first,

			@Schema(description = "Is empty result", example = "false") boolean empty,

			@Schema(description = "Sorting information", example = "100") List<String> sorting
	) {}

	public PaginatedResponse() {
		this(
				ApiStatus.SUCCESS.getCode(),
				ApiStatus.SUCCESS.getMessage(),
				List.of(),
				new PaginationInfo(0, 0, 1, 1, 10, false, true, true, List.of())
		);
	}

	public PaginatedResponse(List<T> data, long total) {
		this(
				ApiStatus.SUCCESS.getCode(),
				ApiStatus.SUCCESS.getMessage(),
				data,
				new PaginationInfo(
						total,
						0,
						1,
						1,
						10,
						false,
						true,
						true,
						List.of()
				)
		);
	}

	public PaginatedResponse(List<T> data, long total, Pageable pageable) {
		this(
				ApiStatus.SUCCESS.getCode(),
				ApiStatus.SUCCESS.getMessage(),
				data,
				calculatePaginationInfo(total, pageable)
		);
	}

	public PaginatedResponse(
			ApiStatus code,
			String message,
			List<T> data,
			long total,
			Pageable pageable
	) {
		this(
				code.getCode(),
				message,
				data,
				calculatePaginationInfo(total, pageable)
		);
	}

	private static PaginationInfo calculatePaginationInfo(
			long total,
			Pageable pageable
	) {
		int pageSize = pageable.getPageSize() < 1 ? 20 : pageable.getPageSize();
		int page = Math.max(pageable.getPageNumber(), 1);

		int totalPages = (int) Math.ceil((double) total / pageSize);
		boolean isLast = page >= totalPages;
		boolean isFirst = page < 2;
		boolean isEmpty = total == 0;
		long currentPage = Math.min(
				total - (long) (page - 1) * pageSize,
				pageSize
		);

		return new PaginationInfo(
				total,
				currentPage,
				totalPages,
				page,
				pageSize,
				isLast,
				isFirst,
				isEmpty,
				SortUtils.fromPageable(pageable)
		);
	}
}