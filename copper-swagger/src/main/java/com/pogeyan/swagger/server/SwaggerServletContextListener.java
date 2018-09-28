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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.factory.IObjectFacade;
import com.pogeyan.swagger.api.factory.SwaggerApiServiceFactory;
import com.pogeyan.swagger.api.utils.SwaggerUIHelpers;

@WebListener
public class SwaggerServletContextListener implements ServletContextListener {
	private static final String CONFIG_FILENAME = "swaggerrepo.properties";
	private static final String DEFAULT_SERVER_PROPERTY_CLASS_PATH = "src/main/resources/";
	private static final String PROPERTY_SERVER_DESCRIPTION = "serverDescription";
	private static final String PROPERTY_SERVER_VERSION = "serverVersion";
	private static final String PROPERTY_SERVER_TITLE = "serverTitle";
	private static final String PROPERTY_SERVER_NAME = "serverName";
	private static final String PROPERTY_SERVER_URL = "serverUrl";
	private static final String PROPERTY_SERVER_EMAIL = "serverContactEmail";
	private static final String PROPERTY_SERVER_EXTERNAL_DOCUMENT_DESCRIPTION = "serverExternalDocumentDescription";
	private static final String PROPERTY_SERVER_EXTERNAL_DOCUMENT_URL = "serverExternalDocumentUrl";
	private static final String DEFAULT_SERVER_DESCRIPTION = "";
	private static final String DEFAULT_SERVER_TITLE = "";
	private static final String DEFAULT_SERVER_VERSION = "";
	private static final String DEFAULT_SERVER_NAME = "";
	private static final String DEFAULT_SERVER_URL = "";
	private static final String DEFAULT_SERVER_EMAIL = "";
	private static final String DEFAULT_SERVER_TERMS_OF_SERVICE = "http://swagger.io/terms/";
	private static final String DEFAULT_SERVER_EXTERNAL_DOCUMENT_DESCRIPTION = "";
	private static final String DEFAULT_SERVER_EXTERNAL_DOCUMENT_URL = "";
	private static final String DEFAULT_SWAGGER_API_CLASS = "com.pogeyan.swagger.factory.SwaggerFactory";
	private static final String PROPERTY_SWAGGER_API_CLASS = "swaggerSyncApiClass";

