package org.openmrs.module.ohricore.aop;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.module.ohricore.fhir.FhirClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author smallGod date: 30/11/2022
 */
public class PatientInterceptorAdvice implements AfterReturningAdvice {
	
	private static final Logger log = LoggerFactory.getLogger(PatientInterceptorAdvice.class);
	
	@Override
    public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
            throws Throwable {

        System.out.println("After returning Patient Save in EMR");
        try {
            if (methodInvoked.getName().equals(ConceptComputeTrigger.SAVE_PATIENT)) {

                System.out.println("Save Patient method()...");

                for (Object arg : methodArgs) {
                    if (arg instanceof org.openmrs.Patient) {

                        org.openmrs.Patient patient = (org.openmrs.Patient) arg;
                        Patient newPatient = new Patient();

                        Enumerations.AdministrativeGender patientGender;
                        String gender = patient.getGender();
                        switch (gender) {
                            case "male":
                                patientGender = Enumerations.AdministrativeGender.MALE;
                                break;
                            case "female":
                                patientGender = Enumerations.AdministrativeGender.FEMALE;
                                break;
                            default:
                                patientGender = Enumerations.AdministrativeGender.UNKNOWN;
                                break;
                        }
//check if health id exists on MPI (send if doesn't exist) otherwise don't
// -> add global property for health ID
// expose endpoint for search
                        HumanName name = new HumanName();
                        name.setFamily(patient.getFamilyName());
                        name.setGiven(Collections.singletonList(new StringType(patient.getGivenName())));
                        name.setUse(HumanName.NameUse.USUAL);

                        Set<PersonAddress> personAddresses = patient.getAddresses();
                        List<Address> addresses = new ArrayList<>();
                        for (PersonAddress personAddress : personAddresses) {

                            Address address = new Address();
                            address.setUse(Address.AddressUse.HOME);
                            address.setCountry(personAddress.getCountry());
                            address.setCity(personAddress.getAddress1());
                            address.setState(personAddress.getCountry());
                            addresses.add(new Address());
                        }

                        List<PatientIdentifier> patientIds = patient.getActiveIdentifiers();
                        List<Identifier> identifiers = new ArrayList<>();
                        for(PatientIdentifier id: patientIds){
                            Identifier identifier = new Identifier();
                            identifier.setSystem("urn:oid:4.0");//TODO: don't hardcode
                            identifier.setValue(id.getIdentifier());
                            identifiers.add(identifier);
                        }

                        newPatient.setIdentifier(identifiers);
                        newPatient.setGender(patientGender);
                        newPatient.setName(Collections.singletonList(name));
                        newPatient.setBirthDate(patient.getBirthdate());
                        newPatient.setAddress(addresses);


                        System.out.println("Patient: " + newPatient);
                        //String response = FhirClient.postMPIRequest(newPatient);
                        FhirClient.postPatient(newPatient);
                        //System.out.println("Patient registered: " + response);
                    }
                }
            }
        } catch (Exception e) {
            log.error("An un-expected Error occurred while registering a patient: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
