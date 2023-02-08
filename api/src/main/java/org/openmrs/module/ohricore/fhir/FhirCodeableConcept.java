package org.openmrs.module.ohricore.fhir;

import java.util.ArrayList;
import java.util.List;

/**
 * @author smallGod date: 27/07/2022
 */
public class FhirCodeableConcept {
	
	private List<FhirCoding> coding;
	
	private String text;
	
	public FhirCodeableConcept() {
        coding = new ArrayList<>();
    }
	
	public List<FhirCoding> getCoding() {
		return coding;
	}
	
	public void setCoding(List<FhirCoding> coding) {
		this.coding = coding;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
