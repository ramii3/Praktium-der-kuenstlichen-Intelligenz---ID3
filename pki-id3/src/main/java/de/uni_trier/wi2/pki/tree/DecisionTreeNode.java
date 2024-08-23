package de.uni_trier.wi2.pki.tree;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.Start;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.util.EntropyUtils;

import java.util.*;

/**
 * Class for representing a node in the decision tree.
 */
public class DecisionTreeNode {
    /**
     * saves all generated nodes and deletes the parent one from the list
     * allNodes initializes with root node and is empty when tree is finished
     */
    public static List<DecisionTreeNode> allNodes = new LinkedList<>();
    /**
     * The parent node in the decision tree.
     */
    private DecisionTreeNode parent;

    /**
     * The attribute index to check.
     */
    private int attributeIndex;

    /**
     * The checked split condition values and the nodes for these conditions.
     */
    private HashMap<String, DecisionTreeNode> splits = new HashMap<>();

    /**
     * contains the sublist of the parents split for root its BinningDiscretizer.matrix
     */
    List<CSVAttribute[]> nodeMatrix;

    /**
     * used to avoid splits already made
     */
    private List<Integer> alreadySplitted;
    private String name;

    /**
     * the Value of split the node reveives from its parent
     */
    private Integer splitValue = null;
    int depth=0;

    /**
     * the class or labelindexvalue the node predicts an example Element
     */
    private Integer nodeClass = null;
    private int correctGuesses=0;


    //---getter
    public String getName(){
        return name;
    }
    public DecisionTreeNode getParent(){
        return parent;
    }
    public int getAttributeIndex() {
        return attributeIndex;
    }
    public HashMap<String, DecisionTreeNode> getSplits() {
        return splits;
    }
    public void setSplits(HashMap<String,DecisionTreeNode> hashMap){
        splits = hashMap;
    }
    public Integer getSplitValue() {
        return splitValue;
    }
    public Integer getNodeClass() {
        return nodeClass;
    }
    public int getCorrectGuesses(){
        return correctGuesses;
    }
    public void setCorrectGuesses(int correctGuesses){
        this.correctGuesses=correctGuesses;
    }

    public boolean isLeafNode(){
        return splits.isEmpty();
    }


    //-Konstruktoren
    /**
     * root Constructor
     */
    public DecisionTreeNode(List<CSVAttribute[]> nodeMatrix){
        this.nodeMatrix = nodeMatrix;

        this.name = "root";
        allNodes.add(this);
        this.getAlreadySplitted();
        this.calcNodeClass();
    }

    /**
     * Child Constructor
     */
    DecisionTreeNode(List<CSVAttribute[]> nodeMatrix, DecisionTreeNode parent, int splitNumber){
        this.nodeMatrix = nodeMatrix;
        this.parent = parent;
        this.name = convertIDToString(parent.attributeIndex)+"-"+splitNumber;
        this.splitValue =splitNumber;

        this.getAlreadySplitted();
        this.depth = parent.depth+1;
        calcNodeClass();

    }



    //--Konstruktor Methoden
    /**
     * returns name of attribute or index if header isn't present
     */
    public String convertIDToString(int attributeIndex){
        if(CSVReader.getAttributeNames().isEmpty()) return String.valueOf(attributeIndex);
        return CSVReader.getAttributeNames().get(attributeIndex);
    }

