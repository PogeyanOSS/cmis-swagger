package com.pogeyan.swagger.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.pojos.ErrorResponse;
import com.pogeyan.swagger.services.SwaggerApiService;
import com.pogeyan.swagger.utils.HttpUtils;

@WebServlet("/query/*")
public class QuerDocsServlet extends HttpServlet {
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
	public QuerDocsServlet() {
		super();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) {
		try {
			String method = request.getMethod();
			String auth = request.getHeader("Authorization");
			if (auth != null) {
				createSession(request, response, auth);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invaild User Details");
			}
			String credentials[] = HttpUtils.getCredentials(auth);
			String pathFragments[] = HttpUtils.splitPath(request);
			if (METHOD_GET.equals(method)) {
				doGet(request, response, credentials, pathFragments);
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
			String select = null;
			String filter = null;
			String order = null;
			String skipQuery = null;
			String maxQuery = null;
			String repositoryId = pathFragments[0];
			String typeId = pathFragments[1].replace("_", ":");
			String id = pathFragments[2];
			JSONObject obj = null;
			String method = null;
			if (pathFragments.length > 3) {
				method = pathFragments[3];
			}
			String queryString = request.getQueryString();
			if (queryString != null) {
				select = request.getParameter("select");
				filter = request.getParameter("filter");
				order = request.getParameter("orderby");
				skipQuery = request.getParameter("skip");
				maxQuery = request.getParameter("max");
			}
			if (method != null && method.equals("getAll")) {
				if (select != null && filter != null) {
					select = select + "," + URLDecoder.decode(filter, "UTF-8");
				} else if (select == null && filter != null) {
					select = "*," + filter;
				}
				obj = SwaggerApiService.invokeGetAllMethod(repositoryId, typeId, id,
						skipQuery != null && !skipQuery.isEmpty() ? skipQuery.split("=")[1] : null,
						maxQuery != null && !maxQuery.isEmpty() ? maxQuery.split("=")[1] : null, credentials[0],
						credentials[1], select, URLDecoder.decode(order, "UTF-8"));
			} else {
				propMap = SwaggerApiService.invokeGetMethod(repositoryId, typeId, id, credentials[0], credentials[1],
						select);
			}

			if (propMap != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, propMap);
			} else if (obj != null) {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_OK, obj);
			} else {
				HttpUtils.invokeResponseWriter(response, HttpServletResponse.SC_NOT_FOUND,
						pathFragments[1] + " not found");
			}
		} catch (ErrorResponse e) {
			HttpUtils.invokeResponseWriter(response, e.getErrorCode(), e.getMessage());
		}
	}

	private void createSession(HttpServletRequest request, HttpServletResponse response, String authHeader)
			throws IOException {

		StringTokenizer st = new StringTokenizer(authHeader);
		if (st.hasMoreTokens()) {
			String basic = st.nextToken();
			if (basic.equalsIgnoreCase("Basic")) {
				try {
					String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
					int p = credentials.indexOf(":");
					if (p != -1) {
						String username = credentials.substring(0, p).trim();
						String password = credentials.substring(p + 1).trim();
						String pathFragments[] = HttpUtils.splitPath(request);
						String repoId = pathFragments[0];
						Session session;
						try {
							session = SwaggerHelpers.createSession(repoId, username, password);
							SwaggerHelpers.getAllTypes(session);

							if (session == null) {
								response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invaild User Details");
							}

						} catch (Exception e) {
							e.printStackTrace();

							try {
								SwaggerHelpers.removeSession(username);
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invaild User Details");
						}
					} else {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invaild User Details");
					}
				} catch (UnsupportedEncodingException e) {
					throw new Error("Couldn't retrieve authentication", e);
				}
			}
		}

	}
}
