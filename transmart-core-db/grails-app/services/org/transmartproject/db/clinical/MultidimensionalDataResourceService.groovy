/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package org.transmartproject.db.clinical

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import grails.orm.HibernateCriteriaBuilder
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import org.hibernate.criterion.*
import org.hibernate.internal.CriteriaImpl
import org.hibernate.internal.StatelessSessionImpl
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.IterableResult
import org.transmartproject.core.dataquery.SortOrder
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.exceptions.OperationNotImplementedException
import org.transmartproject.core.exceptions.UnsupportedByDataTypeException
import org.transmartproject.core.multidimquery.query.BiomarkerConstraint
import org.transmartproject.core.multidimquery.query.Combination
import org.transmartproject.core.multidimquery.query.ConceptConstraint
import org.transmartproject.core.multidimquery.Dimension
import org.transmartproject.core.multidimquery.Hypercube
import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.multidimquery.MultiDimensionalDataResource
import org.transmartproject.core.multidimquery.query.QueryBuilder
import org.transmartproject.core.multidimquery.query.StudyNameConstraint
import org.transmartproject.core.multidimquery.query.StudyObjectConstraint
import org.transmartproject.core.ontology.MDStudiesResource
import org.transmartproject.core.ontology.MDStudy
import org.transmartproject.core.users.User
import org.transmartproject.db.dataquery.highdim.HighDimensionDataTypeResourceImpl
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.i2b2data.Study
import org.transmartproject.db.metadata.DimensionDescription
import org.transmartproject.db.multidimquery.*
import org.transmartproject.db.multidimquery.query.*
import org.transmartproject.db.user.User as DbUser
import org.transmartproject.db.util.HibernateUtils

import static java.util.Objects.requireNonNull
import static org.transmartproject.db.multidimquery.DimensionImpl.*

class MultidimensionalDataResourceService extends AbstractDataResourceService implements MultiDimensionalDataResource {

    @Autowired
    HighDimensionResourceService highDimensionResourceService

    @Autowired
    MDStudiesResource studiesResource


    @Override Dimension getDimension(String name) {
        def dimension = getBuiltinDimension(name)
        if (!dimension) {
            dimension = fromName(name)
        }
        dimension
    }

    /**
     * See the documentation for {@link MultiDimensionalDataResource#retrieveData}
     */
    @Override HypercubeImpl retrieveData(Map args, String dataType, User user) {
        // Supporting a native Hypercube implementation for high dimensional data is the intention here. As of yet
        // that has not been implemented, so we only support clinical data in this call. Instead there is the
        // highDimension call that uses the old high dim api and converts the tabular result to a hypercube.
        if(dataType != "clinical") throw new OperationNotImplementedException(
                "High dimension datatypes are not yet implemented for the native hypercube")

        Constraint constraint = args.constraint
        Set<DimensionImpl> dimensions = ImmutableSet.copyOf(args.dimensions.collect { toDimensionImpl(it) } ?: [])

        Set<? extends Dimension> supportedDimensions = getSupportedDimensions(constraint)
        // only allow valid dimensions
        dimensions = (Set<DimensionImpl>) dimensions?.findAll { it in supportedDimensions } ?: supportedDimensions

        ImmutableMap<DimensionImpl,SortOrder> orderByDimensions = ImmutableMap.copyOf(parseSort(args.sort))
        def orphanSortDims = (orderByDimensions.keySet() - dimensions)
        if (orphanSortDims) throw new InvalidArgumentsException("Requested ordering on dimension(s) " +
                "${orphanSortDims.collect {it.name}.join(', ')}, which are not part of this query")

        Query query = buildCriteria(dimensions, orderByDimensions)
        HibernateCriteriaQueryBuilder restrictionsBuilder = getCheckedQueryBuilder(user)
        // TODO: check that aliases set by dimensions and by restrictions don't clash

        restrictionsBuilder.applyToCriteria(query.criteriaImpl, [constraint])

        // session will be closed by the Hypercube
        new HypercubeImpl(dimensions, query)
    }

