package com.pogeyan.swagger.services;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.pojos.DefinitionsObject;
import com.pogeyan.swagger.pojos.InfoObject;
import com.pogeyan.swagger.pojos.PathObject;
import com.pogeyan.swagger.pojos.SecurityDefinitionObject;
import com.pogeyan.swagger.pojos.SwaggerJsonObject;
import com.pogeyan.swagger.pojos.TagObject;
import com.pogeyan.swagger.utils.SwaggerHelpers;

public class SwaggerGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGenerator.class);

	public String generateSwagger(String repoId) {
		ObjectMapper mp = new ObjectMapper();
		String jsonString = null;
		try {
			LOG.info("Generating Swagger Json for repository:{}", repoId);
			Session session = SwaggerHelpers.createSession(repoId, "admin", "admin123");
			SwaggerHelpers.getAllTypes(session);
			InfoObject infoObj = SwaggerHelpers.generateInfoObject();
			List<TagObject> tags = SwaggerHelpers.generateTagsForAllTypes();
			Map<String, SecurityDefinitionObject> securityDef = SwaggerHelpers.getSecurityDefinitions();
			Map<String, DefinitionsObject> definitions = SwaggerHelpers.getDefinitions();
			Map<String, PathObject> paths = SwaggerHelpers.generatePathForAllTypes();
			LOG.info("Swagger URL:{}", System.getenv("HOST_SWAGGER_URL"));
			SwaggerJsonObject swaggerObj = new SwaggerJsonObject(SwaggerHelpers.generateInfoObject().getVersion(),
					SwaggerHelpers.getHostSwaggerUrl(), repoId, new String[] { "http" }, infoObj,
					SwaggerHelpers.generateExternalDocsObject(), tags, securityDef, definitions, paths);
			jsonString = mp.writeValueAsString(swaggerObj);
			LOG.info("JsonDoc:{}, for repositoryId:{}", jsonString, repoId);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Json generation exception: {}", e.getMessage());
		}
		return jsonString;

	}
}
