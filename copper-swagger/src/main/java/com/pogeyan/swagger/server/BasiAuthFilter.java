package com.pogeyan.swagger.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Session;

import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.utils.HttpUtils;

@WebFilter("/docs/*")
public class BasiAuthFilter implements Filter {
	private String realm = "Swagger";

	public BasiAuthFilter() {
		// TODO Auto-generated constructor stub
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
		String paramRealm = fConfig.getInitParameter("realm");
		if (paramRealm != null) {
			realm = paramRealm;
		}
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
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

								if (session != null) {
									chain.doFilter(request, response);
								} else {
									unauthorized(response, "Authorization Required");
								}
							} catch (Exception e) {
								e.printStackTrace();

								try {
									SwaggerHelpers.removeSession(username);
								} catch (Exception ex) {
									ex.printStackTrace();
								}

								unauthorized(response, "Authorization Required");
							}
						} else {
							unauthorized(response, "Invalid authentication token");
						}
					} catch (UnsupportedEncodingException e) {
						throw new Error("Couldn't retrieve authentication", e);
					}
				}
			}
		} else {
			unauthorized(response, "Authorization Required");
		}
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
	}

}
