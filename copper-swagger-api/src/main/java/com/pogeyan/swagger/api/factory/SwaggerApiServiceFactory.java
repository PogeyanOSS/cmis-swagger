package com.pogeyan.swagger.api.factory;

public class SwaggerApiServiceFactory {
	static IObjectFacade apiServiceClass;

	public static void add(IObjectFacade apiService) {
		apiServiceClass = apiService;
	}

	public static IObjectFacade getApiService() {
		return apiServiceClass;
	}
}
