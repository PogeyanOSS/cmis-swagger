package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerPutDAO;
import com.pogeyan.swagger.helpers.SwaggerPutHelpers;

public class SwaggerPutDAOImpl implements SwaggerPutDAO {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPutDAOImpl.class);

	@Override
	public TypeDefinition invokePutTypeDefMethod(IRequest obj) throws Exception {

		TypeDefinition typeDef = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerPutDAOImpl",
					"invokePutTypeDefMethod", obj.getRepositoryId(), obj.getInputType(), obj.getObjectIdForMedia());
			typeDef = SwaggerPutHelpers.invokePutTypeDefMethod(obj.getRepositoryId(), obj.getObjectIdForMedia(),
					obj.getInputStream(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for type: {}, Cause: {}", "SwaggerPutDAOImpl",
					"invokePutTypeDefMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);

		}
		return typeDef;
	}

	@Override
	public Map<String, Object> invokePutMethod(IRequest obj) throws Exception {
		Map<String, Object> objectMap = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerPutDAOImpl",
					"invokePutMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			objectMap = SwaggerPutHelpers.invokePutMethod(obj.getRepositoryId(), obj.getType(), obj.getInputType(),
					obj.getInputMap(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for type: {}, Cause: {}", "SwaggerPutDAOImpl",
					"invokePutMethod", obj.getRepositoryId(), obj.getType(), e);
			throw new Exception(e);
		}

		return objectMap;

	}

}
