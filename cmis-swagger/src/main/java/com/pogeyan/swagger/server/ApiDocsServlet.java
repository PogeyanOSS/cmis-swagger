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
			@SuppressWarnings("unused")
			boolean skip = false;
			Map<String, Object> input = null;
			Part filePart = null;
			String method = request.getMethod();
			String auth = request.getHeader("Authorization");
			String credentials[] = HttpUtils.getCredentials(auth);
			String pathFragments[] = HttpUtils.splitPath(request);

			if (METHOD_POST.equals(method)) {
				if (request.getContentType().contains("multipart/form-data")) {
					input = request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
					filePart = request.getPart("file") != null ? request.getPart("file") : null;
				} else if (request.getContentType().equals("application/x-www-form-urlencoded")) {
					input = request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
				} else if (pathFragments[1].equals("_metadata")) {
					skip = true;
				} else {
					String jsonString = IOUtils.toString(request.getInputStream());
					input = mapper.readValue(jsonString, new TypeReference<Map<String, String>>() {
					});
				}
			}
			if (METHOD_PUT.equals(method) && request.getInputStream() != null) {
				if (pathFragments[1].equals("_metadata")) {
					skip = true;
				} else {
					String jsonString = IOUtils.toString(request.getInputStream());
					input = mapper.readValue(jsonString, new TypeReference<Map<String, String>>() {
					});
				}
			}
			if (METHOD_POST.equals(method)) {
				doPost(request, response, credentials, pathFragments, input, filePart);
			} else if (METHOD_GET.equals(method)) {
				doGet(request, response, credentials, pathFragments);
			} else if (METHOD_PUT.equals(method)) {
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
			LOG.info("method:{} repositoryId:{} type:{}", request.getMethod(), pathFragments[0], pathFragments[1]);
			Map<String, Object> propMap = null;
			JSONObject obj = null;
			ContentStream stream = null;
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String id = pathFragments[2];

			if (id != null && !id.equals("media")) {
				if (id.equals("type")) {
					TypeDefinition typedef = SwaggerApiService.invokeGetTypeDefMethod(repositoryId, pathFragments[3],
							credentials[0], credentials[1]);
					obj = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
				} else if (id.equals("getAll")) {
					String skipCount = request.getParameter("skipcount");
					String maxItems = request.getParameter("maxitems");
					obj = SwaggerApiService.invokeGetAllMethod(repositoryId, typeId, skipCount, maxItems,
							credentials[0], credentials[1]);
				} else {
					propMap = SwaggerApiService.invokeGetMethod(repositoryId, typeId, id, credentials[0],
							credentials[1]);
				}
			}
			if (pathFragments.length > 3 && pathFragments[3] != null && pathFragments[2].equals("media")) {
				stream = SwaggerApiService.invokeDownloadMethod(repositoryId, typeId, pathFragments[3], credentials[0],
						credentials[1], response);
			}
			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
			} else if (obj != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, obj);
			} else if (stream != null) {
				HttpUtils.invokeDownloadWriter(stream, response);
			} else {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_FOUND,
						pathFragments[1] + " not found");
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
			LOG.info("method:{} repositoryId:{} type:{}", request.getMethod(), pathFragments[0], pathFragments[1]);
			JSONObject obj = null;
			Acl acl = null;
			Map<String, Object> propMap = null;
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1];
			String parentId = request.getParameter("parentId");
			if (pathFragments.length > 2 && pathFragments[2].equals("type")) {
				TypeDefinition typedef = SwaggerApiService.invokePostTypeDefMethod(repositoryId, credentials[0],
						credentials[1], request.getInputStream());
				obj = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
			} else if (typeId.equals("Acl")) {
				acl = SwaggerApiService.invokePostAcl(repositoryId, pathFragments[2], input, credentials[0],
						credentials[1]);
			} else {
				propMap = SwaggerApiService.invokePostMethod(repositoryId, typeId, parentId, input, credentials[0],
						credentials[1], pathFragments, filePart);
			}
			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CREATED, propMap);
			} else if (obj != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, obj);
			} else if (acl != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, acl);
			} else {
				// error
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_CONFLICT,
						pathFragments[1] + " not created.");
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
