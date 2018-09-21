package com.pogeyan.swagger.api.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerPostDAO;
import com.pogeyan.swagger.helpers.SwaggerPOSTHelpers;

public class SwaggerPostDAOImpl implements SwaggerPostDAO {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPostDAOImpl.class);

	@Override
	public TypeDefinition invokePostTypeDefMethod(IRequest obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acl invokePostAcl(IRequest obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> invokePostMethod(IRequest obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	
//	public TypeDefinition invokePostTypeDefMethod(IRequest obj) {
//
//		TypeDefinition typeDefinition = null;
//		try {
//			LOG.info("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggerPostDAOImpl",
//					"invokePostTypeDefMethod", obj.getRepositoryId(), obj.getInputType());
//			typeDefinition = SwaggerPOSTHelpers.invokePostTypeDefMethod(obj.getRepositoryId(), obj.getUserName(),
//					obj.getPassword(), obj.getInputType());
//		} catch (Exception e) {
//			LOG.error("error while adding Acl for type in repoId: {} for type: {}, Cause: ", obj.getRepositoryId(), obj.getInputType(), e);
//			
//		}
//
//		return typeDefinition;
//	}

	
//	public Acl invokePostAcl(IRequest obj) {
//		Acl Acl = null;
//		try {
//			LOG.info("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggerPostDAOImpl",
//					"invokePostAcl", obj.getRepositoryId(), obj.getInput());
//			Acl = SwaggerPOSTHelpers.invokePostAcl(obj.getRepositoryId(), obj.getaclparam(), obj.getInput(),
//					obj.getUserName(), obj.getPassword());
//		} catch (Exception e) {
//			LOG.error("error while adding Acl in repoId: {}, for type: {}, Cause: ", obj.getRepositoryId(), obj.getInput(), e);
//			
//		}
//
//		return Acl;
//	}
//
//	
//	public Map<String, Object> invokePostMethod(IRequest obj) {
//		Map<String, Object> map = null;
//		try {
//			LOG.info("class name: {}, method name: {}, repositoryId: {}, typeId: {}, object: {}", "SwaggerPostDAOImpl",
//					"invokePostMethod", obj.getRepositoryId(), obj.getType(), obj.getObjectId());
//			map = SwaggerPOSTHelpers.invokePostMethod(obj.getRepositoryId(), obj.getTypeId(), obj.getparent(),
//					obj.getInput(), obj.getUserName(), obj.getPassword(), obj.getObjectId(), obj.getfilePart());
//		} catch (Exception e) {
//			LOG.error("error while adding object in repoId {}, for type: {}, Cause: ", obj.getRepositoryId(), obj.getObjectId(), e);
//		}
//		return map;
//	}
	
}

