package org.openmrs.module.ohricore.reporting.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author smallGod, pius
 * date: 13/09/2021
 */
@Component
public class PatientList extends OHRIReportManager {

    private static final String DATA_SET_UUID = "18f7658c-c915-4c32-adad-df3799855569";

    @Override
    public String getUuid() {
        return "f451a9d6-4881-11e7-a919-92ebcb67fe33";
    }

    @Override
    public String getName() {
        return "List of Patients";
    }

    @Override
    public String getDescription() {
        return "List of all registered patients since a given date";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameterArrayList = new ArrayList<Parameter>();
        parameterArrayList.add(ReportingConstants.START_DATE_PARAMETER);
        return parameterArrayList;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition reportDef = new ReportDefinition();
        reportDef.setUuid(getUuid());
        reportDef.setName(getName());
        reportDef.setDescription(getDescription());
        reportDef.setParameters(getParameters());

        SqlDataSetDefinition sqlDataDef = new SqlDataSetDefinition();
        sqlDataDef.setUuid(DATA_SET_UUID);
        sqlDataDef.setName(getName());
        sqlDataDef.addParameters(getParameters());
        sqlDataDef.setSqlQuery(getSQLQuery());

        reportDef.addDataSetDefinition("newPatientRegistrations", Mapped.mapStraightThrough(sqlDataDef));

        return reportDef;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(ReportManagerUtil.createExcelDesign("c410998d-b71e-428b-8e5c-f88536c83761", reportDefinition));
        return l;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    private String getSQLQuery(){

        return "SELECT pi.identifier as OpenMRS_ID, pn.given_name as Given_Name, pn.family_name as Family_Name, p.date_created as Created " +
                "FROM patient p INNER JOIN person_name pn " +
                "ON p.patient_id = pn.person_id " +
                "INNER JOIN patient_identifier pi " +
                "ON p.patient_id = pi.patient_id " +
                "WHERE p.date_created >= :startDate " +
                "GROUP BY pi.patient_id " +
                "ORDER BY p.patient_id desc;";
    }
}
