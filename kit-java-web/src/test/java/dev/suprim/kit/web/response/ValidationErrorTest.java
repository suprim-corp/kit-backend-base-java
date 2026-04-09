package dev.suprim.kit.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {

	@Test
	void record_fieldsAccessible() {
		ValidationError error = new ValidationError("email", "Invalid format");

		assertEquals("email", error.field());
		assertEquals("Invalid format", error.message());
	}

	@Test
	void record_equality() {
		ValidationError error1 = new ValidationError("email", "Invalid");
		ValidationError error2 = new ValidationError("email", "Invalid");

		assertEquals(error1, error2);
		assertEquals(error1.hashCode(), error2.hashCode());
	}

	@Test
	void record_inequality() {
		ValidationError error1 = new ValidationError("email", "Invalid");
		ValidationError error2 = new ValidationError("name", "Required");

		assertNotEquals(error1, error2);
	}

	@Test
	void record_toString_containsValues() {
		ValidationError error = new ValidationError("email", "Invalid format");

		String str = error.toString();
		assertTrue(str.contains("email"));
		assertTrue(str.contains("Invalid format"));
	}

	@Test
	void record_nullValues_allowed() {
		ValidationError error = new ValidationError(null, null);

		assertNull(error.field());
		assertNull(error.message());
	}
}
