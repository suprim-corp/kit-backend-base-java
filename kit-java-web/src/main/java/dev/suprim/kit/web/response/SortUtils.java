package dev.suprim.kit.web.response;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SortUtils {
	private SortUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static List<String> fromPageable(Pageable pageable) {
		if (Objects.isNull(pageable) || pageable.getSort().isUnsorted()) {
			return List.of();
		}

		return pageable.getSort().stream()
		               .map(order -> order.getProperty() + ";" +
		                             order.getDirection().name()
		               )
		               .collect(Collectors.toList());
	}
}
