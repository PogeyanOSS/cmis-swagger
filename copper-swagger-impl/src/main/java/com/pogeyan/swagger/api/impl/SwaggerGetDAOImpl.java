package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.IRequest;
import com.pogeyan.swagger.api.SwaggerGetDAO;
import com.pogeyan.swagger.helpers.SwaggerGetHelpers;

public class SwaggerGetDAOImpl implements SwaggerGetDAO {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGetDAOImpl.class);

	public JSONObject invokeGetTypeDefMethod(IRequest obj) throws Exception {

		String type = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetTypeDefMethod", obj.getRepositoryId(), obj.getInputType());
			type = obj.getObjectIdForMedia() != null ? obj.getObjectIdForMedia() : obj.getInputType();
			JSONObject typeDefinition = SwaggerGetHelpers.invokeGetTypeDefMethod(obj.getRepositoryId(), type,
					obj.getAuth().getUserName(), obj.getAuth().getPassword(),
					(boolean) obj.getRequestBaggage().get("includeRelationship"));
			return typeDefinition;
		} catch (Exception e) {
			LOG.error(
					"class name: {}, method name: {}, repositoryId: {}, Fetching TypeDefinition Error for type: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeGetTypeDefMethod", obj.getRepositoryId(), type, e);
			throw new Exception(e);
		}
	}

	public ContentStream invokeDownloadMethod(IRequest obj) throws Exception {
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, objectId: {}", "SwaggerGetDAOImpl",
					"invokeDownloadMethod", obj.getRepositoryId(), obj.getType(), obj.getObjectIdForMedia());
			ContentStream stream = SwaggerGetHelpers.invokeDownloadMethod(obj.getRepositoryId(), obj.getType(),
					obj.getObjectIdForMedia(), obj.getAuth().getUserName(), obj.getAuth().getPassword());
			return stream;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for objectId: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeDownloadMethod", obj.getRepositoryId(), obj.getObjectIdForMedia(), e);

			throw new Exception(e);
		}
	}

	public JSONObject invokeGetAllMethod(IRequest obj) throws Exception {
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerGetDAOImpl",
					"invokeGetAllMethod", obj.getRepositoryId(), obj.getType());
			JSONObject jsonType = SwaggerGetHelpers.invokeGetAllMethod(obj.getRepositoryId(), obj.getType(),
					(String) obj.getRequestBaggage().get("parentId"), (String) obj.getRequestBaggage().get("skipcount"),
					(String) obj.getRequestBaggage().get("maxitems"), obj.getAuth().getUserName(),
					obj.getAuth().getPassword(), (String) obj.getRequestBaggage().get("select"),
					(String) obj.getRequestBaggage().get("orderby"),
					(boolean) obj.getRequestBaggage().get("includeRelationship"));
			return jsonType;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, type: {}, Cause: {}", "SwaggerGetDAOImpl",
					"invokeGetAllMethod", obj.getRepositoryId(), obj.getType(), e);
			throw new Exception(e);
		}
	}

	public Map<String, Object> invokeGetMethod(IRequest obj) throws Exception {
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, ObjectId: {}", "SwaggerGetDAOImpl",
					"invokeGetMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			Map<String, Object> objectMap = SwaggerGetHelpers.invokeGetMethod(obj.getRepositoryId(), obj.getType(),
					obj.getInputType(), obj.getAuth().getUserName(), obj.getAuth().getPassword(),
					(String) obj.getRequestBaggage().get("select"));
			return objectMap;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for objectId: {}, Cause: {}",
					"SwaggerGetDAOImpl", "invokeGetMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);
		}

	}

}
