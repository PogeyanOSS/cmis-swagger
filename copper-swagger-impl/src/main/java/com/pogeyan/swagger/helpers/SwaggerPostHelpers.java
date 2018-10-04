package com.pogeyan.swagger.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.Part;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParseException;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.pogeyan.swagger.api.utils.MimeUtils;
import com.pogeyan.swagger.api.utils.RelationType;
import com.pogeyan.swagger.api.utils.SwaggerHelpers;
import com.pogeyan.swagger.impl.factory.SwaggerObjectServiceFactory;
import com.pogeyan.swagger.pojos.ErrorResponse;

public class SwaggerPostHelpers {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerPostHelpers.class);

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
		if (SwaggerHelpers.customTypeHasFolder()) {
			CmisObject object = session.getObjectByPath("/" + returnedType.getId());
			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerObjectServiceFactory.getApiService().beforecreate(session,
					properties);
			if (updateProperties != null && !updateProperties.isEmpty()) {
				CmisObject newObject = object.updateProperties(updateProperties);
			}
			CmisObject newObject = object.updateProperties(updateProperties);
		}
		LOG.debug("class name: {}, method name: {}, repositoryId: {},  type: {}", "SwaggePostHelpers",
				"invokePostTypeDefMethod", repositoryId, inputType);
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

	@SuppressWarnings("unused")
	public static Acl invokePostAcl(String repositoryId, String aclParam, Map<String, Object> inputMap, String userName,
			String password) throws Exception {
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectFactory of = session.getObjectFactory();
		List<Ace> addAces = new ArrayList<Ace>();
		List<Ace> removeAces = new ArrayList<Ace>();
		if (inputMap.size() == 0) {
			throw new Exception("Empty Properties!!");
		}
		CmisObject object = session.getObject(inputMap.get("objectId").toString());
		if (aclParam.equals("addAcl")) {
			addAces.add(of.createAce(inputMap.get("principalId").toString(),
					Collections.singletonList(inputMap.get("permission").toString())));
			removeAces = null;
		} else if (aclParam.equals("removeAcl")) {
			removeAces.add(of.createAce(inputMap.get("principalId").toString(),
					Collections.singletonList(inputMap.get("permission").toString())));
		}
		LOG.debug(
				"class name: {}, method name: {}, repositoryId: {}, aclParam: {}, Adding: {}, removing: {}, given ACEs",
				"SwaggePostHelpers", "invokePostAcl", repositoryId, aclParam, addAces, removeAces);
		Acl acl = session.applyAcl(object, addAces, removeAces, AclPropagation.OBJECTONLY);
		HashMap<String, Object> properties = new HashMap<String, Object>();

		CmisObject updateObject = session.getObject(inputMap.get("objectId").toString());
		Map<String, Object> updateProperties = SwaggerObjectServiceFactory.getApiService().beforeUpdate(session,
				properties, updateObject.getPropertyValue("revisionId"));
		if (updateProperties != null && !updateProperties.isEmpty()) {
			CmisObject newObject = updateObject.updateProperties(updateProperties);
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
			Map<String, Object> inputMap, String userName, String password, String objectId, Part filePart,
			Boolean includeCrud, String jsonString) throws Exception {

		Map<String, Object> propMap = new HashMap<String, Object>();
		Session session = SwaggerHelpers.getSession(repositoryId, userName, password);
		ObjectType typeDefinitionObject = SwaggerHelpers.getTypeDefinition(session, typeId);
		if (objectId != null) {
			String typeIdName = SwaggerHelpers.getIdName(typeDefinitionObject);
			String customId = null;
			if (SwaggerHelpers.customTypeHasFolder()) {
				customId = typeDefinitionObject.isBaseType() ? objectId : typeId + "::" + typeIdName + "::" + objectId;
			} else {
				customId = objectId;
			}
			Document document = ((Document) session.getObject(customId))
					.setContentStream(SwaggerPostHelpers.getContentStream(filePart), true);

			HashMap<String, Object> properties = new HashMap<String, Object>();
			Map<String, Object> updateProperties = SwaggerObjectServiceFactory.getApiService().beforeUpdate(session,
					properties, document.getPropertyValue("revisionId"));
			if (updateProperties != null && !updateProperties.isEmpty()) {
				CmisObject newObject = document.updateProperties(updateProperties);
			}
			propMap = SwaggerHelpers.compileProperties(document, session);
			return propMap;
		} else {
			ContentStream setContentStream = SwaggerPostHelpers.getContentStream(filePart);
			LOG.debug("class name: {}, method name: {}, repositoryId: {}, type: {}", "SwaggePostHelpers",
					"invokePostMethod", repositoryId, typeId);
			if (typeDefinitionObject != null) {
				if (includeCrud) {
					Map<String, Object> resultPropMap = SwaggerPostHelpers.crudOperation(session, repositoryId,
							typeDefinitionObject, jsonString, userName, password);
					return resultPropMap;
				} else {

					Map<String, Object> serializeMap = SwaggerHelpers.deserializeInput(inputMap, typeDefinitionObject,
							session);
					BaseTypeId baseTypeId = typeDefinitionObject.isBaseType() ? typeDefinitionObject.getBaseTypeId()
							: typeDefinitionObject.getBaseType().getBaseTypeId();
					Map<String, Object> properties = SwaggerObjectServiceFactory.getApiService().beforecreate(session,
							serializeMap);
					CmisObject cmisObject = SwaggerPostHelpers.createForBaseTypes(session, baseTypeId, parentId,
							properties, setContentStream);
					propMap = SwaggerHelpers.compileProperties(cmisObject, session);
					return propMap;
				}

			}
		}
		return null;
	}

	public static ContentStream getContentStream(Part filePart) throws IOException {
		ContentStream setContentStream = null;
		if (filePart != null) {
			String file = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String extension = Files.getFileExtension(file);
			String name = Files.getNameWithoutExtension(file);
			InputStream fileContent = filePart.getInputStream();
			BigInteger size = BigInteger.valueOf(filePart.getSize());
			LOG.debug("class name: {}, method name: {}", "SwaggerPostHelpers", "ContentStream");
			setContentStream = new ContentStreamImpl(name, size, MimeUtils.guessMimeTypeFromExtension(extension),
					fileContent);
			return setContentStream;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> crudOperation(Session session, String repositoryId, ObjectType typeDef,
			String input, String userName, String password) throws JSONParseException {
		try {

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(input);
			JSONArray repoArray = (JSONArray) obj;
			for (Object object : repoArray) {
				Map<String, Object> props = (Map<String, Object>) object;
				Map<String, List<Map<String, Object>>> relatedMap = new LinkedHashMap<>();
				getMapValue(props, relatedMap);
				boolean isDeleted = false;
				Map<String, Object> attachmentProps = new HashMap<String, Object>();
				String type = null;
				List<Object> newAclArray = new ArrayList<>();
				List<Object> oldAclArray = new ArrayList<>();
				if (props.containsKey("_deleted")) {
					isDeleted = (boolean) props.get("_deleted");
				} else {
					type = props.entrySet().stream()
							.filter(objData -> ((objData.getKey().equals(PropertyIds.OBJECT_TYPE_ID)
									|| objData.getKey().equals("objectTypeId") || objData.getKey().equals("type"))))
							.map(a -> (String) a.getValue()).findFirst().get();
					typeDef = SwaggerHelpers.getTypeDefinition(session, type);
				}
				if (typeDef != null && props.get("acl") != null) {
					newAclArray = (ArrayList<Object>) props.get("acl");
					props.remove("acl");
				}
				Map<String, Object> properties = SwaggerPostHelpers.compileCrudProperties(session, props, typeDef);

				String id = isDeleted == true ? (String) props.get("_id")
						: (String) properties.get(PropertyIds.OBJECT_ID);
				LOG.debug("className: {}, methodName: {}, repoId: {}, ObjectId: {}", "SwaggerPostHelpers",
						"crudOperation", repositoryId, input);
				try {
					ArrayList<Object> relationData = SwaggerHelpers.getDescendantsForRelationObjects(userName, password,
							repositoryId, id);
					if (relationData != null) {
						Map<String, List<Map<String, Object>>> resultedObject = formMapData(session, relationData);
						Map<String, Map<String, List<Map<String, Object>>>> resultedInnerMap = checkingMap(relatedMap,
								resultedObject);
						if (isDeleted) {
							if (resultedObject.get(id) != null && resultedObject.get(id).size() > 0) {
								Map<String, Object> resultObjectProps = resultedObject.get(id).get(0);
								deleteObject(session, resultObjectProps.get("id").toString(), new ArrayList<>());
							}
						} else {
							// acl compare
							List<Ace> aceList = new ArrayList<Ace>();
							if (resultedObject.get(id) != null && resultedObject.get(id).size() > 0) {
								Map<String, Object> resultObjectProps = resultedObject.get(id).get(0);
								oldAclArray = (List<Object>) resultObjectProps.get("acl");
								if (newAclArray != null && newAclArray.size() > 0) {
									if (oldAclArray.containsAll(newAclArray)) {
										aceList = null;
									} else {
										aceList.addAll(formAceList(newAclArray));
									}
								}
							}
							CmisObject objectToUpdate = session.getObject(id);
							CmisObject updatedObj = updateObject(session, objectToUpdate,
									typeDef.getBaseTypeId().value(), properties, null, aceList);
							createForRelationObjects(updatedObj, relatedMap, session, true, aceList, resultedInnerMap);

						}
					} else {
						if (isDeleted) {
							// do nothing, means deleted in our side as well
						} else {
							List<Ace> aceList = new ArrayList<Ace>();
							if (newAclArray != null && newAclArray.size() > 0) {
								aceList.addAll(formAceList(newAclArray));
							}
							CmisObject outerObj = createForMainObject(session, properties, typeDef, null, aceList);
							createForRelationObjects(outerObj, relatedMap, session, false, aceList, null);
							// add attachments here to main object make
							// folder with name objId_attachments, for each
							// attachment add a document
							if (!attachmentProps.isEmpty()) {
								addAttachments(session, attachmentProps, aceList, outerObj.getId(), null, false);
							}
						}
					}
					ArrayList<Object> returnRelationData = SwaggerHelpers.getDescendantsForRelationObjects(userName,
							password, repositoryId, id);
					if (returnRelationData != null) {
						Map<String, Object> resultObject = SwaggerPostHelpers.formRelationDataForCrud(session,
								returnRelationData);
						return ((ArrayList<JSONObject>) resultObject.entrySet().iterator().next().getValue()).get(0);
					} else {
						return new HashMap<>();
					}
				} catch (Exception ex) {
					LOG.error(
							"className: {}, methodName: {}, repoId: {}, Error in getDescendantsForRelationObjects for repoId: {}, Exception: {}",
							"SwaggerPostHelpers", "crudOperation", repositoryId, ex);
				}
			}
		} catch (Exception e) {
			LOG.error(
					"className: {}, methodName: {}, repoId: {}, Exception in compiling properties or type doesn't exist: {}",
					"SwaggerPostHelpers", "crudOperation", repositoryId, e);
		}
		return null;

	}

	private static Map<String, Object> compileCrudProperties(Session session, Map<String, Object> jsonObject,
			TypeDefinition typeDef) {
		try {
			// filter out all keys which have "parent_child" type format in
			// them,
			// and add as main objects
			Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
			Map<String, Object> props = jsonObject.entrySet().stream()
					.filter(a -> a.getValue() != null && !(p.matcher(a.getKey()).find()))
					.collect(Collectors.toMap(a -> getQueryName(a.getKey()), b -> b.getValue()));

			Object id = props.get("_id") != null ? props.get("_id")
					: props.get(PropertyIds.OBJECT_ID) != null ? (String) props.get(PropertyIds.OBJECT_ID)
							: props.get("id") != null ? props.get("id") : UUID.randomUUID().toString();
			if (id != null && typeDef != null) {

				Map<String, Object> propMapForMainObj = new HashMap<String, Object>();
				typeDef.getPropertyDefinitions().entrySet().stream()
						.filter(map -> (!(map.getKey().equalsIgnoreCase("cmis:name")
								|| map.getKey().equalsIgnoreCase("cmis:objectTypeId")
								|| map.getKey().equalsIgnoreCase("cmis:objectId"))))
						.forEach(a -> {
							if (props.get(a.getKey()) != null) {
								propMapForMainObj.put(a.getKey(), props.get(a.getKey()));
							}
						});
				propMapForMainObj.put(PropertyIds.OBJECT_TYPE_ID, typeDef.getId());

				propMapForMainObj.putAll(props.entrySet().stream()
						.filter(obj -> (obj.getKey().equals(PropertyIds.OBJECT_ID) || obj.getKey().equals("id")
								|| obj.getKey().equals("_id")))
						.collect(Collectors.toMap(a -> PropertyIds.OBJECT_ID, b -> String.valueOf(b.getValue()),
								(p1, p2) -> p2)));

				String name = props.get(PropertyIds.NAME) != null ? (String) props.get(PropertyIds.NAME)
						: propMapForMainObj.get(PropertyIds.OBJECT_ID) != null
								? (String) propMapForMainObj.get(PropertyIds.OBJECT_ID) : (String) id;

				propMapForMainObj.put(PropertyIds.NAME, name);

				Map<String, Object> deprops = SwaggerHelpers.deserializeInput(propMapForMainObj, (ObjectType) typeDef,
						session);
				if (LOG.isDebugEnabled()) {
					LOG.debug("className: {}, methodName: {}, repoId: {}, CompileProperties: {}", "SwaggerPostHelpers",
							"compileCrudProperties", session.getRepositoryInfo().getId(), deprops);
				}
				return deprops;
			} else {
				// throw new Exception("Id should not be null");
			}
		} catch (Exception ex) {
			LOG.error("className: {}, methodName: {}, repoId: {}, Error in compileCrudProperties, Exception: {}",
					"SwaggerPostHelpers", "compileCrudProperties", session.getRepositoryInfo().getId(), ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> formRelationDataForCrud(Session session, ArrayList<Object> relationData) {
		Map<String, Object> relMap = new LinkedHashMap<String, Object>();
		if (relationData != null) {
			relationData.forEach(relObj -> {
				JSONObject jsonSub = new JSONObject();
				LinkedHashMap<Object, Object> relationObjectCrudMap = (LinkedHashMap<Object, Object>) relObj;
				LinkedHashMap<Object, Object> getRelationshipCrudData = (LinkedHashMap<Object, Object>) relationObjectCrudMap
						.get("object");
				LinkedHashMap<Object, Object> getRelationshipObjectCrudData = (LinkedHashMap<Object, Object>) getRelationshipCrudData
						.get("object");
				Map<String, Object> succintProps = (Map<String, Object>) getRelationshipObjectCrudData
						.get("succinctProperties");
				JSONObject objmainProps = new JSONObject();

				String relType = succintProps.get(PropertyIds.NAME).toString();
				if (relType.contains(",")) {

					relType = succintProps.get(PropertyIds.NAME).toString()
							.substring(succintProps.get(PropertyIds.NAME).toString().lastIndexOf(",") + 1);
					succintProps.put(PropertyIds.NAME,
							succintProps.get(PropertyIds.NAME).toString().substring(0,
									succintProps.get(PropertyIds.NAME).toString().lastIndexOf(",") == -1
											? succintProps.get(PropertyIds.NAME).toString().length()
											: succintProps.get(PropertyIds.NAME).toString().lastIndexOf(",")));
				}

				objmainProps.putAll(succintProps);
				ArrayList<JSONObject> list = relMap.get(relType) != null ? (ArrayList<JSONObject>) relMap.get(relType)
						: new ArrayList<>();
				jsonSub.putAll(objmainProps);
				ArrayList<Object> childrenRelationDataCrud = (ArrayList<Object>) relationObjectCrudMap.get("children");
				if (childrenRelationDataCrud != null) {
					jsonSub.putAll(formRelationDataForCrud(session, childrenRelationDataCrud));
				}
				list.add(jsonSub);
				relMap.put(relType, list);
			});
		}
		return relMap;
	}

	@SuppressWarnings("unused")
	private static Map<String, Map<String, List<Map<String, Object>>>> checkingMap(
			Map<String, List<Map<String, Object>>> beforeMap, Map<String, List<Map<String, Object>>> afterMap) {
		Map<String, Map<String, List<Map<String, Object>>>> resultedMapValues = new HashMap<>();
		Map<String, List<Map<String, Object>>> updated = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> deleted = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> created = new LinkedHashMap<>();
		beforeMap.entrySet().stream().forEach(k -> {
			if (afterMap.get(k.getKey()) != null) {
				List<Map<String, Object>> afterMapValue = afterMap.get(k.getKey());
				List<Map<String, Object>> updatedChildValue = new ArrayList<>();
				List<Map<String, Object>> DeletedChildValue = new ArrayList<>();
				List<Map<String, Object>> createdChildValue = new ArrayList<>();
				afterMapValue.forEach(childValue -> {
					Map<String, Boolean> childDeletedValue = new HashMap<>();
					childDeletedValue.put("isDeleted", true);
					k.getValue().forEach(afterChildValue -> {
						if (afterChildValue.get("id").toString().equals(childValue.get("id").toString())) {
							updatedChildValue.add(afterChildValue);
							childDeletedValue.remove("isDeleted");
							childDeletedValue.put("isDeleted", false);
						}
					});
					if (childDeletedValue.get("isDeleted")) {
						DeletedChildValue.add(childValue);
					}
				});

				k.getValue().forEach(childValue -> {
					Map<String, Boolean> childCreatedValue = new HashMap<>();
					childCreatedValue.put("isCreated", true);
					afterMapValue.forEach(afterChildValue -> {
						if (afterChildValue.get("id").toString().equals(childValue.get("id").toString())) {
							childCreatedValue.put("isCreated", false);
						}
					});
					if (childCreatedValue.get("isCreated")) {
						createdChildValue.add(childValue);
					}
				});
				updated.put(k.getKey(), updatedChildValue);
				deleted.put(k.getKey(), DeletedChildValue);
				created.put(k.getKey(), createdChildValue);
			} else {
				created.put(k.getKey(), k.getValue());
			}
		});
		resultedMapValues.put(RelationType.CREATED.value(), created);
		resultedMapValues.put(RelationType.UPDATED.value(), updated);
		Map<String, List<Map<String, Object>>> deletedData = deleted.entrySet().stream()
				.filter(t -> t.getValue().size() > 0).collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
		Set<String> removedKeys = new HashSet<String>(afterMap.keySet());
		removedKeys.removeAll(beforeMap.keySet());
		Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
		removedKeys.forEach(k -> {
			if (p.matcher(k).find()) {
				getKeyDeletedObject(k, afterMap, deletedData);
			}
		});
		Set<String> RelationName = deletedData.keySet();
		List<String> deletedObjects = new ArrayList<>();
		if (deletedObjects.size() > 0) {
			deletedObjects.forEach(t -> {
				if (deletedData.containsKey(t)) {
					deletedData.remove(t);
				}

			});
		}

		resultedMapValues.put(RelationType.DELETED.value(), deletedData);
		return resultedMapValues;
	}

	private static CmisObject updateObject(Session tokenSession, CmisObject obj, String type,
			Map<String, Object> properties, Map<String, Object> attachmentProps, List<Ace> aceList) throws Exception {

		LOG.debug("className: {}, methodName: {}, object: {}", "SwaggerPostHelpers", "updateObject-CRUD operation",
				obj.getId());
		properties.remove(PropertyIds.OBJECT_ID);
		properties.remove(PropertyIds.OBJECT_TYPE_ID);
		obj.updateProperties(properties);
		CmisObject updatedObj = tokenSession.getObject(obj.getId());
		// update attachment for document
		if (attachmentProps != null && !attachmentProps.isEmpty()) {
			addAttachments(tokenSession, attachmentProps, null, updatedObj.getId(), null, true);
		}
		// update acl
		if (aceList != null && !aceList.isEmpty()) {
			tokenSession.applyAcl(new ObjectIdImpl(updatedObj.getId()), aceList, null, AclPropagation.OBJECTONLY);
		}
		return updatedObj;

	}

	private static void deleteObject(Session tokenSession, String objectToBeDeleted, List<String> deletedIds) {

		LOG.debug("className: {}, methodName: {}, object: {}", "SwaggerPostHelpers", "deleteObject-CRUD operation",
				objectToBeDeleted);
		deleteAttachments(tokenSession, objectToBeDeleted);
		deleteRelationObjects(tokenSession, objectToBeDeleted, deletedIds);
		tokenSession.delete(new ObjectIdImpl(objectToBeDeleted));

	}

	private static void createForRelationObjects(CmisObject outerObj, Map<String, List<Map<String, Object>>> jsonObject,
			Session session, Boolean isUpdate, List<Ace> aceList,
			Map<String, Map<String, List<Map<String, Object>>>> resultedMap) {

		// filter all keys which have "parent_child" format in their values and
		// add child objects, and create relation with their respective parents.
		// A recursive function
		if (resultedMap != null) {
			if (resultedMap.get(RelationType.DELETED.value()) != null
					&& !resultedMap.get(RelationType.DELETED.value()).isEmpty()) {
				deleteRelationFlow(session, resultedMap.get(RelationType.DELETED.value()));
			}
			if (resultedMap.get(RelationType.CREATED.value()) != null
					&& !resultedMap.get(RelationType.CREATED.value()).isEmpty()) {
				relationFlow(session, resultedMap.get(RelationType.CREATED.value()), RelationType.CREATED, aceList);
			}
			if (resultedMap.get(RelationType.UPDATED.value()) != null
					&& !resultedMap.get(RelationType.UPDATED.value()).isEmpty()) {
				relationFlow(session, resultedMap.get(RelationType.UPDATED.value()), RelationType.UPDATED, aceList);
			}
		} else {
			relationFlow(session, jsonObject, RelationType.CREATED, aceList);
		}
	}

	private static void deleteRelationFlow(Session session, Map<String, List<Map<String, Object>>> jsonObject) {
		List<String> deletedIds = new ArrayList<>();
		jsonObject.entrySet().stream().forEach(k -> {
			if (k.getValue() != null && !k.getValue().isEmpty()) {
				k.getValue().stream().forEach(obj -> {
					String objectId = obj.get("id").toString();
					if (!deletedIds.contains(objectId)) {
						deleteObject(session, objectId, deletedIds);
						deletedIds.add(objectId);
					}
				});
			}
		});

	}

	@SuppressWarnings("unchecked")
	public static List<Ace> formAceList(List<Object> newAclArray) {
		List<Ace> aceList = new ArrayList<Ace>();
		newAclArray.forEach((aclObject) -> {
			Map<String, Object> aclProps = (Map<String, Object>) aclObject;
			AccessControlEntryImpl ace = new AccessControlEntryImpl(
					new AccessControlPrincipalDataImpl(aclProps.get("principalId").toString()),
					Arrays.asList(aclProps.get("permission").toString()));
			aceList.add(ace);
		});
		return aceList;
	}

	@SuppressWarnings("unchecked")
	private static void addAttachments(Session tokenSession, Map<String, Object> attachmentProps, List<Ace> aceList,
			String mainObjId, CmisObject attachFolder, boolean forUpdate) throws Exception {
		if (attachmentProps != null && mainObjId != null) {

			if (attachFolder != null) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("className: {}, methodName: {}, Adding attachments for objectId: {}",
							"SwaggerPostHelpers", "addAttachments", mainObjId);
				}
				attachmentProps.forEach((filename, attachment) -> {
					HashMap<String, Object> attachData = (HashMap<String, Object>) attachment;
					String contentType = (String) attachData.get("content_type");
					String data = (String) attachData.get("data");
					InputStream inputStream = new ReaderInputStream(new StringReader(data), StandardCharsets.UTF_8);
					ContentStream stream = new ContentStreamImpl(filename, BigInteger.valueOf(data.length()),
							contentType, inputStream);

					Map<String, Object> aprops = new HashMap<>();
					aprops.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
					aprops.put(PropertyIds.NAME, filename);

					try {
						@SuppressWarnings("unused")
						CmisObject attachmentObj = SwaggerPostHelpers.createForBaseTypes(tokenSession,
								BaseTypeId.CMIS_DOCUMENT, attachFolder.getId(), aprops, stream);
					} catch (Exception ex) {
						LOG.error("className: {}, methodName: {}, error: {}", "SwaggerPostHelpers", "addAttachments",
								ex);
					}
				});
			} else {
				CmisObject attachFol = null;
				if (forUpdate) {
					attachFol = tokenSession.getObjectByPath("/" + mainObjId + "_attachments");
				} else {
					// folder not present , create new
					Map<String, Object> fprops = new HashMap<>();
					fprops.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
					fprops.put(PropertyIds.NAME, mainObjId + "_attachments");
					attachFol = SwaggerPostHelpers.createForBaseTypes(tokenSession, BaseTypeId.CMIS_FOLDER, null,
							fprops, null);
				}
				addAttachments(tokenSession, attachmentProps, aceList, mainObjId, attachFol, forUpdate);
			}
		}

	}

	private static void deleteAttachments(Session tokenSession, String string) {
		try {
			Folder folderAttachment = (Folder) tokenSession.getObjectByPath("/" + string + "_attachments");
			tokenSession.deleteTree(new ObjectIdImpl(folderAttachment.getId()), true, null, true);
		} catch (Exception ex) {
			// folder doesnt exist, do nothing
		}
	}

	public static String getQueryName(String name) {
		if (name.equalsIgnoreCase("path") || name.equalsIgnoreCase("description") || name.equalsIgnoreCase("parentId")
				|| name.equalsIgnoreCase("contentStreamLength") || name.equalsIgnoreCase("contentStreamFileName")
				|| name.equalsIgnoreCase("contentStreamMimeType") || name.equalsIgnoreCase("checkinComment")
				|| name.equalsIgnoreCase("versionLabel") || name.equalsIgnoreCase("isMajorVersion")
				|| name.equalsIgnoreCase("isLatestVersion") || name.equalsIgnoreCase("isLatestMajorVersion")
				|| name.equalsIgnoreCase("name") || name.equalsIgnoreCase("isPrivateWorkingCopy")
				|| name.equalsIgnoreCase("createdBy") || name.equalsIgnoreCase("contentStreamId")
				|| name.equalsIgnoreCase("versionSeriesCheckedOutId") || name.equalsIgnoreCase("versionSeriesId")
				|| name.equalsIgnoreCase("isVersionSeriesCheckedOut") || name.equalsIgnoreCase("isImmutable")
				|| name.equalsIgnoreCase("modifiedBy") || name.equalsIgnoreCase("versionSeriesCheckedOutBy")) {
			return getFieldName(name);
		} else if (name.equalsIgnoreCase("id")) {
			return "cmis:objectId";
		} else if (name.equalsIgnoreCase("typeId")) {
			return "cmis:objectTypeId";
		} else if (name.equalsIgnoreCase("modifiedBy")) {

			return "cmis:lastModifiedBy";
		} else if (name.equalsIgnoreCase("createdAt")) {
			return "cmis:creationDate";
		} else if (name.equalsIgnoreCase("token")) {
			return "cmis:changeToken";
		} else if (name.equalsIgnoreCase("modifiedAt")) {
			return "cmis:lastModificationDate";
		} else if (name.equalsIgnoreCase("baseId")) {
			return "cmis:baseTypeId";
		} else {
			return name;
		}
	}

	private static String getFieldName(Object value) {
		String valueString = value.toString();
		String stringValue = "cmis:" + valueString;
		return stringValue;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Map<String, Object>>> formMapData(Session session, ArrayList<Object> relationData) {
		Map<String, List<Map<String, Object>>> relatedMap = new HashMap<>();
		if (relationData != null) {
			relationData.forEach(relObj -> {
				LinkedHashMap<Object, Object> relObjMap = (LinkedHashMap<Object, Object>) relObj;
				LinkedHashMap<Object, Object> object = (LinkedHashMap<Object, Object>) relObjMap.get("object");
				LinkedHashMap<Object, Object> getObject = (LinkedHashMap<Object, Object>) object.get("object");
				LinkedHashMap<Object, Object> succintProps = (LinkedHashMap<Object, Object>) getObject
						.get("succinctProperties");
				LinkedHashMap<Object, Object> aclProps = (LinkedHashMap<Object, Object>) getObject.get("acl");
				JSONObject objmainProps = formProperties(session, succintProps, false, true, true, aclProps);
				String relType = succintProps.get(PropertyIds.OBJECT_ID).toString();
				List<Map<String, Object>> resultesMap = new ArrayList<>();
				resultesMap.add(objmainProps);
				relatedMap.put(relType, resultesMap);
				ArrayList<Object> children = (ArrayList<Object>) relObjMap.get("children");
				if (children != null) {
					formChildData(children, relatedMap);
				}
			});
		}
		return relatedMap;
	}

	private static CmisObject createForMainObject(Session session, Map<String, Object> deProps, TypeDefinition typeDef,
			Map<String, Object> foreignKey, List<Ace> aceList) throws Exception {

		String parentId = deProps.get("parentId") == null ? null : deProps.get("parentId").toString();
		if (LOG.isDebugEnabled()) {
			LOG.debug("className: {}, methodName: {}, repositoryId: {}, Creating new Object with properties: {}",
					"SwaggerPostHelpers", "createForMainObject", session.getRepositoryInfo().getId(), deProps);
		}
		CmisObject mainObj = SwaggerPostHelpers.createForBaseTypes(session, typeDef.getBaseTypeId(), parentId, deProps,
				null);
		return mainObj;
	}

	private static Map<String, List<Map<String, Object>>> getKeyDeletedObject(String key,
			Map<String, List<Map<String, Object>>> afterMap, Map<String, List<Map<String, Object>>> deleted) {
		List<Map<String, Object>> object = afterMap.get(key);
		if (object.size() > 0) {
			deleted.put(key, object);
		}
		return deleted;
	}

	public static List<String> deleteRelationObjects(Session session, String id, List<String> deletedIds) {

		ItemIterable<Relationship> source = session.getRelationships(new ObjectIdImpl(id), true,
				RelationshipDirection.SOURCE, null, new OperationContextImpl());
		List<String> targetObjectId = new ArrayList<>();
		source.forEach(relId -> {
			CmisObject targetObject = relId.getTarget();
			targetObjectId.add(targetObject.getId());
			deletedIds.add(targetObject.getId());
			if (LOG.isDebugEnabled()) {
				LOG.debug("className: {}, methodName: {}, repositoryId: {}, Deleting relation object: {}",
						"SwaggerPostHelpers", "deleteRelationObjects", session.getRepositoryInfo().getId(),
						relId.getId());
			}
			relId.delete();
		});
		ItemIterable<Relationship> target = session.getRelationships(new ObjectIdImpl(id), true,
				RelationshipDirection.TARGET, null, new OperationContextImpl());
		target.forEach(relId -> {
			session.delete(new ObjectIdImpl(relId.getId()));
		});
		if (targetObjectId.size() > 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("className: {}, methodName: {}, repositoryId: {}, Deleting target objects: {}",
						"SwaggerPostHelpers", "deleteRelationObjects", session.getRepositoryInfo().getId(),
						targetObjectId);
			}
			for (String deletedId : targetObjectId) {
				deleteRelationObjects(session, deletedId, deletedIds);
				session.delete(new ObjectIdImpl(deletedId));
			}
		}
		return deletedIds;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject formProperties(Session session, LinkedHashMap<Object, Object> succintProps,
			boolean conflicts, boolean mainObject, boolean descendants, LinkedHashMap<Object, Object> aclProps) {
		JSONObject newObj = new JSONObject();
		succintProps.entrySet().stream().forEach(map -> {
			String type = (String) succintProps.get(PropertyIds.OBJECT_TYPE_ID);
			String key = (String) map.getKey();
			if (isBaseType(type)) {
				if (key.equals(PropertyIds.NAME)) {
					String v = map.getValue().toString();
					newObj.put(key, succintProps.put(key,
							v.substring(0, v.lastIndexOf(",") == -1 ? v.length() : v.lastIndexOf(","))));
				} else {
					newObj.put(key, map.getValue());
					if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
						if (descendants) {
							newObj.put("id", map.getValue());
						} else {
							newObj.put("_id", map.getValue());
						}
					}
				}

			} else {
				if (!(key.equalsIgnoreCase(PropertyIds.NAME) || key.equalsIgnoreCase(PropertyIds.LAST_MODIFIED_BY)
						|| key.equalsIgnoreCase(PropertyIds.CREATED_BY) || key.equalsIgnoreCase(PropertyIds.PATH)
						|| key.equalsIgnoreCase(PropertyIds.DESCRIPTION)
						|| key.equalsIgnoreCase(PropertyIds.CHANGE_TOKEN)
						|| key.equalsIgnoreCase(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS)
						|| key.equalsIgnoreCase(PropertyIds.PARENT_ID) || key.equalsIgnoreCase(PropertyIds.BASE_TYPE_ID)
						|| key.equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)
						|| key.equalsIgnoreCase(PropertyIds.CREATION_DATE)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_LENGTH)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_FILE_NAME)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)
						|| key.equalsIgnoreCase(PropertyIds.CHECKIN_COMMENT)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_LABEL)
						|| key.equalsIgnoreCase(PropertyIds.IS_MAJOR_VERSION)
						|| key.equalsIgnoreCase(PropertyIds.IS_LATEST_VERSION)
						|| key.equalsIgnoreCase(PropertyIds.CONTENT_STREAM_ID)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)
						|| key.equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)
						|| key.equalsIgnoreCase("cmis:previousVersionObjectId")
						|| key.equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)
						|| key.equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)
						|| key.equalsIgnoreCase(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)
						|| key.equalsIgnoreCase(PropertyIds.IS_LATEST_MAJOR_VERSION)
						|| key.equalsIgnoreCase("includeForReplication") || key.equalsIgnoreCase("hasAttachments")
						|| key.equalsIgnoreCase(PropertyIds.IS_IMMUTABLE))) {
					if (key.equalsIgnoreCase("conflictRelationId")) {
						if (descendants) {
							newObj.put(key, (List<String>) map.getValue());
						}
					}
					if (mainObject) {
						if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							if (descendants) {
								newObj.put("id", map.getValue());
							} else {
								newObj.put("_id", map.getValue());
							}
						} else if (key.equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)) {
							newObj.put("type", map.getValue());
						}
					} else {
						if (key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							newObj.put("id", map.getValue());
						} else if (!key.equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)
								&& !key.equalsIgnoreCase("revisionId") && !key.equalsIgnoreCase("conflictRelationId")
								&& !key.equalsIgnoreCase(PropertyIds.OBJECT_ID)) {
							newObj.put(key, map.getValue());
						}
					}
				}
			}
		});
		// do we need to include conflicts for relation objects?
		if (session != null) {
			if (mainObject && aclProps != null) {
				List<Map<String, String>> aclList = new ArrayList<Map<String, String>>();
				List<Map<Object, Object>> acesList = (List<Map<Object, Object>>) aclProps.get("aces");
				if (acesList != null && acesList.size() > 0) {
					acesList.stream().forEach(ace -> {
						Map<Object, Object> principal = (HashMap<Object, Object>) ace.get("principal");
						List<String> permission = (List<String>) ace.get("permissions");
						Map<String, String> aceMap = new HashMap<String, String>();
						aceMap.put("principalId", principal.get("principalId").toString());
						aceMap.put("permission", permission.get(0));
						aclList.add(aceMap);
					});
					newObj.put("acl", aclList);
				}
			}
		}

		return newObj;
	}

	public static boolean isBaseType(String type) {
		try {
			BaseTypeId value = BaseTypeId.fromValue(type);
			if (value != null) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private static void relationFlow(Session session, Map<String, List<Map<String, Object>>> jsonObject,
			RelationType type, List<Ace> aceList) {
		Map<String, Map<String, Object>> relationShip = new LinkedHashMap<>();
		jsonObject.entrySet().stream().forEach(k -> {
			String relType = k.getKey();

			CmisObject relTypeItem = session.getObjectByPath("/cmis_ext:relationmd/" + relType);
			String targetType = relTypeItem.getPropertyValue("target_table");
			Map<String, Object> foreignKey = new HashMap<>();

			if (k.getValue() != null && !k.getValue().isEmpty()) {
				k.getValue().stream().forEach(obj -> {
					Map<String, Object> sectionMap = (Map<String, Object>) obj;
					TypeDefinition targetTypeDef = session.getTypeDefinition(targetType);
					Map<String, Object> deProps = compileCrudProperties(session, sectionMap, targetTypeDef);
					if (type.equals(RelationType.CREATED)) {
						createNewObject(session, deProps, targetTypeDef, foreignKey, aceList,
								obj.get("sourceParentId").toString(), relType, relationShip);
					} else if (type.equals(RelationType.UPDATED)) {
						updateRelationObject(session, deProps, sectionMap, targetTypeDef, aceList,
								obj.get("sourceParentId").toString(), relType);
					}
				});
			}
		});
		if (!relationShip.isEmpty() && relationShip.size() > 0) {
			createNewRelationShipsObject(session, relationShip, aceList);
		}
	}

	private static Map<String, Object> createNewObject(Session session, Map<String, Object> deProps,
			TypeDefinition targetTypeDef, Map<String, Object> foreignKey, List<Ace> aceList, String parentId,
			String relName, Map<String, Map<String, Object>> relationShip) {
		try {
			LOG.info("class name: {}, method name: {}, repositoryId: {}", "SwaggerPostHelpers", "createNewObject",
					session.getRepositoryInfo().getId());
			CmisObject innerObject = createForMainObject(session, deProps, targetTypeDef, foreignKey, aceList);
			Map<String, Object> relationShipObjects = new HashMap<>();
			relationShipObjects.put("sourceId", parentId);
			relationShipObjects.put("targetId", innerObject.getId());
			relationShipObjects.put("relType", relName);
			relationShip.put(innerObject.getId(), relationShipObjects);
		} catch (Exception ex) {
			LOG.error("class name: {}, method name: {}, repositoryId: {}, Exception: {}", "SwaggerPostHelpers",
					"createNewObject", session.getRepositoryInfo().getId(), ex);
		}
		return deProps;
	}

	private static void updateRelationObject(Session session, Map<String, Object> deprops,
			Map<String, Object> sectionMap, TypeDefinition targetTypeDef, List<Ace> aceList, String sourceId,
			String relName) {
		try {
			CmisObject updateObj = session.getObject(deprops.get(PropertyIds.OBJECT_ID).toString());
			updateObject(session, updateObj, targetTypeDef.getBaseTypeId().value(), deprops, null, aceList);
		} catch (Exception ex) {
			LOG.error(
					"class name: {}, method name: {}, repositoryId: {}, Error in updateRelationObject for id: {}, Cause: {}",
					"SwaggerPostHelpers", "updateRelationObject", session.getRepositoryInfo().getId(),
					deprops.get(PropertyIds.OBJECT_ID), ex);
		}
	}

	private static void createNewRelationShipsObject(Session session, Map<String, Map<String, Object>> relationShip,
			List<Ace> aceList) {
		try {
			relationShip.entrySet().stream().forEach(k -> {
				if (k.getValue() != null && k.getValue().size() > 0) {
					createRelationship(session, k.getValue().get("sourceId").toString(),
							k.getValue().get("targetId").toString(), k.getValue().get("relType").toString(), aceList);
				}
			});
		} catch (Exception ex) {
			LOG.error(
					"class name: {}, method name: {}, repositoryId: {}, Error while creating relationships, Exception: {}",
					"SwaggerPostHelpers", "createNewRelationShipsObject", session.getRepositoryInfo().getId(), ex);
		}
	}

	private static void createRelationship(Session session, String sourceId, String targetId, String objectTypeId,
			List<Ace> aceList) {

		if (LOG.isDebugEnabled()) {
			LOG.debug(
					"class name: {}, method name: {}, repositoryId: {}, Creating relationship for source: {}, target: {}",
					"SwaggerPostHelpers", "createRelationship", session.getRepositoryInfo().getId(), sourceId,
					targetId);
		}
		Map<String, Object> relProps = new HashMap<String, Object>();
		relProps.put(PropertyIds.SOURCE_ID, sourceId);
		relProps.put(PropertyIds.TARGET_ID, targetId);
		relProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis_ext:relationship");
		relProps.put("relation_name", objectTypeId);
		relProps.put(PropertyIds.NAME, sourceId + "_" + targetId);
		session.createRelationship(relProps, null, aceList, null);
	}

	public static CmisObject createForBaseTypes(Session session, BaseTypeId baseTypeId, String parentId,
			Map<String, Object> input, ContentStream stream) throws Exception {
		try {
			LOG.debug("class name: {}, method name: {}, repositoryId: {}, baseTypeId: {}", "SwaggerPostHelpers",
					"createForBaseTypes", session.getRepositoryInfo().getId(), baseTypeId.value());
			if (baseTypeId.equals(BaseTypeId.CMIS_FOLDER)) {
				CmisObject folder = null;
				if (parentId != null) {
					folder = ((Folder) session.getObject(parentId)).createFolder(input);
					return folder;
				} else {
					ObjectId id = session.getRootFolder().createFolder(input);
					folder = session.getObject(id);
					return folder;
				}
			} else if (baseTypeId.equals(BaseTypeId.CMIS_DOCUMENT)) {
				CmisObject document = null;
				if (parentId != null) {
					document = ((Folder) session.getObject(parentId)).createDocument(input,
							stream != null ? stream : null, null);
					return document;
				} else {
					ObjectId id = session.createDocument(input, null, stream != null ? stream : null, null);
					document = session.getObject(id);
					return document;
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
				CmisObject relationship = session.getObject(id);
				return relationship;
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

	@SuppressWarnings("unchecked")
	public static Map<String, List<Map<String, Object>>> formChildData(ArrayList<Object> relationData,
			Map<String, List<Map<String, Object>>> relatedMap) {

		if (relationData != null) {
			relationData.forEach(relObj -> {

				LinkedHashMap<Object, Object> relObjMap = (LinkedHashMap<Object, Object>) relObj;
				LinkedHashMap<Object, Object> childObject = (LinkedHashMap<Object, Object>) relObjMap.get("object");
				LinkedHashMap<Object, Object> getChildObj = (LinkedHashMap<Object, Object>) childObject.get("object");
				LinkedHashMap<Object, Object> succintProps = (LinkedHashMap<Object, Object>) getChildObj
						.get("succinctProperties");
				JSONObject objmainProps = null;
				String relType = succintProps.get(PropertyIds.NAME).toString();
				if (relType.contains(",")) {
					objmainProps = formProperties(null, succintProps, false, false, true, null);
					relType = succintProps.get(PropertyIds.NAME).toString()
							.substring(succintProps.get(PropertyIds.NAME).toString().lastIndexOf(",") + 1);
				} else {
					objmainProps = formProperties(null, succintProps, false, true, true, null);
				}
				List<Map<String, Object>> resultesMap = new ArrayList<>();
				resultesMap.add(objmainProps);
				List<Map<String, Object>> listPrvs = relatedMap.get(relType);
				if (listPrvs == null) {
					listPrvs = new ArrayList<>();
				}
				listPrvs.addAll(resultesMap);
				ArrayList<Object> childrenMap = (ArrayList<Object>) relObjMap.get("children");
				if (childrenMap != null) {
					formChildData(childrenMap, relatedMap);
				}
				relatedMap.put(relType, listPrvs);
			});
		}
		return relatedMap;
	}

	@SuppressWarnings("unchecked")
	private static void getMapValue(Map<String, Object> props, Map<String, List<Map<String, Object>>> relatedMap) {
		Pattern p = Pattern.compile("[a-zA-Z]_[a-zA-Z]");
		List<String> id = new ArrayList<>();
		if (props.get("_id") != null) {
			id.add(props.get("_id").toString());
		} else if (props.get("id") != null) {
			id.add(props.get("id").toString());
		} else {
			String uuid = UUID.randomUUID().toString();
			id.add(uuid);
			props.put("id", uuid);
		}

		Map<String, Object> propsObject = props.entrySet().stream()
				.filter(a -> a.getValue() != null && p.matcher(a.getKey()).find())
				.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
		if (propsObject != null) {
			propsObject.entrySet().stream().forEach(obj -> {
				String relType = obj.getKey();
				ArrayList<Object> headerSectionArray = (ArrayList<Object>) obj.getValue();
				if (headerSectionArray != null && !headerSectionArray.isEmpty() && headerSectionArray.size() > 0) {
					getChildObject(relType, headerSectionArray, relatedMap, p, id);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static void getChildObject(String key, ArrayList<Object> headerSectionArray,
			Map<String, List<Map<String, Object>>> relatedMap, Pattern p, List<String> id) {
		List<Map<String, Object>> listResultMap = new ArrayList<>();
		if (headerSectionArray != null) {
			headerSectionArray.forEach(section -> {
				Map<String, Object> sectionMap = (Map<String, Object>) section;
				getMapValue(sectionMap, relatedMap);
				Map<String, Object> result = sectionMap.entrySet().stream()
						.filter(a -> a.getValue() != null && !p.matcher(a.getKey()).find())
						.collect(Collectors.toMap(a -> a.getKey(), b -> b.getValue()));
				result.put("sourceParentId", id.get(0));
				listResultMap.add(result);
			});
		}
		if (relatedMap.get(key) != null) {
			List<Map<String, Object>> newList = new ArrayList<>(relatedMap.get(key));
			newList.addAll(listResultMap);
			relatedMap.remove(key);
			relatedMap.put(key, newList);
		} else {
			relatedMap.put(key, listResultMap);
		}
	}
}
