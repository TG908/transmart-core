/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package org.transmartproject.rest

import grails.artefact.Controller
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.grails.web.converters.exceptions.ConverterException
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.binding.BindingException
import org.transmartproject.core.binding.BindingHelper
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.multidimquery.MultiDimensionalDataResource
import org.transmartproject.core.multidimquery.PatientSetResource
import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.multidimquery.query.ConstraintFactory
import org.transmartproject.rest.user.AuthContext
import org.transmartproject.rest.misc.RequestUtils

@CompileStatic
abstract class AbstractQueryController implements Controller {

    @Autowired
    MultiDimensionalDataResource multiDimService

    @Autowired
    PatientSetResource patientSetResource

    @Autowired
    AuthContext authContext

    @Autowired
    HypercubeDataSerializationService hypercubeDataSerializationService

    protected static Constraint getConstraintFromString(String constraintText) {
        if (!constraintText) {
            throw new InvalidArgumentsException('Empty constraint parameter.')
        }
        try {
            def constraint = ConstraintFactory.read(constraintText)
            if (constraint) {
                return constraint.normalise()
            }
            throw new InvalidArgumentsException("Invalid constraint parameter: ${constraintText}")
        } catch (ConverterException c) {
            throw new InvalidArgumentsException("Cannot parse constraint parameter: ${constraintText}", c)
        }
    }

    protected Constraint bindConstraint(String constraintText) {
        try {
            return getConstraintFromString(constraintText)
        } catch (BindingException e) {
            def error = [
                    httpStatus: 400,
                    message   : e.message,
                    type      : e.class.simpleName,
            ] as Map<String, Object>

            if (e.errors) {
                error.errors = e.errors
                        .collect { [propertyPath: it.propertyPath.toString(), message: it.message] }

            }

            response.status = 400
            render error as JSON
            return null
        }
    }

    /**
     * Gets arguments from received REST request
     * either from queryString for GET method
     * or from request body for POST method.
     *
     * @return Map with passed arguments
     */
    protected Map getGetOrPostParams() {
        if(request.method == 'POST') {
            def parameters = request.JSON as Map<String, Object>
            return parameters.collectEntries { String k, v ->
                if (v instanceof Object[] || v instanceof List) {
                    [k, v.collect {
                        if (it instanceof Map) {
                            BindingHelper.objectMapper.writeValueAsString((Map) it)
                        } else
                            it.toString()
                    }]
                } else if (v instanceof Map) {
                    [k, BindingHelper.objectMapper.writeValueAsString((Map)v)]
                } else {
                    [k, v.toString()]
                }
            }
        }
        return (params as Map<String, Object>).collectEntries { String k, v ->
            if (!RequestUtils.GLOBAL_PARAMS.contains(k)) {
                if(v instanceof Object[] || v instanceof List) {
                    [k, v.collect { URLDecoder.decode(it.toString(), 'UTF-8') }]
                } else {
                    [k, URLDecoder.decode(v.toString(), 'UTF-8')]
                }
            } else [:]
        }
    }
}
