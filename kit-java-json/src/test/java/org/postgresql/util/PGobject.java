package org.postgresql.util;

/**
 * Mock PGobject for testing JsonUtils.normalizeJsonStrings() reflection code path.
 * Mimics the real PostgreSQL PGobject class structure.
 */
public class PGobject {
	private String value;
	private boolean shouldThrow = false;

	public String getValue() {
		if (shouldThrow) {
			throw new RuntimeException("Simulated getValue failure");
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setThrowOnGetValue(boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}
}
