package com.pogeyan.swagger.apis;


import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

public interface SwaggerPostDAO {
	

	public TypeDefinition invokePostTypeDefMethod(IRequest obj);

	public Acl invokePostAcl(IRequest obj);

	public Map<String, Object> invokePostMethod(IRequest obj);

}
