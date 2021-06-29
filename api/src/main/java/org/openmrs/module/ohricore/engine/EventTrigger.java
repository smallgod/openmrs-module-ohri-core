package org.openmrs.module.ohricore.engine;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 24/06/2021
 */
public enum EventTrigger {
	
	SAVE_ENCOUNTER("saveEncounter"), UNKNOWN("UNKNOWN");
	
	private final String triggerName;
	
	EventTrigger(String value) {
		this.triggerName = value;
	}
	
	public static EventTrigger convertToEnum(String enumValue) {
		
		if (enumValue != null) {
			for (EventTrigger value : EventTrigger.values()) {
				if (enumValue.equalsIgnoreCase(value.getValue())) {
					return value;
				}
			}
		}
		return EventTrigger.UNKNOWN;
	}
	
	public String getValue() {
		return this.triggerName;
	}
}
