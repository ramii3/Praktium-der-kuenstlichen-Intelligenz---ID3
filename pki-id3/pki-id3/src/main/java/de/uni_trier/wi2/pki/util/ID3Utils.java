package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.sql.SQLOutput;
import java.util.Collection;


import java.util.LinkedList;
import java.util.List;

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

    public static void buildNodes(List<DecisionTreeNode> allNodes){
        if(allNodes.isEmpty()) return;
        //System.out.println("size of allNodes "+allNodes.size());
        DecisionTreeNode tempNode = allNodes.get(0);
        allNodes.remove(0);
        if(tempNode.alreadySplitted.size() > BinningDiscretizer.numberOfAttributes-1) return;
        tempNode.applySplit();


    }



    public static void displayTree(DecisionTreeNode tree){
        LinkedList<DecisionTreeNode> nodeList = new LinkedList<>();
        LinkedList<DecisionTreeNode> childrenList = new LinkedList<>();
        nodeList.add(tree);
        DecisionTreeNode tempNode;
        DecisionTreeNode tempNode2;
        while(!nodeList.isEmpty()){
           tempNode = nodeList.get(0);
           tempNode2 = tempNode;
           if(tempNode2.parent!=null) System.out.println("Parents: ");
           while(tempNode2.parent!=null) {
               System.out.println("  "+tempNode2.parent.name);
               tempNode2 = tempNode2.parent;
           }
            System.out.println("CurrentNode: ");
            System.out.println("  "+tempNode.name);

            nodeList.addAll(tempNode.splits.values());
            childrenList.addAll(tempNode.splits.values());

            if(!childrenList.isEmpty()) System.out.println("Children: ");
            for (DecisionTreeNode node: childrenList
                 ) {
                System.out.println("  "+node.name);
            }
           nodeList.remove(0);
            childrenList.clear();
            System.out.println("");
        }

    }
    public static DecisionTreeNode createTree(List<CSVAttribute[]> examples, int labelIndex) {
        DecisionTreeNode tree= new DecisionTreeNode(examples,labelIndex);
        while(!DecisionTreeNode.allNodes.isEmpty()){
            buildNodes(DecisionTreeNode.allNodes);
        }
        return tree;
    }

}
