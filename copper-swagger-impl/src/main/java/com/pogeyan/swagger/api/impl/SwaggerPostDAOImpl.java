package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.IRequest;
import com.pogeyan.swagger.api.SwaggerPostDAO;
import com.pogeyan.swagger.helpers.SwaggerPostHelpers;

public class SwaggerPostDAOImpl implements SwaggerPostDAO {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPostDAOImpl.class);

	public TypeDefinition invokePostTypeDefMethod(IRequest obj) throws Exception {

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerPostDAOImpl",
					"invokePostTypeDefMethod", obj.getRepositoryId(), obj.getInputType());
			TypeDefinition typeDefinition = SwaggerPostHelpers.invokePostTypeDefMethod(obj.getRepositoryId(),
					obj.getAuth().getUserName(), obj.getAuth().getPassword(), obj.getInputStream());
			return typeDefinition;

		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for type: {}, Cause: {}",
					"SwaggerPostDAOImpl", "invokePostTypeDefMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);
		}
	}

	public Acl invokePostAcl(IRequest obj) throws Exception {

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, Input type: {}", "SwaggerPostDAOImpl",
					"invokePostAcl", obj.getRepositoryId(), obj.getInputType());
			Acl acl = SwaggerPostHelpers.invokePostAcl(obj.getRepositoryId(), obj.getInputType(), obj.getInputMap(),
					obj.getAuth().getUserName(), obj.getAuth().getPassword());
			return acl;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for type: {}, Cause: {}",
					"SwaggerPostDAOImpl", "invokePostAcl", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);
		}
	}

	public Map<String, Object> invokePostMethod(IRequest obj) throws Exception {

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, typeId: {}, object: {}", "SwaggerPostDAOImpl",
					"invokePostMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			Map<String, Object> map = SwaggerPostHelpers.invokePostMethod(obj.getRepositoryId(), obj.getType(),
					(String) obj.getRequestBaggage().get("parentId"), obj.getInputMap(), obj.getAuth().getUserName(),
					obj.getAuth().getPassword(), obj.getInputType(), obj.getFilePart(),
					(boolean) obj.getRequestBaggage().get("includeRelation"), obj.getJsonString());
			return map;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, for type: {}, Cause: {}",
					"SwaggerPostDAOImpl", "invokePostMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);
		}

	}

}
