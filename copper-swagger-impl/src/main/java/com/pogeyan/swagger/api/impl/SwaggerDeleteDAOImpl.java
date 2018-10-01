package com.pogeyan.swagger.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.IRequest;
import com.pogeyan.swagger.api.SwaggerDeleteDAO;
import com.pogeyan.swagger.helpers.SwaggerDeleteHelpers;

public class SwaggerDeleteDAOImpl implements SwaggerDeleteDAO {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerDeleteDAOImpl.class);

	@Override
	public boolean invokeDeleteTypeDefMethod(IRequest obj) throws Exception {

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, inputType: {}, type: {}",
					"SwaggerDeleteDAOImpl", "invokeDeleteTypeDefMethod", obj.getRepositoryId(), obj.getType(),
					obj.getObjectIdForMedia());
			// obj.getObjectIdForMedia() id of the type to be deleted
			boolean del = SwaggerDeleteHelpers.invokeDeleteTypeDefMethod(obj.getRepositoryId(),
					obj.getObjectIdForMedia(), obj.getAuth().getUserName(), obj.getAuth().getPassword());
			return del;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, object: {}, Cause: {}",
					"SwaggerDeleteDAOImpl", "invokeDeleteTypeDefMethod", obj.getRepositoryId(),
					obj.getObjectIdForMedia(), e);
			throw new Exception(e);
		}
	}

	@Override
	public boolean invokeDeleteMethod(IRequest obj) throws Exception {

		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerDeleteDAOImpl",
					"invokeDeleteMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			boolean delete = SwaggerDeleteHelpers.invokeDeleteMethod(obj.getRepositoryId(), obj.getType(),
					obj.getInputType(), obj.getAuth().getUserName(), obj.getAuth().getPassword());
			return delete;
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, type: {}, Cause: {}", "SwaggerDeleteDAOImpl",
					"invokeDeleteMethod", obj.getRepositoryId(), obj.getInputType(), e);
			throw new Exception(e);

		}

	}

}
