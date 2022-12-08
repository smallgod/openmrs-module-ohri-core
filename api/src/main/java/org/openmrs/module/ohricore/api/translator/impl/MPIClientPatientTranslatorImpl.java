package org.openmrs.module.ohricore.api.translator.impl;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.openmrs.module.ohricore.api.MPIClientPatient;
import org.openmrs.module.ohricore.api.translator.MPIClientPatientTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notNull;
//import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import java.util.Date;

/**
 * @author smallGod date: 08/12/2022
 */
@Component
public class MPIClientPatientTranslatorImpl implements MPIClientPatientTranslator {
	
	@Autowired
	private PatientIdentifierTranslator identifierTranslator;
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Autowired
	private BirthDateTranslator birthDateTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private FhirPersonDao fhirPersonDao;
	
	@Autowired
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Override
	public MPIClientPatient toFhirResource(@Nonnull org.openmrs.Patient openmrsPatient) {
		notNull(openmrsPatient, "The Openmrs Patient object should not be null");
		
		MPIClientPatient patient = new MPIClientPatient();
		patient.setId(openmrsPatient.getUuid());
		patient.setActive(!openmrsPatient.getVoided());
		
		for (PatientIdentifier identifier : openmrsPatient.getActiveIdentifiers()) {
			patient.addIdentifier(identifierTranslator.toFhirResource(identifier));
		}
		
		for (PersonName name : openmrsPatient.getNames()) {
			patient.addName(nameTranslator.toFhirResource(name));
		}
		
		if (openmrsPatient.getGender() != null) {
			patient.setGender(genderTranslator.toFhirResource(openmrsPatient.getGender()));
		}
		
		patient.setBirthDateElement(birthDateTranslator.toFhirResource(openmrsPatient));
		
		if (openmrsPatient.getDead()) {
			if (openmrsPatient.getDeathDate() != null) {
				patient.setDeceased(new DateTimeType(openmrsPatient.getDeathDate()));
			} else {
				patient.setDeceased(new BooleanType(true));
			}
		} else {
			patient.setDeceased(new BooleanType(false));
		}
		
		for (PersonAddress address : openmrsPatient.getAddresses()) {
			patient.addAddress(addressTranslator.toFhirResource(address));
		}
		
		patient.setTelecom(getPatientContactDetails(openmrsPatient));
		patient.getMeta().setLastUpdated(getLastUpdated(openmrsPatient));
		
		return patient;
	}
	
	public List<ContactPoint> getPatientContactDetails(@Nonnull org.openmrs.Patient patient) {
        return fhirPersonDao
                .getActiveAttributesByPersonAndAttributeTypeUuid(patient,
                        globalPropertyService.getGlobalProperty(FhirConstants.PERSON_CONTACT_POINT_ATTRIBUTE_TYPE))
                .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
    }
	
	@Override
	public org.openmrs.Patient toOpenmrsType(@Nonnull MPIClientPatient fhirPatient) {
		notNull(fhirPatient, "The Patient object should not be null");
		return toOpenmrsType(new org.openmrs.Patient(), fhirPatient);
	}
	
	@Override
    public org.openmrs.Patient toOpenmrsType(@Nonnull org.openmrs.Patient currentPatient, @Nonnull MPIClientPatient patient) {
        notNull(currentPatient, "The existing Openmrs Patient object should not be null");
        notNull(patient, "The Patient object should not be null");

        currentPatient.setUuid(patient.getId());

        for (Identifier identifier : patient.getIdentifier()) {
            PatientIdentifier omrsIdentifier = identifierTranslator.toOpenmrsType(identifier);
            if (omrsIdentifier != null) {
                currentPatient.addIdentifier(omrsIdentifier);
            }
        }

        for (HumanName name : patient.getName()) {
            currentPatient.addName(nameTranslator.toOpenmrsType(name));
        }

        if (patient.hasGender()) {
            currentPatient.setGender(genderTranslator.toOpenmrsType(patient.getGender()));
        }

        if (patient.hasBirthDateElement()) {
            birthDateTranslator.toOpenmrsType(currentPatient, patient.getBirthDateElement());
        }

        if (patient.hasDeceased()) {
            try {
                patient.getDeceasedBooleanType();

                currentPatient.setDead(patient.getDeceasedBooleanType().booleanValue());
            }
            catch (FHIRException ignored) {}

            try {
                patient.getDeceasedDateTimeType();

                currentPatient.setDead(true);
                currentPatient.setDeathDate(patient.getDeceasedDateTimeType().getValue());
            }
            catch (FHIRException ignored) {}
        }

        for (Address address : patient.getAddress()) {
            currentPatient.addAddress(addressTranslator.toOpenmrsType(address));
        }

        patient.getTelecom().stream()
                .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
                .distinct().filter(Objects::nonNull).forEach(currentPatient::addAttribute);

        return currentPatient;
    }
	
	public void setIdentifierTranslator(PatientIdentifierTranslator identifierTranslator) {
		this.identifierTranslator = identifierTranslator;
	}
	
	public void setNameTranslator(PersonNameTranslator nameTranslator) {
		this.nameTranslator = nameTranslator;
	}
	
	public void setGenderTranslator(GenderTranslator genderTranslator) {
		this.genderTranslator = genderTranslator;
	}
	
	public void setBirthDateTranslator(BirthDateTranslator birthDateTranslator) {
		this.birthDateTranslator = birthDateTranslator;
	}
	
	public void setAddressTranslator(PersonAddressTranslator addressTranslator) {
		this.addressTranslator = addressTranslator;
	}
	
	public void setGlobalPropertyService(FhirGlobalPropertyService globalPropertyService) {
		this.globalPropertyService = globalPropertyService;
	}
	
	public void setFhirPersonDao(FhirPersonDao fhirPersonDao) {
		this.fhirPersonDao = fhirPersonDao;
	}
	
	public void setTelecomTranslator(TelecomTranslator<BaseOpenmrsData> telecomTranslator) {
		this.telecomTranslator = telecomTranslator;
	}
	
	//TODO: This piece of code is already defined in teh fhir2 module
	// Remove it from here
	public static Date getLastUpdated(OpenmrsObject object) {
		if (object instanceof Auditable) {
			Auditable auditable = (Auditable) object;
			
			if (auditable.getDateChanged() != null) {
				return auditable.getDateChanged();
			} else {
				return auditable.getDateCreated();
			}
		}
		
		return null;
	}
}
