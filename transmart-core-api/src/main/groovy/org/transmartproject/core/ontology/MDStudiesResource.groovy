package org.transmartproject.core.ontology

import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.users.PatientDataAccessLevel
import org.transmartproject.core.users.User

interface MDStudiesResource {

    /**
     * Returns all studies the user has access to.
     *
     * @return the studies.
     */
    List<MDStudy> getStudies(User user, PatientDataAccessLevel requiredAccessLevel)

    /**
     * Returns the study with the given id.
     *
     * @param id the database identifier of the study
     * @return the study with the id, if it exists and the user has access to the study.
     * @throws NoSuchResourceException iff no study can be found with the id or the user does not have
     * access to the study.
     */
    MDStudy getStudyForUser(Long id, User user) throws NoSuchResourceException

    /**
     * Returns the study with the given study name.
     *
     * @param id the unique study name of the study
     * @return the study with the name, if it exists and the user has access to the study.
     * @throws NoSuchResourceException if no study can be found with the name or the user does not have
     * access to the study.
     */
    MDStudy getStudyByStudyIdForUser(String studyId, User user) throws NoSuchResourceException

    /**
     * Returns a list of studies with the given study names.
     *
     * @param studyIds the list of unique study names of the studies
     * @return the studies with the names, if they exist and the user has access to the studies.
     * @throws NoSuchResourceException if no study can be found with one of the names or the user does not have
     * access to one of the studies.
     */
    List<MDStudy> getStudiesByStudyIdsForUser(List<String> studyIds, User user) throws NoSuchResourceException

    /**
     * Returns the study with the given name.
     *
     * @param studyId the unique name of the study
     * @return the study, if it exists.
     * @throws NoSuchResourceException iff no study can be found with the name.
     */
    MDStudy getStudyByStudyId(String studyId) throws NoSuchResourceException

    /**
     * Returns the study with the given id.
     *
     * @param id the unique id of the study
     * @return the study, if it exists.
     * @throws NoSuchResourceException iff no study can be found with the id.
     */
    MDStudy getStudyById(Long id) throws NoSuchResourceException

    /**
     * Returns the study id for the study with the given id.
     *
     * @param id the unique id of the study
     * @return the study name of the study, if it exists.
     * @throws NoSuchResourceException iff no study can be found with the id.
     */
    String getStudyIdById(Long id) throws NoSuchResourceException

}
