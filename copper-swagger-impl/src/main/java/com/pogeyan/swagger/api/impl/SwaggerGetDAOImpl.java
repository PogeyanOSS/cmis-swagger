package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerGetDAO;
import com.pogeyan.swagger.helpers.SwaggerGetHelpers;

public class SwaggerGetDAOImpl implements SwaggerGetDAO {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGetDAOImpl.class);

	public JSONObject invokeGetTypeDefMethod(IRequest obj) {

		JSONObject typeDefinition = null;
		try {

			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetTypeDefMethod", obj.getRepositoryId(), obj.getType());

			typeDefinition = SwaggerGetHelpers.invokeGetTypeDefMethod(obj.getRepositoryId(),
					obj.getObjectIdForMedia() != null ? obj.getObjectIdForMedia() : obj.getType(), obj.getUserName(),
					obj.getPassword(), (boolean) obj.getRequestBaggage().get("includeRelationship"));
		} catch (Exception e) {
			LOG.error("Fetching TypeDefinition Error in repoId: {}, for type: {}, Cause: ", obj.getRepositoryId(),
					obj.getType(), e);
		}
		return typeDefinition;
	}

	public ContentStream invokeDownloadMethod(IRequest obj) {
		ContentStream stream = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, objectId: {}", "SwaggerGetDAOImpl",
					"invokeDownloadMethod", obj.getRepositoryId(), obj.getType(), obj.getObjectIdForMedia());
			stream = SwaggerGetHelpers.invokeDownloadMethod(obj.getRepositoryId(), obj.getType(),
					obj.getObjectIdForMedia(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("Fetching Content in repoId: {} for objectId: {}, Cause: ", obj.getRepositoryId(),
					obj.getObjectIdForMedia(), e);
		}

		return stream;

	}

	public JSONObject invokeGetAllMethod(IRequest obj) {
		JSONObject jsontype = null;

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetAllMethod", obj.getRepositoryId(), obj.getType());
			jsontype = SwaggerGetHelpers.invokeGetAllMethod(obj.getRepositoryId(), obj.getType(),
					(String) obj.getRequestBaggage().get("parentId"), (String) obj.getRequestBaggage().get("skipcount"),
					(String) obj.getRequestBaggage().get("maxitems"), obj.getUserName(), obj.getPassword(),
					(String) obj.getRequestBaggage().get("select"), (String) obj.getRequestBaggage().get("orderby"),
					(boolean) obj.getRequestBaggage().get("includeRelationship"));
		} catch (Exception e) {
			LOG.error("Fetching Objects in repoId: {}, Cause: {}", obj.getRepositoryId(), e);
		}
		return jsontype;

	}

	public Map<String, Object> invokeGetMethod(IRequest obj) {
		Map<String, Object> objectMap = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, ObjectId: {}", "SwaggerGetDAOImpl",
					"invokeGetMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			objectMap = SwaggerGetHelpers.invokeGetMethod(obj.getRepositoryId(), obj.getType(), obj.getInputType(),
					obj.getUserName(), obj.getPassword(), (String) obj.getRequestBaggage().get("select"));
		} catch (Exception e) {
			LOG.error("Error in Fetching object in repoId: {}, for objectId: {}, Cause: ", obj.getRepositoryId(),
					obj.getInputType(), e);
		}
		return objectMap;

	}

}
