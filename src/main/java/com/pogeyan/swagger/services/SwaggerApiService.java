package com.pogeyan.swagger.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.pogeyan.swagger.pojos.ErrorResponse;
import com.pogeyan.swagger.utils.MimeUtils;
import com.pogeyan.swagger.utils.SwaggerHelpers;

/**
 * SwaggerApiService Operations
 *
 */
/**
 * @author Mohamed
 *
 */
public class SwaggerApiService {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerApiService.class);

	/**
	 * @param repositoryId
	 *            the property repositoryId is identifier for the repository.
	 * @param typeId
	 *            the property typeId of an object-type specified in the
	 *            repository.
	 * @param parentId
	 *            the property parentId is used to get the object-type’s
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
			Map<String, String> input, String userName, String password, String[] pathFragments, Part filePart)
			throws Exception {
		CmisObject cmisObj = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObj = SwaggerHelpers.getType(session, typeId);
		if (pathFragments.length > 2 && pathFragments[2] != null) {
			String idName = SwaggerHelpers.getIdName(typeObj);
			String customId = null;
			if (SwaggerHelpers.customTypeHasFolder()) {
				customId = typeObj.isBaseType() ? pathFragments[2] : typeId + "::" + idName + "::" + pathFragments[2];
			} else {
				customId = pathFragments[2];
			}
			Document doc = ((Document) session.getObject(customId)).setContentStream(getContentStream(filePart), true);
			Map<String, Object> propMap = compileProperties(doc);
			LOG.info("customId:{} properties:{}", customId, propMap);
			return propMap;
		} else {
			ContentStream setContentStream = getContentStream(filePart);
			// baseType
			if (typeObj != null) {
				Map<String, Object> serializeMap = deserializeInput(input, typeObj, true);

				if (typeObj.isBaseType()) {
					cmisObj = createForBaseTypes(session, typeObj.getBaseTypeId(), parentId, serializeMap,
							setContentStream);
				} else {
					// custom type
					cmisObj = createForBaseTypes(session, typeObj.getBaseType().getBaseTypeId(), parentId, serializeMap,
							setContentStream);
				}
				Map<String, Object> propMap = compileProperties(cmisObj);
				LOG.info("objectType:{} properties:{}", typeObj.getId(), propMap);
				return propMap;
			}
		}
		return null;
	}

	private static Map<String, Object> deserializeInput(Map<String, String> input, ObjectType obj, boolean temp)
			throws Exception {
		LOG.info("deSerializing Input:{}", input);
		Map<String, Object> serializeMap = new HashMap<String, Object>();
		Map<String, PropertyDefinition<?>> dataPropDef = obj.getPropertyDefinitions();

		for (String var : input.keySet()) {
			if (var.equals("parentId")) {
				continue;
			}
			if (input.get(var) != null) {
				PropertyDefinition<?> defObj = dataPropDef.get(var);
				PropertyType reqPropType = defObj.getPropertyType();
				if (reqPropType.equals(PropertyType.INTEGER)) {
					Integer result = Integer.parseInt(input.get(var));
					serializeMap.put(var, result);
				} else if (reqPropType.equals(PropertyType.BOOLEAN)) {
					Boolean result = Boolean.parseBoolean(input.get(var));
					serializeMap.put(var, result);
				} else if (reqPropType.equals(PropertyType.DATETIME)) {
					SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
					if (temp) {

						GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
						calendar.setTimeInMillis(sdf.parse(input.get(var)).getTime());
						serializeMap.put(var, calendar);
					} else {
						serializeMap.put(var, input.get(var));
					}
				} else if (reqPropType.equals(PropertyType.DECIMAL)) {
					BigDecimal result = new BigDecimal(input.get(var));
					serializeMap.put(var, result);
				} else {
					// string type
					serializeMap.put(var, input.get(var));
				}
			} else {
				continue;
			}
		}
		LOG.info("serializedMap:{}", serializeMap);
		return serializeMap;
	}

	private static Map<String, Object> compileProperties(CmisObject cmisObj) throws Exception {
		Map<String, String> propMap = new HashMap<String, String>();
		cmisObj.getProperties().stream().forEach(a -> {
			propMap.put(a.getDefinition().getId(), a.getValueAsString());
		});
		Map<String, Object> outputMap = deserializeInput(propMap, cmisObj.getType(), false);

		return outputMap;
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
	 *            the property parentId is used to get the object-type’s
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
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeobj = SwaggerHelpers.getType(session, typeId);

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
			Map<String, Object> propMap = compileProperties(obj);
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
		ObjectType typeobj = SwaggerHelpers.getType(session, typeId);
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
			obj.delete();
			return true;
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
			Map<String, String> input, String userName, String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeobj = SwaggerHelpers.getType(session, typeId);
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
			Map<String, Object> serializeMap = deserializeInput(input, typeobj, true);
			obj.updateProperties(serializeMap);
			Map<String, Object> propMap = compileProperties(obj);
			return propMap;
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
		ObjectType typeobj = SwaggerHelpers.getType(session, typeId);
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
	public static TypeDefinition invokeGetTypeDefMethod(String repositoryId, String id, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		TypeDefinition typeDef = session.getTypeDefinition(id);
		LOG.info("Get TypeDefinition:{}", typeDef);
		return typeDef;
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
	public static Acl invokePostAcl(String repositoryId, String id, Map<String, String> input, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectFactory of = session.getObjectFactory();
		List<Ace> addAces = new ArrayList<Ace>();
		List<Ace> removeAces = new ArrayList<Ace>();
		CmisObject obj = session.getObject(input.get("objectId"));
		if (id.equals("addAcl")) {
			addAces.add(of.createAce(input.get("principalId"), Collections.singletonList(input.get("permission"))));
			removeAces = null;
		} else if (id.equals("removeAcl")) {
			removeAces.add(of.createAce(input.get("principalId"), Collections.singletonList(input.get("permission"))));
		}
		LOG.info("id :{} Adding {} , removing {} given ACEs, propagation:{}", id, addAces, removeAces,
				AclPropagation.OBJECTONLY);
		Acl acl = session.applyAcl(obj, addAces, removeAces, AclPropagation.OBJECTONLY);
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
	public static JSONObject invokeGetAllMethod(String repositoryId, String type, String skipCount, String maxItems,
			String userName, String password) throws Exception {
		JSONObject json = new JSONObject();
		ItemIterable<CmisObject> children = null;
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeObj = SwaggerHelpers.getType(session, type);
		OperationContext context = new OperationContextImpl();
		if (maxItems != null) {
			context.setMaxItemsPerPage(Integer.parseInt(maxItems));
		}
		if (typeObj.isBaseType()) {
			children = session.getRootFolder().getChildren(context);
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
			Map<String, Object> propmap = compileProperties(child);
			json.put(child.getName(), propmap);
		}
		return json;
	}
}
