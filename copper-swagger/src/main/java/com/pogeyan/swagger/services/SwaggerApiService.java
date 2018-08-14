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
package com.pogeyan.swagger.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.pogeyan.swagger.api.utils.MimeUtils;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.factory.SwaggerApiServiceFactory;
import com.pogeyan.swagger.pojos.ErrorResponse;

/**
 * SwaggerApiService Operations
 *
 */
/**
 * @author Vani
 *
 */
@SuppressWarnings("unused")
public class SwaggerApiService {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerApiService.class);

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param parentId
	 *            the property parentId is used to get the object-type�s
	 *            immediate parent type.
	 * @param input
	 *            the property input is used to get all request parameters.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @param pathFragments
	 *            the property pathFragments is used to get request path
	 *            parameters.
	 * @param filePart
	 *            the property filePart is used to get file details from
	 *            request.
	 * @return response object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> invokePostMethod(String repositoryId, String typeId, String parentId,
			Map<String, Object> input, String userName, String password, String[] pathFragments, Part filePart,
			String relation) throws Exception {
		CmisObject cmisObj = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObj = SwaggerHelpers.getType(typeId);
		OperationContext context = new OperationContextImpl();
		List<Map<String, Object>> relationObjectArray = new ArrayList<Map<String, Object>>();
		if (typeObj == null) {
			SwaggerHelpers.getAllTypes(session);
			typeObj = SwaggerHelpers.getType(typeId);
		}
		if (pathFragments.length > 2 && pathFragments[2] != null) {
			String idName = SwaggerHelpers.getIdName(typeObj);
			String customId = null;
			if (SwaggerHelpers.customTypeHasFolder()) {
				customId = typeObj.isBaseType() ? pathFragments[2] : typeId + "::" + idName + "::" + pathFragments[2];
			} else {
				customId = pathFragments[2];
			}
			Document doc = ((Document) session.getObject(customId)).setContentStream(getContentStream(filePart), true);

			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
					properties, doc.getPropertyValue("revisionId"));
			if (updateProperties != null) {
				CmisObject newObj = doc.updateProperties(updateProperties);
			}
			Map<String, Object> propMap = compileProperties(doc, session);
			LOG.info("customId: {}, properties: {}", customId, propMap);
			return propMap;
		} else {
			ContentStream setContentStream = getContentStream(filePart);
			// baseType
			if (typeObj != null) {
				Map<String, Object> propMap = createObject(input, typeObj, session, parentId, setContentStream);
				LOG.info("objectType: {}, properties: {}", typeObj.getId(), propMap);
				if (relation != null) {
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(relation);
					JSONArray jsonObject = (JSONArray) obj;
					LOG.info("relation: {}, properties: {}", relation, obj);
					for (Object ob : jsonObject) {
						Map<String, Object> relationObject = (Map<String, Object>) ob;
						String targetTypeId = relationObject.get("cmis:objectTypeId").toString();
						String sourceTypeId = propMap.get("cmis:objectTypeId").toString();
						String relationShipName = sourceTypeId + "_" + targetTypeId;
						LOG.info("relationShipName: {}", relationShipName);
						ObjectType targetObj = SwaggerHelpers.getType(targetTypeId);
						Map<String, Object> relationPropMap = createObject(relationObject, targetObj, session, parentId,
								setContentStream);
						LOG.info("objectType: {}, properties: {}", targetObj.getId(), relationPropMap);
						context.setFilterString("cmis:name,cmis:name eq " + relationShipName);
						ItemIterable<CmisObject> relationTargetObject = ((Folder) session
								.getObjectByPath("/cmis_ext:relationmd")).getChildren(context);
						for (CmisObject targetObject : relationTargetObject) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("cmis:objectTypeId", "cmis_ext:relationship");
							map.put("cmis:sourceId", propMap.get("cmis:objectId"));
							map.put("cmis:targetId", relationPropMap.get("cmis:objectId"));
							map.put("cmis:name", relationShipName + "_" + propMap.get("cmis:objectId") + "_"
									+ relationPropMap.get("cmis:objectId"));
							map.put("relation_name", relationShipName);
							session.createRelationship(map);
							relationObjectArray.add(relationPropMap);
						}
					}
					propMap.put("relations", relationObjectArray);
					return propMap;
				} else {
					return propMap;
				}
			}
			if (typeObj == null) {
				LOG.error("objectType: {}, repositoryId: {}", typeObj, repositoryId);
			}
		}
		return null;
	}

	private static Map<String, Object> createObject(Map<String, Object> input, ObjectType typeObj, Session session,
			String parentId, ContentStream setContentStream) throws Exception {
		Map<String, Object> propMap = null;
		Map<String, Object> serializeMap = deserializeInput(input, typeObj, session);
		BaseTypeId baseTypeId = typeObj.isBaseType() ? typeObj.getBaseTypeId() : typeObj.getBaseType().getBaseTypeId();
		Map<String, Object> properties = SwaggerApiServiceFactory.getApiService().beforecreate(session, serializeMap);
		CmisObject cmisObj = createForBaseTypes(session, baseTypeId, parentId, properties, setContentStream);
		propMap = compileProperties(cmisObj, session);
		return propMap;
	}

	private static Map<String, Object> compileProperties(Object obj, Session session) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> deserializeInput(Map<String, Object> input, ObjectType obj, Session session)
			throws Exception {
		LOG.info("deSerializing Input:{}", input);
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
					if (valueOfType instanceof Integer) {
						Integer valueBigInteger = convertInstanceOfObject(valueOfType, Integer.class);
						serializeMap.put(var, valueBigInteger);
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
		LOG.info("serializedMap:{}", serializeMap);
		return serializeMap;
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

	private static Map<String, Object> compileProperties(CmisObject cmisObj, Session session) throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		cmisObj.getProperties().stream().forEach(a -> {
			propMap.put(a.getDefinition().getId(), a.getValues());
		});
		Map<String, Object> outputMap = deserializeInputForResponse(propMap, cmisObj.getType(), session);

		return outputMap;
	}

	private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
		try {
			return clazz.cast(o);
		} catch (ClassCastException e) {
			return null;
		}
	}

	private static CmisObject createForBaseTypes(Session session, BaseTypeId baseTypeId, String parentId,
			Map<String, Object> input, ContentStream stream) throws Exception {
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

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param parentId
	 *            the property parentId is used to get the object-type�s
	 *            immediate parent type.
	 * @param input
	 *            the property input is used to get all request parameters.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return response object
	 * @throws Exception
	 */
	public static Map<String, Object> invokeGetMethod(String repositoryId, String typeId, String id, String userName,
			String password, String filter) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		ObjectType typeobj = SwaggerHelpers.getType(typeId);

		String idName = SwaggerHelpers.getIdName(typeobj);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeobj.isBaseType() ? id : typeId + "::" + idName + "::" + id;
		} else {
			customId = id;
		}
		OperationContext context = new OperationContextImpl();
		if (filter != null) {
			context.setFilterString(filter);
		}

		CmisObject obj = session.getObject(customId, context);
		LOG.info("TypeId:{},id:{},Object:{}", typeId, customId, obj);
		if (obj != null && typeobj.getId().equals(obj.getType().getId())) {
			Map<String, Object> propMap = compileProperties(obj, session);
			return propMap;
		} else {
			throw new Exception("Type Missmatch");
		}
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param id
	 *            the property id is used to get the object.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return if true means object deleted successfully.
	 * @throws Exception
	 */
	public static boolean invokeDeleteMethod(String repositoryId, String typeId, String id, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeobj = SwaggerHelpers.getType(typeId);
		String idName = SwaggerHelpers.getIdName(typeobj);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeobj.isBaseType() ? id : typeId + "::" + idName + "::" + id;
		} else {
			customId = id;
		}
		CmisObject obj = session.getObject(customId);
		LOG.info("TypeId:{},id:{},Object:{}", typeId, customId, obj);

		if (obj != null && typeobj.getId().equals(obj.getType().getId())) {
			boolean isdelete = SwaggerApiServiceFactory.getApiService().beforeDelete(session, obj);
			if (isdelete) {
				obj.delete();
				return true;
			}
		} else {
			throw new Exception("Type Missmatch");
		}
		return false;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param id
	 *            the property parentId is used to get the object.
	 * @param input
	 *            the property input is used to get all request parameters.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return response object
	 * @throws Exception
	 */
	public static Map<String, Object> invokePutMethod(String repositoryId, String typeId, String id,
			Map<String, Object> input, String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeobj = SwaggerHelpers.getType(typeId);
		String idName = SwaggerHelpers.getIdName(typeobj);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeobj.isBaseType() ? id : typeId + "::" + idName + "::" + id;
		} else {
			customId = id;
		}
		CmisObject obj = session.getObject(customId);
		LOG.info("TypeId:{},id:{},Object:{}", typeId, customId, obj);
		if (obj != null && typeobj.getId().equals(obj.getType().getId())) {
			Map<String, Object> serializeMap = deserializeInput(input, typeobj, session);
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
					serializeMap, obj.getPropertyValue("revisionId"));
			if (updateProperties != null) {
				CmisObject newObj = obj.updateProperties(updateProperties);
				Map<String, Object> propMap = compileProperties(newObj, session);
				return propMap;
			}
			return null;
		} else {
			throw new Exception("Type Missmatch");
		}
	}

	private static ContentStream getContentStream(Part filePart) throws IOException {
		ContentStream setContentStream = null;
		if (filePart != null) {
			String file = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String extension = Files.getFileExtension(file);
			String name = Files.getNameWithoutExtension(file);
			InputStream fileContent = filePart.getInputStream();
			BigInteger size = BigInteger.valueOf(filePart.getSize());
			LOG.info("filName:{},extension:{},size:{}", name, extension, size);
			setContentStream = new ContentStreamImpl(name, size, MimeUtils.guessMimeTypeFromExtension(extension),
					fileContent);
			return setContentStream;
		}
		return null;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param id
	 *            the property parentId is used to get the object.
	 * @param input
	 *            the property input is used to get all request parameters.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @param response
	 *            the property response is used to get the file response.
	 * @return ContentStream
	 * @throws Exception
	 */
	public static ContentStream invokeDownloadMethod(String repositoryId, String typeId, String id, String userName,
			String password, HttpServletResponse response) throws Exception {

		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeobj = SwaggerHelpers.getType(typeId);
		String idName = SwaggerHelpers.getIdName(typeobj);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeobj.isBaseType() ? id : typeId + "::" + idName + "::" + id;
		} else {
			customId = id;
		}
		LOG.info("TypeId:{},id:{}", typeId, customId);
		ContentStream stream = ((Document) session.getObject(customId)).getContentStream(customId);
		return stream;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @param inputType
	 *            the property inputType is used to get the objectType and
	 *            convert into type definition.
	 * @return TypeDefinition is used to get the user define data fields to
	 *         server
	 * @throws Exception
	 */
	public static TypeDefinition invokePostTypeDefMethod(String repositoryId, String userName, String password,
			InputStream inputType) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDef = TypeUtils.readFromJSON(inputType);
		TypeDefinition returnedType = session.createType(typeDef);
		LOG.info("Created TypeDefinition:{}", returnedType);
		if (SwaggerHelpers.customTypeHasFolder()) {
			CmisObject obj = session.getObjectByPath("/" + returnedType.getId());
			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforecreate(session,
					properties);
			CmisObject newObj = obj.updateProperties(updateProperties);
		}
		return returnedType;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param id
	 *            the property id is used to get the particular type definition.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return TypeDefinition is used to get the user define data fields to
	 *         server
	 * @throws Exception
	 */
	public static JSONObject invokeGetTypeDefMethod(String repositoryId, String typeId, String userName,
			String password, boolean includeRelationship) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		JSONObject json = new JSONObject();
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		JSONArray JsonArray = new JSONArray();
		TypeDefinition typedef = SwaggerHelpers.getType(typeId);
		JSONObject obj = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
		if (includeRelationship) {
			List<FileableCmisObject> relationType = SwaggerHelpers.getRelationshipType(session, typeId);
			JSONArray childJson = null;
			if (relationType != null) {
				childJson = getRelationshipChild(session, relationType, JsonArray);

				obj.put("relations", childJson);
			} else {
				obj.put("relations", childJson);
			}
		}
		return obj;
	}

	/**
	 * /**
	 * 
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param id
	 *            the property id is used to get the particular type definition.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return true if type deleted successfully,false type not deleted
	 *         successfully
	 * @throws Exception
	 */
	public static boolean invokeDeleteTypeDefMethod(String repositoryId, String id, String userName, String password)
			throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDef = session.getTypeDefinition(id);
		LOG.info("Delete TypeDefinition:{}", typeDef);
		if (typeDef != null) {
			session.deleteType(id);
			return true;
		}
		return false;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param id
	 *            the property id is used to get the particular type definition.
	 * @param inputType
	 *            the property inputType is used to get the objectType and
	 *            convert into type definition.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return TypeDefinition is used to get the user define data fields to
	 *         server
	 * @throws Exception
	 */
	public static TypeDefinition invokePutTypeDefMethod(String repositoryId, String id, InputStream inputType,
			String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDef = session.getTypeDefinition(id);
		if (typeDef != null) {
			TypeDefinition typedefinition = TypeUtils.readFromJSON(inputType);
			TypeDefinition returnedType = session.updateType(typedefinition);
			LOG.info("Updated TypeDefinition:{}", typeDef);
			return returnedType;
		}
		return null;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param id
	 *            the property id is used to get the particular type definition.
	 * @param inputType
	 *            the property inputType is used to get the objectType and
	 *            convert into type definition.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return Access Control List is used to get user list of read / write
	 *         access for that particular object
	 * @throws Exception
	 */
	public static Acl invokePostAcl(String repositoryId, String id, Map<String, Object> input, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectFactory of = session.getObjectFactory();
		List<Ace> addAces = new ArrayList<Ace>();
		List<Ace> removeAces = new ArrayList<Ace>();
		CmisObject obj = session.getObject(input.get("objectId").toString());
		if (id.equals("addAcl")) {
			addAces.add(of.createAce(input.get("principalId").toString(),
					Collections.singletonList(input.get("permission").toString())));
			removeAces = null;
		} else if (id.equals("removeAcl")) {
			removeAces.add(of.createAce(input.get("principalId").toString(),
					Collections.singletonList(input.get("permission").toString())));
		}
		LOG.info("id :{} Adding {} , removing {} given ACEs, propagation:{}", id, addAces, removeAces,
				AclPropagation.OBJECTONLY);
		Acl acl = session.applyAcl(obj, addAces, removeAces, AclPropagation.OBJECTONLY);
		HashMap<String, Object> properties = new HashMap<String, Object>();
		CmisObject upObj = session.getObject(input.get("objectId").toString());
		Map<String, Object> updateproperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
				properties, upObj.getPropertyValue("revisionId"));
		CmisObject newObj = upObj.updateProperties(updateproperties);

		return acl;
	}

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param type
	 *            the property type is used to get particular type definition.
	 * @param skipCount
	 *            the property skipCount is used to how many objects user want
	 *            to skip.
	 * @param maxItems
	 *            the property maxItems is used to how many objects want per
	 *            page.
	 * @param userName
	 *            the property userName is used to login the particular
	 *            repository.
	 * @param password
	 *            the property password is used to login the particular
	 *            repository.
	 * @return list of ObjectData
	 * @throws Exception
	 */
	public static JSONObject invokeGetAllMethod(String repositoryId, String type, String id, String skipCount,
			String maxItems, String userName, String password, String filter, String orderBy) throws Exception {
		JSONObject json = new JSONObject();
		ItemIterable<CmisObject> children = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		ObjectType typeObj = SwaggerHelpers.getType(type);
		OperationContext context = new OperationContextImpl();
		if (maxItems != null) {
			context.setMaxItemsPerPage(Integer.parseInt(maxItems));
		}
		if (filter != null) {
			context.setFilterString(filter);
		}
		if (orderBy != null) {
			context.setOrderBy(orderBy);
		}
		if (typeObj.isBaseType()) {
			if (id != null) {
				Folder object = (Folder) session.getObject(id);
				json.put(object.getName(), object);
				children = object.getChildren(context);
			} else {
				if (!type.equalsIgnoreCase("cmis:folder") && !type.equalsIgnoreCase("cmis:document")
						&& !type.equalsIgnoreCase("cmis:relationship") && !type.equalsIgnoreCase("cmis:item")
						&& !type.equalsIgnoreCase("cmis:secondary")) {
					Folder typeFolder = (Folder) session.getObjectByPath("/" + type);
					id = typeFolder.getId();
					if (id != null) {
						json.put(typeFolder.getName(), typeFolder);
						children = typeFolder.getChildren(context);
					}
				} else {
					children = session.getRootFolder().getChildren(context);
				}
			}
			if (skipCount != null) {
				children = children.skipTo(Integer.parseInt(skipCount));
			}
		} else {
			children = ((Folder) session.getObjectByPath("/" + typeObj.getId())).getChildren(context);
			if (skipCount != null) {
				children = children.skipTo(Integer.parseInt(skipCount));
			}
		}

		for (CmisObject child : children.getPage()) {
			Map<String, Object> propmap = compileProperties(child, session);
			json.put(child.getName(), propmap);
		}
		return json;
	}

	private static JSONArray getRelationshipChild(Session session, List<FileableCmisObject> relationType,
			JSONArray JsonArray) throws Exception {
		if (relationType.size() > 0) {
			for (CmisObject types : relationType) {
				JSONObject childObject = new JSONObject();
				Map<String, Object> propmap = compileProperties(types, session);
				childObject.put(types.getName(), propmap);
				JsonArray.add(childObject);
			}
		}
		return JsonArray;
	}
}
