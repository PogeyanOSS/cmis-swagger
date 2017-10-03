package com.pogeyan.swagger.pojos;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class ExternalDocs {
	private String description;
	private String url;

	public ExternalDocs() {
	}

	public ExternalDocs(String description, String url) {
		super();
		this.description = description;
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
