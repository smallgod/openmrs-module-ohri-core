package org.openmrs.module.ohricore.fhir;

/**
 * @author smallGod
 * date: 27/07/2022
 */
public class FhirCoding {

    private String system;
    private String version;
    private String code;
    private String display;
    private boolean userSelected;

    public FhirCoding() {
    }

    public FhirCoding(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public boolean isUserSelected() {
        return userSelected;
    }

    public void setUserSelected(boolean userSelected) {
        this.userSelected = userSelected;
    }
}