    /**
     * @param list the list to copy
     * @return a copied list with no connection to old one
     */
    public List<Integer> copyList(List<Integer> list){
        List<Integer> newList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            int copy = list.get(i);
            newList.add(copy);
        }
        return newList;
    }

    /**
     * @return a list of all attributes the node was already split on
     */
    public List<Integer> getAlreadySplitted(){
        if(this.parent==null){
            this.alreadySplitted = new ArrayList<>();
            return new ArrayList<>();
        }
        List<Integer> alreadySplitted = copyList(parent.alreadySplitted);

        alreadySplitted.add(EntropyUtils.getSplitAttribute());
        this.alreadySplitted = alreadySplitted;
        return alreadySplitted;
    }

    /**
     *
     * @param data a array of ints which index stands for the labelattribute value and which entries represent how often the labelattribute value is present
     * @return the index of the biggest Entry
     */
    private int getBiggestClass(int[] data){
        int temp=0;
        int isaver=0;
        for (int i = 0; i < data.length; i++) {
            if(data[i]>=temp){
                temp=data[i];
                isaver=i;}
            //System.out.println("datalength: " +data.length);
        }
        return isaver;
    }

    /**
     * calc node class by counting each occurrence of one labelattribute in an int array which index represents the labelattribute Value
     */
    private void calcNodeClass(){
        int[] leaveNode = new int[(int)(double)EntropyUtils.getMax(Start.getLabelindex(),this.nodeMatrix)+1];

        for (int i = 0; i < this.nodeMatrix.size(); i++) {
            leaveNode[(int)(double)(Double)this.nodeMatrix.get(i)[Start.getLabelindex()].getValue()]++;

        }

        this.nodeClass = getBiggestClass(leaveNode);

        //System.out.println("Leavclass: "+this.nodeClass+" "+this.name+" false: "+leaveNode[0]+" true: " );

    }




    //--wählt passenden split

    /**
     * if list is undefined it was'nt initialized by calcInformation gain which means either list is pure or InfoGain is 0
     * @param list
     * @return
     */
    public boolean checkIfNodeListIsPure(List<Double> list){
        return list == null;
    }

    /**
     *
     * @param nodeElements
     * @param labelindex
     * @param alreadySplitted
     * @return the split which is going to be used to create the children or if no bestSplit available null
     */
    public List<List<CSVAttribute[]>> chooseSplit(List<CSVAttribute[]> nodeElements, int labelindex, List<Integer> alreadySplitted){
        List<Double> informationGains = EntropyUtils.calcInformationGain(nodeElements,labelindex,alreadySplitted);
        if(checkIfNodeListIsPure(informationGains)){
           return null;}
        return EntropyUtils.getBestSplit();
    }



    //--wendet passenden Split an

    /**
     *
     * @param split
     * @return if maxDepth is exceeded or split is null true else false
     */
    public boolean cancelSplit(List<List<CSVAttribute[]>> split){
       int maxDepth= Start.getMaxDepth();
        if(this.depth>=maxDepth||split==null){
            EntropyUtils.resetEntropyUtils();
            return true;
        }
        // if Node goes to deep or is pure
        return false;

    }

    /**
     * applies split to a node splits gets filled with the children
     */
    public void applySplit(){

        List<List<CSVAttribute[]>> split = chooseSplit(nodeMatrix, Start.getLabelindex(), alreadySplitted);
        if(cancelSplit(split)) {return;}

        int splitIndex = EntropyUtils.getSplitAttribute(); //attributeIndex
        this.attributeIndex = splitIndex;
        String attributeName = convertIDToString(splitIndex);

        if(name.equals("root")) name=attributeName;

        DecisionTreeNode decisionTreeNode;

        for (int i = 0; i < split.size(); i++) {
            while(split.get(i).isEmpty()){i++;}// skips splitvalue which don't contain anything

            decisionTreeNode = new DecisionTreeNode(split.get(i), this, i);
            this.splits.put(attributeName+","+i,decisionTreeNode);
            allNodes.add(decisionTreeNode);

        }
        EntropyUtils.resetEntropyUtils();
    }





    //Methoden um den Baum aufzuräumen
    /**
    * Es kann dazu kommen dass Knoten entstehen welche für den Split gleiche Ergebnisblätter haben
    * (nur bei Angabe einer maximalen Tiefe). Diese Blätter werden rekursiv zusammen geführt solange bis
    * die vorhandenen Blätter nicht mehr identische klassen haben.
    * */
    private void parentCleanUp() {
        if (this.parent == null) return;

        DecisionTreeNode parent = this.parent;
        LinkedList<DecisionTreeNode> children = new LinkedList<>(parent.splits.values());
        if (children.isEmpty()) return;
        if(!children.getFirst().isLeafNode()) return;
        int nodeClass = children.getFirst().nodeClass;
        for (DecisionTreeNode child : children) {
            if (!child.isLeafNode() || child.nodeClass != nodeClass) {
                return;
            }
        }
        //System.out.println("kam vor");
        // Alle Kinder haben die gleiche Klasse
        parent.nodeClass = nodeClass;
        parent.splits= new HashMap<>();
        parent.splitValue = 0;
    }
    public static void traverseAndCleanUp(DecisionTreeNode node) {
        if (node == null) return;

        // Rekursiver Aufruf für alle Kinder
        for (DecisionTreeNode child : node.splits.values()) {
            traverseAndCleanUp(child);
        }


        // Führe parentCleanUp für den aktuellen Knoten aus
        node.parentCleanUp();
    }

}