    /**
     * @param sort is either an ordered map of dimension -> sort order, or a list where each item is either a
     * dimension or a two-item list [dimension, sort order]. A dimension is either a Dimension object or a string
     * with the dimension's name. A sort order is either a member of the SortOrder enum, or a string 'asc' or 'desc'
     * (case insensitive).
     * @return An ordered Map<DimensionImpl, SortOrder>
     */
    Map<DimensionImpl, SortOrder> parseSort(sort) {
        if (sort == null) {
            [:]
        } else if (sort instanceof Map) {
            (Map) sort.collectEntries { [toDimensionImpl(it.key), toSortOrder(it.value)] }
        } else if (sort instanceof List) {
            sort.collectEntries {
                it instanceof List ?
                        [toDimensionImpl(it[0]), toSortOrder(it[1])] :
                        [toDimensionImpl(it), SortOrder.ASC]
            }
        }
    }

    @Memoized
    @Transactional(readOnly = true)
    List<Dimension> getAllDimensions() {
        DimensionDescription.allDimensions
    }

    Set<MDStudy> getConstraintStudies(Constraint constraint) {
        // Add any studies that are being selected on
        def studyIds = findStudyNameConstraints(constraint)*.studyId
        Set studies = (studyIds.empty ? [] : studyIds.collect { studiesResource.getStudyByStudyId(it) }) +
                findStudyObjectConstraints(constraint)*.study as Set
        studies
    }

    Set<Dimension> getAvailableDimensions(Iterable<MDStudy> studies) {
        //TODO Remove after adding all the dimension, added to prevent e2e tests failing
        def notImplementedDimensions = [AssayDimension, BioMarkerDimension, ProjectionDimension]
        // This throws a LegacyStudyException for non-17.1 style studies
        // This could probably be done more efficiently, but GORM support for many-to-many collections is pretty
        // buggy. And usually the studies and dimensions will be cached in memory.
        List<Dimension> availableDimensions = studies ? studies*.dimensions.flatten() : allDimensions
        ImmutableSet.copyOf availableDimensions.findAll {
            !(it.class in notImplementedDimensions)
        }
    }

    Set<? extends Dimension> getSupportedDimensions(Constraint constraint) {
        getAvailableDimensions(getConstraintStudies(constraint))
    }

    private Query buildCriteria(Set<DimensionImpl> dimensions, ImmutableMap<DimensionImpl,SortOrder> orderDims) {
        def nonSortableDimensions = orderDims.keySet().findAll { !(it instanceof AliasAwareDimension) }
        if (nonSortableDimensions) throw new InvalidArgumentsException("Sorting over these dimensions is not " +
                "supported: " + nonSortableDimensions.collect {it.name}.join(','))
        ImmutableMap<AliasAwareDimension, SortOrder> orderByDimensions = (ImmutableMap) orderDims

        // We need methods from different interfaces that StatelessSessionImpl implements.
        def session = (StatelessSessionImpl) sessionFactory.openStatelessSession()
        session.connection().autoCommit = false

        HibernateCriteriaBuilder q = HibernateUtils.createCriteriaBuilder(ObservationFact, 'observation_fact', session)
        q.with {
            // The main reason to use this projections block is that it clears all the default projections that
            // select all fields.
            projections {
                // NUM_FIXED_PROJECTIONS must match the number of projections defined here
                property 'valueType', 'valueType'
                property 'textValue', 'textValue'
                property 'numberValue', 'numberValue'
                property 'rawValue', 'rawValue'
            }
        }

        Query query = new Query(q, [modifierCodes: ['@']], null)

        dimensions.each {
            it.selectIDs(query)
        }

        def actualSortOrder = [:]

        boolean hasModifiers = dimensions.any { it instanceof ModifierDimension }
        if (hasModifiers) {
            def nonModifierSortableDimensions = orderByDimensions.keySet().collectMany {
                (it in modifierSortableDimensions) ? [] : [it.name] }
            if (nonModifierSortableDimensions) {
                def modifier = dimensions.findAll {it instanceof ModifierDimension }.collect { it.name }.join(", ")
                throw new UnsupportedByDataTypeException("Sorting over these dimensions is not supported when querying" +
                        " $modifier dimensions:" + nonModifierSortableDimensions.join(", "))
            }

            // Make sure all primary key dimension columns are selected, even if they are not part of the result
            primaryKeyDimensions.each {
                if (!(it in dimensions)) {
                    it.selectIDs(query)
                }
            }

            Set<AliasAwareDimension> neededPrimaryKeyDimensions = primaryKeyDimensions as Set<AliasAwareDimension>
            q.with {
                // instanceNum is not a dimension
                property 'instanceNum', 'instanceNum'

                orderByDimensions.each { AliasAwareDimension aaDim, SortOrder so ->
                    order(aaDim.alias, so.string())
                    actualSortOrder[aaDim] = so
                    neededPrimaryKeyDimensions.remove(aaDim)
                }
                neededPrimaryKeyDimensions.each {
                    order it.alias
                    actualSortOrder[it] = SortOrder.ASC
                }

                order 'instanceNum'
            }
        } else {
            q.with {
                orderByDimensions.each { AliasAwareDimension aaDim, SortOrder so ->
                    order(aaDim.alias, so.string())
                    actualSortOrder[aaDim] = so
                }
            }
        }
        query.actualSortOrder = ImmutableMap.copyOf(actualSortOrder)

        q.with {
            inList 'modifierCd', query.params.modifierCodes
        }

        query
    }

