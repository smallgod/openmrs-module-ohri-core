package org.openmrs.module.ohricore.web.resource;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.ExternalPatientSearchService;
import org.openmrs.module.ohricore.api.model.ExternalPatient;
import org.openmrs.module.ohricore.web.controller.ExternalPatientsSearchController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author smallGod date: 07/12/2022
 */
@Resource(name = RestConstants.VERSION_1 + ExternalPatientsSearchController.MPI_REST_NAMESPACE + "/external-patient", supportedClass = Patient.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class ExternalPatientSearchResource extends DelegatingCrudResource<ExternalPatient> {
	
	private ExternalPatientSearchService getService() {
		return Context.getService(ExternalPatientSearchService.class);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		
		DelegatingResourceDescription resourceDescription = new DelegatingResourceDescription();
		if (representation instanceof RefRepresentation) {
			this.addSharedResourceDescriptionProperties(resourceDescription);
			resourceDescription.addProperty("familyName", Representation.REF);
			resourceDescription.addProperty("givenName", Representation.REF);
			resourceDescription.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			
		} else if (representation instanceof DefaultRepresentation) {
			System.out.println("default rep...");
			this.addSharedResourceDescriptionProperties(resourceDescription);
			resourceDescription.addProperty("familyName", Representation.DEFAULT);
			resourceDescription.addProperty("givenName", Representation.DEFAULT);
			resourceDescription.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			
		} else if (representation instanceof FullRepresentation) {
			this.addSharedResourceDescriptionProperties(resourceDescription);
			resourceDescription.addProperty("familyName", Representation.FULL);
			resourceDescription.addProperty("givenName", Representation.FULL);
			
		} else if (representation instanceof CustomRepresentation) {
			resourceDescription = null;
		}
		
		return resourceDescription;
	}
	
	private void addSharedResourceDescriptionProperties(DelegatingResourceDescription resourceDescription) {
		resourceDescription.addSelfLink();
		resourceDescription.addProperty("uuid");
		resourceDescription.addProperty("display");
		resourceDescription.addProperty("name");
		resourceDescription.addProperty("description");
		resourceDescription.addProperty("familyName");
		resourceDescription.addProperty("givenName");
	}
	
	@PropertyGetter("display")
	public String getDisplay(ExternalPatient patient) {
		return "External Patient Name";
	}
	
	@Override
    protected PageableResult doSearch(RequestContext requestContext) {

        System.out.println("doSearch(): " + requestContext.getParameter("healthid"));

        List<ExternalPatient> patients = new ArrayList<>();
        String healthId = requestContext.getParameter("healthid");

        if (StringUtils.isNotBlank(healthId)) {
			ExternalPatient patient = getService().getPatientByHealthId(healthId);
            patients.add(patient);
        }
        return new NeedsPaging(patients, requestContext);
    }
	
	@Override
	public ExternalPatient getByUniqueId(String healthId) {
		System.out.println("getByUniqueId(): " + healthId);
		if (StringUtils.isNotBlank(healthId)) {
			return getService().getPatientByHealthId(healthId);
		}
		return null;
	}
	
	@Override
	protected void delete(ExternalPatient patient, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ExternalPatient newDelegate() {
		return null;
	}
	
	@Override
	public ExternalPatient save(ExternalPatient patient) {
		return null;
	}
	
	@Override
	public void purge(ExternalPatient patient, RequestContext requestContext) throws ResponseException {
		
	}
}
