package org.openmrs.module.ohricore.engine;

import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;

import java.util.List;

/**
 * @author smallGod date: 28/06/2021
 */
public class OHRIComputedConceptsFactory {
	
	public static List<OHRIComputedConcept> getComputedConcepts() {
		
		List<OHRIComputedConcept> ohriComputedConcepts = Context.getRegisteredComponents(OHRIComputedConcept.class);
		return ohriComputedConcepts;
	}
}
