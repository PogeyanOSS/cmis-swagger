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
public class SwaggerJsonObject {

	private String swagger;
	private String host;
	private String basePath;
	private String schemes[];
	private InfoObject info;
	private ExternalDocs externalDocs;
	private List<TagObject> tags;
	private Map<String, SecurityDefinitionObject> securityDefinitions;
	private Map<String, DefinitionsObject> definitions;
	private Map<String, PathObject> paths;

	public SwaggerJsonObject() {
		super();
	}

	public SwaggerJsonObject(String swaggerVersion, String host, String basePath, String schemes[], InfoObject info,
			ExternalDocs externalDocs, List<TagObject> tags, Map<String, SecurityDefinitionObject> securityDefinitions,
			Map<String, DefinitionsObject> definitions, Map<String, PathObject> paths) {
		super();
		this.swagger = swaggerVersion;
		this.host = host;
		this.basePath = basePath;
		this.schemes = schemes;
		this.info = info;
		this.externalDocs = externalDocs;
		this.tags = tags;
		this.securityDefinitions = securityDefinitions;
		this.definitions = definitions;
		this.paths = paths;
	}

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swagger) {
		this.swagger = swagger;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String[] getSchemes() {
		return schemes;
	}

	public void setSchemes(String[] schemes) {
		this.schemes = schemes;
	}

	public InfoObject getInfo() {
		return info;
	}

	public void setInfo(InfoObject info) {
		this.info = info;
	}

	public ExternalDocs getExternalDocs() {
		return externalDocs;
	}

	public void setExternalDocs(ExternalDocs externalDocs) {
		this.externalDocs = externalDocs;
	}

	public List<TagObject> getTags() {
		return tags;
	}

	public void setTags(List<TagObject> tags) {
		this.tags = tags;
	}

	public Map<String, SecurityDefinitionObject> getSecurityDefinitions() {
		return securityDefinitions;
	}

	public void setSecurityDefinitions(Map<String, SecurityDefinitionObject> securityDefinitions) {
		this.securityDefinitions = securityDefinitions;
	}

	public Map<String, DefinitionsObject> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(Map<String, DefinitionsObject> definitions) {
		this.definitions = definitions;
	}

	public Map<String, PathObject> getPaths() {
		return paths;
	}

	public void setPaths(Map<String, PathObject> paths) {
		this.paths = paths;
	}

}