    static final List<AliasAwareDimension> primaryKeyDimensions = ImmutableList.of(
            // primary key columns excluding modifierCd and instanceNum
            CONCEPT, PROVIDER, PATIENT, VISIT, START_TIME)

    static final List<Dimension> modifierSortableDimensions = ImmutableList.of(
            // Primary key dimensions plus those on which sorting is compatible with modifiers. This compatibility is
            // determined at data load time: to be compatible the values in the ObservationFact columns for these
            // dimensions must be identical for all rows that have the same primary key excluding modifier_cd, in
            // other words for an ObservationFact value row and its associated modifier rows.
            CONCEPT, PROVIDER, PATIENT, VISIT, START_TIME, /*TRIAL_VISIT, STUDY, END_TIME*/)

    private static DimensionImpl toDimensionImpl(dimOrDimName) {
        if(dimOrDimName instanceof DimensionImpl) {
            return dimOrDimName
        }
        if(dimOrDimName instanceof String) {
            def dim = DimensionDescription.findByName(dimOrDimName)?.dimension
            if(dim == null) throw new InvalidArgumentsException("Unknown dimension: $dimOrDimName")
            return dim
        }
        throw new InvalidArgumentsException("dimension $dimOrDimName is not a valid dimension or dimension name")
    }

    private static SortOrder toSortOrder(it) {
        if (it instanceof SortOrder) return it
        try {
            return SortOrder.valueOf(((String) it).toUpperCase())
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentsException("'$it' is not a valid sort order")
        }
    }

    /*
    note: efficiently extracting the available dimension elements for dimensions is possible using nonstandard
    sql. For Postgres:
    SELECT array_agg(DISTINCT patient_num), array_agg(DISTINCT concept_cd),
        array_agg(distinct case when modifier_cd = 'SOMEMODIFIER' then tval_char end)... FROM observation_facts WHERE ...
      Maybe doing something with unnest() can also help but I haven't figured that out yet.
    For Oracle we should look into the UNPIVOT operator
     */


    @Lazy
    private Criterion defaultHDModifierCriterion = Restrictions.in('modifierCd',
            highDimensionResourceService.knownMarkerTypes.collect { "TRANSMART:HIGHDIM:${it.toUpperCase()}".toString() })

    private Criterion HDModifierCriterionForType(String type) {
        HighDimensionDataTypeResourceImpl hdres = highDimensionResourceService.getSubResourceForType(type)
        Restrictions.in('modifierCd',
                hdres.platformMarkerTypes.collect {"TRANSMART:HIGHDIM:${it.toUpperCase()}".toString()} )
    }

    @Override
    IterableResult getDimensionElements(Dimension dimension, Constraint constraint, User user) {
        if(constraint) checkAccess(constraint, user)
        HibernateCriteriaQueryBuilder builder = getCheckedQueryBuilder(user)
        DetachedCriteria dimensionCriteria = builder.buildElementsCriteria((DimensionImpl) dimension, constraint)

        return getIterable(dimensionCriteria)
    }

