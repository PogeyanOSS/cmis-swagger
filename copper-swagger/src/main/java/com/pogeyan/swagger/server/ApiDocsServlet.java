/**
 * Copyright 2017 Pogeyan Technologies
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.pogeyan.swagger.server;

import java.util.Map;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.HttpUtils;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.apis.IRequest;
import com.pogeyan.swagger.apis.SwaggerDeleteDAO;
//import com.pogeyan.swagger.apis.SwaggerDeleteDAO;
import com.pogeyan.swagger.apis.SwaggerGetDAO;
import com.pogeyan.swagger.apis.SwaggerPostDAO;
import com.pogeyan.swagger.apis.SwaggerPutDAO;
//import com.pogeyan.swagger.apis.SwaggerPutDAO;
import com.pogeyan.swagger.factory.SwaggerMethodFactory;
import com.pogeyan.swagger.pojos.ErrorResponse;

/**
 * Servlet implementation class ApiDocsServlet
 */
@WebServlet("/api/*")
@MultipartConfig

public class ApiDocsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ApiDocsServlet.class);
	ObjectMapper mapper = new ObjectMapper();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ApiDocsServlet() {
		super();

	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) {
		SwaggerMethodFactory sFactory = new SwaggerMethodFactory();
		String method = request.getMethod();
		try {
			IRequest reqObj = SwaggerHelpers.getImplClient(request);

			if ((SwaggerHelpers.METHOD_GET.equals(method))) {
				SwaggerGetDAO getDao = sFactory.getMethodService(SwaggerGetDAO.class);
				doGet(getDao, reqObj, response);
			} else if ((SwaggerHelpers.METHOD_POST.equals(method))) {
				SwaggerPostDAO postDao = sFactory.getMethodService(SwaggerPostDAO.class);
				doPost(postDao, reqObj, response);
			} else if (SwaggerHelpers.METHOD_PUT.equals(method)) {
				SwaggerPutDAO putDao = sFactory.getMethodService(SwaggerPutDAO.class);
				doPut(putDao, reqObj, response);
			} else if (SwaggerHelpers.METHOD_DELETE.equals(method)) {
				SwaggerDeleteDAO deleteDao = sFactory.getMethodService(SwaggerDeleteDAO.class);
				doDelete(deleteDao, reqObj, response);
			}

		} catch (Exception e) {
			ErrorResponse resp = SwaggerHelpers.handleException(e);
			HttpUtils.invokeResponseWriter(response, resp.getErrorCode(), resp.getError());
		}

	}

	/**
	 * @param reqObj
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doGet(SwaggerGetDAO getDao, IRequest reqObj, HttpServletResponse response) throws Exception {

		Map<String, Object> propMap = null;
		JSONObject jsonObj = null;
		ContentStream stream = null;
		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doGet",
				reqObj.getRepositoryId(), reqObj.getType());

		if (reqObj.getType() != null && !reqObj.getType().equals(SwaggerHelpers.MEDIA)) {
			if (reqObj.getInputType().equals(SwaggerHelpers.TYPE)) {
				jsonObj = getDao.invokeGetTypeDefMethod(reqObj);

			} else if (reqObj.getInputType().equals(SwaggerHelpers.GETALL)) {
				jsonObj = getDao.invokeGetAllMethod(reqObj);
			} else {
				propMap = getDao.invokeGetMethod(reqObj);
			}
		}

		if (reqObj.getObjectIdForMedia() != null && reqObj.getInputType().equals(SwaggerHelpers.MEDIA)) {
			stream = getDao.invokeDownloadMethod(reqObj);
		}

		if (propMap != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
		} else if (jsonObj != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, jsonObj);
		} else if (stream != null) {
			HttpUtils.invokeDownloadWriter(stream, response);
		} else {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_FOUND, reqObj.getType() + " not found");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(SwaggerPostDAO postDao, IRequest reqObj, HttpServletResponse response) throws Exception {
		JSONObject typeDefinitonObject = null;
		Acl objectAcl = null;
		Map<String, Object> propMap = null;

		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServelet", "doPost",
				reqObj.getRepositoryId(), reqObj.getType());

		if (reqObj.getInputType() != null) {

			if (reqObj.getInputType().equals(SwaggerHelpers.TYPE)) {
				TypeDefinition typeDefiniton = postDao.invokePostTypeDefMethod(reqObj);
				typeDefinitonObject = JSONConverter.convert(typeDefiniton, DateTimeFormat.SIMPLE);
			} else {
				objectAcl = postDao.invokePostAcl(reqObj);
			}
		} else {
			propMap = postDao.invokePostMethod(reqObj);
		}

		if (propMap != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CREATED, propMap);
		} else if (typeDefinitonObject != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, typeDefinitonObject);
		} else if (objectAcl != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, objectAcl);
		} else {
			// error
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CONFLICT,
					reqObj.getType() + " not created.");
		}
	}

	protected void doPut(SwaggerPutDAO putDao, IRequest reqObj, HttpServletResponse response) throws Exception {

		JSONObject typeDefinitionObject = new JSONObject();
		Map<String, Object> propMap = null;
		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doPut",
				reqObj.getRepositoryId(), reqObj.getType());

		if (reqObj.getType() != null)
			if (reqObj.getInputType().equals(SwaggerHelpers.TYPE)) {
				TypeDefinition typeDefinition = putDao.invokePutTypeDefMethod(reqObj);
				typeDefinitionObject = JSONConverter.convert(typeDefinition, DateTimeFormat.SIMPLE);
			} else {
				propMap = putDao.invokePutMethod(reqObj);
			}

		if (propMap != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
		} else if (typeDefinitionObject != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, typeDefinitionObject);
		} else {
			// error
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_MODIFIED,
					reqObj.getType() + "not updated.");
		}

	}

	protected void doDelete(SwaggerDeleteDAO deleteDAO, IRequest reqObj, HttpServletResponse response)
			throws Exception {

		String statusMessage = null;
		int code = HttpServletResponse.SC_NOT_FOUND;
		boolean status = false;
		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doDelete",
				reqObj.getRepositoryId(), reqObj.getType());
		if (reqObj.getType() != null) {
			if (reqObj.getInputType().equals(SwaggerHelpers.TYPE)) {
				status = deleteDAO.invokeDeleteTypeDefMethod(reqObj);
			} else {
				status = deleteDAO.invokeDeleteMethod(reqObj);
			}
		} else if (status) {
			code = HttpServletResponse.SC_OK;
			statusMessage = reqObj.getType() + " Deleted Successfully";
		} else {
			HttpUtils.invokeResponseWriter(response, code, statusMessage);
		}
	}
}
