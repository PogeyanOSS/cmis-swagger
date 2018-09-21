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

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.pojos.DefinitionsObject;
import com.pogeyan.swagger.pojos.ExternalDocs;
import com.pogeyan.swagger.pojos.InfoObject;
import com.pogeyan.swagger.pojos.ParameterObject;
import com.pogeyan.swagger.pojos.PathCommonObject;
import com.pogeyan.swagger.pojos.PathObject;
import com.pogeyan.swagger.pojos.ResponseObject;
import com.pogeyan.swagger.pojos.SecurityDefinitionObject;
import com.pogeyan.swagger.pojos.TagObject;

public class SwaggerUIDefnHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerUIDefnHelpers.class);
	public static InfoObject infoObj = new InfoObject();
	public static ExternalDocs externalDocsObject = new ExternalDocs();
	public static String hostSwaggerUrl;

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
		for (ObjectType type : SwaggerHelpers.getTypeCacheMap().asMap().values()) {
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
		for (ObjectType type : SwaggerHelpers.getTypeCacheMap().asMap().values()) {

			ArrayList<String> required = new ArrayList<String>();
			required.add(PropertyIds.OBJECT_TYPE_ID);
			required.add(PropertyIds.NAME);

			String defName = getDefinitionName(type);
			Map<String, String> xml = new HashMap<String, String>();
			xml.put("name", type.getDescription());

			Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();
			Set<Entry<String, PropertyDefinition<?>>> data = type.getPropertyDefinitions().entrySet();

			if (type.isBaseType()) {
				for (Entry<String, PropertyDefinition<?>> propertiesValues : data) {
					HashMap<String, String> propObjBase = new HashMap<String, String>();
					if (propertiesValues.getKey().equals(PropertyIds.OBJECT_TYPE_ID)
							|| propertiesValues.getKey().equals(PropertyIds.NAME)
							|| propertiesValues.getKey().equals(PropertyIds.DESCRIPTION)
							|| propertiesValues.getKey().equals(PropertyIds.SOURCE_ID)
							|| propertiesValues.getKey().equals(PropertyIds.TARGET_ID)
							|| propertiesValues.getKey().equals(PropertyIds.POLICY_TEXT)) {
						propObjBase.put("type", PropertyType.STRING.toString().toLowerCase());
						if (propertiesValues.getKey().equals(PropertyIds.OBJECT_TYPE_ID)) {
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
					HashMap<String, String> propObject = new HashMap<String, String>();
					if (propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_LENGTH)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.OBJECT_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_FILE_NAME)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CHECKIN_COMMENT)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_LABEL)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_MAJOR_VERSION)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_LATEST_VERSION)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_LATEST_MAJOR_VERSION)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_IMMUTABLE)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
							|| propertiesValues.getKey().equalsIgnoreCase("cmis:previousVersionObjectId")
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CREATION_DATE)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CHANGE_TOKEN)
							|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)) {

						continue;

					} else {
						SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
						if (propertiesValues.getValue().getPropertyType().equals(PropertyType.INTEGER)) {
							propObject.put("type", propertiesValues.getValue().getPropertyType().name().toLowerCase());
							propObject.put("format", "int64");
						} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DATETIME)) {
							propObject.put("type", "string");
							propObject.put("format", "data-time");
							propObject.put("example", sdf.format(new Date(System.currentTimeMillis())));
						} else if (propertiesValues.getValue().getPropertyType().equals(PropertyType.DECIMAL)) {
							propObject.put("type", "number");
							propObject.put("format", "double");
						} else {
							propObject.put("type", "string");
						}
						if (propertiesValues.getKey().equals(PropertyIds.OBJECT_TYPE_ID)) {
							propObject.put("example", type.getQueryName());
						}
						properties.put(propertiesValues.getKey(), propObject);
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

		for (ObjectType type : SwaggerHelpers.getTypeCacheMap().asMap().values()) {

			String[] consumes = new String[] { "application/json" };
			String[] produces = new String[] { "application/json" };
			Map<String, String> schema = new HashMap<String, String>();
			String defName = getDefinitionName(type);
			schema.put("$ref", "#/definitions/" + defName);
			String id = SwaggerHelpers.getIdName(type);
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
			ResponseObject respObject1 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject respObject2 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject respObject3 = new ResponseObject("successful operation", schema);
			getResponsesMap.put("404", respObject1);
			getResponsesMap.put("400", respObject2);
			getResponsesMap.put("200", respObject3);

			ParameterObject getParams = new ParameterObject("path", id, "ID of " + type.getDescription() + " to return",
					true, null, "string", null, null, "int64", null);
			PathCommonObject getCommonObject = new PathCommonObject(new String[] { defName },
					"Get " + defName + " by Id", null, "get" + defName + "ById", null, produces,
					id != null ? new ParameterObject[] { getParams } : null, getResponsesMap, security);

			// DELETE METHOD
			// delete folder /folder/{folderId} DELETE
			Map<String, ResponseObject> delResponses = new HashMap<String, ResponseObject>();
			ResponseObject object4 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject object5 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject object61 = new ResponseObject("CmisNotSupportedException", null);

			delResponses.put("404", object4);
			delResponses.put("405", object61);
			delResponses.put("400", object5);

			PathCommonObject deleteCommonObject = new PathCommonObject(new String[] { defName },
					"Deletes a  " + defName + " Object", null, "delete" + defName, null, produces,
					id != null ? new ParameterObject[] { getParams } : null, delResponses, security);

			// UPDATE METHOD
			// update folder /folder/{folderId} PUT
			Map<String, ResponseObject> putResponses = new HashMap<String, ResponseObject>();
			ResponseObject object6 = new ResponseObject(type.getDescription() + " not found", null);
			ResponseObject object7 = new ResponseObject("Invalid ID supplied", null);
			ResponseObject error11 = new ResponseObject("InvalidArgumentException", null);
			ResponseObject error21 = new ResponseObject("CmisContentAlreadyExistsException", null);
			ResponseObject error31 = new ResponseObject("CmisRuntimeException", null);
			ResponseObject error41 = new ResponseObject("CmisStreamNotSupportedException", null);
			ResponseObject error51 = new ResponseObject("CmisServiceUnavailableException", null);
			ResponseObject error61 = new ResponseObject("CmisNotSupportedException", null);
			ResponseObject object31 = new ResponseObject("Successful Operation", null);
			putResponses.put("200", object31);
			putResponses.put("400", error11);
			putResponses.put("409", error21);
			putResponses.put("500", error31);
			putResponses.put("403", error41);
			putResponses.put("503", error51);
			putResponses.put("404", object6);
			putResponses.put("405", error61);
			putResponses.put("400", object7);
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

			PathObject pathObj2 = new PathObject(null, getCommonObject, putCommonObject, deleteCommonObject);
			pathMap.put(id != null ? "/" + defName + "/{" + id + "}" : "/" + defName, pathObj2);

			// POST METHODS
			if (type.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {

				// post /doc/{docId}/uploadFile

				Map<String, ResponseObject> uploadResponses = new HashMap<String, ResponseObject>();
				ResponseObject uploadResp = new ResponseObject("Successful Operation", null);
				ResponseObject createdResp = new ResponseObject("Created", null);
				ResponseObject error1 = new ResponseObject("InvalidArgumentException", null);
				ResponseObject error2 = new ResponseObject("CmisContentAlreadyExistsException", null);
				ResponseObject error3 = new ResponseObject("CmisRuntimeException", null);
				ResponseObject error4 = new ResponseObject("CmisStreamNotSupportedException", null);
				ResponseObject error5 = new ResponseObject("CmisServiceUnavailableException", null);
				ResponseObject error6 = new ResponseObject("CmisNotSupportedException", null);

				uploadResponses.put("400", error1);
				uploadResponses.put("409", error2);
				uploadResponses.put("500", error3);
				uploadResponses.put("403", error4);
				uploadResponses.put("503", error5);
				uploadResponses.put("201", createdResp);
				uploadResponses.put("200", uploadResp);
				uploadResponses.put("405", error6);

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
			ResponseObject respObject11 = new ResponseObject("CmisNotSupportedException", null);
			ResponseObject respObject21 = new ResponseObject("Successful Operation", null);
			ResponseObject createdResp = new ResponseObject("Created", null);
			ResponseObject error1 = new ResponseObject("InvalidArgumentException", null);
			ResponseObject error2 = new ResponseObject("CmisContentAlreadyExistsException", null);
			ResponseObject error3 = new ResponseObject("CmisRuntimeException", null);
			ResponseObject error4 = new ResponseObject("CmisStreamNotSupportedException", null);
			ResponseObject error5 = new ResponseObject("CmisServiceUnavailableException", null);
			postFormResponses.put("400", error1);
			postFormResponses.put("409", error2);
			postFormResponses.put("500", error3);
			postFormResponses.put("403", error4);
			postFormResponses.put("503", error5);
			postFormResponses.put("201", createdResp);
			postFormResponses.put("405", respObject11);
			postFormResponses.put("400", error1);
			postFormResponses.put("200", respObject21);

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
			if (propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_LENGTH)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.PATH)
					|| propertiesValues.getKey().equalsIgnoreCase("cmis:previousVersionObjectId")
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.PARENT_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_FILE_NAME)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CHECKIN_COMMENT)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_LABEL)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_MAJOR_VERSION)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_LATEST_VERSION)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_LATEST_MAJOR_VERSION)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CREATED_BY)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.IS_IMMUTABLE)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CREATION_DATE)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.CHANGE_TOKEN)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)) {
				continue;
			}

			boolean required = false;
			String paramType = null;
			String format = null;
			if (propertiesValues.getValue() != null && propertiesValues.getValue().getLocalName() != null
					&& propertiesValues.getValue().getLocalName().equals("primaryKey")
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.NAME)
					|| propertiesValues.getKey().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)) {
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

			ParameterObject object = new ParameterObject("formData", propertiesValues.getKey(),
					propertiesValues.getValue().getDescription(), required, null, paramType, null, null, format,
					propertiesValues.getKey().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID) ? type.getId() : null);
			list.add(object);
		}
		return list;
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

	public static void setHostSwaggerUrl(String swaggerServerUrl) {
		hostSwaggerUrl = swaggerServerUrl;
	}

	public static String getHostSwaggerUrl() {
		return hostSwaggerUrl;

	}

}
