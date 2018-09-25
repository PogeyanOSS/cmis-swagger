package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerPostDAO;
import com.pogeyan.swagger.helpers.SwaggerPostHelpers;

public class SwaggerPostDAOImpl implements SwaggerPostDAO {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPostDAOImpl.class);

	
	public TypeDefinition invokePostTypeDefMethod(IRequest obj) {

		TypeDefinition typeDefinition = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggerPostDAOImpl",
					"invokePostTypeDefMethod", obj.getRepositoryId(), obj.getInputType());
			typeDefinition = SwaggerPostHelpers.invokePostTypeDefMethod(obj.getRepositoryId(), obj.getUserName(),
					obj.getPassword(), obj.getInputStream());
			
		} catch (Exception e) {
			LOG.error("error while adding Acl for type in repoId: {} for type: {}, Cause: ", obj.getRepositoryId(), obj.getInputStream(), e);
		
		}

		return typeDefinition;
	}

	
	public Acl invokePostAcl(IRequest obj) {
		Acl Acl = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggerPostDAOImpl",
					"invokePostAcl", obj.getRepositoryId(), obj.getInputType());
			Acl = SwaggerPostHelpers.invokePostAcl(obj.getRepositoryId(),obj.getInputType(), obj.getInputMap(),
					obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("error while adding Acl in repoId: {}, for type: {}, Cause: ", obj.getRepositoryId(), obj.getInputType(), e);
			
		}

		return Acl;
	}

	
	public Map<String, Object> invokePostMethod(IRequest obj) {
		Map<String, Object> map = null;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, typeId: {}, object: {}", "SwaggerPostDAOImpl",
					"invokePostMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			map = SwaggerPostHelpers.invokePostMethod(obj.getRepositoryId(), obj.getType(), obj.getparentId(),
					obj.getInputMap(), obj.getUserName(), obj.getPassword(), obj.getInputType(), obj.getFilePart(),obj.getincludeCurd(),obj.getJsonString());
		} catch (Exception e) {
			LOG.error("error while adding object in repoId {}, for type: {}, Cause: ", obj.getRepositoryId(),obj.getInputType(), e);
		}
		return map;
	}
	
}

