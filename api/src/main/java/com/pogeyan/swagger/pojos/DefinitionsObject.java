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