    static List<StudyNameConstraint> findStudyNameConstraints(Constraint constraint) {
        if (constraint instanceof StudyNameConstraint) {
            return [constraint]
        } else if (constraint instanceof Combination) {
            constraint.args.collectMany { findStudyNameConstraints(it) }
        } else {
            return []
        }
    }

    static List<StudyObjectConstraint> findStudyObjectConstraints(Constraint constraint) {
        if (constraint instanceof StudyObjectConstraint) {
            return [constraint]
        } else if (constraint instanceof Combination) {
            constraint.args.collectMany { findStudyObjectConstraints(it) }
        } else {
            return []
        }
    }

    static List<ConceptConstraint> findConceptConstraints(Constraint constraint) {
        if (constraint instanceof ConceptConstraint) {
            return [constraint]
        } else if (constraint instanceof Combination) {
            constraint.args.collectMany { findConceptConstraints(it) }
        } else {
            return []
        }
    }

    private static List<BiomarkerConstraint> findAllBiomarkerConstraints(Constraint constraint) {
        if (constraint instanceof BiomarkerConstraint) {
            return [constraint]
        } else if (constraint instanceof Combination) {
            constraint.args.collectMany { findAllBiomarkerConstraints(it) }
        } else {
            return []
        }
    }

    @CompileStatic
    @Override
    Hypercube highDimension(
            Constraint assayConstraint,
            BiomarkerConstraint biomarkerConstraint = new BiomarkerConstraint(),
            String projectionName = Projection.ALL_DATA_PROJECTION,
            User user,
            String type) {
        projectionName = projectionName ?: Projection.ALL_DATA_PROJECTION
        checkAccess(assayConstraint, user)

        List<AssayConstraint> oldAssayConstraints = getOldAssayConstraint(assayConstraint, user, type)
        if(!oldAssayConstraints) {
            return new EmptyHypercube()
        }

        HighDimensionDataTypeResource typeResource
        if(type == 'autodetect') {
            Map<HighDimensionDataTypeResource, Collection<Assay>> assaysByType =
                    highDimensionResourceService.getSubResourcesAssayMultiMap(oldAssayConstraints)
            if (assaysByType.size() == 1) {
                typeResource = assaysByType.keySet()[0]
            } else {
                assert assaysByType.size() != 0, "cannot happen"
                throw new InvalidQueryException("Autodetecting the high dimensional type found multiple applicable " +
                        "types: ${assaysByType.keySet()*.dataTypeName.join(', ')}. Please choose one.")
            }
        } else {
            try {
                typeResource = highDimensionResourceService.getSubResourceForType(type)
            } catch (NoSuchResourceException e) {
                throw new InvalidQueryException("Unknown high dimensional data type.", e)
            }
        }

        Projection projection = typeResource.createProjection(projectionName)

        List<DataConstraint> dataConstraints = []
        if (biomarkerConstraint?.biomarkerType) {
            dataConstraints << typeResource.createDataConstraint(biomarkerConstraint.params, biomarkerConstraint.biomarkerType)
        }
        try {
            TabularResult table = typeResource.retrieveData(oldAssayConstraints, dataConstraints, projection)
            return new HddTabularResultHypercubeAdapter(table)
        } catch (EmptySetException e) {
            return new EmptyHypercube()
        }
    }

    @Override
    PagingDataTableImpl retrieveDataTable(Map args, String type, Constraint constraint, User user) {
        args = parseDataTableArgs(args, type)
        Hypercube cube = retrieveClinicalData(args, constraint, user)
        return new PagingDataTableImpl(args, cube)
    }

    FullDataTable retrieveStreamingDataTable(Map args, String type, Constraint constraint, User user) {
        args = parseDataTableArgs(args, type)
        Hypercube cube = retrieveClinicalData(args, constraint, user)
        return new FullDataTable(args, cube)
    }

