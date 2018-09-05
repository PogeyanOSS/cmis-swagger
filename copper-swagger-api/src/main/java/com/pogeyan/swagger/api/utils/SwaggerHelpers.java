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
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParseException;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.pogeyan.swagger.pojos.DefinitionsObject;
import com.pogeyan.swagger.pojos.ErrorResponse;
import com.pogeyan.swagger.pojos.ExternalDocs;
import com.pogeyan.swagger.pojos.InfoObject;
import com.pogeyan.swagger.pojos.ParameterObject;
import com.pogeyan.swagger.pojos.PathCommonObject;
import com.pogeyan.swagger.pojos.PathObject;
import com.pogeyan.swagger.pojos.ResponseObject;
import com.pogeyan.swagger.pojos.SecurityDefinitionObject;
import com.pogeyan.swagger.pojos.TagObject;

/**
 * SwaggerHelpers operations.
 */
public class SwaggerHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerHelpers.class);
	private static Cache<String, ObjectType> typeCacheMap;
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
	public static InfoObject infoObj = new InfoObject();
	public static ExternalDocs externalDocsObject = new ExternalDocs();
	public static String hostSwaggerUrl;
	static {
		typeCacheMap = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
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
	 * @return session is a connection to a CMIS repository with a specific user.
	 * @throws Exception
	 *             Swagger CMIS session connection URL not defined in environment
	 *             variable
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
	 * @return session is a connection to a CMIS repository with a specific user.
	 * @throws Exception
	 *             Swagger CMIS session connection URL not defined in environment
	 *             variable
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

			LOG.info("Creating session for repository: {}, connection URL: {}", repoId, connectionUrl);
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
			LOG.error("Exception on createSession: {}", e.getMessage());
			ErrorResponse resp = SwaggerHelpers.handleException(e);
			throw new ErrorResponse(resp);
		}
		return session;

	}

	/**
	 * @param session
	 *            the property session is used to get all types present that
	 *            particular repository
	 */
	public static void getAllTypes(Session session) {
		LOG.info("Getting all types in repository");
		List<String> list = getBaseTypeList();
		for (String type : list) {
			ObjectType baseType = session.getTypeDefinition(type);
			typeCacheMap.put(baseType.getId().toString(), baseType);
			List<Tree<ObjectType>> allTypes = session.getTypeDescendants(type, -1, true);
			for (Tree<ObjectType> object : allTypes) {
				getChildTypes(object);
			}
		}
		LOG.debug("Types in repository:{}", typeCacheMap.toString());
	}

	public static Boolean getTypeIsPresents() {
		if (typeCacheMap.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @param session
	 *            the property session is used to get all details about that
	 *            repository.
	 * @param typeId
	 *            the property typeId is used to get that particular type based on
	 *            this id.
	 * @return the Object Type
	 */
	public static ObjectType getType(String typeId) {
		return typeCacheMap.getIfPresent(typeId);
	}

	public static ItemIterable<CmisObject> getRelationshipType(Session session, String typeId) {
		ObjectType relationshipType = typeCacheMap.getIfPresent("cmis_ext:relationmd");
		if (relationshipType != null) {
			Folder relationObject = (Folder) session.getObjectByPath("/" + relationshipType.getId());
			if (relationObject != null) {
				OperationContext context = new OperationContextImpl();
				context.setFilterString("target_table,source_table eq " + typeId);
				ItemIterable<CmisObject> relationDescendants = relationObject.getChildren(context);
				return relationDescendants;
			}
		}
		return null;

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
			typeCacheMap.put(typeChildren.getItem().getId().toString(), typeChildren.getItem());
			for (Tree<ObjectType> object : getChildren) {
				getChildTypes(object);
			}
		} else {
			// this will add all children
			typeCacheMap.put(typeChildren.getItem().getId().toString(), typeChildren.getItem());
		}
	}

	/**
	 * @return TagObject is adds metadata to a single tag that is used by the
	 *         Operation Object. It is not mandatory to have a Tag Object per tag
	 *         defined in the Operation Object instances. List of tags used by the
	 *         specification with additional metadata. The order of the tags can be
	 *         used to reflect on their order by the parsing tools. Not all tags
	 *         that are used by the Operation Object must be declared. The tags that
	 *         are not declared MAY be organized randomly or based on the tools'
	 *         logic. Each tag name in the list MUST be unique.
	 */
	public static List<TagObject> generateTagsForAllTypes() {
		List<TagObject> tagsList = new ArrayList<TagObject>();
		for (ObjectType type : typeCacheMap.asMap().values()) {
			String name = getDefinitionName(type);
			TagObject tag = new TagObject(name, type.getDescription() + " Tag", externalDocsObject);
			tagsList.add(tag);
		}

		// add Type TagObject
		TagObject tagType = new TagObject("Types", "MetaData Type " + " Tag", externalDocsObject);
		tagsList.add(tagType);

		// add Acl TagObject
		TagObject aclType = new TagObject("Acl", "Acl Tag", externalDocsObject);
		tagsList.add(aclType);
		tagsList.sort((TagObject a, TagObject b) -> a.getName().toLowerCase().compareTo(b.getName().toLowerCase()));
		LOG.debug("Tags:{}", tagsList.toString());
		return tagsList;
	}

	/**
	 * @return it will returns Map<String, SecurityDefinitionObject> defines a
	 *         security scheme that can be used by the operations. Supported schemes
	 *         are HTTP authentication, an API key (either as a header or as a query
	 *         parameter), OAuth2's common flows (implicit, password, application
	 *         and access code) as defined in RFC6749, and OpenID Connect Discovery
	 */
	public static Map<String, SecurityDefinitionObject> getSecurityDefinitions() {
		Map<String, SecurityDefinitionObject> security = new HashMap<String, SecurityDefinitionObject>();

		SecurityDefinitionObject basicAuth = new SecurityDefinitionObject("basic", null, null, null, null, null);
		security.put("BasicAuth", basicAuth);
		LOG.debug("security:{}", security.toString());
		return security;

	}

	/**
	 * @return it will Map<String, DefinitionsObject> defines it will store each and
	 *         every request schema definitions for all ObjectType data
	 */
	public static Map<String, DefinitionsObject> getDefinitions() {
		Map<String, DefinitionsObject> definitionsMap = new HashMap<String, DefinitionsObject>();
		for (ObjectType type : typeCacheMap.asMap().values()) {

			ArrayList<String> required = new ArrayList<String>();
			required.add("cmis:objectTypeId");
			required.add("cmis:name");

			String defName = getDefinitionName(type);
			Map<String, String> xml = new HashMap<String, String>();
			xml.put("name", type.getDescription());

			Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();
			Set<Entry<String, PropertyDefinition<?>>> data = type.getPropertyDefinitions().entrySet();

			if (type.isBaseType()) {
				for (Entry<String, PropertyDefinition<?>> propertiesValues : data) {
					HashMap<String, String> propObjBase = new HashMap<String, String>();
					if (propertiesValues.getKey().equals("cmis:objectTypeId")
							|| propertiesValues.getKey().equals("cmis:name")
							|| propertiesValues.getKey().equals("cmis:description")
							|| propertiesValues.getKey().equals("cmis:sourceId")
							|| propertiesValues.getKey().equals("cmis:targetId")
							|| propertiesValues.getKey().equals("cmis:policyText")) {
						propObjBase.put("type", PropertyType.STRING.toString().toLowerCase());
						if (propertiesValues.getKey().equals("cmis:objectTypeId")) {
							propObjBase.put("example", type.getQueryName());
						}
					} else {
						continue;
					}
					properties.put(propertiesValues.getKey(), propObjBase);
				}

			} else {
				// custom type
				for (Entry<String, PropertyDefinition<?>> propertiesValues : data) {
					HashMap<String, String> propObj = new HashMap<String, String>();
					if (propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamLength")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:objectId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamFileName")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamMimeType")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:checkinComment")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionLabel")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isMajorVersion")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isLatestVersion")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isLatestMajorVersion")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isPrivateWorkingCopy")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:createdBy")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isVersionSeriesCheckedOut")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:isImmutable")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:modifiedBy")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:previousVersionObjectId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:lastModificationDate")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:lastModifiedBy")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:baseTypeId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutBy")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:baseTypeId")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:creationDate")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:secondaryObjectTypeIds")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:changeToken")
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutBy")) {

						continue;

					} else {
						SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
						if (propertiesValues.getValue().getPropertyType().equals(PropertyType.INTEGER)) {
							propObj.put("type", propertiesValues.getValue().getPropertyType().name().toLowerCase());
							propObj.put("format", "int64");
						} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DATETIME)) {
							propObj.put("type", "string");
							propObj.put("format", "data-time");
							propObj.put("example", sdf.format(new Date(System.currentTimeMillis())));
						} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DECIMAL)) {
							propObj.put("type", "number");
							propObj.put("format", "double");
						} else {
							propObj.put("type", "string");
						}
						if (propertiesValues.getKey().equals("cmis:objectTypeId")) {
							propObj.put("example", type.getQueryName());
						}
						properties.put(propertiesValues.getKey(), propObj);
					}
				}
			}

			DefinitionsObject definitions = new DefinitionsObject("object", required, xml, properties);
			definitionsMap.put(defName, definitions);
		}

		Map<String, String> xmlType = new HashMap<String, String>();
		xmlType.put("name", "Type");
		Map<String, Map<String, String>> propertiesForType = new HashMap<String, Map<String, String>>();
		Field[] fieldsType = AbstractTypeDefinition.class.getDeclaredFields();
		for (Field f : fieldsType) {// java.util.Map
			HashMap<String, String> propObj = new HashMap<String, String>();
			if (f.getType().equals(Integer.class)) {
				propObj.put("type", "integer");
				propObj.put("format", "int64");
			} else if (f.getType().equals(Boolean.class)) {
				propObj.put("type", "boolean");
			} else if (f.getType().equals(TypeMutability.class)) {
				propObj.put("$ref", "#/definitions/TypeMutability");
			} else if (f.getName().equals("propertyDefinitions")) {
				propObj.put("$ref", "#/definitions/PropertyDefinitions");
			} else {
				propObj.put("type", "string");
			}
			propertiesForType.put(f.getName(), propObj);
		}

		DefinitionsObject definitionsType = new DefinitionsObject("object", null, xmlType, propertiesForType);
		definitionsMap.put("Types", definitionsType);

		Map<String, String> xmlMut = new HashMap<String, String>();
		xmlMut.put("name", "TypeMutability");
		Map<String, Map<String, String>> propertiesForTypeMut = new HashMap<String, Map<String, String>>();
		Field[] fieldsMut = TypeMutability.class.getDeclaredFields();
		for (Field f : fieldsMut) {
			HashMap<String, String> propObj = new HashMap<String, String>();
			if (f.getType().equals(Boolean.class)) {
				propObj.put("type", "boolean");
			} else {
				propObj.put("type", "string");
			}
			propertiesForTypeMut.put(f.getName(), propObj);
		}

		DefinitionsObject definitionsTypeMut = new DefinitionsObject("object", null, xmlMut, propertiesForTypeMut);
		definitionsMap.put("TypeMutability", definitionsTypeMut);

		Map<String, String> xmlPropDef = new HashMap<String, String>();
		xmlPropDef.put("name", "PropertyDefinitions");
		Map<String, Map<String, String>> propertiesForPropDef = new HashMap<String, Map<String, String>>();
		Field[] fieldsPropDef = AbstractPropertyDefinition.class.getDeclaredFields();
		for (Field f : fieldsPropDef) {
			HashMap<String, String> propObj = new HashMap<String, String>();
			if (f.getType().equals(Integer.class)) {
				propObj.put("type", "integer");
				propObj.put("format", "int64");
			} else if (f.getType().equals(Boolean.class)) {
				propObj.put("type", "boolean");
			} else {
				propObj.put("type", "string");
			}
			propertiesForPropDef.put(f.getName(), propObj);
		}

		DefinitionsObject definitionsPropDef = new DefinitionsObject("object", null, xmlPropDef, propertiesForPropDef);
		definitionsMap.put("PropertyDefinitions", definitionsPropDef);

		Map<String, String> xmlAcl = new HashMap<String, String>();
		xmlPropDef.put("name", "Acl");
		Map<String, Map<String, String>> propertiesForAcl = new HashMap<String, Map<String, String>>();
		HashMap<String, String> princObj = new HashMap<String, String>();
		princObj.put("type", "string");
		propertiesForAcl.put("objectId", princObj);
		propertiesForAcl.put("principalId", princObj);
		propertiesForAcl.put("permission", princObj);

		DefinitionsObject definitionsAcl = new DefinitionsObject("object", null, xmlAcl, propertiesForAcl);
		definitionsMap.put("Acl", definitionsAcl);

		LOG.debug("definitions:{}", definitionsMap.toString());
		return definitionsMap;

	}

	/**
	 * @return it will return Map<String, PathObject> that include
	 *         ParameterObject,ResponseObject,PathCommonObject finally it will club
	 *         into one object as PathObject and returns. ParameterObject describes
	 *         a single operation parameter. A unique parameter is defined by a
	 *         combination of a name and location. ResponseObject describes a single
	 *         response from an API Operation, including design-time, static links
	 *         to operations based on the response. PathCommonObject describes the
	 *         operations available on a single path. A Path Item MAY be empty, due
	 *         to ACL constraints. The path itself is still exposed to the
	 *         documentation viewer but they will not know which operations and
	 *         parameters are available. PathObject holds the relative paths to the
	 *         individual end points and their operations. The path is appended to
	 *         the URL from the Server Object in order to construct the full URL.
	 *         The Paths MAY be empty, due to ACL constraints
	 */
	public static Map<String, PathObject> generatePathForAllTypes() {
		Map<String, PathObject> pathMap = new HashMap<String, PathObject>();
		String basicAuth[] = new String[] {};
		Map<String, String[]> api = new HashMap<String, String[]>();
		api.put("BasicAuth", basicAuth);
		List<Map<String, String[]>> security = new ArrayList<Map<String, String[]>>();
		security.add(api);

		for (ObjectType type : typeCacheMap.asMap().values()) {

			String[] consumes = new String[] { "application/json" };
			String[] produces = new String[] { "application/json" };
			Map<String, String> schema = new HashMap<String, String>();
			String defName = getDefinitionName(type);
			schema.put("$ref", "#/definitions/" + defName);
			String id = getIdName(type);
			// GET METHODS
			// get all folders /folder GET skipCount and maxItems in query
			if (type.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)
					|| type.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
				ParameterObject skipCount = new ParameterObject("query", "skipcount", null, false, null, "integer",
						null, null, null, null);
				ParameterObject maxItems = new ParameterObject("query", "maxitems", null, false, null, "integer", null,
						null, null, null);
				Map<String, ResponseObject> getAllresponses = new HashMap<String, ResponseObject>();
				ResponseObject obj3 = new ResponseObject("Successful Operation", null);
				getAllresponses.put("200", obj3);
				PathCommonObject getAllObj = new PathCommonObject(new String[] { defName },
						"Get All " + defName + " Objects", null, "getAll" + defName, null, produces,
						new ParameterObject[] { skipCount, maxItems }, getAllresponses, security);

				PathObject pathGetAllObj = new PathObject(null, getAllObj, null, null);
				pathMap.put("/" + defName + "/getAll", pathGetAllObj);
			}
			// get folder /folder/{folderId} GET
			Map<String, ResponseObject> getResponsesMap = new HashMap<String, ResponseObject>();
			ResponseObject respObj1 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject respObj2 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject respObj3 = new ResponseObject("successful operation", schema);
			getResponsesMap.put("404", respObj1);
			getResponsesMap.put("400", respObj2);
			getResponsesMap.put("200", respObj3);

			ParameterObject getParams = new ParameterObject("path", id, "ID of " + type.getDescription() + " to return",
					true, null, "string", null, null, "int64", null);
			PathCommonObject getCommonObject = new PathCommonObject(new String[] { defName },
					"Get " + defName + " by Id", null, "get" + defName + "ById", null, produces,
					id != null ? new ParameterObject[] { getParams } : null, getResponsesMap, security);

			// DELETE METHOD
			// delete folder /folder/{folderId} DELETE
			Map<String, ResponseObject> delResponses = new HashMap<String, ResponseObject>();
			ResponseObject obj4 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject obj5 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject obj61 = new ResponseObject("CmisNotSupportedException", null);

			delResponses.put("404", obj4);
			delResponses.put("405", obj61);
			delResponses.put("400", obj5);

			PathCommonObject deleteCommonObject = new PathCommonObject(new String[] { defName },
					"Deletes a  " + defName + " Object", null, "delete" + defName, null, produces,
					id != null ? new ParameterObject[] { getParams } : null, delResponses, security);

			// UPDATE METHOD
			// update folder /folder/{folderId} PUT
			Map<String, ResponseObject> putResponses = new HashMap<String, ResponseObject>();
			ResponseObject obj6 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject obj7 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject err11 = new ResponseObject("InvalidArgumentException", null);
			ResponseObject err21 = new ResponseObject("CmisContentAlreadyExistsException", null);
			ResponseObject err31 = new ResponseObject("CmisRuntimeException", null);
			ResponseObject err41 = new ResponseObject("CmisStreamNotSupportedException", null);
			ResponseObject err51 = new ResponseObject("CmisServiceUnavailableException", null);
			ResponseObject err61 = new ResponseObject("CmisNotSupportedException", null);
			ResponseObject obj31 = new ResponseObject("Successful Operation", null);
			putResponses.put("200", obj31);
			putResponses.put("400", err11);
			putResponses.put("409", err21);
			putResponses.put("500", err31);
			putResponses.put("403", err41);
			putResponses.put("503", err51);
			putResponses.put("404", obj6);
			putResponses.put("405", err61);
			putResponses.put("400", obj7);
			ParameterObject putParamsBody = new ParameterObject("body", "body",
					type.getDescription() + " Object that needs to be updated in the repository", true, schema, null,
					null, null, null, null);
			ParameterObject putParamsPath = new ParameterObject("path", id,
					"ID of " + type.getDescription() + " to return", true, null, "string", null, null, null, null);
			PathCommonObject putCommonObject = new PathCommonObject(new String[] { defName },
					"Update " + defName + " Object", null, "update" + defName, consumes, produces,
					id != null ? new ParameterObject[] { putParamsPath, putParamsBody }
							: new ParameterObject[] { putParamsBody },
					putResponses, security);

			PathObject pObj2 = new PathObject(null, getCommonObject, putCommonObject, deleteCommonObject);
			pathMap.put(id != null ? "/" + defName + "/{" + id + "}" : "/" + defName, pObj2);

			// POST METHODS
			if (type.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {

				// post /doc/{docId}/uploadFile

				Map<String, ResponseObject> uploadResponses = new HashMap<String, ResponseObject>();
				ResponseObject uploadResp = new ResponseObject("Successful Operation", null);
				ResponseObject createdResp = new ResponseObject("Created", null);
				ResponseObject err1 = new ResponseObject("InvalidArgumentException", null);
				ResponseObject err2 = new ResponseObject("CmisContentAlreadyExistsException", null);
				ResponseObject err3 = new ResponseObject("CmisRuntimeException", null);
				ResponseObject err4 = new ResponseObject("CmisStreamNotSupportedException", null);
				ResponseObject err5 = new ResponseObject("CmisServiceUnavailableException", null);
				ResponseObject err6 = new ResponseObject("CmisNotSupportedException", null);

				uploadResponses.put("400", err1);
				uploadResponses.put("409", err2);
				uploadResponses.put("500", err3);
				uploadResponses.put("403", err4);
				uploadResponses.put("503", err5);
				uploadResponses.put("201", createdResp);
				uploadResponses.put("200", uploadResp);
				uploadResponses.put("405", err6);

				ParameterObject uploadPostParams = new ParameterObject("path", id, "ID of " + type.getDescription(),
						true, null, "string", null, null, "int64", null);
				ParameterObject uploadFileParams = new ParameterObject("formData", "file", "Document to upload", true,
						null, "file", null, null, null, null);
				PathCommonObject uploadPostObj = new PathCommonObject(new String[] { defName }, "Uploads a document",
						null, "uploadDoc" + defName, new String[] { "multipart/form-data" }, null,
						new ParameterObject[] { uploadPostParams, uploadFileParams }, uploadResponses, security);
				PathObject uploadPathObj = new PathObject(uploadPostObj, null, null, null);
				pathMap.put("/" + defName + "/{" + id + "}" + "/uploadDocument", uploadPathObj);

				// get for download media /document/media/{id}
				Map<String, ResponseObject> getMediaResponses = new HashMap<String, ResponseObject>();
				Map<String, String> mediaSchema = new HashMap<String, String>();
				mediaSchema.put("type", "file");
				ResponseObject mediaResp = new ResponseObject("Successful Operation", mediaSchema);
				ResponseObject err42 = new ResponseObject("CmisStreamNotSupportedException", null);
				getMediaResponses.put("403", err42);
				getMediaResponses.put("200", mediaResp);
				ParameterObject getParamsObject = new ParameterObject("path", id, "ID of " + type.getDescription(),
						true, null, "string", null, null, "int64", null);
				PathCommonObject getObj = new PathCommonObject(new String[] { defName }, "Downloads a file", null,
						"downloadDoc" + defName, null, new String[] { "application/*" },
						new ParameterObject[] { getParamsObject }, getMediaResponses, security);
				PathObject pObject = new PathObject(null, getObj, null, null);
				pathMap.put("/" + defName + "/media/{" + id + "}", pObject);
			}
			// formdata of post
			Map<String, ResponseObject> postFormResponses = new HashMap<String, ResponseObject>();
			ResponseObject respObj11 = new ResponseObject("CmisNotSupportedException", null);
			ResponseObject respObj21 = new ResponseObject("Successful Operation", null);
			ResponseObject createdResp = new ResponseObject("Created", null);
			ResponseObject err1 = new ResponseObject("InvalidArgumentException", null);
			ResponseObject err2 = new ResponseObject("CmisContentAlreadyExistsException", null);
			ResponseObject err3 = new ResponseObject("CmisRuntimeException", null);
			ResponseObject err4 = new ResponseObject("CmisStreamNotSupportedException", null);
			ResponseObject err5 = new ResponseObject("CmisServiceUnavailableException", null);
			postFormResponses.put("400", err1);
			postFormResponses.put("409", err2);
			postFormResponses.put("500", err3);
			postFormResponses.put("403", err4);
			postFormResponses.put("503", err5);
			postFormResponses.put("201", createdResp);
			postFormResponses.put("405", respObj11);
			postFormResponses.put("400", err1);
			postFormResponses.put("200", respObj21);

			ParameterObject postPathParams1 = new ParameterObject("query", "parentId",
					"Parent ID of " + type.getDescription(), false, null, "string", null, null, null, null);
			// get array of parameter objects
			ParameterObject uploadFormFileParams = new ParameterObject("formData", "file", "Document to upload", false,
					null, "file", null, null, null, null);
			List<ParameterObject> paramList = getFormDataObjects(type);

			paramList.add(postPathParams1);

			if (type.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
				paramList.add(uploadFormFileParams);
			}
			PathCommonObject postObj = new PathCommonObject(new String[] { defName },
					"Add a new " + defName + " Object", null, "add" + defName + "formData",
					type.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT) ? new String[] { "multipart/form-data" }
							: new String[] { "application/x-www-form-urlencoded" },
					produces, paramList.toArray(new ParameterObject[paramList.size()]), postFormResponses, security);

			PathObject pathObj = new PathObject(postObj, null, null, null);
			pathMap.put("/" + defName, pathObj);
		}

		// generate path for Types
		pathMap = PostTypeDefinitionCreation(pathMap, security);
		// generate path for ACL
		pathMap = postAclDefinitionCreation(pathMap, security);

		LOG.debug("path:{}", pathMap.toString());
		return pathMap;
	}

	/**
	 * @param pathMap
	 *            the property pathMap is used to store pathMap.It will give
	 *            Map<String, PathObject> pathMap.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations. The
	 *         path is appended to the URL from the Server Object in order to
	 *         construct the full URL. The Paths MAY be empty, due to ACL
	 *         constraints
	 */
	private static Map<String, PathObject> PostTypeDefinitionCreation(Map<String, PathObject> pathMap,
			List<Map<String, String[]>> security) {
		Map<String, String> schema = new HashMap<String, String>();
		schema.put("$ref", "#/definitions/Types");
		Map<String, ResponseObject> typeResponses = new HashMap<String, ResponseObject>();
		ResponseObject typeResp = new ResponseObject("Successful Operation", null);
		typeResponses.put("200", typeResp);
		ParameterObject typeParams = new ParameterObject("path", "typeId", "TypeId to return", true, null, "string",
				null, null, null, null);
		pathMap = postCreateTypeDefinitionCreation(pathMap, schema, typeResponses, security);
		PathCommonObject getTypeObject = postGetTypeDefinitionCreation(security, typeParams);
		PathCommonObject putTypeObject = postUpdateTypeDefinitionCreation(schema, security, typeParams, typeResponses);
		PathCommonObject deleteTypeObject = postDeleteTypeDefinitionCreation(security, typeParams);
		PathObject getTypePathObj = new PathObject(null, getTypeObject, putTypeObject, deleteTypeObject);
		pathMap.put("/_metadata/type/{typeId}", getTypePathObj);
		return pathMap;

	}

	/**
	 * @param pathMap
	 *            the property pathMap is used to store pathMap.It will give
	 *            Map<String, PathObject> pathMap.
	 * @param schema
	 *            the property schema is used to hold all swagger object.
	 * @param typeResponses
	 *            the property typeResponse it will give map of ResponseObject. it
	 *            describes a single response from an API Operation, including
	 *            design-time, static links to operations based on the response.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations. The
	 *         path is appended to the URL from the Server Object in order to
	 *         construct the full URL. The Paths MAY be empty, due to ACL
	 *         constraints
	 */
	private static Map<String, PathObject> postCreateTypeDefinitionCreation(Map<String, PathObject> pathMap,
			Map<String, String> schema, Map<String, ResponseObject> typeResponses,
			List<Map<String, String[]>> security) {
		ParameterObject typeFileParams = new ParameterObject("body", "body", "To create new Types", true, schema, null,
				null, null, null, null);
		PathCommonObject typePostObj = new PathCommonObject(new String[] { "Types" }, "Creates a new Type", null,
				"createType", new String[] { "application/json" }, null, new ParameterObject[] { typeFileParams },
				typeResponses, security);
		PathObject typePathObj = new PathObject(typePostObj, null, null, null);
		pathMap.put("/_metadata/type", typePathObj);
		return pathMap;

	}

	/**
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject. it
	 *            describes a single operation parameter. A unique parameter is
	 *            defined by a combination of a name and location.
	 * @return PathCommonObject describes the operations available on a single path.
	 *         A Path Item MAY be empty, due to ACL constraints. The path itself is
	 *         still exposed to the documentation viewer but they will not know
	 *         which operations and parameters are available
	 */
	private static PathCommonObject postGetTypeDefinitionCreation(List<Map<String, String[]>> security,
			ParameterObject typeParams) {
		Map<String, ResponseObject> getResponsesMap = new HashMap<String, ResponseObject>();
		ResponseObject respObj1 = new ResponseObject("Type not found", null);
		ResponseObject respObj2 = new ResponseObject("Invalid ID supplied", null);
		ResponseObject respObj3 = new ResponseObject("successful operation", null);
		getResponsesMap.put("404", respObj1);
		getResponsesMap.put("400", respObj2);
		getResponsesMap.put("200", respObj3);

		PathCommonObject getTypeObject = new PathCommonObject(new String[] { "Types" }, "Get Type by Id", null,
				"getTypeById", null, new String[] { "application/json" }, new ParameterObject[] { typeParams },
				getResponsesMap, security);
		return getTypeObject;

	}

	/**
	 * @param schema
	 *            the property schema is used to hold all swagger object.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject. it
	 *            describes a single operation parameter. A unique parameter is
	 *            defined by a combination of a name and location.
	 * @param typeResponses
	 *            the property typeResponse it will give map of ResponseObject. it
	 *            describes a single response from an API Operation, including
	 *            design-time, static links to operations based on the response.
	 * @return PathCommonObject is used to do the operations available on a single
	 *         path. A Path Item MAY be empty, due to ACL constraints. The path
	 *         itself is still exposed to the documentation viewer but they will not
	 *         know which operations and parameters are available
	 * 
	 */
	private static PathCommonObject postUpdateTypeDefinitionCreation(Map<String, String> schema,
			List<Map<String, String[]>> security, ParameterObject typeParams,
			Map<String, ResponseObject> typeResponses) {
		ParameterObject updateTypeFileParams = new ParameterObject("body", "body", "To update a Type", true, schema,
				null, null, null, null, null);
		PathCommonObject typePutObj = new PathCommonObject(new String[] { "Types" }, "Update a Type", null,
				"updateType", new String[] { "application/json" }, null,
				new ParameterObject[] { updateTypeFileParams, typeParams }, typeResponses, security);

		return typePutObj;

	}

	/**
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject. it
	 *            describes a single operation parameter. A unique parameter is
	 *            defined by a combination of a name and location.
	 * @return PathCommonObject is used to do the operations available on a single
	 *         path. A Path Item MAY be empty, due to ACL constraints. The path
	 *         itself is still exposed to the documentation viewer but they will not
	 *         know which operations and parameters are available
	 */
	private static PathCommonObject postDeleteTypeDefinitionCreation(List<Map<String, String[]>> security,
			ParameterObject typeParams) {
		Map<String, ResponseObject> delTypeResponses = new HashMap<String, ResponseObject>();
		ResponseObject obj1 = new ResponseObject("Type not found", null);
		ResponseObject obj2 = new ResponseObject("Invalid ID supplied", null);
		delTypeResponses.put("404", obj1);
		delTypeResponses.put("400", obj2);
		PathCommonObject deleteTypeObject = new PathCommonObject(new String[] { "Types" }, "Deletes a Type", null,
				"deleteType", null, new String[] { "application/json" }, new ParameterObject[] { typeParams },
				delTypeResponses, security);
		return deleteTypeObject;

	}

	/**
	 * @param pathMap
	 *            the property pathMap is used to store pathMap.It will give
	 *            Map<String, PathObject> pathMap.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations. The
	 *         path is appended to the URL from the Server Object in order to
	 *         construct the full URL. The Paths MAY be empty, due to ACL
	 *         constraints
	 */
	private static Map<String, PathObject> postAclDefinitionCreation(Map<String, PathObject> pathMap,
			List<Map<String, String[]>> security) {
		Map<String, String> schemAcl = new HashMap<String, String>();
		schemAcl.put("$ref", "#/definitions/Acl");
		Map<String, ResponseObject> aclResponses = new HashMap<String, ResponseObject>();
		ResponseObject aclResp = new ResponseObject("Successful Operation", null);
		aclResponses.put("200", aclResp);

		pathMap = postAddAclDefinitionCreation(pathMap, schemAcl, aclResponses, security);

		pathMap = postRemoveAclDefinitionCreation(pathMap, schemAcl, aclResponses, security);

		return pathMap;

	}

	/**
	 * @param pathMap
	 *            the property pathMap is used to store pathMap.It will give
	 *            Map<String, PathObject> pathMap.
	 * @param schemAcl
	 *            the property schemAcl is used to hold all swagger object.
	 * @param aclResponses
	 *            the property aclResponses it will give map of ResponseObject. it
	 *            describes a single response from an API Operation, including
	 *            design-time, static links to operations based on the response.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual end points and their operations. The
	 *         path is appended to the URL from the Server Object in order to
	 *         construct the full URL. The Paths MAY be empty, due to ACL
	 *         constraints
	 */
	private static Map<String, PathObject> postAddAclDefinitionCreation(Map<String, PathObject> pathMap,
			Map<String, String> schemAcl, Map<String, ResponseObject> aclResponses,
			List<Map<String, String[]>> security) {
		ParameterObject postAddAclParams = new ParameterObject("body", "body", "Add Acl", true, schemAcl, null, null,
				null, null, null);
		PathCommonObject aclAddPostObj = new PathCommonObject(new String[] { "Acl" }, "Adds a new Acl", null, "addAcl",
				new String[] { "application/json" }, null, new ParameterObject[] { postAddAclParams }, aclResponses,
				security);
		PathObject aclAddPathObj = new PathObject(aclAddPostObj, null, null, null);
		pathMap.put("/Acl/addAcl", aclAddPathObj);
		return pathMap;

	}

	/**
	 * @param pathMap
	 *            the property pathMap is used to store pathMap.It will give
	 *            Map<String, PathObject> pathMap.
	 * @param schemAcl
	 *            the property schemAcl is used to hold all swagger object.
	 * @param aclResponses
	 *            the property aclResponses it will give map of ResponseObject. it
	 *            describes a single response from an API Operation, including
	 *            design-time, static links to operations based on the response.
	 * @param security
	 *            the property security is used to defines a security scheme that
	 *            can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual end points and their operations. The
	 *         path is appended to the URL from the Server Object in order to
	 *         construct the full URL. The Paths MAY be empty, due to ACL
	 *         constraints
	 */
	private static Map<String, PathObject> postRemoveAclDefinitionCreation(Map<String, PathObject> pathMap,
			Map<String, String> schemAcl, Map<String, ResponseObject> aclResponses,
			List<Map<String, String[]>> security) {
		ParameterObject delAclParams = new ParameterObject("body", "body", "Remove Acl", true, schemAcl, null, null,
				null, null, null);
		PathCommonObject aclRemObj = new PathCommonObject(new String[] { "Acl" }, "Removes an Acl", null, "removeAcl",
				new String[] { "application/json" }, null, new ParameterObject[] { delAclParams }, aclResponses,
				security);
		PathObject aclRemPathObj = new PathObject(aclRemObj, null, null, null);
		pathMap.put("/Acl/removeAcl", aclRemPathObj);
		return pathMap;

	}

	/**
	 * @param type
	 *            the property type is used to get that ObjectType.
	 * @return list of parameterObject is used to do a single operation parameter. A
	 *         unique parameter is defined by a combination of a name and location
	 */
	private static List<ParameterObject> getFormDataObjects(ObjectType type) {
		List<ParameterObject> list = new ArrayList<>();

		Set<Entry<String, PropertyDefinition<?>>> data = type.getPropertyDefinitions().entrySet();
		for (Entry<String, PropertyDefinition<?>> propertiesValues : data) {
			if (propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamLength")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:path")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:allowedChildObjectTypeIds")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:parentId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamFileName")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamMimeType")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:checkinComment")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionLabel")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isMajorVersion")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isLatestVersion")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isLatestMajorVersion")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isPrivateWorkingCopy")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:createdBy")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:contentStreamId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isVersionSeriesCheckedOut")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:isImmutable")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:modifiedBy")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:previousVersionObjectId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:lastModificationDate")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:lastModifiedBy")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:baseTypeId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutBy")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:baseTypeId")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:creationDate")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:secondaryObjectTypeIds")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:changeToken")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:versionSeriesCheckedOutBy")) {
				continue;
			}

			boolean required = false;
			String paramType = null;
			String format = null;
			if (propertiesValues.getValue() != null && propertiesValues.getValue().getLocalName() != null
					&& propertiesValues.getValue().getLocalName().equals("primaryKey")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:name")
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:objectTypeId")) {
				required = true;
			}

			if (propertiesValues.getValue().getPropertyType().equals(PropertyType.INTEGER)) {
				paramType = propertiesValues.getValue().getPropertyType().name().toLowerCase();
				format = "int32";
			} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DATETIME)) {
				paramType = "string";
				format = "data-time";
			} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DECIMAL)) {
				paramType = "number";
				format = "double";
			} else {
				paramType = "string";
			}

			ParameterObject obj = new ParameterObject("formData", propertiesValues.getKey(),
					propertiesValues.getValue().getDescription(), required, null, paramType, null, null, format,
					propertiesValues.getKey().equalsIgnoreCase("cmis:objectTypeId") ? type.getId() : null);
			list.add(obj);
		}
		return list;
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
						? propertiesValues.getValue().getLocalName().equals("primaryKey")
						: false) {
					objectIdName = propertiesValues.getKey();
					return objectIdName;
				}
			}
		}
		return type.getBaseType().getPropertyDefinitions().get("cmis:objectId").getId();

	}

	/**
	 * @param type
	 *            the property type is used to get the ObjectType.
	 * @return typeId used to get the particular object in data
	 */
	private static String getDefinitionName(ObjectType type) {
		return type.getId();
	}

	/**
	 * @return InfoObject used provides metadata about the API. The metadata MAY be
	 *         used by the clients if needed, and MAY be presented in editing or
	 *         documentation generation tools for convenience
	 */
	public static InfoObject generateInfoObject() {
		return infoObj;
	}

	/**
	 * @return ExternalDocs allows referencing an external resource for extended
	 *         documentation
	 */
	public static ExternalDocs generateExternalDocsObject() {
		return externalDocsObject;
	}

	/**
	 * @param description
	 *            A short description of the application. CommonMark syntax MAY be
	 *            used for rich text representation.
	 * @param version
	 *            The version of the OpenAPI document (which is distinct from the
	 *            OpenAPI Specification version or the API implementation version).
	 * @param title
	 *            The title of the application.
	 * @param termsOfService
	 *            A URL to the Terms of Service for the API. MUST be in the format
	 *            of a URL.
	 * @param contact
	 *            The contact information for the exposed API.
	 * @param license
	 *            The license information for the exposed API.
	 */
	public static void setInfoObject(String description, String version, String title, String termsOfService,
			Map<String, String> contact, Map<String, String> license) {
		infoObj.setDescription(description);
		infoObj.setTitle(title);
		infoObj.setVersion(version);
		infoObj.setTermsOfService(termsOfService);
		infoObj.setContact(contact);
		infoObj.setLicense(license);
	}

	/**
	 * @param description
	 *            A short description of the target documentation. CommonMark syntax
	 *            MAY be used for rich text representation.
	 * @param url
	 *            The URL for the target documentation. Value MUST be in the format
	 *            of a URL.
	 */
	public static void setExternalDocsObject(String description, String url) {
		externalDocsObject.setDescription(description);
		externalDocsObject.setUrl(url);
	}

	/**
	 * @return it will give all base type id's
	 */
	public static List<String> getBaseTypeList() {
		List<String> list = new ArrayList<String>();
		list.add("cmis:folder");
		list.add("cmis:document");
		list.add("cmis:item");
		list.add("cmis:relationship");
		list.add("cmis:policy");
		list.add("cmis_ext:relationmd");
		list.add("cmis_ext:relationship");
		list.add("cmis_ext:config");
		return list;
	}

	/**
	 * @param ex
	 *            the property ex is used to catch various exception in server.
	 * @return ErrorResponse give the correct exception with error code
	 */
	public static ErrorResponse handleException(Exception ex) {
		String errorMessage;
		int code;
		ex.printStackTrace();
		LOG.error("CmisBaseResponse error: {},{}", ex.getMessage(), ex.getStackTrace());

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
	 * @return true means type have custom folder,false means type does not have any
	 *         custom folder
	 */
	public static boolean customTypeHasFolder() {
		final String value = System.getenv("CUSTOM_TYPE_HAS_FOLDER");
		if (value != null) {
			return value.equalsIgnoreCase("1");
		}
		return false;
	}

	public static void setHostSwaggerUrl(String swaggerServerUrl) {
		hostSwaggerUrl = swaggerServerUrl;
	}

	public static String getHostSwaggerUrl() {
		return hostSwaggerUrl;

	}

	/**
	 * 
	 * @param httpRequest
	 * @return username and password from the request
	 * @throws UnsupportedEncodingException
	 */

	private static String getB64Auth(String username, String password) {
		String source = username + ":" + password;
		String encoding = "Basic " + Base64.getEncoder().encodeToString(source.getBytes());

		return encoding;
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
		// TODO Auto-generated method stub
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
				LOG.error("class name: {}, method name: {}, repositoryId: {},Empty ResponseEntity with resCode: {}",
						"SwaggerHelpers", "getDescendantsForRelationObjects", repoId, resCode);
				throw new Exception("resCode:" + resCode);
			}
		} catch (Exception e) {
			LOG.info(
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

	@SuppressWarnings("unchecked")
	public static Map<String, Object> formRelationData(Session session, ArrayList<Object> relationData) {
		Map<String, Object> relMap = new LinkedHashMap<String, Object>();
		if (relationData != null) {
			relationData.forEach(relationObj -> {
				JSONObject childJson = new JSONObject();
				LinkedHashMap<Object, Object> relationObjectMap = (LinkedHashMap<Object, Object>) relationObj;
				LinkedHashMap<Object, Object> getRelationshipData = (LinkedHashMap<Object, Object>) relationObjectMap
						.get("object");
				LinkedHashMap<Object, Object> getRelationshipObjectData = (LinkedHashMap<Object, Object>) getRelationshipData
						.get("object");
				Map<String, Object> succintProps = (Map<String, Object>) getRelationshipObjectData
						.get("succinctProperties");
				childJson.putAll(succintProps);
				String relId = succintProps.get(PropertyIds.OBJECT_ID).toString();
				ArrayList<JSONObject> list = relMap.get(relId) != null ? (ArrayList<JSONObject>) relMap.get(relId)
						: new ArrayList<>();
				ArrayList<Object> childrenRelationData = (ArrayList<Object>) relationObjectMap.get("children");
				if (childrenRelationData != null) {
					childJson.put("relation", formRelationData(session, childrenRelationData));
				}
				list.add(childJson);
				relMap.put(relId, list);
			});
		}
		return relMap;
	}

	@SuppressWarnings("unchecked")
	private static void getMapValue(Map<String, Object> props, Map<String, List<Map<String, Object>>> relatedMap) {
		Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
		List<String> id = new ArrayList<>();
		if (props.get("_id") != null) {
			id.add(props.get("_id").toString());
		} else if (props.get("id") != null) {
			id.add(props.get("id").toString());
		} else {
			String uuid = UUID.randomUUID().toString();
			id.add(uuid);
			props.put("id", uuid);
		}

		Map<String, Object> propsObject = props.entrySet().stream()
				.filter(a -> a.getValue() != null && p.matcher(a.getKey()).find())
				.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
		if (propsObject != null) {
			propsObject.entrySet().stream().forEach(obj -> {
				String relType = obj.getKey();
				ArrayList<Object> headerSectionArray = (ArrayList<Object>) obj.getValue();
				if (headerSectionArray != null && !headerSectionArray.isEmpty() && headerSectionArray.size() > 0) {
					getChildObject(relType, headerSectionArray, relatedMap, p, id);
				}
			});
		}

	}

	@SuppressWarnings("unchecked")
	private static void getChildObject(String key, ArrayList<Object> headerSectionArray,
			Map<String, List<Map<String, Object>>> relatedMap, Pattern p, List<String> id) {
		List<Map<String, Object>> listResultMap = new ArrayList<>();
		if (headerSectionArray != null) {
			headerSectionArray.forEach(section -> {
				Map<String, Object> sectionMap = (Map<String, Object>) section;
				getMapValue(sectionMap, relatedMap);
				Map<String, Object> result = sectionMap.entrySet().stream()
						.filter(a -> a.getValue() != null && !p.matcher(a.getKey()).find())
						.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
				result.put("sourceParentId", id.get(0));
				listResultMap.add(result);
			});
		}
		if (relatedMap.get(key) != null) {
			List<Map<String, Object>> newList = new ArrayList<>(relatedMap.get(key));
			newList.addAll(listResultMap);
			relatedMap.remove(key);
			relatedMap.put(key, newList);
		} else {
			relatedMap.put(key, listResultMap);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Map<String, Object>>> formMapData(Session session, ArrayList<Object> relationData) {
		Map<String, List<Map<String, Object>>> relatedMap = new HashMap<>();
		if (relationData != null) {
			relationData.forEach(relObj -> {
				LinkedHashMap<Object, Object> relObjMap = (LinkedHashMap<Object, Object>) relObj;
				LinkedHashMap<Object, Object> object1 = (LinkedHashMap<Object, Object>) relObjMap.get("object");
				LinkedHashMap<Object, Object> object2 = (LinkedHashMap<Object, Object>) object1.get("object");
				LinkedHashMap<Object, Object> succintProps = (LinkedHashMap<Object, Object>) object2
						.get("succinctProperties");
				LinkedHashMap<Object, Object> aclProps = (LinkedHashMap<Object, Object>) object2.get("acl");
				JSONObject objmainProps = formProperties(session, succintProps, false, true, true, aclProps);
				String relType = succintProps.get(PropertyIds.OBJECT_ID).toString();
				List<Map<String, Object>> resultesMap = new ArrayList<>();
				resultesMap.add(objmainProps);
				relatedMap.put(relType, resultesMap);
				ArrayList<Object> children1 = (ArrayList<Object>) relObjMap.get("children");
				if (children1 != null) {
					formChildData(children1, relatedMap);
				}
			});
		}
		return relatedMap;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Map<String, Object>>> formChildData(ArrayList<Object> relationData,
			Map<String, List<Map<String, Object>>> relatedMap) {

		if (relationData != null) {
			relationData.forEach(relObj -> {

				LinkedHashMap<Object, Object> relObjMap = (LinkedHashMap<Object, Object>) relObj;
				LinkedHashMap<Object, Object> object1 = (LinkedHashMap<Object, Object>) relObjMap.get("object");
				LinkedHashMap<Object, Object> object2 = (LinkedHashMap<Object, Object>) object1.get("object");
				LinkedHashMap<Object, Object> succintProps = (LinkedHashMap<Object, Object>) object2
						.get("succinctProperties");
				JSONObject objmainProps = null;
				String relType = succintProps.get(PropertyIds.NAME).toString();
				if (relType.contains(",")) {
					objmainProps = formProperties(null, succintProps, false, false, true, null);
					relType = succintProps.get(PropertyIds.NAME).toString()
							.substring(succintProps.get(PropertyIds.NAME).toString().lastIndexOf(",") + 1);
				} else {
					objmainProps = formProperties(null, succintProps, false, true, true, null);
				}
				List<Map<String, Object>> resultesMap = new ArrayList<>();
				resultesMap.add(objmainProps);
				List<Map<String, Object>> listPrvs = relatedMap.get(relType);
				if (listPrvs == null) {
					listPrvs = new ArrayList<>();
				}
				listPrvs.addAll(resultesMap);
				ArrayList<Object> children1 = (ArrayList<Object>) relObjMap.get("children");
				if (children1 != null) {
					formChildData(children1, relatedMap);
				}
				relatedMap.put(relType, listPrvs);
			});
		}
		return relatedMap;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject formProperties(Session session, LinkedHashMap<Object, Object> succintProps,
			boolean conflicts, boolean mainObject, boolean descendants, LinkedHashMap<Object, Object> aclProps) {
		JSONObject newObj = new JSONObject();
		succintProps.entrySet().stream().forEach(map -> {
			String type = (String) succintProps.get(PropertyIds.OBJECT_TYPE_ID);
			String key = (String) map.getKey();
			if (isBaseType(type)) {
				if (key.equals(PropertyIds.NAME)) {
					String v = map.getValue().toString();
					newObj.put(key, succintProps.put(key,
							v.substring(0, v.lastIndexOf(",") == -1 ? v.length() : v.lastIndexOf(","))));
				} else {
					newObj.put(key, map.getValue());
					if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
						if (descendants) {
							newObj.put("id", map.getValue());
						} else {
							newObj.put("_id", map.getValue());
						}
					}
				}

			} else {
				if (!(key.equalsIgnoreCase(PropertyIds.NAME) || key.equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
						|| key.equalsIgnoreCase(PropertyIds.CREATED_BY) || key.equalsIgnoreCase(PropertyIds.PATH)
						|| key.equalsIgnoreCase(PropertyIds.DESCRIPTION)
						|| key.equalsIgnoreCase(PropertyIds.CHANGE_TOKEN)
						|| key.equalsIgnoreCase(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS)
						|| key.equalsIgnoreCase(PropertyIds.PARENT_ID) || key.equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
						|| key.equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)
						|| key.equalsIgnoreCase(PropertyIds.CREATION_DATE)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_LENGTH)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_FILE_NAME)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)
						|| key.equalsIgnoreCase(PropertyIds.CHECKIN_COMMENT)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_LABEL)
						|| key.equalsIgnoreCase(PropertyIds.IS_MAJOR_VERSION)
						|| key.equalsIgnoreCase(PropertyIds.IS_LATEST_VERSION)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_ID)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)
						|| key.equalsIgnoreCase("cmis:previousVersionObjectId")
						|| key.equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)
						|| key.equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)
						|| key.equalsIgnoreCase(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)
						|| key.equalsIgnoreCase(PropertyIds.IS_LATEST_MAJOR_VERSION)
						|| key.equalsIgnoreCase("includeForReplication") || key.equalsIgnoreCase("hasAttachments")
						|| key.equalsIgnoreCase(PropertyIds.IS_IMMUTABLE))) {
					if (key.equalsIgnoreCase("conflictRelationId")) {
						if (descendants) {
							newObj.put(key, (List<String>) map.getValue());
						}
					}
					if (mainObject) {
						if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							if (descendants) {
								newObj.put("id", map.getValue());
							} else {
								newObj.put("_id", map.getValue());
							}
						} else if (key.equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)) {
							newObj.put("type", map.getValue());
						}
					} else {
						if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							newObj.put("id", map.getValue());
						} else if (!key.equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)
								&& !key.equalsIgnoreCase("revisionId") && !key.equalsIgnoreCase("conflictRelationId")
								&& !key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							newObj.put(key, map.getValue());
						}
					}
				}
			}
		});
		// do we need to include conflicts for relation objects?
		if (session != null) {
			if (mainObject && aclProps != null) {
				List<Map<String, String>> aclList = new ArrayList<Map<String, String>>();
				List<Map<Object, Object>> acesList = (List<Map<Object, Object>>) aclProps.get("aces");
				if (acesList != null && acesList.size() > 0) {
					acesList.stream().forEach(ace -> {
						Map<Object, Object> principal = (HashMap<Object, Object>) ace.get("principal");
						List<String> permission = (List<String>) ace.get("permissions");
						Map<String, String> aceMap = new HashMap<String, String>();
						aceMap.put("principalId", principal.get("principalId").toString());
						aceMap.put("permission", permission.get(0));
						aclList.add(aceMap);
					});
					newObj.put("acl", aclList);
				}
			}
		}

		return newObj;
	}

	public static boolean isBaseType(String type) {
		try {
			BaseTypeId value = BaseTypeId.fromValue(type);
			if (value != null) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private static void createForRelationObjects(CmisObject outerObj, Map<String, List<Map<String, Object>>> jsonObject,
			Session session, Boolean isUpdate, List<Ace> aceList,
			Map<String, Map<String, List<Map<String, Object>>>> resultedMap) {

		// filter all keys which have "parent_child" format in their values and
		// add child objects, and create relation with their respective parents.
		// A recursive function
		if (resultedMap != null) {
			if (resultedMap.get(RelationType.DELETED.value()) != null
					&& !resultedMap.get(RelationType.DELETED.value()).isEmpty()) {
				deleteRelationFlow(session, resultedMap.get(RelationType.DELETED.value()));
			}
			if (resultedMap.get(RelationType.CREATED.value()) != null
					&& !resultedMap.get(RelationType.CREATED.value()).isEmpty()) {
				relationFlow(session, resultedMap.get(RelationType.CREATED.value()), RelationType.CREATED, aceList);
			}
			if (resultedMap.get(RelationType.UPDATED.value()) != null
					&& !resultedMap.get(RelationType.UPDATED.value()).isEmpty()) {
				relationFlow(session, resultedMap.get(RelationType.UPDATED.value()), RelationType.UPDATED, aceList);
			}
		} else {
			relationFlow(session, jsonObject, RelationType.CREATED, aceList);
		}
	}

	private static void deleteRelationFlow(Session session, Map<String, List<Map<String, Object>>> jsonObject) {
		List<String> deletedIds = new ArrayList<>();
		jsonObject.entrySet().stream().forEach(k -> {
			if (k.getValue() != null && !k.getValue().isEmpty()) {
				k.getValue().stream().forEach(obj -> {
					List<String> ids = new ArrayList<String>();
					ids.add(0, "");

					if (!deletedIds.contains(obj.get("id").toString())) {
						deleteObject(session, obj, ids, null, deletedIds, false);
						deletedIds.add(obj.get("id").toString());
					}
				});
			}
		});

	}

	private static void relationFlow(Session session, Map<String, List<Map<String, Object>>> jsonObject,
			RelationType type, List<Ace> aceList) {
		Map<String, Map<String, Object>> relationShip = new LinkedHashMap<>();
		Map<String, Map<String, Object>> conflictRelationShips = new LinkedHashMap<>();
		jsonObject.entrySet().stream().forEach(k -> {
			String relType = k.getKey();
			CmisObject relTypeItem = session.getObjectByPath("/cmis_ext:relationmd/" + relType);
			String targetType = relTypeItem.getPropertyValue("target_table");
			Map<String, Object> foreignKey = new HashMap<>();
			// TODO later using
			// if (relTypeItem.getPropertyValue("target_column") != null) {
			// foreignKey.put(relTypeItem.getPropertyValue("target_column"),
			// outerObj.getPropertyValue(relTypeItem.getPropertyValue("target_column")));
			// }
			if (k.getValue() != null && !k.getValue().isEmpty()) {
				k.getValue().stream().forEach(obj -> {
					Map<String, Object> sectionMap = (Map<String, Object>) obj;
					TypeDefinition targetTypeDef = session.getTypeDefinition(targetType);
					Map<String, Object> deprops = compileProperties(session, sectionMap, targetTypeDef, null, null);
					if (type.equals(RelationType.CREATED)) {
						createNewObject(session, deprops, targetTypeDef, foreignKey, aceList,
								obj.get("sourceParentId").toString(), relType, relationShip);
					} else if (type.equals(RelationType.UPDATED)) {
						updateRelationObject(session, deprops, sectionMap, targetTypeDef, aceList,
								obj.get("sourceParentId").toString(), relType, conflictRelationShips);
					}
				});
			}
		});
		if (!relationShip.isEmpty() && relationShip.size() > 0) {
			createNewRelationShipsObject(session, relationShip, aceList);
		}

	}

	private static void createNewObject(Session session, Map<String, Object> deprops, TypeDefinition targetTypeDef,
			Map<String, Object> foreignKey, List<Ace> aceList, String parentId, String relName,
			Map<String, Map<String, Object>> relationShip) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating new Relation Object with properties: {}", deprops);
			}
			CmisObject innerObject = createForMainObject(session, deprops, targetTypeDef, foreignKey, aceList);
			Map<String, Object> relationShipObjects = new HashMap<>();
			relationShipObjects.put("sourceId", parentId);
			relationShipObjects.put("targetId", innerObject.getId());
			relationShipObjects.put("relType", relName);
			relationShip.put(innerObject.getId(), relationShipObjects);
		} catch (Exception ex) {
			LOG.error("Error while creating new Relation Object, id: {}, Exception: {}",
					deprops.get(PropertyIds.OBJECT_ID), ex);
		}

	}

	private static void createNewRelationShipsObject(Session session, Map<String, Map<String, Object>> relationShip,
			List<Ace> aceList) {
		try {
			relationShip.entrySet().stream().forEach(k -> {
				if (k.getValue() != null && k.getValue().size() > 0) {
					createRelationship(session, k.getValue().get("sourceId").toString(),
							k.getValue().get("targetId").toString(), k.getValue().get("relType").toString(), aceList);
				}
			});
		} catch (Exception ex) {
			LOG.error("BulkDocs Error while creating relationships, Exception: {}", ex);
		}
	}

	private static void updateRelationObject(Session session, Map<String, Object> deprops,
			Map<String, Object> sectionMap, TypeDefinition targetTypeDef, List<Ace> aceList, String sourceId,
			String relName, Map<String, Map<String, Object>> conflictRelationShips) {
		try {
			CmisObject updateObj = session.getObject(deprops.get(PropertyIds.OBJECT_ID).toString());
			if (deprops.get("lastModifiedDate") != null && updateObj.getPropertyValue("lastModifiedDate") != null) {
				long lastModifiedDate = ((BigInteger) updateObj.getPropertyValue("lastModifiedDate")).longValue();
				BigInteger lmd = (BigInteger) deprops.get("lastModifiedDate");
				long latestModifiedDate = lmd.longValue();
				if (latestModifiedDate > lastModifiedDate) {
					updateObject(session, updateObj, targetTypeDef.getBaseTypeId().value(), deprops, null, aceList);
				}
			} else {
				LOG.info("UpdateRelationObject lastModifiedDate is null for this object: {}", updateObj.getId());
			}
		} catch (Exception ex) {
			LOG.error("Error in updateRelationObject for id: {}, Cause: {}", deprops.get(PropertyIds.OBJECT_ID), ex);
		}
	}

	private static Map<String, Object> compileProperties(Session session, Map<String, Object> jsonObject,
			TypeDefinition typeDef, Map<String, Object> foreignKey, Boolean hasAttachments) {
		try {
			// filter out all keys which have "parent_child" type format in
			// them,
			// and add as main objects
			Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
			Map<String, Object> props = jsonObject.entrySet().stream()
					.filter(a -> a.getValue() != null && !(p.matcher(a.getKey()).find()))
					.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));

			Object id = props.get("_id") != null ? props.get("_id")
					: props.get(PropertyIds.OBJECT_ID) != null ? (String) props.get(PropertyIds.OBJECT_ID)
							: props.get("id") != null ? props.get("id") : UUID.randomUUID().toString();
			if (id != null && typeDef != null) {

				Map<String, Object> propMapForMainObj = new HashMap<String, Object>();
				typeDef.getPropertyDefinitions().entrySet().stream()
						.filter(map -> (!(map.getKey().equalsIgnoreCase("cmis:name")
								|| map.getKey().equalsIgnoreCase("cmis:objectTypeId")
								|| map.getKey().equalsIgnoreCase("cmis:objectId"))))
						.forEach(a -> {
							if (props.get(a.getKey()) != null) {
								propMapForMainObj.put(a.getKey(), props.get(a.getKey()));
							}
						});
				propMapForMainObj.put(PropertyIds.OBJECT_TYPE_ID, typeDef.getId());

				propMapForMainObj.putAll(props.entrySet().stream()
						.filter(obj -> (obj.getKey().equals(PropertyIds.OBJECT_ID) || obj.getKey().equals("id")
								|| obj.getKey().equals("_id")))
						.collect(Collectors.toMap(a -> PropertyIds.OBJECT_ID, b -> String.valueOf(b.getValue()),
								(p1, p2) -> p2)));

				String name = props.get(PropertyIds.NAME) != null ? (String) props.get(PropertyIds.NAME)
						: propMapForMainObj.get(PropertyIds.OBJECT_ID) != null
								? (String) propMapForMainObj.get(PropertyIds.OBJECT_ID)
								: (String) id;

				propMapForMainObj.put(PropertyIds.NAME, name);

				// add foreignKey for relationships
				if (foreignKey != null) {
					propMapForMainObj.putAll(foreignKey);
				}

				// add secondaryTypeIds

				// check for attachments
				if (hasAttachments != null) {
					propMapForMainObj.put("hasAttachments", hasAttachments);
				}

				Map<String, Object> deprops = deserializeInput(propMapForMainObj, (ObjectType) typeDef, session);
				if (LOG.isDebugEnabled()) {
					LOG.debug("CompileProperties: {}", deprops);
				}
				return deprops;
			} else {
				// throw new Exception("Id should not be null");
			}
		} catch (Exception ex) {
			LOG.error("BulkDocs Error in compiling properties, Exception: {}", ex);
		}
		return null;
	}

	private static CmisObject updateObject(Session tokenSession, CmisObject obj, String type,
			Map<String, Object> properties, Map<String, Object> attachmentProps, List<Ace> aceList) throws Exception {

		LOG.info("Updating object: {}", obj.getId());
		properties.remove(PropertyIds.NAME);
		properties.remove(PropertyIds.OBJECT_ID);
		properties.remove(PropertyIds.OBJECT_TYPE_ID);
		obj.updateProperties(properties);
		CmisObject updatedObj = tokenSession.getObject(obj.getId());
		// update attachment for document
		if (attachmentProps != null && !attachmentProps.isEmpty()) {
			addAttachments(tokenSession, attachmentProps, null, updatedObj.getId(), null, true);
		}
		// update acl
		if (aceList != null && !aceList.isEmpty()) {
			tokenSession.applyAcl(new ObjectIdImpl(updatedObj.getId()), aceList, null, AclPropagation.OBJECTONLY);
		}
		return updatedObj;

	}

	private static void deleteObject(Session tokenSession, Map<String, Object> obj, List<String> ids,
			Map<String, Object> properties, List<String> deletedIds, boolean forMainObject) {

	}

	private static void createRelationship(Session session, String sourceId, String targetId, String objectTypeId,
			List<Ace> aceList) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating relationship for source: {}, target: {}", sourceId, targetId);
		}
		Map<String, Object> relProps = new HashMap<String, Object>();
		relProps.put(PropertyIds.SOURCE_ID, sourceId);
		relProps.put(PropertyIds.TARGET_ID, targetId);
		relProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis_ext:relationship");
		relProps.put("relation_name", objectTypeId);
		relProps.put(PropertyIds.NAME, sourceId + "_" + targetId);

		session.createRelationship(relProps, null, aceList, null);
	}

	private static CmisObject createForMainObject(Session session, Map<String, Object> deprops, TypeDefinition typeDef,
			Map<String, Object> foreignKey, List<Ace> aceList) throws Exception {

		String parentId = deprops.get("parentId") == null ? null : deprops.get("parentId").toString();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating new Object with properties: {}", deprops);
		}
		CmisObject mainObj = createForBaseTypes(session, typeDef.getBaseTypeId(), parentId, deprops, null, aceList);
		return mainObj;
	}

	@SuppressWarnings("unchecked")
	private static void addAttachments(Session tokenSession, Map<String, Object> attachmentProps, List<Ace> aceList,
			String mainObjId, CmisObject attachFolder, boolean forUpdate) throws Exception {
		if (attachmentProps != null && mainObjId != null) {

			if (attachFolder != null) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding attachments for objectId: {}", mainObjId);
				}
				attachmentProps.forEach((filename, attachment) -> {
					HashMap<String, Object> attachData = (HashMap<String, Object>) attachment;
					String contentType = (String) attachData.get("content_type");
					String data = (String) attachData.get("data");
					InputStream inputStream = new org.apache.commons.io.input.ReaderInputStream(new StringReader(data),
							StandardCharsets.UTF_8);
					ContentStream stream = new ContentStreamImpl(filename, BigInteger.valueOf(data.length()),
							contentType, inputStream);

					Map<String, Object> aprops = new HashMap<>();
					aprops.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
					aprops.put(PropertyIds.NAME, filename);

					try {
						CmisObject attachmentObj = createForBaseTypes(tokenSession, BaseTypeId.CMIS_DOCUMENT,
								attachFolder.getId(), aprops, stream, aceList);
					} catch (Exception ex) {
						LOG.error("Error in creating attachment document, error: {}", ex);
					}
				});
			} else {
				CmisObject attachFol = null;
				if (forUpdate) {
					attachFol = tokenSession.getObjectByPath("/" + mainObjId + "_attachments");
				} else {
					// folder not present , create new
					Map<String, Object> fprops = new HashMap<>();
					fprops.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
					fprops.put(PropertyIds.NAME, mainObjId + "_attachments");
					attachFol = createForBaseTypes(tokenSession, BaseTypeId.CMIS_FOLDER, null, fprops, null, aceList);
				}
				addAttachments(tokenSession, attachmentProps, aceList, mainObjId, attachFol, forUpdate);
			}
		}

	}

	private static void deleteAttachments(Session tokenSession, String id) {
		try {
			Folder folderAttachment = (Folder) tokenSession.getObjectByPath("/" + id + "_attachments");
			tokenSession.deleteTree(new ObjectIdImpl(folderAttachment.getId()), true, null, true);
		} catch (Exception ex) {
			// folder doesnt exist, do nothing
		}
	}

	public static CmisObject createForBaseTypes(Session session, BaseTypeId baseTypeId, String parentId,
			Map<String, Object> input, ContentStream stream, List<Ace> aceList) throws Exception {
		try {
			LOG.info("BaseTypeID:{}", baseTypeId.value());
			if (baseTypeId.equals(BaseTypeId.CMIS_FOLDER)) {
				CmisObject fol = null;
				if (parentId != null) {
					fol = ((Folder) session.getObject(parentId)).createFolder(input);
					return fol;
				} else {
					ObjectId id = session.getRootFolder().createFolder(input);
					fol = session.getObject(id);
					return fol;
				}
			} else if (baseTypeId.equals(BaseTypeId.CMIS_DOCUMENT)) {
				CmisObject doc = null;
				if (parentId != null) {
					doc = ((Folder) session.getObject(parentId)).createDocument(input, stream != null ? stream : null,
							null);
					return doc;
				} else {
					ObjectId id = session.createDocument(input, null, stream != null ? stream : null, null);
					doc = session.getObject(id);
					return doc;
				}
			} else if (baseTypeId.equals(BaseTypeId.CMIS_ITEM)) {
				CmisObject item = null;
				if (parentId != null) {
					item = ((Folder) session.getObject(parentId)).createItem(input);
					return item;
				} else {
					ObjectId id = session.createItem(input, null);
					item = session.getObject(id);
					return item;
				}
			} else if (baseTypeId.equals(BaseTypeId.CMIS_RELATIONSHIP)) {
				ObjectId id = session.createRelationship(input);
				CmisObject rel = session.getObject(id);
				return rel;
			} else if (baseTypeId.equals(BaseTypeId.CMIS_POLICY)) {
				CmisObject policy = null;
				if (parentId != null) {
					policy = ((Folder) session.getObject(parentId)).createPolicy(input);
					return policy;
				} else {
					ObjectId polId = session.createPolicy(input, null);
					policy = session.getObject(polId);
					return policy;
				}
			} else if (baseTypeId.equals(BaseTypeId.CMIS_SECONDARY)) {
				return null;
			}
		} catch (Exception e) {
			ErrorResponse resp = SwaggerHelpers.handleException(e);
			throw new ErrorResponse(resp);
		}
		return null;
	}

	public static Map<String, Object> compileProperties(CmisObject cmisObj, Session session) throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		cmisObj.getProperties().stream().forEach(a -> {
			propMap.put(a.getDefinition().getId(), a.getValues());
		});
		Map<String, Object> outputMap = deserializeInputForResponse(propMap, cmisObj.getType(), session);

		return outputMap;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> deserializeInputForResponse(Map<String, Object> input, ObjectType obj,
			Session session) throws Exception {
		LOG.info("deSerializing Input:{}", input);
		Map<String, Object> serializeMap = new HashMap<String, Object>();
		Map<String, PropertyDefinition<?>> dataPropDef = obj.getPropertyDefinitions();
		for (String var : input.keySet()) {
			List<?> valueOfType = (List<?>) input.get(var);

			if (var.equals("parentId")) {
				continue;
			}
			if (valueOfType != null) {
				PropertyType reqPropType = null;
				PropertyDefinition<?> defObj = dataPropDef.get(var);
				if (defObj == null) {

					List<?> secondaryValues = (List<?>) input.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
					for (Object stype : secondaryValues) {
						TypeDefinition type = session.getTypeDefinition((String) stype);
						for (Entry<String, PropertyDefinition<?>> t : type.getPropertyDefinitions().entrySet()) {
							if (t.getValue().getId().equals(var)) {
								reqPropType = t.getValue().getPropertyType();
							}
						}
					}

				} else {
					reqPropType = defObj.getPropertyType();
				}

				if (reqPropType.equals(PropertyType.INTEGER)) {
					if (valueOfType.size() == 1) {
						Integer valueBigInteger = convertInstanceOfObject(valueOfType.get(0), Integer.class);
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
		LOG.info("serializedMap:{}", serializeMap);
		return serializeMap;
	}

	public static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
		try {
			return clazz.cast(o);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void crudOperation(Session session, String repositoryId, ObjectType typeDef, String input,
			String userName, String password) throws JSONParseException {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(input);
			JSONArray repoArray = (JSONArray) obj;
			System.out.println(repoArray);
			for (Object object : repoArray) {
				Map<String, Object> props = (Map<String, Object>) object;
				Map<String, List<Map<String, Object>>> relatedMap = new LinkedHashMap<>();
				getMapValue(props, relatedMap);
				boolean isDeleted = false;
				Map<String, Object> attachmentProps = new HashMap<String, Object>();
				String type = null;
				List<Object> newAclArray = new ArrayList<>();
				List<Object> oldAclArray = new ArrayList<>();
				if (props.containsKey("_deleted")) {
					isDeleted = (boolean) props.get("_deleted");
				} else {
					type = props.entrySet().stream()
							.filter(objData -> ((objData.getKey().equals(PropertyIds.OBJECT_TYPE_ID)
									|| objData.getKey().equals("objectTypeId") || objData.getKey().equals("type"))))
							.map(a -> (String) a.getValue()).findFirst().get();
					typeDef = SwaggerHelpers.getType(type);
				}
				if (typeDef != null && props.get("acl") != null) {
					newAclArray = (ArrayList<Object>) props.get("acl");
					props.remove("acl");
				}
				Map<String, Object> properties = compileCrudProperties(session, props, typeDef);

				String id = isDeleted == true ? (String) props.get("_id")
						: (String) properties.get(PropertyIds.OBJECT_ID);

				try {
					ArrayList<Object> relationData = getDescendantsForRelationObjects(userName, password, repositoryId,
							id);
					if (relationData != null) {
						Map<String, List<Map<String, Object>>> resultedObject = formMapData(session, relationData);
						Map<String, Map<String, List<Map<String, Object>>>> resultedInnerMap = checkingMap(relatedMap,
								resultedObject);
						if (isDeleted) {
							if (resultedObject.get(id) != null && resultedObject.get(id).size() > 0) {
								Map<String, Object> resultObjectProps = resultedObject.get(id).get(0);
								// deleteObject(tokenSession, resultObjectProps, ids, conflictRevIds,
								// properties,
								// new ArrayList<>(), true, revisionId);
							}
						} else {
							// acl compare
							List<Ace> aceList = new ArrayList<Ace>();
							if (resultedObject.get(id) != null && resultedObject.get(id).size() > 0) {
								Map<String, Object> resultObjectProps = resultedObject.get(id).get(0);
								oldAclArray = (List<Object>) resultObjectProps.get("acl");
								if (newAclArray != null && newAclArray.size() > 0) {
									if (oldAclArray.containsAll(newAclArray)) {
										aceList = null;
									} else {
										aceList.addAll(formAceList(newAclArray));
									}
								}
							}
							CmisObject objectToUpdate = session.getObject(id);
							CmisObject updatedObj = updateObject(session, objectToUpdate,
									typeDef.getBaseTypeId().value(), properties, null, aceList);
							createForRelationObjects(updatedObj, relatedMap, session, true, aceList, resultedInnerMap);

						}
					} else {
						if (isDeleted) {
							// do nothing, means deleted in our side as well
						} else {
							List<Ace> aceList = new ArrayList<Ace>();
							if (newAclArray != null && newAclArray.size() > 0) {
								aceList.addAll(formAceList(newAclArray));
							}
							CmisObject outerObj = createForMainObject(session, properties, typeDef, null, aceList);
							createForRelationObjects(outerObj, relatedMap, session, false, aceList, null);
							// add attachments here to main object make
							// folder with name objId_attachments, for each
							// attachment add a document
							if (!attachmentProps.isEmpty()) {
								addAttachments(session, attachmentProps, aceList, outerObj.getId(), null, false);
							}
						}
					}

				} catch (Exception ex) {
					LOG.error("BulkDocs Error in repoId: {}, Exception: {}", repositoryId, ex);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static Map<String, Object> compileCrudProperties(Session session, Map<String, Object> jsonObject,
			TypeDefinition typeDef) {
		try {
			// filter out all keys which have "parent_child" type format in
			// them,
			// and add as main objects
			Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
			Map<String, Object> props = jsonObject.entrySet().stream()
					.filter(a -> a.getValue() != null && !(p.matcher(a.getKey()).find()))
					.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));

			Object id = props.get("_id") != null ? props.get("_id")
					: props.get(PropertyIds.OBJECT_ID) != null ? (String) props.get(PropertyIds.OBJECT_ID)
							: props.get("id") != null ? props.get("id") : UUID.randomUUID().toString();
			if (id != null && typeDef != null) {

				Map<String, Object> propMapForMainObj = new HashMap<String, Object>();
				typeDef.getPropertyDefinitions().entrySet().stream()
						.filter(map -> (!(map.getKey().equalsIgnoreCase("cmis:name")
								|| map.getKey().equalsIgnoreCase("cmis:objectTypeId")
								|| map.getKey().equalsIgnoreCase("cmis:objectId"))))
						.forEach(a -> {
							if (props.get(a.getKey()) != null) {
								propMapForMainObj.put(a.getKey(), props.get(a.getKey()));
							}
						});
				propMapForMainObj.put(PropertyIds.OBJECT_TYPE_ID, typeDef.getId());

				propMapForMainObj.putAll(props.entrySet().stream()
						.filter(obj -> (obj.getKey().equals(PropertyIds.OBJECT_ID) || obj.getKey().equals("id")
								|| obj.getKey().equals("_id")))
						.collect(Collectors.toMap(a -> PropertyIds.OBJECT_ID, b -> String.valueOf(b.getValue()),
								(p1, p2) -> p2)));

				String name = props.get(PropertyIds.NAME) != null ? (String) props.get(PropertyIds.NAME)
						: propMapForMainObj.get(PropertyIds.OBJECT_ID) != null
								? (String) propMapForMainObj.get(PropertyIds.OBJECT_ID)
								: (String) id;

				propMapForMainObj.put(PropertyIds.NAME, name);

				Map<String, Object> deprops = deserializeInput(propMapForMainObj, (ObjectType) typeDef, session);
				if (LOG.isDebugEnabled()) {
					LOG.debug("CompileProperties: {}", deprops);
				}
				return deprops;
			} else {
				// throw new Exception("Id should not be null");
			}
		} catch (Exception ex) {
			LOG.error("BulkDocs Error in compiling properties, Exception: {}", ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Ace> formAceList(List<Object> newAclArray) {
		List<Ace> aceList = new ArrayList<Ace>();
		newAclArray.forEach((aclObject) -> {
			Map<String, Object> aclProps = (Map<String, Object>) aclObject;
			AccessControlEntryImpl ace = new AccessControlEntryImpl(
					new AccessControlPrincipalDataImpl(aclProps.get("principalId").toString()),
					Arrays.asList(aclProps.get("permission").toString()));
			aceList.add(ace);
		});
		return aceList;
	}

	@SuppressWarnings("unused")
	private static Map<String, Map<String, List<Map<String, Object>>>> checkingMap(
			Map<String, List<Map<String, Object>>> beforeMap, Map<String, List<Map<String, Object>>> afterMap) {
		Map<String, Map<String, List<Map<String, Object>>>> resultedMapValues = new HashMap<>();
		Map<String, List<Map<String, Object>>> updated = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> deleted = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> created = new LinkedHashMap<>();
		beforeMap.entrySet().stream().forEach(k -> {
			if (afterMap.get(k.getKey()) != null) {
				List<Map<String, Object>> afterMapValue = afterMap.get(k.getKey());
				List<Map<String, Object>> updatedChildValue = new ArrayList<>();
				List<Map<String, Object>> DeletedChildValue = new ArrayList<>();
				List<Map<String, Object>> createdChildValue = new ArrayList<>();
				afterMapValue.forEach(childValue -> {
					Map<String, Boolean> childDeletedValue = new HashMap<>();
					childDeletedValue.put("isDeleted", true);
					k.getValue().forEach(afterChildValue -> {
						if (afterChildValue.get("id").toString().equals(childValue.get("id").toString())) {
							updatedChildValue.add(afterChildValue);
							childDeletedValue.remove("isDeleted");
							childDeletedValue.put("isDeleted", false);
						}
					});
					if (childDeletedValue.get("isDeleted")) {
						DeletedChildValue.add(childValue);
					}
				});

				k.getValue().forEach(childValue -> {
					Map<String, Boolean> childCreatedValue = new HashMap<>();
					childCreatedValue.put("isCreated", true);
					afterMapValue.forEach(afterChildValue -> {
						if (afterChildValue.get("id").toString().equals(childValue.get("id").toString())) {
							childCreatedValue.put("isCreated", false);
						}
					});
					if (childCreatedValue.get("isCreated")) {
						createdChildValue.add(childValue);
					}
				});
				updated.put(k.getKey(), updatedChildValue);
				deleted.put(k.getKey(), DeletedChildValue);
				created.put(k.getKey(), createdChildValue);
			} else {
				created.put(k.getKey(), k.getValue());
			}
		});
		resultedMapValues.put(RelationType.CREATED.value(), created);
		resultedMapValues.put(RelationType.UPDATED.value(), updated);
		Map<String, List<Map<String, Object>>> deletedData = deleted.entrySet().stream()
				.filter(t -> t.getValue().size() > 0).collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
		Set<String> removedKeys = new HashSet<String>(afterMap.keySet());
		removedKeys.removeAll(beforeMap.keySet());
		Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
		removedKeys.forEach(k -> {
			if (p.matcher(k).find()) {
				getKeyDeletedObject(k, afterMap, deletedData);
			}
		});
		Set<String> RelationName = deletedData.keySet();
		List<String> deletedObjects = new ArrayList<>();
		// RelationName.forEach(t -> {
		// removeChild(t, deletedData, deletedObjects);
		// });
		if (deletedObjects.size() > 0) {
			deletedObjects.forEach(t -> {
				if (deletedData.containsKey(t)) {
					deletedData.remove(t);
				}

			});
		}

		resultedMapValues.put(RelationType.DELETED.value(), deletedData);
		return resultedMapValues;
	}

	private static Map<String, List<Map<String, Object>>> getKeyDeletedObject(String key,
			Map<String, List<Map<String, Object>>> afterMap, Map<String, List<Map<String, Object>>> deleted) {
		List<Map<String, Object>> object = afterMap.get(key);
		if (object.size() > 0) {
			deleted.put(key, object);
		}
		return deleted;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> deserializeInput(Map<String, Object> input, ObjectType obj, Session session)
			throws Exception {

		Map<String, Object> serializeMap = new HashMap<String, Object>();
		Map<String, PropertyDefinition<?>> dataPropDef = obj.getPropertyDefinitions();
		for (String var : input.keySet()) {
			Object valueOfType = input.get(var);
			if (var.equals("parentId")) {
				continue;
			}
			if (valueOfType != null) {
				PropertyType reqPropType = null;
				PropertyDefinition<?> defObj = dataPropDef.get(var);
				if (defObj == null) {
					List<?> secondaryValues = (List<?>) input.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
					if (secondaryValues != null && !secondaryValues.isEmpty()) {
						for (Object stype : secondaryValues) {
							TypeDefinition type = session.getTypeDefinition((String) stype);
							for (Entry<String, PropertyDefinition<?>> t : type.getPropertyDefinitions().entrySet()) {
								if (t.getValue().getId().equals(var)) {
									reqPropType = t.getValue().getPropertyType();
								}
							}
						}
					}

				} else {
					reqPropType = defObj.getPropertyType();
				}
				if (reqPropType.equals(PropertyType.INTEGER)) {
					if (valueOfType instanceof Integer || valueOfType instanceof BigInteger) {
						BigInteger valueBigInteger = convertInstanceOfObject(valueOfType, BigInteger.class);
						Integer value;
						if (valueBigInteger == null) {
							value = convertInstanceOfObject(valueOfType, Integer.class);
						} else {
							value = valueBigInteger.intValue();
						}
						serializeMap.put(var, BigInteger.valueOf(value));
					} else if (valueOfType instanceof List<?>) {
						List<BigInteger> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}

				} else if (reqPropType.equals(PropertyType.BOOLEAN)) {
					if (valueOfType instanceof Boolean) {
						Boolean booleanValue = convertInstanceOfObject(valueOfType, Boolean.class);
						serializeMap.put(var, booleanValue);
					} else if (valueOfType instanceof List<?>) {
						List<Boolean> booleanValue = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, booleanValue);
					}

				} else if (reqPropType.equals(PropertyType.DATETIME)) {

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
					}

				} else if (reqPropType.equals(PropertyType.DECIMAL)) {
					if (valueOfType instanceof Double) {
						Double value = convertInstanceOfObject(valueOfType, Double.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof List<?>) {
						List<BigDecimal> value = convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
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
		return serializeMap;
	}
}
