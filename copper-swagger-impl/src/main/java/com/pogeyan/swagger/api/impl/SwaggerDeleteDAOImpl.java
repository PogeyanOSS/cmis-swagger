package com.pogeyan.swagger.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerDeleteDAO;
import com.pogeyan.swagger.helpers.SwaggerDeleteHelpers;

public class SwaggerDeleteDAOImpl implements SwaggerDeleteDAO {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerDeleteDAOImpl.class);

	@Override
	public boolean invokeDeleteTypeDefMethod(IRequest obj) {

		boolean del = false;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {} object: {}", "SwaggerDeleteDAOImpl",
					"invokeDeleteTypeDefMethod", obj.getRepositoryId(), obj.getType(), obj.getObjectIdForMedia());
			// here obj.getObjectIdForMedia() id the type to be deleted
			del = SwaggerDeleteHelpers.invokeDeleteTypeDefMethod(obj.getRepositoryId(), obj.getObjectIdForMedia(),
					obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("deleting objects in repoId: {}, Cause: ", obj.getRepositoryId(), e);
			e.printStackTrace();
		}
		return del;

	}

	@Override
	public boolean invokeDeleteMethod(IRequest obj) {

		boolean delete = false;
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerDeleteDAOImpl",
					"invokeDeleteMethod", obj.getRepositoryId(), obj.getType(), obj.getInputType());
			delete = SwaggerDeleteHelpers.invokeDeleteMethod(obj.getRepositoryId(), obj.getType(), obj.getInputType(),
					obj.getUserName(), obj.getPassword());
		} catch (Exception e) {
			LOG.error("deleting objects in repoId: {}, Cause: ", obj.getRepositoryId(), e);
			e.printStackTrace();
		}
		return delete;
	}

}
