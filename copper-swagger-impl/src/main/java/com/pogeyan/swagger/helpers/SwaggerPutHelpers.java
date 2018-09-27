package com.pogeyan.swagger.helpers;

import java.io.InputStream;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.factory.SwaggerApiServiceFactory;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;

public class SwaggerPutHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPutHelpers.class);

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param id
	 *            the property id is used to get the particular type definition.
	 * @param string
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
	public static TypeDefinition invokePutTypeDefMethod(String repositoryId, String typeId, InputStream string,
			String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDefinition = session.getTypeDefinition(typeId);
		if (typeDefinition != null) {
			TypeDefinition typeDef = TypeUtils.readFromJSON(string);
			TypeDefinition returnedType = session.updateType(typeDef);
			LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerPutHelpers",
					"invokePutTypeDefMethod", repositoryId, typeId);
			return returnedType;
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
	 * @return response object
	 * @throws Exception
	 */
	public static Map<String, Object> invokePutMethod(String repositoryId, String typeId, String objectId,
			Map<String, Object> inputMap, String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeDefinitionObj = SwaggerHelpers.getType(typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeDefinitionObj);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeDefinitionObj.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customId = objectId;
		}
		CmisObject object = session.getObject(customId);
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}, object: {}", "SwaggerPutHelpers",
				"invokePutMethod", repositoryId, typeId, objectId);
		if (object != null && typeDefinitionObj.getId().equals(object.getType().getId())) {
			Map<String, Object> serializeMap = SwaggerHelpers.deserializeInput(inputMap, typeDefinitionObj, session);
			Map<String, Object> updateProperties = SwaggerApiServiceFactory.getApiService().beforeUpdate(session,
					serializeMap, object.getPropertyValue("revisionId"));
			if (updateProperties != null) {
				CmisObject updatedObj = object.updateProperties(updateProperties);
				Map<String, Object> propMap = SwaggerHelpers.compileProperties(updatedObj, session);
				return propMap;
			}
			return null;
		} else {
			throw new Exception("Type Missmatch or object not found");
		}
	}

}
