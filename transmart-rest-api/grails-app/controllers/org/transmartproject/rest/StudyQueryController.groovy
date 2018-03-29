/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package org.transmartproject.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.transmartproject.core.ontology.MDStudiesResource
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.ontology.MDStudy
import org.transmartproject.rest.marshallers.ContainerResponseWrapper
import org.transmartproject.rest.marshallers.StudyWrapper
import static org.transmartproject.rest.misc.RequestUtils.checkForUnsupportedParams

class StudyQueryController extends AbstractQueryController {

    static responseFormats = ['json', 'hal']

    @Autowired
    MDStudiesResource studiesResource

    /**
     * Studies endpoint:
     * <code>/v2/studies</code>
     *
     * @return a list of {@link org.transmartproject.db.i2b2data.Study} objects
     * that are accessible for the user.
     */
    def listStudies(@RequestParam('api_version') String apiVersion) {
        checkForUnsupportedParams(params, [])
        def studies = studiesResource.getStudies(currentUser)
        respond wrapStudies(apiVersion, studies)
    }

    /**
     * Study endpoint:
     * <code>/v2/studies/${id}</code>
     *
     * @param id the study id
     *
     * @return the {@link org.transmartproject.db.i2b2data.Study} object with id ${id}
     * if it exists and the user has access; null otherwise.
     */
    def findStudy(
            @RequestParam('api_version') String apiVersion,
            @PathVariable('id') Long id) {
        if (id == null) {
            throw new InvalidArgumentsException("Parameter 'id' is missing.")
        }
        checkForUnsupportedParams(params, ['id'])

        def study = studiesResource.getStudyForUser(id, currentUser)

        respond new StudyWrapper(
                study: study,
                apiVersion: apiVersion
        )
    }

    /**
     * Study endpoint:
     * <code>/v2/studies/studyId/${studyIds}</code>
     *
     * @param a list of the study names
     *
     * @return a list of the {@link org.transmartproject.db.i2b2data.Study} objects with names ${studyIds}
     * if all of them exist and the user has access; null otherwise.
     */
    def findStudyByStudyId(
            @RequestParam('api_version') String apiVersion,
            @PathVariable('studyIds') String studyIds) {
        if (studyIds == null || studyIds.trim().empty) {
            throw new InvalidArgumentsException("Parameter 'studyIds' is missing.")
        }

        checkForUnsupportedParams(params, ['studyIds'])

        List<String> studyIdList = studyIds.split(',')
        def studies = studiesResource.getStudiesByStudyIdsForUser(studyIdList, currentUser)

        respond wrapStudies(apiVersion, studies)
    }

    private static wrapStudies(String apiVersion, Collection<MDStudy> source) {
        new ContainerResponseWrapper(
                key: 'studies',
                container: source.collect { new StudyWrapper(apiVersion: apiVersion, study: it) },
                componentType: MDStudy,
        )
    }

}
