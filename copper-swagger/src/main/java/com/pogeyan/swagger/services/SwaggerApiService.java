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
 *    
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
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.pogeyan.swagger.api.utils.MimeUtils;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.factory.SwaggerApiServiceFactory;

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
	public static Map<String, Object> invokePostMethod(String repositoryId, String typeId, String parentId,
			Map<String, Object> input, String userName, String password, String[] pathFragments, Part filePart,
			Boolean includeCrud, String inputString) throws Exception {
		CmisObject cmisObj = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObj = SwaggerHelpers.getType(typeId);
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
			Map<String, Object> propMap = SwaggerHelpers.compileProperties(doc, session);
			LOG.info("customId: {} properties: {}", customId, propMap);
			return propMap;
		} else {
			ContentStream setContentStream = getContentStream(filePart);
			// baseType
			if (typeObj != null) {
				if (includeCrud) {
					Map<String, Object> resultPropMap = SwaggerHelpers.crudOperation(session, repositoryId, typeObj,
							inputString, userName, password);
					return resultPropMap;
				} else {
					Map<String, Object> serializeMap = deserializeInput(input, typeObj, session);
					BaseTypeId baseTypeId = typeObj.isBaseType() ? typeObj.getBaseTypeId()
							: typeObj.getBaseType().getBaseTypeId();
					Map<String, Object> properties = SwaggerApiServiceFactory.getApiService().beforecreate(session,
							serializeMap);
					cmisObj = SwaggerHelpers.createForBaseTypes(session, baseTypeId, parentId, properties,
							setContentStream, null);
					Map<String, Object> propMap = SwaggerHelpers.compileProperties(cmisObj, session);
					LOG.info("objectType: {} properties: {}", typeObj.getId(), propMap);
					return propMap;
				}

			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> deserializeInput(Map<String, Object> input, ObjectType obj, Session session)
			throws Exception {
		LOG.info("deSerializing Input: {}", input);
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
						Integer valueBigInteger = SwaggerHelpers.convertInstanceOfObject(valueOfType, Integer.class);
						serializeMap.put(var, valueBigInteger);
					} else if (valueOfType instanceof List<?>) {
						List<BigInteger> value = SwaggerHelpers.convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}

				} else if (reqPropType.equals(PropertyType.BOOLEAN)) {
					if (valueOfType instanceof Boolean) {
						Boolean booleanValue = SwaggerHelpers.convertInstanceOfObject(valueOfType, Boolean.class);
						serializeMap.put(var, booleanValue);
					} else if (valueOfType instanceof List<?>) {
						List<Boolean> booleanValue = SwaggerHelpers.convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, booleanValue);
					}

				} else if (reqPropType.equals(PropertyType.DATETIME)) {

					// SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd
					// HH:mm:ss Z yyyy", Locale.US);

					if (valueOfType instanceof GregorianCalendar) {
						Long value = SwaggerHelpers.convertInstanceOfObject(valueOfType, Long.class);
						GregorianCalendar lastModifiedCalender = new GregorianCalendar();
						lastModifiedCalender.setTimeInMillis(value);
						serializeMap.put(var, lastModifiedCalender);
					} else if (valueOfType instanceof List<?>) {
						List<Long> value = SwaggerHelpers.convertInstanceOfObject(valueOfType, List.class);
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
						Double value = SwaggerHelpers.convertInstanceOfObject(valueOfType, Double.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof List<?>) {
						List<BigDecimal> value = SwaggerHelpers.convertInstanceOfObject(valueOfType, List.class);
						serializeMap.put(var, value);
					}
				} else {
					// string type
					if (valueOfType instanceof String) {
						String value = SwaggerHelpers.convertInstanceOfObject(valueOfType, String.class);
						serializeMap.put(var, value);
					} else if (valueOfType instanceof List<?>) {
						List<String> value = SwaggerHelpers.convertInstanceOfObject(valueOfType, List.class);
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
			Map<String, Object> propMap = SwaggerHelpers.compileProperties(obj, session);
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
				Map<String, Object> propMap = SwaggerHelpers.compileProperties(newObj, session);
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
			ItemIterable<CmisObject> relationType = SwaggerHelpers.getRelationshipType(session, typeId);
			getRelationshipChild(session, relationType, obj);
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
			String maxItems, String userName, String password, String filter, String orderBy,
			boolean includeRelationship) throws Exception {
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
						&& !type.equalsIgnoreCase("cmis_ext:relation") && !type.equalsIgnoreCase("cmis:item")
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
		LOG.info("class name: {}, method name: {}, repositoryId: {}, Fetching RelationshipType for type: {}",
				"SwaggerapiService", "invokeGetAllMethod", repositoryId, type);
		JSONArray JsonArray = new JSONArray();
		for (CmisObject child : children.getPage()) {
			if (includeRelationship) {
				ArrayList<Object> relationData = SwaggerHelpers.getDescendantsForRelationObjects(userName, password,
						repositoryId, child.getId());
				Map<String, Object> data = SwaggerHelpers.formRelationData(session, relationData);
				json.putAll(data);
			}
		}

		return json;
	}

	private static JSONObject getRelationshipChild(Session session, ItemIterable<CmisObject> relationType,
			JSONObject mainObject) throws Exception {
		JSONArray JsonArray = new JSONArray();
		if (relationType != null) {
			for (CmisObject types : relationType) {
				JSONObject childObject = new JSONObject();
				Map<String, Object> propmap = SwaggerHelpers.compileProperties(types, session);
				TypeDefinition typedef = SwaggerHelpers.getType(propmap.get("target_table").toString());
				JSONObject obj = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
				ItemIterable<CmisObject> relationInnerChildType = SwaggerHelpers.getRelationshipType(session,
						typedef.getId());
				if (relationInnerChildType != null) {
					getRelationshipChild(session, relationInnerChildType, obj);
				}
				childObject.put(typedef.getId(), obj);
				JsonArray.add(childObject);
			}
		}
		mainObject.put("relations", JsonArray);
		return mainObject;
	}

}