    private Map parseDataTableArgs(Map args, String type) {
        if(type != 'clinical') throw new OperationNotImplementedException("High dimensional data is not supported in " +
                "data table format")

        def rowSort = parseSort(args.rowSort)
        def columnSort = parseSort(args.columnSort)
        List<DimensionImpl> rowDimensions = args.rowDimensions = (List) requireNonNull(
                args.rowDimensions?.collect { toDimensionImpl(it) })
        List<DimensionImpl> columnDimensions = args.columnDimensions = (List) requireNonNull(
                args.columnDimensions?.collect { toDimensionImpl(it) })

        def invalidRowSorts = rowSort ? rowSort.keySet() - rowDimensions : null
        if(invalidRowSorts) throw new InvalidArgumentsException("Only dimensions specified in rowDimensions can be " +
                "specified in rowSort: "+invalidRowSorts.join(', '))
        def invalidColumnSorts = columnSort ? columnSort.keySet() - columnDimensions : null
        if(invalidColumnSorts) throw new InvalidArgumentsException("Only dimensions specified in columnDimensions can" +
                " be specified in columnSort "+invalidColumnSorts.join(', '))

        args.userSort = rowSort + columnSort

        for(def dim : rowDimensions) {
            rowSort.putIfAbsent(dim, SortOrder.ASC)
        }
        for(def dim : columnDimensions) {
            columnSort.putIfAbsent(dim, SortOrder.ASC)
        }

        args.sort = rowSort + columnSort
        args.dimensions = rowDimensions + columnDimensions

        args
    }

    @Override
    List retrieveHighDimDataTypes(Constraint assayConstraint, User user) {
        assert assayConstraint instanceof Constraint
        checkAccess(assayConstraint, user)
        List<AssayConstraint> oldAssayConstraints = getOldAssayConstraint(assayConstraint, user, 'autodetect')
        if(!oldAssayConstraints) {
            return []
        }

        Map<HighDimensionDataTypeResource, Collection<Assay>> assaysByType =
                highDimensionResourceService.getSubResourcesAssayMultiMap(oldAssayConstraints)

        assaysByType.keySet()?.collect {it.dataTypeName}
    }

    private List<Study> selectStudiesWithDimensionSupport(Iterable<Study> studies, DimensionImpl dimension) {
        DetachedCriteria studiesCriteria = DetachedCriteria.forClass(Study)
            .createAlias('dimensionDescriptions', 'd')
            .add(Restrictions.in('id', studies*.id))
            .add(Restrictions.in('d.name', dimension.name))

        getList(studiesCriteria)
    }

    private List<AssayConstraint> getOldAssayConstraint(Constraint assayConstraint, User user, String type) {
        assert user instanceof DbUser

        Collection<Study> userStudies = accessControlChecks.getDimensionStudiesForUser(user)
        List<Study> assaySupportStudies = selectStudiesWithDimensionSupport(userStudies, ASSAY)
        if (!assaySupportStudies) {
            log.debug("No studies with assay dimension for user ${user.name} were found.")
            return Collections.emptyList()
        }

        QueryBuilder builder = HibernateCriteriaQueryBuilder.forStudies(assaySupportStudies)

        Criterion modifierCriterion = type == 'autodetect' ?
                defaultHDModifierCriterion : HDModifierCriterionForType(type)

        DetachedCriteria assayIdsCriteria = builder.buildCriteria(assayConstraint, modifierCriterion)
                .setProjection(Projections.property('numberValue'))

        List<BigDecimal> assayIds = getList(assayIdsCriteria)

        if (assayIds.empty) {
            Collections.emptyList()
        } else {
            [
                    highDimensionResourceService
                            .createAssayConstraint([ids: assayIds] as Map, AssayConstraint.ASSAY_ID_LIST_CONSTRAINT)
            ]
        }
    }

    @Override
    Hypercube retrieveClinicalData(Constraint constraint, User user) {
        retrieveClinicalData([:], constraint, user)
    }

    @Override
    Hypercube retrieveClinicalData(Map args, Constraint constraint, User user) {
        assert constraint instanceof Constraint
        checkAccess(constraint, user)
        def dataType = 'clinical'
        retrieveData([*:args, constraint: constraint], dataType, user)
    }

}

@TupleConstructor
class Query {
    HibernateCriteriaBuilder criteria
    Map params
    ImmutableMap<DimensionImpl,SortOrder> actualSortOrder

    CriteriaImpl getCriteriaImpl() { (CriteriaImpl) criteria.instance }
}

