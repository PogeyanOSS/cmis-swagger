package com.pogeyan.swagger.apis;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

public interface SwaggerPostDAO {

	public TypeDefinition invokePostTypeDefMethod(IRequest obj) throws Exception;

	public Acl invokePostAcl(IRequest obj) throws Exception;

	public Map<String, Object> invokePostMethod(IRequest obj) throws Exception;

}
