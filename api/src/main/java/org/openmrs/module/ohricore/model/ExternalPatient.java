package org.openmrs.module.ohricore.model;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * @author smallGod date: 07/12/2022
 */
public class ExternalPatient extends BaseOpenmrsMetadata {
	
	private Integer id;
	
	private String familyName;
	
	private String givenName;
	
	public ExternalPatient() {
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
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
