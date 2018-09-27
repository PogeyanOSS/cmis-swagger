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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.HttpUtils;
import com.pogeyan.swagger.api.utils.SwaggerUIHelpers;
import com.pogeyan.swagger.services.SwaggerGenerator;

/**
 * Servlet implementation class SwaggerDocsServlet
 */
@WebServlet(urlPatterns = "/docs/*")
public class SwaggerDocsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerDocsServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SwaggerDocsServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathFragments[] = HttpUtils.splitPath(request);
		String repositoryId = pathFragments[0];
		LOG.info("class name: {}, method name: {}, repositoryId: {}", "SwaggerDocsServlet", "doGet", repositoryId);
		if (repositoryId != null) {
			SwaggerUIHelpers.setHostSwaggerUrl(request.getServerName() + ":" + request.getServerPort() + "/api/");
			SwaggerGenerator swaggerGenerator = new SwaggerGenerator();
			String content = swaggerGenerator.generateSwagger(repositoryId);
			response.getWriter().write(content);
		} else {
			throw new CmisObjectNotFoundException("Repo Id is null or invalid");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
