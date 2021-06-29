package org.openmrs.module.ohricore;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
public enum ConceptUUID {
	
	STRING1("cebc1b1e-70ea-4e6b-813e-59a5a0a4d23b"), STRING2("992a28d9-642e-4395-a8f5-d445b45282d2"), STRING3(
	        "3949c668-b9c0-4d31-847c-996ac75190bd"), HIV_STATUS("UUID"), //TODO: put in correct UUID
	UNKNOWN("UNKNOWN");
	
	private final String uuid;
	
	ConceptUUID(String uuid) {
		this.uuid = uuid;
	}
	
	public static ConceptUUID convert(String uuid) {
		
		if (uuid != null) {
			for (ConceptUUID uuidEnum : ConceptUUID.values()) {
				if (uuid.equalsIgnoreCase(uuidEnum.getUUID())) {
					return uuidEnum;
				}
			}
		}
		throw new EnumConstantNotPresentException(ConceptUUID.class, uuid);
	}
	
	public String getUUID() {
		return this.uuid;
	}
}
