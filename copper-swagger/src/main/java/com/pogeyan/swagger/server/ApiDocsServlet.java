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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.cxf.helpers.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.pojos.ErrorResponse;
import com.pogeyan.swagger.services.SwaggerApiService;
import com.pogeyan.swagger.utils.HttpUtils;

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
		try {
			Map<String, Object> input = null;
			Part filePart = null;
			String method = request.getMethod();
			String authorization = request.getHeader("Authorization");
			String credentials[] = HttpUtils.getCredentials(authorization);
			String pathFragments[] = HttpUtils.splitPath(request);
			String typeId = pathFragments[1];
			if (METHOD_POST.equals(method)) {
				if (request.getContentType().contains("multipart/form-data")) {
					input = request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
					filePart = request.getPart("file") != null ? request.getPart("file") : null;
				} else if (request.getContentType().equals("application/x-www-form-urlencoded")) {
					input = request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
				} else if (typeId.equals("_metadata")) {
				} else {
					String jsonString = IOUtils.toString(request.getInputStream());
					input = mapper.readValue(jsonString, new TypeReference<Map<String, String>>() {
					});
				}
				doPost(request, response, credentials, pathFragments, input, filePart);
			} else if (METHOD_GET.equals(method)) {
				doGet(request, response, credentials, pathFragments);
			} else if (METHOD_PUT.equals(method)) {

				if (request.getInputStream() != null) {
					if (typeId.equals("_metadata")) {
					} else {
						String jsonString = IOUtils.toString(request.getInputStream());
						input = mapper.readValue(jsonString, new TypeReference<Map<String, String>>() {
						});
					}
				}
				doPut(request, response, credentials, pathFragments, input);
			} else if (METHOD_DELETE.equals(method)) {
				doDelete(request, response, credentials, pathFragments);
			}

		} catch (Exception e) {
			ErrorResponse resp = SwaggerHelpers.handleException(e);
			HttpUtils.invokeResponseWriter(response, resp.getErrorCode(), resp.getError());
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response, String[] credentials,
			String[] pathFragments) throws Exception {
		try {
			Map<String, Object> propMap = null;
			JSONObject object = null;
			ContentStream stream = null;
			String username = credentials[0];
			String password = credentials[1];
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String type = pathFragments[2];
			String inputId = pathFragments.length > 3 ? pathFragments[3] : null;
			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServlet", "doGet",
					repositoryId, typeId);
			String select = null;
			String filter = null;
			String order = null;
			if (request.getQueryString() != null) {
				select = request.getParameter("select") != null ? request.getParameter("select").replace("_", ":")
						: null;
				filter = request.getParameter("filter") != null ? request.getParameter("filter").replace("_", ":")
						: null;
				order = request.getParameter("orderby") != null ? request.getParameter("orderby").replace("_", ":")
						: null;
			}

			if (select != null && filter != null) {
				select = select + "," + URLDecoder.decode(filter, "UTF-8");
			} else if (select == null && filter != null) {
				select = "*," + filter;
			}
			if (inputId != null) {
				if (type.equals("type")) {
					String includeRelationship = request.getParameter("includeRelationship");
					object = SwaggerApiService.invokeGetTypeDefMethod(repositoryId, typeId, username, password,
							includeRelationship != null ? Boolean.parseBoolean(includeRelationship) : false);
				}

				else if (type.equals("media")) {
					stream = SwaggerApiService.invokeDownloadMethod(repositoryId, typeId, inputId, username, password,
							response);
				}
			} else if (type.equals("getAll")) {
				String skipCount = request.getParameter("skipcount");
				String maxItems = request.getParameter("maxitems");
				String parentId = request.getParameter("parentId");
				String includeRelationship = request.getParameter("includeRelationship");
				object = SwaggerApiService.invokeGetAllMethod(repositoryId, typeId, parentId != null ? parentId : null,
						skipCount, maxItems, username, password, select, order,
						includeRelationship != null ? Boolean.parseBoolean(includeRelationship) : false);
			} else {
				propMap = SwaggerApiService.invokeGetMethod(repositoryId, typeId, type, username, password, select);
			}

			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
			} else if (object != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, object);
			} else if (stream != null) {
				HttpUtils.invokeDownloadWriter(stream, response);
			} else {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_FOUND, typeId + " not found");
			}
		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getMessage());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response, String[] credentials,
			String pathFragments[], Map<String, Object> input, Part filePart) throws Exception {
		try {
			JSONObject typedefinitonObject = null;
			Acl objectacl = null;
			Map<String, Object> propMap = new HashMap<>();
			String username = credentials[0];
			String password = credentials[1];
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String parentId = request.getParameter("parentId");
			String typeInput = pathFragments.length > 2 ? pathFragments[2] : null;

			LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ApiDocsServelet", "doPost",
					repositoryId, typeId);
			if (typeInput != null) {
				if (typeInput.equals("type")) {
					TypeDefinition typedefiniton = SwaggerApiService.invokePostTypeDefMethod(repositoryId, username,
							password, request.getInputStream());
					typedefinitonObject = JSONConverter.convert(typedefiniton, DateTimeFormat.SIMPLE);
				} else if (typeId.equals("Acl")) {
					objectacl = SwaggerApiService.invokePostAcl(repositoryId, typeInput, input, username, password);
				}
			} else {
				propMap = SwaggerApiService.invokePostMethod(repositoryId, typeId, parentId, input, username, password,
						typeInput, filePart);
			}
			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CREATED, propMap);
			} else if (typedefinitonObject != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, typedefinitonObject);
			} else if (objectacl != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, objectacl);
			} else {
				// error
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CONFLICT, typeId + " not created.");
			}
		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getError());
		}
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse response, String[] credentials,
			String[] pathFragments, Map<String, Object> input) throws Exception {
		try {
			LOG.info("method:{} repositoryId:{} type:{}", req.getMethod(), pathFragments[0], pathFragments[1]);
			JSONObject obj = new JSONObject();
			Map<String, Object> propMap = null;
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String id = pathFragments[2];
			if (id.equals("type")) {
				TypeDefinition typedef = SwaggerApiService.invokePutTypeDefMethod(repositoryId, pathFragments[3],
						req.getInputStream(), credentials[0], credentials[1]);
				obj = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
			} else {
				propMap = SwaggerApiService.invokePutMethod(repositoryId, typeId, id, input, credentials[0],
						credentials[1]);
			}
			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
			} else if (obj != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, obj);
			} else {
				// error
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_MODIFIED,
						pathFragments[1] + "not updated.");
			}
		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getError());
		}
	}

	protected void doDelete(HttpServletRequest req, HttpServletResponse response, String[] credentials,
			String[] pathFragments) throws Exception {
		try {
			LOG.info("method:{} repositoryId:{} type:{}", req.getMethod(), pathFragments[0], pathFragments[1]);
			String statusMessage = null;
			int code = 0;
			boolean status = false;
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String id = pathFragments[2];
			if (id != null) {
				if (id.equals("type")) {
					status = SwaggerApiService.invokeDeleteTypeDefMethod(repositoryId, pathFragments[3], credentials[0],
							credentials[1]);
				} else {
					status = SwaggerApiService.invokeDeleteMethod(repositoryId, typeId, id, credentials[0],
							credentials[1]);
				}
			}
			if (status) {
				code = HttpServletResponse.SC_OK;
				statusMessage = pathFragments[1] + " Deleted Successfully";
			}
			HttpUtils.invokeResponseWriter(response, code, statusMessage);

		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getError());
		}
	}

}
