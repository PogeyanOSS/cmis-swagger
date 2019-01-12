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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisServiceUnavailableException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisTooManyRequestsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.pogeyan.swagger.api.AuthMessage;
import com.pogeyan.swagger.api.IRequest;
import com.pogeyan.swagger.api.RequestMessage;
import com.pogeyan.swagger.pojos.ErrorResponse;

/**
 * SwaggerHelpers operations.
 * 
 * @param <Irequest>
 */
public class SwaggerHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerHelpers.class);
	public static Map<String, ObjectType> typeMap = new HashMap<String, ObjectType>();
	private static Cache<String, Session> sessionMap;
	public static final int InvalidArgumentExceptionCode = 400;
	public static final int ConstraintExceptionCode = 409;
	public static final int CmisObjectNotFoundExceptionCode = 404;
	public static final int CmisUpdateConflictExceptionCode = 409;
	public static final int CmisContentAlreadyExistsExceptionCode = 409;
	public static final int CmisNotSupportedExceptionCode = 405;
	public static final int IllegalArgumentExceptionCode = 400;
	public static final int CmisFilterNotValidExceptionCode = 400;
	public static final int CmisNameConstraintViolationExceptionCode = 409;
	public static final int CmisRuntimeExceptionCode = 500;
	public static final int CmisPermissionDeniedExceptionCode = 403;
	public static final int CmisStorageExceptionCode = 500;
	public static final int CmisStreamNotSupportedExceptionCode = 403;
	public static final int CmisVersioningExceptionCode = 409;
	public static final int CmisTooManyRequestsExceptionCode = 429;
	public static final int CmisServiceUnavailableExceptionCode = 503;
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	public static final String CMIS_EXT_RELATIONMD = "cmis_ext:relationmd";
	public static final String CMIS_EXT_RELATIONSHIP = "cmis_ext:relationship";
	public static final String CMIS_EXT_CONFIG = "cmis_ext:config";
	public static final String MEDIA = "media";
	public static final String GETALL = "getAll";
	public static final String TYPE = "type";
	public static final String GETALLTYPES = "types";
	public static final String ACL = "acl";
	public static final String ADD_ACL = "addAcl";
	public static final String REMOVE_ACL = "removeAcl";

	public static ObjectMapper mapper = new ObjectMapper();

	static {
		sessionMap = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	}

	/**
	 * @param repoId
	 *            the property repoId,it will specify which repository to be
	 *            connected for that user.
	 * @param userName
	 *            the property userName is used to login that particular user
	 *            repository.
	 * @param password
	 *            the property password is used to login that particular user
	 *            repository.
	 * @return session is a connection to a CMIS repository with a specific
	 *         user.
	 * @throws Exception
	 *             Swagger CMIS session connection URL not defined in
	 *             environment variable
	 * @throws Exception
	 *             if session not found
	 */
	public static Session getSession(String repoId, String userName, String password) throws Exception {

		return sessionMap.getIfPresent(userName) != null ? sessionMap.getIfPresent(userName)
				: createSession(repoId, userName, password);

	}

	/**
	 * 
	 * @param userName
	 *            the property userName is used to login that particular user
	 *            repository
	 */
	public static void removeSession(String userName) throws Exception {
		sessionMap.invalidate(userName);
	}

	/**
	 * @param repoId
	 *            the property repoId,it will specify which repository to be
	 *            connected for that user.
	 * @param userName
	 *            the property userName is used to login that particular user
	 *            repository.
	 * @param password
	 *            the property password is used to login that particular user
	 *            repository.
	 * @return session is a connection to a CMIS repository with a specific
	 *         user.
	 * @throws Exception
	 *             Swagger CMIS session connection URL not defined in
	 *             environment variable
	 * @throws Exception
	 *             if session not found
	 */
	public static Session createSession(String repoId, String userName, String password) throws Exception {
		Session session = null;
		try {
			String connectionUrl = System.getenv("SWAGGER_CMIS_SESSION_CONNECTION_URL");
			if (connectionUrl == null) {
				throw new CmisInvalidArgumentException(
						"Swagger cmis session connection url not defined in environment variable");
			}
			String connectionString = null;
			if (customTypeHasFolder()) {
				connectionString = connectionUrl + repoId;
			} else {
				connectionString = connectionUrl;
			}

			LOG.info("class name: {}, method name: {}, repositoryId: {}", "SwaggerHelpers", "createSession", repoId);
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameters = new HashMap<String, String>();
			// user credentials
			parameters.put(SessionParameter.USER, userName);
			parameters.put(SessionParameter.PASSWORD, password);
			parameters.put(SessionParameter.BROWSER_URL, connectionString);
			parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
			parameters.put(SessionParameter.REPOSITORY_ID, repoId);
			session = factory.createSession(parameters);
			session.setDefaultContext(session.createOperationContext());
			sessionMap.put(userName, session);
		} catch (Exception e) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, Exception on createSession: {}",
					"SwaggerHelpers", "createSession", repoId, e);
			ErrorResponse response = SwaggerHelpers.handleException(e);
			throw new ErrorResponse(response);
		}
		return session;

	}

	/**
	 * @param session
	 *            the property session is used to get all types present that
	 *            particular repository
	 */
	public static void getAllTypes(Session session) {
		List<String> list = getBaseTypeList();
		for (String type : list) {
			ObjectType baseType = session.getTypeDefinition(type);
			getTypeMap().put(baseType.getId().toString(), baseType);
			List<Tree<ObjectType>> allTypes = session.getTypeDescendants(type, -1, true);
			for (Tree<ObjectType> object : allTypes) {
				getChildTypes(object);
			}
		}
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, Types in repository: {}", "SwaggerHelpers",
				"getAllTypes", session.getRepositoryInfo().getId(), typeMap);
	}

	/**
	 * @param typeChildren
	 *            the property typeChildren it will give children's of one
	 *            particular type.
	 */
	public static void getChildTypes(Tree<ObjectType> typeChildren) {
		List<Tree<ObjectType>> getChildren = typeChildren.getChildren();
		if (getChildren.size() > 0) {
			// this will add the higher level item
			getTypeMap().put(typeChildren.getItem().getId().toString(), typeChildren.getItem());
			for (Tree<ObjectType> object : getChildren) {
				getChildTypes(object);
			}
		} else {
			// this will add all children
			getTypeMap().put(typeChildren.getItem().getId().toString(), typeChildren.getItem());
		}
	}

	/**
	 * @param type
	 *            the property type is used to get the ObjectType.
	 * @return ObjectId used to get the particular object in data
	 */
	public static String getIdName(ObjectType type) {
		String objectIdName = null;
		if (type.isBaseType()) {
			objectIdName = "Id";
			return objectIdName;
		} else {
			Set<Entry<String, PropertyDefinition<?>>> data = type.getPropertyDefinitions().entrySet();
			for (Entry<String, PropertyDefinition<?>> propertiesValues : data) {
				if (propertiesValues.getValue() != null && propertiesValues.getValue().getLocalName() != null
						? propertiesValues.getValue().getLocalName().equals("primaryKey") : false) {
					objectIdName = propertiesValues.getKey();
					return objectIdName;
				}
			}
		}
		return type.getBaseType().getPropertyDefinitions().get("cmis:objectId").getId();

	}

	/**
	 * @return it will give all base type id's
	 */
	public static List<String> getBaseTypeList() {
		List<String> list = new ArrayList<String>();
		list.add(BaseTypeId.CMIS_FOLDER.value());
		list.add(BaseTypeId.CMIS_DOCUMENT.value());
		list.add(BaseTypeId.CMIS_ITEM.value());
		list.add(BaseTypeId.CMIS_RELATIONSHIP.value());
		list.add(BaseTypeId.CMIS_POLICY.value());
		list.add(SwaggerHelpers.CMIS_EXT_RELATIONMD);
		list.add(SwaggerHelpers.CMIS_EXT_RELATIONSHIP);
		list.add(SwaggerHelpers.CMIS_EXT_CONFIG);
		return list;
	}

	//
	/**
	 * @param ex
	 *            the property ex is used to catch various exception in server.
	 * @return ErrorResponse give the correct exception with error code
	 */
	public static ErrorResponse handleException(Exception ex) {
		String errorMessage;
		int code;
		LOG.error("class name: {}, method name: {}, repositoryId: {}, CmisBaseResponse error: {}", "SwaggerHelpers",
				"handleException", ex.getMessage());

		if (ex instanceof CmisInvalidArgumentException) {
			errorMessage = ((CmisInvalidArgumentException) ex).getErrorContent();
			code = InvalidArgumentExceptionCode;
		} else if (ex instanceof CmisObjectNotFoundException) {
			errorMessage = ((CmisObjectNotFoundException) ex).getErrorContent();
			code = CmisObjectNotFoundExceptionCode;
		} else if (ex instanceof CmisConstraintException) {
			errorMessage = ((CmisConstraintException) ex).getErrorContent();
			code = ConstraintExceptionCode;
		} else if (ex instanceof CmisUpdateConflictException) {
			errorMessage = ((CmisUpdateConflictException) ex).getErrorContent();
			code = CmisUpdateConflictExceptionCode;
		} else if (ex instanceof CmisNotSupportedException) {
			errorMessage = ((CmisNotSupportedException) ex).getErrorContent();
			code = CmisNotSupportedExceptionCode;
		} else if (ex instanceof IllegalArgumentException) {
			errorMessage = ex.getMessage();
			code = IllegalArgumentExceptionCode;
		} else if (ex instanceof CmisRuntimeException) {
			errorMessage = ((CmisRuntimeException) ex).getErrorContent();
			code = CmisRuntimeExceptionCode;
		} else if (ex instanceof CmisStorageException) {
			errorMessage = ((CmisStorageException) ex).getErrorContent();
			code = CmisStorageExceptionCode;
		} else if (ex instanceof CmisContentAlreadyExistsException) {
			errorMessage = ((CmisContentAlreadyExistsException) ex).getErrorContent();
			code = CmisContentAlreadyExistsExceptionCode;
		} else if (ex instanceof CmisFilterNotValidException) {
			errorMessage = ((CmisFilterNotValidException) ex).getErrorContent();
			code = CmisFilterNotValidExceptionCode;
		} else if (ex instanceof CmisNameConstraintViolationException) {
			errorMessage = ((CmisNameConstraintViolationException) ex).getErrorContent();
			code = CmisNameConstraintViolationExceptionCode;
		} else if (ex instanceof CmisPermissionDeniedException) {
			errorMessage = ((CmisPermissionDeniedException) ex).getErrorContent();
			code = CmisPermissionDeniedExceptionCode;
		} else if (ex instanceof CmisStreamNotSupportedException) {
			errorMessage = ((CmisStreamNotSupportedException) ex).getErrorContent();
			code = CmisStreamNotSupportedExceptionCode;
		} else if (ex instanceof CmisVersioningException) {
			errorMessage = ((CmisVersioningException) ex).getErrorContent();
			code = CmisVersioningExceptionCode;
		} else if (ex instanceof CmisTooManyRequestsException) {
			errorMessage = ((CmisTooManyRequestsException) ex).getErrorContent();
			code = CmisTooManyRequestsExceptionCode;
		} else if (ex instanceof CmisServiceUnavailableException) {
			errorMessage = ((CmisServiceUnavailableException) ex).getErrorContent();
			code = CmisServiceUnavailableExceptionCode;
		} else if (ex instanceof UncheckedExecutionException) {
			errorMessage = ex.getMessage();
			code = CmisServiceUnavailableExceptionCode;
		} else {
			errorMessage = ex.getMessage();
			code = CmisServiceUnavailableExceptionCode;
		}
		return new ErrorResponse(errorMessage, code);
	}

	/**
	 * @return true means type have custom folder,false means type does not have
	 *         any custom folder
	 */
	public static boolean customTypeHasFolder() {
		final String value = System.getenv("CUSTOM_TYPE_HAS_FOLDER");
		if (value != null) {
			return value.equalsIgnoreCase("1");
		}
		return false;
	}

	/**
	 * 
	 * @return return the Cache Map which contains all the types
	 */
	public static Map<String, ObjectType> getTypeMap() {
		return typeMap;
	}

	/**
	 * 
	 * @param cmisObject
	 *            compileProperties for the cmisObject
	 * @param session
	 *            the property session is used to get all details about that
	 *            repository.
	 * @throws Exception
	 *             error in compileProperties
	 */
	public static Map<String, Object> compileProperties(CmisObject cmisObject, Session session) throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		cmisObject.getProperties().stream().forEach(a -> {
			propMap.put(a.getDefinition().getId(), a.getValues());
		});
		Map<String, Object> outputMap = deserializeInputForResponse(propMap, cmisObject.getType(), session);

		return outputMap;
	}

	private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
		try {
			return clazz.cast(o);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> deserializeInputForResponse(Map<String, Object> input, ObjectType object,
			Session session) throws Exception {

		Map<String, Object> serializeMap = new HashMap<String, Object>();
		Map<String, PropertyDefinition<?>> dataPropDef = object.getPropertyDefinitions();
		for (String var : input.keySet()) {
			List<?> valueOfType = (List<?>) input.get(var);

			if (var.equals("parentId")) {
				continue;
			}
			if (valueOfType != null) {
				PropertyType reqPropType = null;
				PropertyDefinition<?> definitionObject = dataPropDef.get(var);
				if (definitionObject == null) {

					List<?> secondaryValues = (List<?>) input.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
					for (Object stringtype : secondaryValues) {
						TypeDefinition type = session.getTypeDefinition((String) stringtype);
						for (Entry<String, PropertyDefinition<?>> t : type.getPropertyDefinitions().entrySet()) {
							if (t.getValue().getId().equals(var)) {
								reqPropType = t.getValue().getPropertyType();
							}
						}
					}

				} else {
					reqPropType = definitionObject.getPropertyType();
				}

				if (reqPropType.equals(PropertyType.INTEGER)) {
					if (valueOfType.size() == 1) {
						BigInteger valueBigInteger = convertInstanceOfObject(valueOfType.get(0), BigInteger.class);
						serializeMap.put(var, valueBigInteger);
					} else {
						List<BigInteger> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}

				} else if (reqPropType.equals(PropertyType.BOOLEAN)) {
					if (valueOfType.size() == 1) {
						Boolean booleanValue = convertInstanceOfObject(valueOfType.get(0), Boolean.class);
						serializeMap.put(var, booleanValue);
					} else {
						List<Boolean> booleanValue = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, booleanValue);
					}

				} else if (reqPropType.equals(PropertyType.DATETIME)) {

					// SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd
					// HH:mm:ss Z yyyy", Locale.US);

					if (valueOfType.size() == 1) {
						GregorianCalendar lastModifiedCalender = (GregorianCalendar) valueOfType.get(0);
						serializeMap.put(var, lastModifiedCalender.getTimeInMillis());
					} else {
						List<GregorianCalendar> value = convertInstanceOfObject(valueOfType, List.class);
						List<Long> calenderList = new ArrayList<>();
						value.forEach(v -> {
							calenderList.add(v.getTimeInMillis());
						});
						serializeMap.put(var, calenderList);
					}

				} else if (reqPropType.equals(PropertyType.DECIMAL)) {
					if (valueOfType.size() == 1) {
						Double value = convertInstanceOfObject(valueOfType.get(0), Double.class);
						serializeMap.put(var, value);
					} else {
						List<BigDecimal> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}
				} else {
					// string type
					if (valueOfType.size() == 1) {
						String value = convertInstanceOfObject(valueOfType.get(0), String.class);
						serializeMap.put(var, value);
					} else {
						List<String> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}
				}
			} else {
				continue;
			}
		}
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}, serializedMap:{}", "SwaggerHelpers",
				"deserializeInputForResponse", session.getRepositoryInfo().getId(), object.getBaseTypeId().value(),
				serializeMap);
		return serializeMap;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> deserializeInput(Map<String, Object> input, ObjectType object, Session session)
			throws Exception {

		Map<String, Object> serializeMap = new HashMap<String, Object>();
		Map<String, PropertyDefinition<?>> dataPropertyDefinition = object.getPropertyDefinitions();
		for (String var : input.keySet()) {
			Object valueOfType = input.get(var);
			if (var.equals("parentId")) {
				continue;
			}
			if (valueOfType != null) {
				PropertyType reqPropertyType = null;
				PropertyDefinition<?> definitionObject = dataPropertyDefinition.get(var);
				if (definitionObject == null) {
					List<?> secondaryValues = (List<?>) input.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
					for (Object stype : secondaryValues) {
						TypeDefinition type = session.getTypeDefinition((String) stype);
						for (Entry<String, PropertyDefinition<?>> t : type.getPropertyDefinitions().entrySet()) {
							if (t.getValue().getId().equals(var)) {
								reqPropertyType = t.getValue().getPropertyType();
							}
						}
					}

				} else {
					reqPropertyType = definitionObject.getPropertyType();
				}
				// checking cardinality
				if (definitionObject.getCardinality().value().equals(Cardinality.MULTI.value())) {
					if (valueOfType instanceof String || valueOfType instanceof Integer
							|| valueOfType instanceof Boolean || valueOfType instanceof GregorianCalendar
							|| valueOfType instanceof Double) {
						valueOfType = convertInstanceOfObject(Arrays.asList(valueOfType), List.class);
					}
				}
				if (reqPropertyType.equals(PropertyType.INTEGER)) {
					if (valueOfType instanceof Integer) {
						Integer valueBigInteger = convertInstanceOfObject(valueOfType, Integer.class);
						serializeMap.put(var, BigInteger.valueOf(valueBigInteger));
					} else if (valueOfType instanceof BigInteger) {
						serializeMap.put(var, ((BigInteger) valueOfType).intValue());
					} else if (valueOfType instanceof List<?>) {
						List<BigInteger> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof String) {
						serializeMap.put(var, new BigInteger((String) valueOfType));
					}

				} else if (reqPropertyType.equals(PropertyType.BOOLEAN)) {
					if (valueOfType instanceof Boolean) {
						Boolean booleanValue = convertInstanceOfObject(valueOfType, Boolean.class);
						serializeMap.put(var, booleanValue);
					} else if (valueOfType instanceof List<?>) {
						List<Boolean> booleanValue = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, booleanValue);
					} else if (valueOfType instanceof String) {
						serializeMap.put(var, Boolean.valueOf((String) valueOfType));
					}

				} else if (reqPropertyType.equals(PropertyType.DATETIME)) {

					// SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd
					// HH:mm:ss Z yyyy", Locale.US);

					if (valueOfType instanceof GregorianCalendar) {
						Long value = convertInstanceOfObject(valueOfType, Long.class);
						GregorianCalendar lastModifiedCalender = new GregorianCalendar();
						lastModifiedCalender.setTimeInMillis(value);
						serializeMap.put(var, lastModifiedCalender);
					} else if (valueOfType instanceof List<?>) {
						List<Long> value = convertInstanceOfObject(valueOfType, List.class);
						List<GregorianCalendar> calenderList = new ArrayList<>();
						value.forEach(v -> {
							GregorianCalendar lastModifiedCalender = new GregorianCalendar();
							lastModifiedCalender.setTimeInMillis(v);
							calenderList.add(lastModifiedCalender);
						});
						serializeMap.put(var, calenderList);
						;
					} else if (valueOfType instanceof String) {
						GregorianCalendar lastModifiedCalender = new GregorianCalendar();
						lastModifiedCalender.setTimeInMillis(Long.valueOf((String) valueOfType));
						serializeMap.put(var, lastModifiedCalender);
					}

				} else if (reqPropertyType.equals(PropertyType.DECIMAL)) {
					if (valueOfType instanceof Double) {
						Double value = convertInstanceOfObject(valueOfType, Double.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof List<?>) {
						List<BigDecimal> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof String) {
						serializeMap.put(var, Double.valueOf((String) valueOfType));
					}
				} else {
					// string type
					if (valueOfType instanceof String) {
						String value = convertInstanceOfObject(valueOfType, String.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof List<?>) {
						List<String> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}
				}
			} else {
				continue;
			}
		}

		LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}, serializedMap: {}", "SwaggerHelpers",
				"deserializeInput", session.getRepositoryInfo().getId(), object.getBaseTypeId().value(), serializeMap);
		return serializeMap;
	}

	/**
	 * 
	 * @param httpRequest
	 * @param repoId
	 *            repository Id
	 * @param objectId
	 *            objectId for which the relation data needs to be fetched
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Object> getDescendantsForRelationObjects(String username, String password, String repoId,
			String objectId) {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = null;
		try {
			String connectionString = System.getenv("SWAGGER_CMIS_SESSION_CONNECTION_URL") + repoId;
			String reqUrl = connectionString + "?objectId=" + objectId
					+ "&cmisselector=descendants&depth=-1&includeAllowableActions=true&includeRelationships=none&renditionFilter=cmis%3Anone&includePathSegment=true&succinct=true&includeACL=true";
			HttpGet getRequest = new HttpGet(reqUrl.trim());
			getRequest.addHeader("Content-Type", "application/json");
			getRequest.addHeader("Authorization", getB64Auth(username, password));
			httpResponse = httpClient.execute(getRequest);
			int resCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity resEntity = httpResponse.getEntity();
			if (resEntity != null) {
				String resBody = EntityUtils.toString(resEntity);
				ArrayList<Object> json = new ObjectMapper().readValue(resBody, ArrayList.class);
				return json;
			} else {
				LOG.error("class name: {}, method name: {}, repositoryId: {}, Empty ResponseEntity with resCode: {}",
						"SwaggerHelpers", "getDescendantsForRelationObjects", repoId, resCode);
				throw new Exception("resCode:" + resCode);
			}
		} catch (Exception e) {
			LOG.warn(
					"class name: {}, method name: {}, repositoryId: {}, Error in building an HTTP Request or Descendants are null!",
					"SwaggerHelpers", "getDescendantsForRelationObjects", repoId);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				LOG.error(
						"class name: {}, method name: {}, repositoryId: {}, Execption in closing httpClient stream: {}",
						"SwaggerHelpers", "getDescendantsForRelationObjects", repoId, ex);
			}
		}
		return null;
	}

	private static String getB64Auth(String username, String password) {
		String source = username + ":" + password;
		String encoding = "Basic " + Base64.getEncoder().encodeToString(source.getBytes());

		return encoding;
	}

	public static IRequest getImplClient(HttpServletRequest request) throws Exception {

		Map<String, Object> inputMap = new HashMap<String, Object>();
		Map<String, Object> requestBaggage = new HashMap<String, Object>();
		Part filePart = null;
		String jsonString = null;

		String authorization = request.getHeader("Authorization");
		String pathFragments[] = HttpUtils.splitPath(request);
		String method = request.getMethod();
		RequestMessage sRequestMessage = new RequestMessage(new AuthMessage(authorization), pathFragments);

		if (METHOD_POST.equals(method)) {
			if (request.getContentType().contains("multipart/form-data")) {
				inputMap = request.getParameterMap().entrySet().stream()
						.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
				filePart = request.getPart("file") != null ? request.getPart("file") : null;
			} else if (request.getContentType().equals("application/x-www-form-urlencoded")) {
				inputMap = request.getParameterMap().entrySet().stream()
						.collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()[0]));
			} else if (sRequestMessage.getType().equals("_metadata")) {
			} else {
				jsonString = IOUtils.toString(request.getInputStream());
			}
			String parentId = request.getParameter("parentId");
			requestBaggage.put("parentId", parentId);

			if (sRequestMessage.getInputType() != null && sRequestMessage.getInputType().equals(SwaggerHelpers.TYPE)) {
				sRequestMessage.setInputStream(request.getInputStream());
			}
			String includeRelationString = request.getParameter("includeRelation");
			boolean crudOperation = includeRelationString != null ? Boolean.parseBoolean(includeRelationString) : false;
			requestBaggage.put("includeRelation", crudOperation);

			if (!crudOperation && jsonString != null) {
				inputMap = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
				});
			}

		} else if (METHOD_PUT.equals(method) && request.getInputStream() != null) {
			if (sRequestMessage.getType().equals("_metadata")) {
				sRequestMessage.setInputStream(request.getInputStream());
			} else {
				jsonString = IOUtils.toString(request.getInputStream());
				inputMap = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
				});
			}

		} else if (METHOD_GET.equals(method)) {
			String select = null;
			String filter = null;
			String order = null;
			if (request.getQueryString() != null) {
				select = request.getParameter("select") != null ? request.getParameter("select").replace("_", ":")
						: null;
				filter = request.getParameter("filter") != null ? request.getParameter("filter").replace("_", ":")
						: null;
				order = request.getParameter("orderby") != null ? request.getParameter("orderby").replace("_", ":")
						: null;
			}

			if (select != null && filter != null) {
				select = select + "," + URLDecoder.decode(filter, "UTF-8");
			} else if (select == null && filter != null) {
				select = "*," + filter;
			}

			requestBaggage.put("select", select);
			requestBaggage.put("orderby", order);
			requestBaggage.put("filter", filter);

			String skipCount = request.getParameter("skipcount");
			String maxItems = request.getParameter("maxitems");
			String parentId = request.getParameter("parentId");
			String includeRelationshipString = request.getParameter("includeRelationship");
			boolean includeRelationship = includeRelationshipString != null
					? Boolean.parseBoolean(includeRelationshipString) : false;

			requestBaggage.put("skipcount", skipCount);
			requestBaggage.put("maxitems", maxItems);
			requestBaggage.put("parentId", parentId);
			requestBaggage.put("includeRelationship", includeRelationship);
		}
		sRequestMessage.setInputMap(inputMap);
		sRequestMessage.setFilePart(filePart);
		sRequestMessage.setJsonString(jsonString);
		sRequestMessage.setRequestBaggage(requestBaggage);
		IRequest reqObj = sRequestMessage;
		return reqObj;
	}

	public static ObjectType getTypeDefinition(Session session, String typeId) throws Exception {
		try {
			ObjectType typeDefinition = session.getTypeDefinition(typeId);
			if (typeDefinition == null) {
				throw new Exception("Type: " + typeId + " not present!");
			}
			return typeDefinition;
		} catch (Exception e) {
			LOG.error(
					"class name: {}, method name: {}, repositoryId: {}, Fetching TypeDefinition Error for type: {}, Cause: {}",
					"SwaggerHelpers", "getTypeDefinition", session.getRepositoryInfo().getId(), typeId, e);
			throw new Exception("Type: " + typeId + " not present!");
		}
	}
}
