package com.pogeyan.swagger.api;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

public interface SwaggerGetDAO {

	public JSONObject invokeGetTypeDefMethod(IRequest obj) throws Exception;

	public ContentStream invokeDownloadMethod(IRequest obj) throws Exception;

	public JSONObject invokeGetAllMethod(IRequest obj) throws Exception;

	public Map<String, Object> invokeGetMethod(IRequest obj) throws Exception;
}
