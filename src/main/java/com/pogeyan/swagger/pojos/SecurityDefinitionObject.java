package com.pogeyan.swagger.pojos;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class SecurityDefinitionObject {

	private String type;
	private String authorizationUrl;
	private String flow;
	private Map<String, String> scopes;
	private String name;
	private String in;

	public SecurityDefinitionObject() {
		super();
	}

	public SecurityDefinitionObject(String type, String authorizationUrl, String flow, Map<String, String> scopes,
			String name, String in) {
		super();
		this.type = type;
		this.authorizationUrl = authorizationUrl;
		this.flow = flow;
		this.scopes = scopes;
		this.name = name;
		this.in = in;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public Map<String, String> getScopes() {
		return scopes;
	}

	public void setScopes(Map<String, String> scopes) {
		this.scopes = scopes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

}
