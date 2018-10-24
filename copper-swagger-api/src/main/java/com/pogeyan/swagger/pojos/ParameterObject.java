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

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class ParameterObject {
	private String in;
	private String name;
	private String description;
	private boolean required;
	private Map<String, String> schema;
	private String type;
	private String collectionFormat;
	private Map<String, Object> items;
	private String format;
	private String defaultParam;

	public ParameterObject() {
		super();
	}

	public ParameterObject(String in, String name, String description, boolean required, Map<String, String> schema,
			String type, String collectionFormat, Map<String, Object> items, String format, String defaultParam) {
		super();
		this.in = in;
		this.name = name;
		this.description = description;
		this.required = required;
		this.schema = schema;
		this.type = type;
		this.collectionFormat = collectionFormat;
		this.items = items;
		this.format = format;
		this.defaultParam = defaultParam;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Map<String, String> getSchema() {
		return schema;
	}

	public void setSchema(Map<String, String> schema) {
		this.schema = schema;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCollectionFormat() {
		return collectionFormat;
	}

	public void setCollectionFormat(String collectionFormat) {
		this.collectionFormat = collectionFormat;
	}

	public Map<String, Object> getItems() {
		return items;
	}

	public void setItems(Map<String, Object> items) {
		this.items = items;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@JsonProperty(value = "default")
	public String getDefaultparam() {
		return defaultParam;
	}

	public void setDefaultparam(String defaultparam) {
		this.defaultParam = defaultparam;
	}

}
