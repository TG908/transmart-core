/* (c) Copyright 2018  The Hyve B.V. */

package org.transmartproject.db.clinical

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.transmartproject.core.config.SystemResource
import org.transmartproject.core.multidimquery.Counts
import org.transmartproject.core.multidimquery.query.PatientSetConstraint
import org.transmartproject.core.ontology.MDStudiesResource
import org.transmartproject.core.ontology.MDStudy
import org.transmartproject.core.users.PatientDataAccessLevel
import org.transmartproject.core.users.User

import java.sql.Connection
import java.util.stream.Collectors

@CompileStatic
class AggregateDataOptimisationsService {

    @Autowired
    MDStudiesResource studiesResource

    @Autowired
    MultidimensionalDataResourceService multidimensionalDataResourceService

    @Autowired
    SystemResource systemResource

    @Autowired
    SessionFactory sessionFactory

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate

    /**
     * Checks if the bitset view is available and can be used for counts per study and concept.
     * @return
     */
    @Memoized
    boolean isCountsPerStudyAndConceptForPatientSetEnabled() {
        dbViewExists('biomart_user', 'study_concept_patient_set_bitset')
    }

    /**
     * Refreshes biomart_user.study_concept_bitset
     */
    void clearPatientSetBitset() {
        refreshMaterializedView('biomart_user', 'study_concept_bitset')
    }

    /**
     * Implementation using bit sets. Only returns patient counts, not observation counts.
     * @param constraint
     * @param user
     * @return
     */
    Map<String, Map<String, Counts>> countsPerStudyAndConceptForPatientSet(PatientSetConstraint constraint, User user) {
        def resultInstanceId = constraint.patientSetId
        log.info "Counts per study and concept for patient set ${resultInstanceId} using bitsets ..."

        Set<String> studyIds = studiesResource.getStudies(user, PatientDataAccessLevel.SUMMARY).stream()
                .map({ MDStudy study -> study.name })
                .collect(Collectors.toSet())

        def t1 = new Date()

        List<Map<String, Object>> rows = namedParameterJdbcTemplate
                .queryForList(
                '''select 
                    study_id,
                    concept_cd,
                    patient_count
                   from biomart_user.study_concept_patient_set_bitset
                   where result_instance_id = :result_instance_id and study_id in (:study_ids)''',
                [result_instance_id: resultInstanceId, study_ids: studyIds])

        Map<String, Map<String, Counts>> result = [:]
        for (Map<String, Object> row : rows) {
            String studyId = row.get('study_id')
            Map<String, Counts> countsPerConcept = result.get(studyId)
            if (!countsPerConcept) {
                countsPerConcept = [:]
                result.put(studyId, countsPerConcept)
            }
            countsPerConcept.put((String) row.get('concept_cd'), new Counts(-1L, (long) row.get('patient_count')))
        }
        def t2 = new Date()
        log.info "Counts per study and concept for patient set done. (took ${t2.time - t1.time} ms.)"
        return result
    }

    private Connection getConnection() {
        ((JdbcTemplate) namedParameterJdbcTemplate.jdbcOperations).dataSource.connection
    }

    private boolean dbViewExists(String schema, String function) {
        getConnection().metaData.getTables(null, schema, function, ['VIEW'].toArray(new String[0])).next()
    }

    private void refreshMaterializedView(String schema, String function) {
        def connection = getConnection()
        try {
            log.info "Refreshing $schema.$function materialized view ..."
            getConnection().prepareStatement("refresh materialized VIEW $schema.$function").executeUpdate()
        } catch (Exception e) {
            log.warn "$schema.$function materialized view not updated. $e.message"
        } finally {
            connection?.close()
        }
    }
}


