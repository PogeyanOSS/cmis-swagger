package com.pogeyan.swagger.api.factory;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;

public interface IObjectFacade {
	public Map<String, Object> beforecreate(Session session, Map<String, Object> input) throws Exception;

	public Map<String, Object> beforeUpdate(Session session, Map<String, Object> input, List<String> revIds)
			throws Exception;

	public boolean beforeDelete(Session session, CmisObject obj) throws Exception;
}
