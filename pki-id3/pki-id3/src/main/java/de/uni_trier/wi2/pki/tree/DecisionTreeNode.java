package de.uni_trier.wi2.pki.tree;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.util.EntropyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for representing a node in the decision tree.
 */
public class DecisionTreeNode {

    /**
     * The parent node in the decision tree.
     */
    public DecisionTreeNode parent;

    /**
     * The attribute index to check.
     */
    protected int attributeIndex;

    /**
     * The checked split condition values and the nodes for these conditions.
     */
    public HashMap<String, DecisionTreeNode> splits = new HashMap<>();

    List<CSVAttribute[]> nodeElements;

    public List<Integer> alreadySplitted;

    public static List<DecisionTreeNode> allNodes = new LinkedList<>();
    public String name;

    double informationGain = 0;

    int labelindex;


    public DecisionTreeNode(List<CSVAttribute[]> nodeElements, int labelindex){
        this.nodeElements = nodeElements;
        this.labelindex = labelindex;
        this.name = "root";
        allNodes.add(this);
        this.getAlreadySplitted();
    }
    DecisionTreeNode(List<CSVAttribute[]> nodeElements, DecisionTreeNode parent, int splitNumber){
        this.nodeElements = nodeElements;
        this.parent = parent;
        this.name = convertIDToString(parent.attributeIndex)+"-"+splitNumber;
        this.labelindex= parent.labelindex;
        this.getAlreadySplitted();

    }

    public String convertIDToString(int attributeIndex){
        return CSVReader.attributeNames.get(attributeIndex);
    }

    public List<Integer> copyList(List<Integer> list){
        List<Integer> newList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            int copy = list.get(i);
            newList.add(copy);
        }
        return newList;
    }

    public List<Integer> getAlreadySplitted(){
        if(this.parent==null){
            this.alreadySplitted = new ArrayList<>();
            return new ArrayList<>();}
        List<Integer> alreadySplitted;
        alreadySplitted = copyList(parent.alreadySplitted);
        alreadySplitted.add(EntropyUtils.splitAttribute);
        this.alreadySplitted = alreadySplitted;
        return alreadySplitted;
    }

    public boolean checkIfPure(List<Double> list){
        return list == null;
    }
    public List<List<CSVAttribute[]>> chooseSplit(List<CSVAttribute[]> nodeElements, int labelindex, List<Integer> alreadySplitted){
        List<Double> informationGains = EntropyUtils.calcInformationGain(nodeElements,labelindex,alreadySplitted);
        if(checkIfPure(informationGains)){
           return null;}
        //System.out.println(informationGains.size());
        this.informationGain = EntropyUtils.bestInformatioGain;
        System.out.println(EntropyUtils.bestInformatioGain);
        return EntropyUtils.bestSplit;
    }

    public boolean cancelSplit(List<List<CSVAttribute[]>> split,double informationGain){
        if(split!=null &&informationGain>0.01) return false;

        resetEntropyUtils();
        return true;
    }
    public void applySplit(){

        List<List<CSVAttribute[]>> split = chooseSplit(nodeElements, labelindex, alreadySplitted);
        if(cancelSplit(split,EntropyUtils.bestInformatioGain)) {return;}
        int splitIndex = EntropyUtils.splitAttribute; //attributeIndex
        this.attributeIndex = splitIndex;
        String attributeName = convertIDToString(splitIndex);
        DecisionTreeNode decisionTreeNode;

        for (int i = 0; i < split.size(); i++) {
            while(split.get(i).isEmpty()){i++;}// skips splitvalue which don't contain anything

            decisionTreeNode = new DecisionTreeNode(split.get(i), this, i);
            this.splits.put(attributeName+"-"+i,decisionTreeNode);
            allNodes.add(decisionTreeNode);

        }
        resetEntropyUtils();
    }
    public static void resetEntropyUtils(){
        EntropyUtils.bestEntropy=Double.MAX_VALUE;
        EntropyUtils.bestSplit= new LinkedList<>();
        EntropyUtils.bestInformatioGain = Double.MIN_VALUE;
    }// is needed to reuse EntropyUtils otherwise the new node would interfere with the values of the old node

}