	static final Logger LOG = LoggerFactory.getLogger(SwaggerServletContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		try {
			boolean factory = createServiceFactory(sce);
			if (!factory) {
				throw new IllegalArgumentException("Swagger server property manager class not initilaized");
			}
		} catch (Exception e) {
			LOG.error("Service factory couldn't be created: {}", e.toString(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

	private boolean createServiceFactory(ServletContextEvent sce) throws FileNotFoundException {
		// load properties
		InputStream stream = null;
		try {
			String filePath = null;
			String propertyFileLocation = System.getenv("SWAGGER_SERVER_PROPERTY_FILE_LOCATION");
			if (propertyFileLocation == null) {
				propertyFileLocation = DEFAULT_SERVER_PROPERTY_CLASS_PATH;
				filePath = SwaggerServletContextListener.class.getClassLoader().getResource(CONFIG_FILENAME).getPath();
			} else {
				filePath = propertyFileLocation;
			}
			stream = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			return initializeExtensions(DEFAULT_SERVER_TITLE, DEFAULT_SERVER_VERSION, DEFAULT_SERVER_NAME,
					DEFAULT_SERVER_URL, DEFAULT_SERVER_DESCRIPTION, DEFAULT_SERVER_EMAIL,
					DEFAULT_SERVER_TERMS_OF_SERVICE, DEFAULT_SERVER_EXTERNAL_DOCUMENT_DESCRIPTION,
					DEFAULT_SERVER_EXTERNAL_DOCUMENT_URL, DEFAULT_SWAGGER_API_CLASS);
		}

		Properties props = new Properties();
		try {
			props.load(stream);
		} catch (IOException e) {
			LOG.error("Cannot load configuration: {}", e.toString(), e);
			return false;
		} finally {
			IOUtils.closeQuietly(stream);
		}

		Map<String, String> parameters = new HashMap<String, String>();

		for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			parameters.put(key, value);
		}

		// get 'serverPropertyClassDetails' property
		String swaggerSeverTitle = props.getProperty(PROPERTY_SERVER_TITLE);
		if (swaggerSeverTitle == null) {
			swaggerSeverTitle = DEFAULT_SERVER_TITLE;
		}
		String swaggerSeverVersion = props.getProperty(PROPERTY_SERVER_VERSION);
		if (swaggerSeverVersion == null) {
			swaggerSeverVersion = DEFAULT_SERVER_VERSION;
		}
		String swaggerSeverDescription = props.getProperty(PROPERTY_SERVER_DESCRIPTION);
		if (swaggerSeverDescription == null) {
			swaggerSeverDescription = DEFAULT_SERVER_DESCRIPTION;
		}
		String swaggerSeverName = props.getProperty(PROPERTY_SERVER_NAME);
		if (swaggerSeverName == null) {
			swaggerSeverName = DEFAULT_SERVER_NAME;
		}
		String swaggerSeverURL = props.getProperty(PROPERTY_SERVER_URL);
		if (swaggerSeverURL == null) {
			swaggerSeverURL = DEFAULT_SERVER_URL;
		}
		String swaggerSeverEmail = props.getProperty(PROPERTY_SERVER_EMAIL);
		if (swaggerSeverEmail == null) {
			swaggerSeverEmail = DEFAULT_SERVER_EMAIL;
		}
		String serverExternalDocumentDescription = props.getProperty(PROPERTY_SERVER_EXTERNAL_DOCUMENT_DESCRIPTION);
		if (serverExternalDocumentDescription == null) {
			serverExternalDocumentDescription = DEFAULT_SERVER_EXTERNAL_DOCUMENT_DESCRIPTION;
		}
		String serverExternalDocumentUrl = props.getProperty(PROPERTY_SERVER_EXTERNAL_DOCUMENT_URL);
		if (serverExternalDocumentUrl == null) {
			serverExternalDocumentUrl = DEFAULT_SERVER_EXTERNAL_DOCUMENT_URL;
		}
		String swaggerApiClass = props.getProperty(PROPERTY_SWAGGER_API_CLASS);
		if (swaggerApiClass == null) {
			swaggerApiClass = DEFAULT_SWAGGER_API_CLASS;
		}
		return initializeExtensions(swaggerSeverTitle, swaggerSeverVersion, swaggerSeverName, swaggerSeverURL,
				swaggerSeverDescription, swaggerSeverEmail, DEFAULT_SERVER_TERMS_OF_SERVICE,
				serverExternalDocumentDescription, serverExternalDocumentUrl, swaggerApiClass);

	}

	private boolean initializeExtensions(String serverTitle, String serverVersion, String serverName, String serverUrl,
			String serverDescription, String serverEmail, String serverTermsOfServcie,
			String serverExternalDocumentDescription, String serverExternalDocumentUrl, String swaggerApiClass) {

		if (initializePropertyDetails(serverTitle, serverVersion, serverName, serverUrl, serverDescription, serverEmail,
				DEFAULT_SERVER_TERMS_OF_SERVICE, serverExternalDocumentDescription, serverExternalDocumentUrl)) {
			if (initializeSwaggerApiService(swaggerApiClass)) {
				return true;
			}
		}

		return false;
	}

	private boolean initializePropertyDetails(String serverTitle, String serverVersion, String serverName,
			String serverUrl, String serverDescription, String serverEmail, String serverTermsOfServcie,
			String serverExternalDocumentDescription, String serverExternalDocumentUrl) {
		Map<String, String> contact = new HashMap<String, String>();
		Map<String, String> license = new HashMap<String, String>();

		contact.put("email", serverEmail);
		license.put("name", serverName);
		license.put("url", serverUrl);
		SwaggerUIHelpers.setInfoObject(serverDescription, serverVersion, serverTitle, serverTermsOfServcie, contact,
				license);
		SwaggerUIHelpers.setExternalDocsObject(serverExternalDocumentDescription, serverExternalDocumentUrl);
		return true;

	}

	private boolean initializeSwaggerApiService(String swaggerApiClass) {

		try {
			LOG.info("class name: {}, method name: {}", "SwaggerServletContextListener", "initializeSwaggerApiService");
			Class<?> swaggerApiServiceClass = Class.forName(swaggerApiClass);
			IObjectFacade apiService = (IObjectFacade) swaggerApiServiceClass.newInstance();
			SwaggerApiServiceFactory.add(apiService);
		} catch (Exception e) {
			LOG.error("Could not create a Swagger Api services factory instance: {}", e);
			return false;
		}
		return true;
	}

}