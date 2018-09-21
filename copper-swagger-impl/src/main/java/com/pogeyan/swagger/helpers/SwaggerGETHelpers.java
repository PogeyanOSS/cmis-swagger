package com.pogeyan.swagger.helpers;

import java.util.ArrayList;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
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

public class SwaggerGETHelpers {

	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGETHelpers.class);

	@SuppressWarnings("unused")
	public static JSONObject invokeGetTypeDefMethod(String repositoryId, String typeId, String userName,
			String password, boolean includeRelationship) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		JSONObject json = new JSONObject();
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		JSONArray JsonArray = new JSONArray();
		TypeDefinition typedefinition = SwaggerHelpers.getType(typeId);
		JSONObject object = JSONConverter.convert(typedefinition, DateTimeFormat.SIMPLE);
		if (includeRelationship) {
			ItemIterable<CmisObject> relationType = SwaggerHelpers.getRelationshipType(session, typeId);
			getRelationshipChild(session, relationType, object);
		}
		return object;
	}

	private static JSONObject getRelationshipChild(Session session, ItemIterable<CmisObject> relationType,
			JSONObject mainObject) throws Exception {
		JSONArray JsonArray = new JSONArray();
		if (relationType != null) {
			for (CmisObject types : relationType) {
				JSONObject childObject = new JSONObject();
				Map<String, Object> propmap = SwaggerHelpers.compileProperties(types, session);
				TypeDefinition typedef = SwaggerHelpers.getType(propmap.get("target_table").toString());
				JSONObject object = JSONConverter.convert(typedef, DateTimeFormat.SIMPLE);
				ItemIterable<CmisObject> relationInnerChildType = SwaggerHelpers.getRelationshipType(session,
						typedef.getId());
				if (relationInnerChildType != null) {
					getRelationshipChild(session, relationInnerChildType, object);
				}
				childObject.put(typedef.getId(), object);
				JsonArray.add(childObject);
			}

		}
		mainObject.put("relations", JsonArray);
		return mainObject;
	}

	public static ContentStream invokeDownloadMethod(String repositoryId, String typeId, String objectId,
			String userName, String password) throws Exception {

		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObject = SwaggerHelpers.getType(typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeObject);
		String customObjectId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customObjectId = typeObject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customObjectId = objectId;
		}

		// LOG.info("class name: {}, method name: {}, repositoryId: {}, typeId:
		// {}, objectId: {}", "SwaggerApiService",
		// "invokeDownloadMethod", repositoryId, typeId, customObjectId);
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
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		ObjectType typeObject = SwaggerHelpers.getType(type);
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
		if (typeObject.isBaseType()) {
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

		// LOG.info("class name: {}, method name: {}, repositoryId: {}, Fetching
		// RelationshipType for type: {}",
		// "SwaggerapiService", "invokeGetAllMethod", repositoryId, type);
		for (CmisObject child : children.getPage()) {
			if (includeRelationship) {
				ArrayList<Object> relationData = SwaggerHelpers.getDescendantsForRelationObjects(userName, password,
						repositoryId, child.getId());
				Map<String, Object> data = SwaggerHelpers.formRelationData(session, relationData);
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
		if (!SwaggerHelpers.getTypeIsPresents()) {
			SwaggerHelpers.getAllTypes(session);
		}
		ObjectType typeobject = SwaggerHelpers.getType(typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeobject);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeobject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customId = objectId;
		}
		OperationContext context = new OperationContextImpl();
		if (filter != null) {
			context.setFilterString(filter);
		}
		LOG.info("class name: {}, method name: {}, repositoryId: {}, type: {}", "ObjectId: {}", "SwaggerGETHelpers",
				"invokeGetMethod", repositoryId, typeId, customId);
		CmisObject object = session.getObject(customId, context);
		if (object != null && typeobject.getId().equals(object.getType().getId())) {
			Map<String, Object> propMap = SwaggerHelpers.compileProperties(object, session);
			return propMap;
		} else {
			throw new Exception("Type Missmatch");
		}
	}
}
