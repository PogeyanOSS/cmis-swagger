/**
 * Copyright 2017 Pogeyan Technologies
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.pogeyan.swagger.pojos;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class PathCommonObject {
	private String[] tags;
	private String summary;
	private String description;
	private String operationId;
	private String[] consumes;
	private String[] produces;
	private ParameterObject[] parameters;
	private Map<String, ResponseObject> responses;
	private List<Map<String, String[]>> security;

	public PathCommonObject() {
		super();
	}

	public PathCommonObject(String[] tags, String summary, String description, String operationId, String[] consumes,
			String[] produces, ParameterObject[] parameters, Map<String, ResponseObject> responses,
			List<Map<String, String[]>> security) {
		super();
		this.tags = tags;
		this.summary = summary;
		this.description = description;
		this.operationId = operationId;
		this.consumes = consumes;
		this.produces = produces;
		this.parameters = parameters;
		this.responses = responses;
		this.security = security;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String[] getConsumes() {
		return consumes;
	}

	public void setConsumes(String[] consumes) {
		this.consumes = consumes;
	}

	public String[] getProduces() {
		return produces;
	}

	public void setProduces(String[] produces) {
		this.produces = produces;
	}

	public ParameterObject[] getParameters() {
		return parameters;
	}

	public void setParameters(ParameterObject[] parameters) {
		this.parameters = parameters;
	}

	public Map<String, ResponseObject> getResponses() {
		return responses;
	}

	public void setResponses(Map<String, ResponseObject> responses) {
		this.responses = responses;
	}

	public List<Map<String, String[]>> getSecurity() {
		return security;
	}

	public void setSecurity(List<Map<String, String[]>> security) {
		this.security = security;
	}

}
