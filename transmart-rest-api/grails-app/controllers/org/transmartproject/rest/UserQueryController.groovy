package org.transmartproject.rest

import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonSlurper
import org.grails.web.converters.exceptions.ConverterException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.ServiceNotAvailableException
import org.transmartproject.core.userquery.SubscriptionFrequency
import org.transmartproject.core.userquery.UserQuery
import org.transmartproject.core.userquery.UserQueryResource
import org.transmartproject.core.userquery.UserQuerySetResource
import org.transmartproject.rest.user.AuthContext

import static org.transmartproject.rest.misc.RequestUtils.checkForUnsupportedParams

class UserQueryController {

    @Autowired
    VersionController versionController

    @Autowired
    AuthContext authContext

    @Autowired
    UserQueryResource userQueryResource

    @Autowired
    UserQuerySetResource userQuerySetResource

    private static final JsonSlurper JSON_SLURPER = new JsonSlurper()

    static responseFormats = ['json']

    def index() {
        List<UserQuery> queries = userQueryResource.list(authContext.user)
        respond([queries: queries.collect { toResponseMap(it) }])
    }

    def get(@PathVariable('id') Long id) {
        checkForUnsupportedParams(params, ['id'])
        UserQuery query = userQueryResource.get(id, authContext.user)
        respond toResponseMap(query)
    }

    def save(@RequestParam('api_version') String apiVersion) {
        def requestJson = request.JSON as Map
        checkForUnsupportedParams(requestJson, ['name', 'patientsQuery', 'observationsQuery', 'bookmarked',
                                                'subscribed', 'subscriptionFreq', 'queryBlob'])
        def patientsQueryString = requestJson.patientsQuery?.toString()
        def observationsQueryString = requestJson.observationsQuery?.toString()
        def queryBlobString = requestJson.queryBlob?.toString()
        boolean subscriptionFreqSpecified = requestJson.containsKey('subscriptionFreq')

        validateJson(patientsQueryString)
        validateJson(observationsQueryString)
        validateJson(queryBlobString)
        validateSubscriptionEnabled(requestJson.subscribed, subscriptionFreqSpecified)

        UserQuery query = userQueryResource.create(authContext.user)
        query.apiVersion = versionController.currentVersion(apiVersion)
        query.with {
            name = requestJson.name
            patientsQuery = patientsQueryString
            observationsQuery = observationsQueryString
            bookmarked = requestJson.bookmarked ?: false
            subscribed = requestJson.subscribed ?: false
            queryBlob = queryBlobString
        }

        if (subscriptionFreqSpecified) {
            validateSubscriptionFrequency(requestJson.subscriptionFreq)
            query.subscriptionFreq = SubscriptionFrequency.valueOf(requestJson.subscriptionFreq)
        }

        userQueryResource.save(query, authContext.user)
        userQuerySetResource.createSetWithInstances(query, patientsQueryString, authContext.user)

        response.status = 201
        respond toResponseMap(query)
    }

    def update(@RequestParam('api_version') String apiVersion,
               @PathVariable('id') Long id) {
        def requestJson = request.JSON as Map

        checkForUnsupportedParams(requestJson, ['name', 'bookmarked', 'subscribed', 'subscriptionFreq'])
        boolean subscriptionFreqSpecified = requestJson.containsKey('subscriptionFreq')

        validateSubscriptionEnabled(requestJson.subscribed, subscriptionFreqSpecified)

        UserQuery query = userQueryResource.get(id, authContext.user)
        if (requestJson.containsKey('name')) {
            query.name = requestJson.name
        }
        if (requestJson.containsKey('bookmarked')) {
            query.bookmarked = requestJson.bookmarked
        }
        if (requestJson.containsKey('subscribed')) {
            query.subscribed = requestJson.subscribed
        }
        if (subscriptionFreqSpecified) {
            validateSubscriptionFrequency(requestJson.subscriptionFreq)
            query.subscriptionFreq = SubscriptionFrequency.valueOf(requestJson.subscriptionFreq)
        }
        userQueryResource.save(query, authContext.user)
        response.status = 204
    }

    def delete(@PathVariable('id') Long id) {
        userQueryResource.delete(id, authContext.user)
        response.status = 204
    }

    private static Map<String, Object> toResponseMap(UserQuery query) {
        query.with {
            [
                    id               : id,
                    name             : name,
                    patientsQuery    : patientsQuery ? JSON_SLURPER.parseText(patientsQuery) : null,
                    observationsQuery: observationsQuery ? JSON_SLURPER.parseText(observationsQuery) : null,
                    apiVersion       : apiVersion,
                    bookmarked       : bookmarked,
                    subscribed       : subscribed,
                    subscriptionFreq : subscriptionFreq,
                    createDate       : createDate,
                    updateDate       : updateDate,
                    queryBlob        : queryBlob ? JSON_SLURPER.parseText(queryBlob) : null,
            ]
        }
    }

    private static void validateJson(String query) {
        if (query) {
            try {
                JSON.parse(query)
            } catch (ConverterException c) {
                throw new InvalidArgumentsException("Query is not a valid JSON")
            }
        }
    }

    private static void validateSubscriptionEnabled(boolean subscribed, boolean subscriptionFreqSpecified) {
        boolean subscriptionEnabled = Holders.config.org.transmartproject.notifications.enabled
        if(!subscriptionEnabled && (subscribed || subscriptionFreqSpecified)) {
            throw new ServiceNotAvailableException(
                    "Subscription functionality is not enabled. Saving subscription data not supported.")
        }
    }

    private static void validateSubscriptionFrequency(String frequency) {
        if (frequency != SubscriptionFrequency.WEEKLY.toString() && frequency != SubscriptionFrequency.DAILY.toString()) {
            throw new InvalidArgumentsException("Value $frequency of subscriptionFreq is not supported.")
        }
    }
}
