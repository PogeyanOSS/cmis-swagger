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
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";

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

			if ((METHOD_GET.equals(method))) {
				SwaggerGetDAO getDao = sFactory.getMethodService(SwaggerGetDAO.class);
				doGet(getDao, reqObj, response);
			} else if ((METHOD_POST.equals(method))) {
				SwaggerPostDAO postDao = sFactory.getMethodService(SwaggerPostDAO.class);
				doPost(postDao, reqObj, response);
			} else if (METHOD_PUT.equals(method)) {
				SwaggerPutDAO putDao = sFactory.getMethodService(SwaggerPutDAO.class);
				doPut(putDao, reqObj, response);
			} else if (METHOD_DELETE.equals(method)) {
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
		try {
			Map<String, Object> propMap = null;
			JSONObject jsonObj = null;
			ContentStream stream = null;
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doGet",
					reqObj.getRepositoryId(), reqObj.getType());

			if (reqObj.getInputType() != null && !reqObj.getInputType().equals("media")) {
				if (reqObj.getInputType().equals("type")) {
					jsonObj = getDao.invokeGetTypeDefMethod(reqObj);

				} else if (reqObj.getInputType().equals("getAll")) {
					jsonObj = getDao.invokeGetAllMethod(reqObj);
				} else {
					propMap = getDao.invokeGetMethod(reqObj);
				}
			}

			if (reqObj.getObjectIdForMedia() != null && reqObj.getInputType().equals("media")) {
				stream = getDao.invokeDownloadMethod(reqObj);
			}

			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
			} else if (jsonObj != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, jsonObj);
			} else if (stream != null) {
				HttpUtils.invokeDownloadWriter(stream, response);
			} else {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_FOUND,
						reqObj.getType() + " not found");
			}
		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getMessage());
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(SwaggerPostDAO PostDao, IRequest reqObj, HttpServletResponse response) throws Exception {
		JSONObject typedefinitonObject = null;
		Acl objectacl = null;
		Map<String, Object> propMap = null;

		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServelet", "doPost",
				reqObj.getRepositoryId(), reqObj.getInputType());
		if (reqObj.getInputType() != null) {

			if (reqObj.getInputType().equals("type")) {
				TypeDefinition typedefiniton = PostDao.invokePostTypeDefMethod(reqObj);
				typedefinitonObject = JSONConverter.convert(typedefiniton, DateTimeFormat.SIMPLE);
				objectacl = PostDao.invokePostAcl(reqObj);
			} else {
				propMap = PostDao.invokePostMethod(reqObj);
			}
		}

		if (propMap != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CREATED, propMap);
		} else if (typedefinitonObject != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, typedefinitonObject);
		} else if (objectacl != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, objectacl);
		} else {
			// error
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CONFLICT,
					reqObj.getType() + " not created.");
		}
	}

	protected void doPut(SwaggerPutDAO PutDao, IRequest reqObj, HttpServletResponse response) throws Exception {
		JSONObject typedefinitionObject = new JSONObject();
		Map<String, Object> propMap = null;
		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doPut",
				reqObj.getRepositoryId(), reqObj.getType());

		if (reqObj.getInputType() != null)
			if (reqObj.getInputType().equals("type")) {
				TypeDefinition typedefinition = PutDao.invokePutTypeDefMethod(reqObj);
				typedefinitionObject = JSONConverter.convert(typedefinition, DateTimeFormat.SIMPLE);
			} else {
				propMap = PutDao.invokePutMethod(reqObj);
			}

		if (propMap != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
		} else if (typedefinitionObject != null) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, typedefinitionObject);
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
			if (reqObj.getInputType().equals("type")) {
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
