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
package com.pogeyan.swagger.api.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpUtils operations
 *
 */
public class HttpUtils {
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

	public static final String JSON_MIME_TYPE = "application/json";
	static ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param request
	 *            the property request is used to get all the request parameters
	 * @return it will give array of path fragment strings
	 */
	public static String[] splitPath(final HttpServletRequest request) {
		assert request != null;

		int prefixLength = request.getContextPath().length() + request.getServletPath().length();
		String p = request.getRequestURI().substring(prefixLength);

		if (p.length() == 0) {
			return new String[0];
		}

		String[] result = p.substring(1).split("/");
		for (int i = 0; i < result.length; i++) {
			result[i] = IOUtils.decodeURL(result[i]);

			// check for malicious characters
			for (int j = 0; j < result[i].length(); j++) {
				char c = result[i].charAt(j);
				if (c == '\n' || c == '\r' || c == '\b' || c == 0) {
					throw new CmisInvalidArgumentException("Invalid path!");
				}
			}
		}
		return result;
	}

	/**
	 * @param auth
	 *            the property auth is used to get the basic authentication
	 *            details.
	 * @return userName and password present in that request
	 * @throws Exception
	 *             if auth property is empty.it will throw The authorization
	 *             header is either empty or isn't Basic.
	 */
	public static String[] getCredentials(String auth) throws Exception {
		if (auth != null && auth.startsWith("Basic")) {
			// Extract credentials
			String usernamepassword = auth.substring("Basic ".length()).trim();
			String credentials = new String(Base64.getDecoder().decode(usernamepassword), Charset.forName("UTF-8"));
			String[] values = credentials.split(":", 2);
			return values;
		} else {
			throw new Exception("The authorization header is either empty or isn't Basic.");
		}
	}

	/**
	 * @param response
	 *            the property response is used to send the result to user.
	 * @param code
	 *            the property code is used to find out current request is
	 *            success or error.
	 * @param propMessage
	 *            the property propMessage is used to get the proper message in
	 *            that request
	 */
	public static void invokeResponseWriter(HttpServletResponse response, int code, Object propMessage) {
		try {
			LOG.info("Setting ResponseWriter");
			response.setContentType(JSON_MIME_TYPE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(code);
			if (propMessage != null) {
				response.getWriter().write(mapper.writeValueAsString(propMessage));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in invokeResponseWriter: {}", e.getMessage());
		}
	}

	/**
	 * @param stream
	 *            the property stream is used to get the content stream for the
	 *            specified document object.
	 * @param response
	 *            the property response is used to send the result to user
	 * @throws Exception
	 *             if stream is null.it will throw the exception
	 */
	public static void invokeDownloadWriter(ContentStream stream, HttpServletResponse response) throws Exception {

		LOG.info("Setting DownloadWriter for Document, fileName:{}", stream.getFileName());
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(stream.getMimeType());

		String contentFilename = stream.getFileName();
		if (contentFilename == null) {
			contentFilename = "content";
		}

		byte[] fileNameBytes = contentFilename.getBytes("utf-8");
		String dispositionFileName = "";
		for (byte b : fileNameBytes)
			dispositionFileName += (char) (b & 0xff);
		response.setHeader(MimeHelper.CONTENT_DISPOSITION,
				MimeHelper.encodeContentDisposition(MimeHelper.DISPOSITION_ATTACHMENT, dispositionFileName));
		InputStream in = stream.getStream();
		try {
			OutputStream out = response.getOutputStream();
			IOUtils.copy(in, out, 64 * 1024);
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			e.printStackTrace();
			LOG.error("Exception: {}", e.getMessage());
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(stream);
		}

	}
}
