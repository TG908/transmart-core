/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package org.transmartproject.rest

import grails.rest.Link
import grails.rest.render.util.AbstractLinkingRenderer
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestParam
import org.transmartproject.core.config.SystemResource
import org.transmartproject.core.tree.TreeNode
import org.transmartproject.core.tree.TreeResource
import org.transmartproject.rest.marshallers.ContainerResponseWrapper
import org.transmartproject.rest.user.AuthContext
import org.transmartproject.core.serialization.TreeJsonSerializer

import static org.transmartproject.rest.misc.RequestUtils.checkForUnsupportedParams

@Slf4j
class TreeController {

    static responseFormats = ['json', 'hal']

    @Autowired
    AuthContext authContext

    @Autowired
    TreeResource treeResource

    @Autowired
    SystemResource systemResource

    /**
     * Tree nodes endpoint:
     * <code>/${apiVersion}/tree_nodes</code>
     *
     * Fetches all ontology nodes the satisfy the specified criteria (all nodes by default)
     * as a forest.
     *
     * @param root (Optional) the root element from which to fetch.
     * @param depth (Optional) the maximum number of levels to fetch.
     * @param constraints flag if the constraints should be included in the result
     *   (always false for hal, defaults to true for json)
     * @param counts flag if counts should be included in the result (default: false)
     * @param tags flag if tags should be included in the result (default: false)
     *
     * @return a forest of ontology nodes.
     */
    def index(@RequestParam('api_version') String apiVersion,
              @RequestParam('root') String root,
              @RequestParam('depth') Integer depth,
              @RequestParam('constraints') Boolean constraints,
              @RequestParam('counts') Boolean counts,
              @RequestParam('tags') Boolean tags) {
        checkForUnsupportedParams(params, ['root', 'depth', 'constraints', 'counts', 'tags'])
        if (root) {
            root = URLDecoder.decode(root, 'UTF-8')
        }
        log.info "Tree. apiVersion = ${apiVersion}, root = ${root}, depth = ${depth}, constraints = ${constraints}, counts = ${counts}, tags = ${tags}"
        List<TreeNode> nodes = treeResource.findNodesForUser(
                root,
                depth,
                counts,
                tags,
                authContext.user)
        log.info "${nodes.size()} results."
        if (request.format == 'hal') {
            respond wrapNodes(apiVersion, root, depth, nodes)
        } else {
            response.status = 200
            response.contentType = 'application/json'
            response.characterEncoding = 'utf-8'
            new TreeJsonSerializer().write([
                        writeConstraints: constraints,
                        writeTags: tags
                    ],
                    nodes,
                    response.outputStream)
        }
    }

    private setVersion(String apiVersion, List<TreeNode> nodes) {
        nodes.each {
            it.apiVersion = apiVersion
            setVersion(apiVersion, it.children)
        }
    }

    /**
     * Wrapper so we can provide a proper HAL response.
     */
    private ContainerResponseWrapper wrapNodes(String apiVersion,
                                               String root,
                                               Integer depth,
                                               List<TreeNode> source) {
        setVersion(apiVersion, source)
        def params = [
                root: root,
                depth: depth
        ].findAll { k, v -> v }.collect { k, v ->
            "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}"
        }.join('&')
        if (params) {
            params = "?$params"
        }
        new ContainerResponseWrapper(
                key: 'tree_nodes',
                container: source,
                componentType: TreeNode,
                links: [
                        new Link(AbstractLinkingRenderer.RELATIONSHIP_SELF,
                                "/${apiVersion}/tree_nodes${params}"
                        )
                ]
        )
    }

}
