package org.openmrs.module.ohricore.web.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author smallGod date: 07/12/2022
 */

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + ExternalPatientsSearchController.MPI_REST_NAMESPACE)
public class ExternalPatientsSearchController extends MainResourceController {
	
	public static final String MPI_REST_NAMESPACE = "/mpi";
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + MPI_REST_NAMESPACE;
	}
}
