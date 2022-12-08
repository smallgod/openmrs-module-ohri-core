package org.openmrs.module.ohricore.api.model;

import org.hl7.fhir.r4.model.Patient;

/**
 * @author smallGod date: 07/12/2022
 */
public class ExternalPatient extends Patient {
	
	private String familyName;
	
	private String givenName;
	
	public ExternalPatient() {
		super();
	}
	
	public String getFamilyName() {
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getGivenName() {
		return givenName;
	}
	
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
}
