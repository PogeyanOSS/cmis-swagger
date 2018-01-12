package com.pogeyan.swagger.sync.factory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pogeyan.swagger.impl.factory.IObjectFacade;

@SuppressWarnings("unused")
public class SwaggerSyncFactory implements IObjectFacade {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerSyncFactory.class);

	@Override
	public boolean beforeDelete(Session session, CmisObject obj) throws Exception {
		try {
			TypeDefinition deleteType = session.getTypeDefinition("cmis:deletedObjects");
			PropertyDefinition<?> deletedObjectIdProperty = deleteType.getPropertyDefinitions().get("deletedObjectId");
			PropertyDefinition<?> deletedRevisionIdsProperty = deleteType.getPropertyDefinitions()
					.get("deletedRevisionIds");
			List<String> revIds = obj.getPropertyValue("revisionId");
			Map<String, Object> delprops = new HashMap<String, Object>();
			delprops.put(PropertyIds.OBJECT_TYPE_ID, "cmis:deletedObjects");
			delprops.put(PropertyIds.NAME, obj.getId());
			delprops.put(deletedObjectIdProperty.getId(), obj.getId());
			delprops.put(deletedRevisionIdsProperty.getId(), revIds);
			ObjectId obj2 = session.createItem(delprops, null);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return true;
	}

	@Override
	public Map<String, Object> beforecreate(Session session, Map<String, Object> input) throws Exception {
		// TODO Auto-generated method stub
		try {
			List<String> revIds = new ArrayList<String>();
			TypeDefinition revisionType = session.getTypeDefinition("cmis:revision");
			PropertyDefinition<?> revisionProperty = revisionType.getPropertyDefinitions().get("revisionId");

			List<Object> secondaryTypes = new ArrayList<Object>();
			secondaryTypes.add(revisionType.getId());
			input.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);
			revIds.add(generateFirstGenRevisionId(String.valueOf((new Object()).hashCode())));
			input.put(revisionProperty.getId(), revIds);
		} catch (Exception e) {
			LOG.error("cmis:revision type not present:", e.getMessage());
			throw new Exception("cmis:revision type not present" + e.getMessage());
		}
		return input;
	}

	@Override
	public Map<String, Object> beforeUpdate(Session session, Map<String, Object> input, List<String> revIds)
			throws Exception {
		// TODO Auto-generated method stub
		try {
			TypeDefinition revisionType = session.getTypeDefinition("cmis:revision");
			PropertyDefinition<?> revisionProperty = revisionType.getPropertyDefinitions().get("revisionId");

			if (revIds != null) {
				List<Object> secondaryTypes = new ArrayList<Object>();
				secondaryTypes.add(revisionType.getId());
				input.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);

				String[] splitrevisionId = revIds.get(revIds.size() - 1).toString().split("-");
				revIds.add((Integer.parseInt(splitrevisionId[0]) + 1) + "-"
						+ generateMD5hash(String.valueOf((new Object()).hashCode())));
				input.put(revisionProperty.getId(), revIds);
				return input;
			}

		} catch (Exception ex) {
			LOG.error("cmis:revision type not present");
			throw new Exception("cmis:revision type not present" + ex.getMessage());
		}
		return null;
	}

	/**
	 * 
	 * @param input
	 *            a hash code value for a object
	 * @return first generation mdhash revision value
	 */
	private static String generateFirstGenRevisionId(String input) {
		return "1-" + generateMD5hash(input);

	}

	/**
	 * 
	 * @param input
	 *            a hash code value for a object
	 * @return mdhash value
	 */
	private static String generateMD5hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
