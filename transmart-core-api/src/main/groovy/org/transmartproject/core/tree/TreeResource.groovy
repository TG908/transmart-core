package org.transmartproject.core.tree

import org.transmartproject.core.users.User

interface TreeResource {

    /**
     * Finds tree nodes with prefix $rootKey up to depth $depth lower than the $rootKey level,
     * and that the user has access to. It determines access based on the security token associated
     * with the tree nodes: a token that represents the study if access is restricted to users that
     * can access that study, or a special token that marks nodes as public.
     * If $counts is true, observation counts and patient counts will be added to leaf nodes.
     *
     * @param rootKey the key of the root element, used as prefix. Default value is '\'.
     * If another key is provided, it must specify an existing node to which the user has access.
     * The root node will be included in the results, '\' is not (unless it is an existing node).
     * @param depth the maximum number of levels below the root element that should be returned
     * (default: 0, meaning no limit).
     * @param includeCounts flag if counts should be added to tree nodes (default: false).
     * @param includeTags flag if tag metadata should be added to tree nodes (default: false).
     * @param user the current user.
     * @return a forest, represented as a list of the top level nodes. Lower nodes will be children
     * of their ancestor nodes.
     */
    List<TreeNode> findNodesForUser(String rootKey, Integer depth, Boolean includeCounts, Boolean includeTags, User currentUser)

}
