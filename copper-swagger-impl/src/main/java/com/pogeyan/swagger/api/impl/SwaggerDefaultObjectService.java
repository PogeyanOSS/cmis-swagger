package com.pogeyan.swagger.api.impl;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;

import com.pogeyan.swagger.api.factory.IObjectFacade;

public class SwaggerDefaultObjectService implements IObjectFacade {

	@Override
	public boolean beforeDelete(Session session, CmisObject obj) throws Exception {
		return true;
	}

	@Override
	public Map<String, Object> beforeUpdate(Session session, Map<String, Object> input, List<String> revIds)
			throws Exception {
		return input;
	}

	@Override
	public Map<String, Object> beforecreate(Session session, Map<String, Object> input) throws Exception {
		return input;
	}

}
