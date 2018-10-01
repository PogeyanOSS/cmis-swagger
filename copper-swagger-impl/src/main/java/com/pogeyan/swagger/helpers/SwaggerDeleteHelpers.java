package com.pogeyan.swagger.helpers;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.impl.factory.SwaggerObjectServiceFactory;

public class SwaggerDeleteHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerDeleteHelpers.class);

	/**
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
	public static boolean invokeDeleteTypeDefMethod(String repositoryId, String type, String userName, String password)
			throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDefinition = session.getTypeDefinition(type);
		LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggerDeleteHelpers",
				"invokeDeleteTypeDefMethod", repositoryId, type);
		if (typeDefinition != null) {
			session.deleteType(type);
			return true;
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
	public static boolean invokeDeleteMethod(String repositoryId, String typeId, String objectId, String userName,
			String password) throws Exception {

		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObject = SwaggerHelpers.getType(typeId);
		String typeIdName = SwaggerHelpers.getIdName(typeObject);
		String customId = null;
		if (SwaggerHelpers.customTypeHasFolder()) {
			customId = typeObject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
		} else {
			customId = objectId;
		}
		CmisObject object = session.getObject(customId);

		if (object != null && typeObject.getId().equals(object.getType().getId())) {
			boolean isdelete = SwaggerObjectServiceFactory.getApiService().beforeDelete(session, object);
			if (isdelete) {
				object.delete();
				return true;
			}
			LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}, objectId: {}",
					"SwaggerDeleteHelpers", "invokeDeleteMethod", repositoryId, typeId, objectId);
		} else {
			throw new Exception("Type Missmatch or object not found");
		}
		return false;
	}
}
