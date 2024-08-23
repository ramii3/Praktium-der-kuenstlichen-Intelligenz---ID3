package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prunes a trained decision tree in a post-pruning way.
 */
public class ReducedErrorPruner {

    /**
     * Prunes the given decision tree in-place.
     *
     * @param trainedDecisionTree The decision tree to prune.
     * @param validationExamples  the examples to validate the pruning with.
     * @param labelAttributeId    The label attribute.
     */
    public static void prune(DecisionTreeNode trainedDecisionTree, List<CSVAttribute[]> validationExamples) {

            CrossValidator.evaluateModel(trainedDecisionTree,validationExamples);
            pruneOnValidatedSet(trainedDecisionTree);

    }

    /**
     * recurvive walk through tree until node is null
     * @param tree the root node of the Tree;
     */
    public static void pruneOnValidatedSet(DecisionTreeNode tree){
        if (tree == null) return;

        // Rekursiver Aufruf für alle Kinder
        for (DecisionTreeNode child : tree.getSplits().values()) {
            pruneOnValidatedSet(child);
        }

        pruneOperation(tree);
    }

    /**
     * if node has higher or same accuracy than its leaves combined it will be changed to a leaf
     * @param node
     */
    private static void pruneOperation(DecisionTreeNode node){
        if(node.isLeafNode()) return;
        if(node.getSplits().values().stream().allMatch(DecisionTreeNode::isLeafNode)){
            Collection<DecisionTreeNode> leaves = node.getSplits().values();
            int correctOnLeaves = leaves.stream().mapToInt(DecisionTreeNode::getCorrectGuesses).sum();
            int correctOnNode = node.getCorrectGuesses();
            if(correctOnNode >= correctOnLeaves){
                node.setSplits(new HashMap<>());
            }
        }
    }



}
