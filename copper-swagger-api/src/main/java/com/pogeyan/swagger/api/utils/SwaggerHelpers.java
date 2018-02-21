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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
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
	 *            the property typeId is used to get that particular type based
	 *            on this id.
	 * @return the Object Type
	 */
	public static ObjectType getType(String typeId) {
		return typeCacheMap.getIfPresent(typeId);
	}

	public static List<FileableCmisObject> getRelationshipType(Session session, String typeId) {
		ObjectType relationshipType = typeCacheMap.getIfPresent("cmis:relation_ext");
		if (relationshipType != null) {
			Folder relationObject = (Folder) session.getObjectByPath("/" + relationshipType.getId());
			if (relationObject != null) {
				List<Tree<FileableCmisObject>> relationDescendants = relationObject.getDescendants(-1);
				if (relationDescendants.size() > 0) {
					List<FileableCmisObject> relationchildObject = relationDescendants.stream()
							.filter(t -> checkSourceDetails(t.getItem().getProperties(), typeId)).map(t -> t.getItem())
							.collect(Collectors.toList());
					return relationchildObject;
				}
			}
		}
		return null;

	}

	private static boolean checkSourceDetails(List<Property<?>> list, String typeId) {
		Property<?> propertyDef = list.stream()
				.filter(t -> t.getId().equals("source_table") && t.getFirstValue().equals(typeId)).findFirst()
				.orElse(null);
		if (propertyDef != null) {
			return true;
		}
		return false;
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
	 *         Operation Object. It is not mandatory to have a Tag Object per
	 *         tag defined in the Operation Object instances. List of tags used
	 *         by the specification with additional metadata. The order of the
	 *         tags can be used to reflect on their order by the parsing tools.
	 *         Not all tags that are used by the Operation Object must be
	 *         declared. The tags that are not declared MAY be organized
	 *         randomly or based on the tools' logic. Each tag name in the list
	 *         MUST be unique.
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
	 *         security scheme that can be used by the operations. Supported
	 *         schemes are HTTP authentication, an API key (either as a header
	 *         or as a query parameter), OAuth2's common flows (implicit,
	 *         password, application and access code) as defined in RFC6749, and
	 *         OpenID Connect Discovery
	 */
	public static Map<String, SecurityDefinitionObject> getSecurityDefinitions() {
		Map<String, SecurityDefinitionObject> security = new HashMap<String, SecurityDefinitionObject>();

		SecurityDefinitionObject basicAuth = new SecurityDefinitionObject("basic", null, null, null, null, null);
		security.put("BasicAuth", basicAuth);
		LOG.debug("security:{}", security.toString());
		return security;

	}

	/**
	 * @return it will Map<String, DefinitionsObject> defines it will store each
	 *         and every request schema definitions for all ObjectType data
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
	 *         ParameterObject,ResponseObject,PathCommonObject finally it will
	 *         club into one object as PathObject and returns. ParameterObject
	 *         describes a single operation parameter. A unique parameter is
	 *         defined by a combination of a name and location. ResponseObject
	 *         describes a single response from an API Operation, including
	 *         design-time, static links to operations based on the response.
	 *         PathCommonObject describes the operations available on a single
	 *         path. A Path Item MAY be empty, due to ACL constraints. The path
	 *         itself is still exposed to the documentation viewer but they will
	 *         not know which operations and parameters are available.
	 *         PathObject holds the relative paths to the individual end points
	 *         and their operations. The path is appended to the URL from the
	 *         Server Object in order to construct the full URL. The Paths MAY
	 *         be empty, due to ACL constraints
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
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations.
	 *         The path is appended to the URL from the Server Object in order
	 *         to construct the full URL. The Paths MAY be empty, due to ACL
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
	 *            the property typeResponse it will give map of ResponseObject.
	 *            it describes a single response from an API Operation,
	 *            including design-time, static links to operations based on the
	 *            response.
	 * @param security
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations.
	 *         The path is appended to the URL from the Server Object in order
	 *         to construct the full URL. The Paths MAY be empty, due to ACL
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
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject.
	 *            it describes a single operation parameter. A unique parameter
	 *            is defined by a combination of a name and location.
	 * @return PathCommonObject describes the operations available on a single
	 *         path. A Path Item MAY be empty, due to ACL constraints. The path
	 *         itself is still exposed to the documentation viewer but they will
	 *         not know which operations and parameters are available
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
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject.
	 *            it describes a single operation parameter. A unique parameter
	 *            is defined by a combination of a name and location.
	 * @param typeResponses
	 *            the property typeResponse it will give map of ResponseObject.
	 *            it describes a single response from an API Operation,
	 *            including design-time, static links to operations based on the
	 *            response.
	 * @return PathCommonObject is used to do the operations available on a
	 *         single path. A Path Item MAY be empty, due to ACL constraints.
	 *         The path itself is still exposed to the documentation viewer but
	 *         they will not know which operations and parameters are available
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
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @param typeParams
	 *            the property typeParams is used to get that ParameterObject.
	 *            it describes a single operation parameter. A unique parameter
	 *            is defined by a combination of a name and location.
	 * @return PathCommonObject is used to do the operations available on a
	 *         single path. A Path Item MAY be empty, due to ACL constraints.
	 *         The path itself is still exposed to the documentation viewer but
	 *         they will not know which operations and parameters are available
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
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual endpoints and their operations.
	 *         The path is appended to the URL from the Server Object in order
	 *         to construct the full URL. The Paths MAY be empty, due to ACL
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
	 *            the property aclResponses it will give map of ResponseObject.
	 *            it describes a single response from an API Operation,
	 *            including design-time, static links to operations based on the
	 *            response.
	 * @param security
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual end points and their operations.
	 *         The path is appended to the URL from the Server Object in order
	 *         to construct the full URL. The Paths MAY be empty, due to ACL
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
	 *            the property aclResponses it will give map of ResponseObject.
	 *            it describes a single response from an API Operation,
	 *            including design-time, static links to operations based on the
	 *            response.
	 * @param security
	 *            the property security is used to defines a security scheme
	 *            that can be used by the operations.
	 * @return it will return Map<String, PathObject> PathObject it holds the
	 *         relative paths to the individual end points and their operations.
	 *         The path is appended to the URL from the Server Object in order
	 *         to construct the full URL. The Paths MAY be empty, due to ACL
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
	 * @return list of parameterObject is used to do a single operation
	 *         parameter. A unique parameter is defined by a combination of a
	 *         name and location
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
			if (propertiesValues.getValue().getLocalName().equals("primaryKey")
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
				if (propertiesValues.getValue().getLocalName().equals("primaryKey")) {
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
	 * @return InfoObject used provides metadata about the API. The metadata MAY
	 *         be used by the clients if needed, and MAY be presented in editing
	 *         or documentation generation tools for convenience
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
	 *            A short description of the application. CommonMark syntax MAY
	 *            be used for rich text representation.
	 * @param version
	 *            The version of the OpenAPI document (which is distinct from
	 *            the OpenAPI Specification version or the API implementation
	 *            version).
	 * @param title
	 *            The title of the application.
	 * @param termsOfService
	 *            A URL to the Terms of Service for the API. MUST be in the
	 *            format of a URL.
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
	 *            A short description of the target documentation. CommonMark
	 *            syntax MAY be used for rich text representation.
	 * @param url
	 *            The URL for the target documentation. Value MUST be in the
	 *            format of a URL.
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

	public static void setHostSwaggerUrl(String swaggerServerUrl) {
		hostSwaggerUrl = swaggerServerUrl;
	}

	public static String getHostSwaggerUrl() {
		return hostSwaggerUrl;

	}
}
