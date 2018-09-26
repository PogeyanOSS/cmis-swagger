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
	public TypeDefinition invokePutTypeDefMethod(IRequest obj) {

		TypeDefinition typeDef = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object :{}", "SwaggerPutDAOImpl",
					"invokePutTypeDefMethod", obj.getRepositoryId(), obj.getType());
			typeDef = SwaggerPutHelpers.invokePutTypeDefMethod(obj.getRepositoryId(), obj.getObjectIdForMedia(),
					obj.getInputStream(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("updating objects in repoId: {}, for type: {}, Cause: ", obj.getRepositoryId(),
					obj.getObjectIdForMedia(), e);

		}
		return typeDef;
	}

	@Override
	public Map<String, Object> invokePutMethod(IRequest obj) {

		Map<String, Object> objectMap = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerPutDAOImpl",
					"invokePutMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			objectMap = SwaggerPutHelpers.invokePutMethod(obj.getRepositoryId(), obj.getType(), obj.getInputType(),
					obj.getInputMap(), obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("updating objects in repoId: {}, fotr type: {}, Cause: ", obj.getRepositoryId(),
					obj.getInputType(), e);
			try {
				if (objectMap != null)
					throw new Exception("Type Missmatch or object not found");
			} catch (Exception e1) {
			}

		}
		return objectMap;

	}

}
