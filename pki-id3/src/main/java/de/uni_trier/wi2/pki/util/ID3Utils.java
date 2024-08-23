package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.XMLWriter;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;


import java.util.LinkedList;
import java.util.List;

import static de.uni_trier.wi2.pki.tree.DecisionTreeNode.traverseAndCleanUp;

/**
 * Utility class for creating a decision tree with the ID3 algorithm.
 */
public class ID3Utils {

    /**
     * Create the decision tree given the example and the index of the label attribute.
     *
     * @param examples   The examples to train with. This is a collection of arrays.
     * @param labelIndex The label of the attribute that should be used as an index.
     * @return The root node of the decision tree
     */
    public static void displayTree(DecisionTreeNode tree){
        LinkedList<DecisionTreeNode> nodeList = new LinkedList<>();
        LinkedList<DecisionTreeNode> childrenList = new LinkedList<>();
        nodeList.add(tree);
        DecisionTreeNode tempNode;
        DecisionTreeNode tempNode2;
        while(!nodeList.isEmpty()){
            tempNode = nodeList.get(0);
            System.out.println(null== tempNode.getNodeClass());
            tempNode2 = tempNode;
            if(tempNode2.getParent()!=null) System.out.println("Parents: ");
            while(tempNode2.getParent()!=null) {
                System.out.println("  "+tempNode2.getParent().getName());
                tempNode2 = tempNode2.getParent();
            }
            System.out.println("CurrentNode: ");
            System.out.println("  "+tempNode.getName());

            nodeList.addAll(tempNode.getSplits().values());
            childrenList.addAll(tempNode.getSplits().values());

            if(!childrenList.isEmpty()) System.out.println("Children: ");
            for (DecisionTreeNode node: childrenList
            ) {
                System.out.println("  "+node.getName());
            }
            System.out.println("splitIndex: "+ tempNode.getSplitValue() +" IsLeafNode"+ tempNode.isLeafNode()+ " NodeClass: "+ tempNode.getNodeClass());
            nodeList.remove(0);
            childrenList.clear();
            System.out.println("");

        }

    }


    // Baum erstellen

    /**
     * gets first from list trys to create children and appends the children to the list while removing the the first entry
     * @param allNodes
     */
    public static void buildNodes(List<DecisionTreeNode> allNodes){
        if(allNodes.isEmpty()) return;
        //System.out.println("size of allNodes "+allNodes.size());
        DecisionTreeNode tempNode = allNodes.get(0);
        allNodes.remove(0);
        if(tempNode.getAlreadySplitted().size() > (CSVReader.getNumberOfAttribute())-1) return;
        tempNode.applySplit();


    }

    /**
     * loops through allNodeslist until its empty and the tree is finished
     * @param examples
     * @return
     * @throws Exception
     */
    public static DecisionTreeNode createTree(List<CSVAttribute[]> examples) throws Exception {
        DecisionTreeNode tree= new DecisionTreeNode(examples);
        while(!DecisionTreeNode.allNodes.isEmpty()){
            buildNodes(DecisionTreeNode.allNodes);
        }


        traverseAndCleanUp(tree);

        return tree;
    }

}
