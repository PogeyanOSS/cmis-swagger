package com.pogeyan.swagger.impl.factory;

import com.pogeyan.swagger.api.factory.IObjectFacade;

public class SwaggerObjectServiceFactory {
	static IObjectFacade apiServiceClass;

	public static void add(IObjectFacade apiService) {
		apiServiceClass = apiService;
	}

	public static IObjectFacade getApiService() {
		return apiServiceClass;
	}
}
