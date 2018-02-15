package com.pogeyan.swagger.factory;

import com.pogeyan.swagger.impl.factory.IObjectFacade;

public class SwaggerApiServiceFactory {

	static IObjectFacade apiServiceClass;

	public static void add(IObjectFacade apiService) {
		apiServiceClass = apiService;
	}

	public static IObjectFacade getApiService() {
		return apiServiceClass;
	}
}
