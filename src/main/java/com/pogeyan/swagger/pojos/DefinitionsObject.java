package com.pogeyan.swagger.pojos;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class DefinitionsObject {
	private String type;
	private List<String> required;
	private Map<String, String> xml;
	private Map<String, Map<String, String>> properties;

	public DefinitionsObject() {
		super();
	}

	public DefinitionsObject(String type, List<String> required, Map<String, String> xml,
			Map<String, Map<String, String>> properties) {
		super();
		this.type = type;
		this.required = required;
		this.xml = xml;
		this.properties = properties;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getRequired() {
		return required;
	}

	public void setRequired(List<String> required) {
		this.required = required;
	}

	public Map<String, String> getXml() {
		return xml;
	}

	public void setXml(Map<String, String> xml) {
		this.xml = xml;
	}

	public Map<String, Map<String, String>> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Map<String, String>> properties) {
		this.properties = properties;
	}

}
