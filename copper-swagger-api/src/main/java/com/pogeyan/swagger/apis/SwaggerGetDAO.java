package com.pogeyan.swagger.apis;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

public interface SwaggerGetDAO {

	public JSONObject invokeGetTypeDefMethod(IRequest obj);

	public ContentStream invokeDownloadMethod(IRequest obj);

	public JSONObject invokeGetAllMethod(IRequest obj);

	public Map<String, Object> invokeGetMethod(IRequest obj);

}
