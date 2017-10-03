package com.pogeyan.swagger.pojos;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class InfoObject {
	private String description;
	private String version;
	private String title;
	private String termsOfService;
	private Map<String, String> contact;
	private Map<String, String> license;

	public InfoObject() {

	}

	public InfoObject(String description, String version, String title, String termsOfService,
			Map<String, String> contact, Map<String, String> license) {
		super();
		this.description = description;
		this.version = version;
		this.title = title;
		this.termsOfService = termsOfService;
		this.contact = contact;
		this.license = license;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTermsOfService() {
		return termsOfService;
	}

	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

	public Map<String, String> getContact() {
		return contact;
	}

	public void setContact(Map<String, String> contact) {
		this.contact = contact;
	}

	public Map<String, String> getLicense() {
		return license;
	}

	public void setLicense(Map<String, String> license) {
		this.license = license;
	}

}
