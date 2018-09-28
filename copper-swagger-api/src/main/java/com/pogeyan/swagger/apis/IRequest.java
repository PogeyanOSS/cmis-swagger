package com.pogeyan.swagger.apis;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.Part;

public interface IRequest {

	public String getRepositoryId();

	public IAuthRequest getAuth();

	public Map<String, Object> getRequestBaggage();

	public Map<String, Object> getInputMap();

	public String getJsonString();

	public Part getFilePart();

	public String getType();

	public String getObjectIdForMedia();

	public String getInputType();

	public InputStream getInputStream();

}