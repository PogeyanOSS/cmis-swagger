package com.pogeyan.swagger.factory;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;

import com.pogeyan.swagger.impl.factory.IObjectFacade;

public class SwaggerFactory implements IObjectFacade {

	@Override
	public boolean beforeDelete(Session session, CmisObject obj) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Map<String, Object> beforeUpdate(Session session, Map<String, Object> input, List<String> revIds)
			throws Exception {
		// TODO Auto-generated method stub
		return input;
	}

	@Override
	public Map<String, Object> beforecreate(Session session, Map<String, Object> input) throws Exception {
		// TODO Auto-generated method stub
		return input;
	}

}
