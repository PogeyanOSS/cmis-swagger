package com.pogeyan.swagger.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.SwaggerHelpers;

public class SwaggerGetHelpers {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGetHelpers.class);
	public static Map<String, ObjectType> typeMap;

	@SuppressWarnings("unused")
	public static JSONObject invokeGetTypeDefMethod(String repositoryId, String typeId, String userName,
			String password, boolean includeRelationship) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		ObjectType typeObject = SwaggerHelpers.getTypeDefinition(session, typeId);
		JSONObject object = JSONConverter.convert(typeObject, DateTimeFormat.SIMPLE);
		if (includeRelationship) {
			LOG.debug("class name: {}, method name: {}, repositoryId: {}, for type: {}", "SwaggerGetHelpers",
					"invokeGetTypeDefMethod", repositoryId, typeId);
			ItemIterable<CmisObject> relationType = getRelationshipType(session, typeId);
			getRelationshipChild(session, relationType, object);
		}
		return object;
	}

	private static JSONObject getRelationshipChild(Session session, ItemIterable<CmisObject> relationType,
			JSONObject mainObject) throws Exception {
		JSONArray jsonArray = new JSONArray();
		if (relationType != null) {
			for (CmisObject types : relationType) {
				JSONObject childObject = new JSONObject();
				Map<String, Object> propMap = SwaggerHelpers.compileProperties(types, session);
				TypeDefinition typeDefinition = SwaggerHelpers.getTypeDefinition(session,
						propMap.get("target_table").toString());
				JSONObject object = JSONConverter.convert(typeDefinition, DateTimeFormat.SIMPLE);
				ItemIterable<CmisObject> relationInnerChildType = getRelationshipType(session, typeDefinition.getId());
				if (relationInnerChildType != null) {
					getRelationshipChild(session, relationInnerChildType, object);
				}
				childObject.put(typeDefinition.getId(), object);
				jsonArray.add(childObject);
			}

		}
		mainObject.put("relations", jsonArray);
		return mainObject;
	}

	public static ContentStream invokeDownloadMethod(String repositoryId, String typeId, String objectId,
			String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObject = SwaggerHelpers.getTypeDefinition(session, typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeObject);
		String customObjectId = null;
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, typeId: {}, objectId: {}", "SwaggerGetHelpers",
				"invokeDownloadMethod", repositoryId, typeId, objectId);
		if (SwaggerHelpers.customTypeHasFolder()) {
			customObjectId = typeObject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customObjectId = objectId;
		}

		ContentStream stream = ((Document) session.getObject(customObjectId)).getContentStream(customObjectId);
		return stream;
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
	public static JSONObject invokeGetAllMethod(String repositoryId, String type, String parentId, String skipCount,
			String maxItems, String userName, String password, String filter, String orderBy,
			boolean includeRelationship) throws Exception {
		JSONObject json = new JSONObject();
		ItemIterable<CmisObject> children = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObject = session.getTypeDefinition(type);
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
		if (typeObject == null) {
			throw new Exception(type + " doesn't exist!");
		}
		if (typeObject != null && typeObject.isBaseType()) {
			if (parentId != null) {
				Folder object = (Folder) session.getObject(parentId);
				json.put(object.getName(), object);
				children = object.getChildren(context);
			} else {

				if (!type.equalsIgnoreCase(BaseTypeId.CMIS_FOLDER.value())
						&& !type.equalsIgnoreCase(BaseTypeId.CMIS_DOCUMENT.value())
						&& !type.equalsIgnoreCase(BaseTypeId.CMIS_RELATIONSHIP.value())
						&& !type.equalsIgnoreCase(BaseTypeId.CMIS_ITEM.value())
						&& !type.equalsIgnoreCase(BaseTypeId.CMIS_SECONDARY.value())) {
					Folder typeFolder = (Folder) session.getObjectByPath("/" + type);
					parentId = typeFolder.getId();
					if (parentId != null) {
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
			children = ((Folder) session.getObjectByPath("/" + typeObject.getId())).getChildren(context);
			if (skipCount != null) {
				children = children.skipTo(Integer.parseInt(skipCount));
			}
		}
		for (CmisObject child : children.getPage()) {
			if (includeRelationship) {
				LOG.debug("class name: {}, method name: {}, repositoryId: {}, Fetching RelationshipType for type: {}",
						"SwaggerGetHelpers", "invokeGetAllMethod", repositoryId, type);
				ArrayList<Object> relationData = SwaggerHelpers.getDescendantsForRelationObjects(userName, password,
						repositoryId, child.getId());
				Map<String, Object> data = SwaggerGetHelpers.formRelationData(session, relationData);
				json.putAll(data);
			} else {
				Map<String, Object> propmap = SwaggerHelpers.compileProperties(child, session);
				json.put(child.getName(), propmap);
			}
		}

		return json;
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
	public static Map<String, Object> invokeGetMethod(String repositoryId, String typeId, String objectId,
			String userName, String password, String filter) throws Exception {

		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObject = SwaggerHelpers.getTypeDefinition(session, typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeObject);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeObject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customId = objectId;
		}
		OperationContext context = new OperationContextImpl();
		if (filter != null) {
			context.setFilterString(filter);
		}
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}, ObjectId: {}", "SwaggerGetHelpers",
				"invokeGetMethod", repositoryId, typeId, objectId);
		CmisObject object = session.getObject(customId, context);
		if (object != null && typeObject.getId().equals(object.getType().getId())) {
			Map<String, Object> propMap = SwaggerHelpers.compileProperties(object, session);
			return propMap;
		} else {
			throw new Exception("Type Missmatch");
		}
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

	public static ItemIterable<CmisObject> getRelationshipType(Session session, String typeId) {
		ObjectType relationshipType = session.getTypeDefinition(SwaggerHelpers.CMIS_EXT_RELATIONMD);
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
}
