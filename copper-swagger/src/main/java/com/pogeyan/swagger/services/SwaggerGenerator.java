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
package com.pogeyan.swagger.services;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.SwaggerUIDefnHelpers;
import com.pogeyan.swagger.pojos.DefinitionsObject;
import com.pogeyan.swagger.pojos.InfoObject;
import com.pogeyan.swagger.pojos.PathObject;
import com.pogeyan.swagger.pojos.SecurityDefinitionObject;
import com.pogeyan.swagger.pojos.SwaggerJsonObject;
import com.pogeyan.swagger.pojos.TagObject;

public class SwaggerGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGenerator.class);

	public String generateSwagger(String repositoryId) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}", "SwaggerGenerator", "generateSwagger", repositoryId);

			InfoObject infoObj = SwaggerUIDefnHelpers.generateInfoObject();
			List<TagObject> tags = SwaggerUIDefnHelpers.generateTagsForAllTypes();
			Map<String, SecurityDefinitionObject> securityDef = SwaggerUIDefnHelpers.getSecurityDefinitions();
			Map<String, DefinitionsObject> definitions = SwaggerUIDefnHelpers.getDefinitions();
			Map<String, PathObject> paths = SwaggerUIDefnHelpers.generatePathForAllTypes();
			SwaggerJsonObject swaggerObject = new SwaggerJsonObject(SwaggerUIDefnHelpers.generateInfoObject().getVersion(),
					SwaggerUIDefnHelpers.getHostSwaggerUrl(), repositoryId, new String[] { "http" }, infoObj,
					SwaggerUIDefnHelpers.generateExternalDocsObject(), tags, securityDef, definitions, paths);
			jsonString = mapper.writeValueAsString(swaggerObject);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Json generation exception: {}", e.getMessage());
		}
		return jsonString;

	}
}
