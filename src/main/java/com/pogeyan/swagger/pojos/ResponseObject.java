package com.pogeyan.swagger.pojos;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class ResponseObject {
	private String description;
	private Map<String, String> schema;

	public ResponseObject() {
		super();
	}

	public ResponseObject(String description, Map<String, String> schema) {
		super();
		this.description = description;
		this.schema = schema;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getSchema() {
		return schema;
	}

	public void setSchema(Map<String, String> schema) {
		this.schema = schema;
	}

}
