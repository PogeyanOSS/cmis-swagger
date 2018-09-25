package com.pogeyan.swagger.api.utils;

public enum RelationType {
	CREATED("created"), UPDATED("updated"), DELETED("deleted");
	private final String value;

	RelationType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static RelationType fromValue(String v) {
		for (RelationType c : RelationType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
