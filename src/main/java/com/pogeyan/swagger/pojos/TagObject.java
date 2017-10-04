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

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(false)
public class TagObject {
	private String name;
	private String description;
	private ExternalDocs externalDocs;

	public TagObject() {
		super();
	}

	public TagObject(String name, String description, ExternalDocs externalDocs) {
		super();
		this.name = name;
		this.description = description;
		this.externalDocs = externalDocs;
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

	public ExternalDocs getExternalDocs() {
		return externalDocs;
	}

	public void setExternalDocs(ExternalDocs externalDocs) {
		this.externalDocs = externalDocs;
	}

}
