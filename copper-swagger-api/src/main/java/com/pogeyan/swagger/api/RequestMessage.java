package com.pogeyan.swagger.api;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.Part;

@SuppressWarnings("unused")
public class RequestMessage implements IRequest {
	private String[] pathFragments;
	private String repositoryId;
	private Map<String, Object> requestBaggage;
	private Map<String, Object> inputMap;
	private String jsonString;
	private Part filePart;
	private String type;
	private InputStream inputStream;
	private String objectIdForMedia;
	private String inputType;
	private IAuthRequest authorization;

	public RequestMessage() {
		super();
	}

	public RequestMessage(IAuthRequest authorization, String[] pathFragments) {
		super();
		this.authorization = authorization;
		this.pathFragments = pathFragments;
		this.repositoryId = pathFragments[0];
		this.type = pathFragments[1];
		if (pathFragments.length > 2 && pathFragments[2] != null) {
			this.inputType = pathFragments[2];
		}
		if (pathFragments.length > 3 && pathFragments[3] != null) {
			this.objectIdForMedia = pathFragments[3];
		}
	}

	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	@Override
	public Map<String, Object> getRequestBaggage() {
		return requestBaggage;
	}

	public void setRequestBaggage(Map<String, Object> requestBaggage) {
		this.requestBaggage = requestBaggage;
	}

	@Override
	public Map<String, Object> getInputMap() {
		return inputMap;
	}

	public void setInputMap(Map<String, Object> inputMap) {
		this.inputMap = inputMap;
	}

	@Override
	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	@Override
	public Part getFilePart() {
		return filePart;
	}

	public void setFilePart(Part filePart) {
		this.filePart = filePart;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getObjectIdForMedia() {
		return objectIdForMedia;
	}

	public void setObjectIdForMedia(String objectIdForMedia) {
		this.objectIdForMedia = objectIdForMedia;
	}

	@Override
	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public IAuthRequest getAuth() {
		return authorization;
	}
}