package com.pogeyan.swagger.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.factory.SwaggerApiServiceFactory;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;

public class SwaggerPOSTHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPOSTHelpers.class);

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
	@SuppressWarnings("unused")
	public static TypeDefinition invokePostTypeDefMethod(String repositoryId, String userName, String password,
			InputStream inputType) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDefinition = TypeUtils.readFromJSON(inputType);
		TypeDefinition returnedType = session.createType(typeDefinition);
		LOG.info("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggerApiService",
				"invokePostTypeDefMethod", repositoryId, inputType);
		if (SwaggerHelpers.customTypeHasFolder()) {
			CmisObject object = session.getObjectByPath("/" + returnedType.getId());
			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforecreate(session,
					properties);
			if (updateProperties != null && !updateProperties.isEmpty()) {
				CmisObject newObject = object.updateProperties(updateProperties);
			}
			CmisObject newObject = object.updateProperties(updateProperties);
		}
		return returnedType;
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

	public static Acl invokePostAcl(String repositoryId, String aclParam, Map<String, Object> input, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectFactory of = session.getObjectFactory();
		List<Ace> addAces = new ArrayList<Ace>();
		List<Ace> removeAces = new ArrayList<Ace>();
		CmisObject object = session.getObject(input.get("objectId").toString());
		if (aclParam.equals("addAcl")) {
			addAces.add(of.createAce(input.get("principalId").toString(),
					Collections.singletonList(input.get("permission").toString())));
			removeAces = null;
		} else if (aclParam.equals("removeAcl")) {
			removeAces.add(of.createAce(input.get("principalId").toString(),
					Collections.singletonList(input.get("permission").toString())));
		}
		LOG.info("class name: {}, method name: {}, repositoryId: {}",
				"aclParam: {}, Adding: {}, removing: {}, given ACEs", "SwaggerApiService", "invokePostAcl",
				repositoryId, aclParam, addAces, removeAces);
		Acl acl = session.applyAcl(object, addAces, removeAces, AclPropagation.OBJECTONLY);
		HashMap<String, Object> properties = new HashMap<String, Object>();

		CmisObject updateObject = session.getObject(input.get("objectId").toString());
		Map<String, Object> updateproperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
				properties, updateObject.getPropertyValue("revisionId"));
		if (updateproperties != null && !updateproperties.isEmpty()) {
			@SuppressWarnings("unused")
			CmisObject newObject = updateObject.updateProperties(updateproperties);
		}
		return acl;
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
	 * @param pathFragments
	 *            the property pathFragments is used to get request path
	 *            parameters.
	 * @param filePart
	 *            the property filePart is used to get file details from
	 *            request.
	 * @return response object
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static Map<String, Object> invokePostMethod(String repositoryId, String typeId, String parentId,
			Map<String, Object> input, String userName, String password, String objectId, Part filePart)
			throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeDefinitionObj = SwaggerHelpers.getType(typeId);
		if (objectId != null) {
			String typeIdName = SwaggerHelpers.getIdName(typeDefinitionObj);
			String customId = null;
			if (SwaggerHelpers.customTypeHasFolder()) {
				customId = typeDefinitionObj.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
			} else {
				customId = objectId;
			}
			Document document = ((Document) session.getObject(customId))
					.setContentStream(SwaggerHelpers.getContentStream(filePart), true);

			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
					properties, document.getPropertyValue("revisionId"));
			if (updateProperties != null && !updateProperties.isEmpty()) {
				CmisObject newObject = document.updateProperties(updateProperties);
			}
			propMap = SwaggerHelpers.compileProperties(document, session);
			return propMap;
		} else {
			ContentStream setContentStream = SwaggerHelpers.getContentStream(filePart);
			if (typeDefinitionObj != null) {
				Map<String, Object> serializeMap = SwaggerHelpers.deserializeInput(input, typeDefinitionObj, session);
				BaseTypeId baseTypeId = typeDefinitionObj.isBaseType() ? typeDefinitionObj.getBaseTypeId()
						: typeDefinitionObj.getBaseType().getBaseTypeId();
				Map<String, Object> properties = SwaggerApiServiceFactory.getApiService().beforecreate(session,
						serializeMap);
				CmisObject cmisObject = SwaggerHelpers.createForBaseTypes(session, baseTypeId, parentId, properties,
						setContentStream);
				propMap = SwaggerHelpers.compileProperties(cmisObject, session);
				return propMap;
			}
		}
		LOG.info("class name: {}, method name: {}, repositoryId: {}, typeId:{}, object: {}", "SwaggerApiService",
				"invokePostMethod", repositoryId, typeId, objectId);
		return null;
	}
}
