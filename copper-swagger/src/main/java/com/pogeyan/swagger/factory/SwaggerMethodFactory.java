package com.pogeyan.swagger.factory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.pogeyan.swagger.api.impl.SwaggerDeleteDAOImpl;
import com.pogeyan.swagger.api.impl.SwaggerGetDAOImpl;
import com.pogeyan.swagger.api.impl.SwaggerPostDAOImpl;
import com.pogeyan.swagger.api.impl.SwaggerPutDAOImpl;
import com.pogeyan.swagger.apis.SwaggerDeleteDAO;
import com.pogeyan.swagger.apis.SwaggerGetDAO;
import com.pogeyan.swagger.apis.SwaggerPostDAO;
import com.pogeyan.swagger.apis.SwaggerPutDAO;

public class SwaggerMethodFactory {
	ServletRequest SRequestMeseage;
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	private static String SGETDAOIMPL = "SwaggerGetDAOImpl";
	private static String SPOSTDAOIMPL = "SwaggerPostDAOImpl";
	private static String SPUTDAOIMPL = "SwaggerPutDAOImpl";
	private static String SDELETEDAOIMPL = "SwaggerDeleteDAOImpl";

	private Map<Class<?>, String> swaggerServiceClass = new HashMap<>();

	public SwaggerMethodFactory() {
		super();
		swaggerServiceClass.put(SwaggerGetDAO.class, SwaggerMethodFactory.SGETDAOIMPL);
		swaggerServiceClass.put(SwaggerPostDAO.class, SwaggerMethodFactory.SPOSTDAOIMPL);
		swaggerServiceClass.put(SwaggerPutDAO.class, SwaggerMethodFactory.SPUTDAOIMPL);
		swaggerServiceClass.put(SwaggerDeleteDAO.class, SwaggerMethodFactory.SDELETEDAOIMPL);
	}

	@SuppressWarnings("unchecked")
	public <T> T getMethodService(Class<?> swaggerServiceClass) throws Exception {
		String className = this.swaggerServiceClass.get(swaggerServiceClass);
		if (className.equals(SwaggerMethodFactory.SGETDAOIMPL)) {
			return (T) new SwaggerGetDAOImpl();
		} else if (className.equals(SwaggerMethodFactory.SPOSTDAOIMPL)) {
			return (T) new SwaggerPostDAOImpl();
		} else if (className.equals(SwaggerMethodFactory.SPUTDAOIMPL)) {
			return (T) new SwaggerPutDAOImpl();
		} else if (className.equals(SwaggerMethodFactory.SDELETEDAOIMPL)) {
			return (T) new SwaggerDeleteDAOImpl();
		}

		return null;
	}

}
