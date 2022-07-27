package org.openmrs.module.ohricore.fhir;

import java.util.ArrayList;
import java.util.List;

/**
 * @author smallGod
 * date: 27/07/2022
 */
public class FhirObsResourceType {

    private String patientId;
    private String valueString;
    private String valueDateTime;
    private String valueBoolean;
    private String valueInteger;
    private FhirCodeableConcept valueCodeableConcept;

    private FhirCodeableConcept code;
    private List<FhirCoding> coding;

    public FhirObsResourceType() {
        coding = new ArrayList<>();
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public String getValueDateTime() {
        return valueDateTime;
    }

    public void setValueDateTime(String valueDateTime) {
        this.valueDateTime = valueDateTime;
    }

    public String getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(String valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public String getValueInteger() {
        return valueInteger;
    }

    public void setValueInteger(String valueInteger) {
        this.valueInteger = valueInteger;
    }

    public FhirCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    public void setValueCodeableConcept(FhirCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }

    public FhirCodeableConcept getCode() {
        return code;
    }

    public void setCode(FhirCodeableConcept code) {
        this.code = code;
    }

    public List<FhirCoding> getCoding() {
        return coding;
    }

    public void setCoding(List<FhirCoding> coding) {
        this.coding = coding;
    }
}
