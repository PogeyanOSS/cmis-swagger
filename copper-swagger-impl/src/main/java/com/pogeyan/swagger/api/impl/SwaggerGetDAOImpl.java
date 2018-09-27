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

	public JSONObject invokeGetTypeDefMethod(IRequest obj) throws Exception {

		JSONObject typeDefinition = null;
		String type = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetTypeDefMethod", obj.getRepositoryId(), obj.getInputType());
			type = obj.getObjectIdForMedia() != null ? obj.getObjectIdForMedia() : obj.getInputType();
			typeDefinition = SwaggerGetHelpers.invokeGetTypeDefMethod(obj.getRepositoryId(), type, obj.getUserName(),
					obj.getPassword(), (boolean) obj.getRequestBaggage().get("includeRelationship"));
		} catch (Exception e) {
			LOG.error(
					"class name: {}, method name: {}, repositoryId: {}, Fetching TypeDefinition Error for type: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeGetTypeDefMethod", obj.getRepositoryId(), type, e);
			throw new Exception(e);
		}
		return typeDefinition;
	}

	public ContentStream invokeDownloadMethod(IRequest obj) throws Exception {
		ContentStream stream = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, objectId: {}", "SwaggerGetDAOImpl",
					"invokeDownloadMethod", obj.getRepositoryId(), obj.getType(), obj.getObjectIdForMedia());
			stream = SwaggerGetHelpers.invokeDownloadMethod(obj.getRepositoryId(), obj.getType(),
					obj.getObjectIdForMedia(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for objectId: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeDownloadMethod", obj.getRepositoryId(), obj.getObjectIdForMedia(), e);
			throw new Exception(e);
		}
		return stream;

	}

	public JSONObject invokeGetAllMethod(IRequest obj) throws Exception {
		JSONObject jsonType = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetAllMethod", obj.getRepositoryId(), obj.getType());
			jsonType = SwaggerGetHelpers.invokeGetAllMethod(obj.getRepositoryId(), obj.getType(),
					(String) obj.getRequestBaggage().get("parentId"), (String) obj.getRequestBaggage().get("skipcount"),
					(String) obj.getRequestBaggage().get("maxitems"), obj.getUserName(), obj.getPassword(),
					(String) obj.getRequestBaggage().get("select"), (String) obj.getRequestBaggage().get("orderby"),
					(boolean) obj.getRequestBaggage().get("includeRelationship"));
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, type: {}, Cause: {}", "SwaggerGetDAOImpl",
					"invokeGetAllMethod", obj.getRepositoryId(), obj.getType(), e);
			throw new Exception(e);
		}
		return jsonType;

	}

	public Map<String, Object> invokeGetMethod(IRequest obj) throws Exception {
		Map<String, Object> objectMap = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, ObjectId: {}", "SwaggerGetDAOImpl",
					"invokeGetMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			objectMap = SwaggerGetHelpers.invokeGetMethod(obj.getRepositoryId(), obj.getType(), obj.getInputType(),
					obj.getUserName(), obj.getPassword(), (String) obj.getRequestBaggage().get("select"));
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for objectId: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeGetMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);
		}
		return objectMap;

	}

}
