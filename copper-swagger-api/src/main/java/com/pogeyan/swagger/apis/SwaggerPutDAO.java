package com.pogeyan.swagger.apis;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

public interface SwaggerPutDAO {

	public TypeDefinition invokePutTypeDefMethod(IRequest obj);

	public Map<String, Object> invokePutMethod(IRequest obj);

	
}
