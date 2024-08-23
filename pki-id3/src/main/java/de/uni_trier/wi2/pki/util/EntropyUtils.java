package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;

import java.util.*;

/**
 * Contains methods that help with computing the entropy.
 */
public class EntropyUtils {

    private static List<List<CSVAttribute[]>> bestSplit; // ein Split ist besteht aus den einzelnen Listen welche durch den Multisplit entstehen
    public static void setBestSplit(List<List<CSVAttribute[]>> bestSplit){
        EntropyUtils.bestSplit=bestSplit;
    }
    public static List<List<CSVAttribute[]>> getBestSplit(){
        return bestSplit;
    }
    private static double bestEntropy = Double.MAX_VALUE;

    private static int splitAttribute;
    private static double bestInformatioGain = 0;

    public static int getSplitAttribute() {
        return splitAttribute;
    }


    /**
     * resets all the saved attributes to evaluate a new Split
     */
    public static void resetEntropyUtils(){
        EntropyUtils.bestEntropy=Double.MAX_VALUE;
        EntropyUtils.setBestSplit(new LinkedList<>());
        EntropyUtils.bestInformatioGain = 0;

    }// is needed to reuse EntropyUtils otherwise the new node would interfere with the values of the old node
    public static void printList(List<Double> list){
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        for (Double d:list
        ) {
            System.out.println(d);
        }
        System.out.println(" ");System.out.println(" ");
    }
    public static void displayAllNodeLists(int labelindex, List<List<CSVAttribute[]>> nodeList){
        String str="";

        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.get(i).size(); j++) {
                str += (int)(double)nodeList.get(i).get(j)[labelindex].getValue() + " =" +(int)(double)nodeList.get(i).get(j)[1].getValue()+" ";
            }
            System.out.println(str);
            str="";
        }
    }//checked

    /**
     * max of one attribute is needed for the entropy calculation, to know how many splits are present
     * and the counting of occurrences of each labelindex
     * @param attribute
     * @param currentList
     * @return
     */
    public static Integer getMax(int attribute, List<CSVAttribute[]> currentList){
        Integer[] data= new Integer[currentList.size()];
        for (int i = 0; i < currentList.size(); i++) {

            data[i] = currentList.get(i)[attribute].getValueAsInteger(); //extracts the attributevalues of the Elements and writes them to a new array
        }
        if(Arrays.stream(data).findAny().isEmpty()){return 0;}
        return Arrays.stream(data).max(Double::compare).get();

    }//checked

    /**
     *
     * @param labelIndex
     * @param currentList
     * @return the number of each labelindex
     */
    private static int[] giveNumberOfEachElement(int labelIndex , List<CSVAttribute[]> currentList){

        int max =(int)(double)  BinningDiscretizer.getMax(labelIndex);
        int[] numbers= new int[max+1];

        for (int i = 0; i < currentList.size(); i++) {
            numbers[currentList.get(i)[labelIndex].getValueAsInteger()]++;
        }
        return numbers;
    }//checked






    //------- Tools for splitting and Entropy

    /**
     *
     * @param labelIndex
     * @param currentList
     * @return the entropy of the given List at the labelattribute
     */
    public static double getEntropy(int labelIndex, List<CSVAttribute[]> currentList){

        double entropy=0;
        int listlength= currentList.size();
        int[] numbers = giveNumberOfEachElement(labelIndex, currentList);
        int max = getMax(labelIndex,currentList);

        for (int i = 0; i <= max; i++) {
            double probability = numbers[i] / (double) listlength;  //one of the Values needs to be double in order for the result to be a double

            if(probability!=0) //avoids NaN results
                entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }//checked

    /**
     *
     * @param attributeID
     * @param currentList
     * @return a list containing the sublists after the main list gets splittet
     */
    public static List<List<CSVAttribute[]>> splitList(int attributeID, List<CSVAttribute[]> currentList){
        List<List<CSVAttribute[]>> nodeLists = new LinkedList<>();

        for (int i = 0; i <= getMax(attributeID,currentList); i++) {
            nodeLists.add(new ArrayList<>());
        }

        for (int i = 0; i < currentList.size(); i++) {


            nodeLists.get(currentList.get(i)[attributeID].getValueAsInteger()).add(currentList.get(i)); // get() receives the Value of the attribute of the current Element so the Element is added to the right Index
        }

        return nodeLists;
    }//checked







    //------- Methode cascading to a list of Entropy of Each Split
    /**
     * the entropy of each sublist resulting by a split
     */
    private static double[] getEachSplitEntropy(int labelindex,  List<List<CSVAttribute[]>> splits){
        double[] entropies= new double[splits.size()];
        for (int i = 0; i < splits.size(); i++) {
           entropies[i] = getEntropy(labelindex,splits.get(i));
        }
        return entropies;
    }

    /**
     * the combined entropy of each sublist
     */
    private static double getCombinedSplitEntropy(double[] entropies, int currentListSize, List<List<CSVAttribute[]>> currentSplit){
        double currentEntropy=0;
        for (int j = 0; j < currentSplit.size(); j++) {

            if(!currentSplit.get(j).isEmpty()){
                currentEntropy += entropies[j] * ((double) currentSplit.get(j).size() / (double)currentListSize);
                //System.out.println(getEntropy(labelindex,tempList.get(j)));
            }//calculates entropy in form of: E(i)*(e(i)/e) + E(i2)*(e(i2)/e)...
        }
        return currentEntropy;
    }


    private static void checkIfBestEntropy(double entropy, List<List<CSVAttribute[]>> currentSplit, int splitAttribute){
        if(EntropyUtils.bestEntropy>entropy){
            bestSplit=currentSplit;
            bestEntropy=entropy;
            EntropyUtils.splitAttribute=splitAttribute;
        }
    }

    /**
     * test all splits and returns a list of them also checks for the best Entropy
     */
    public static List<Double> getAllCombinedSplitEntropies(int labelindex, List<CSVAttribute[]> currentList, List<Integer> alreadySplitted){

        List<Double> listOfEntropies = new ArrayList<>();
        List<List<CSVAttribute[]>> tempSplit;
        double[] entropies;
        double combinedEntropies;
        int numberOfAttributes = currentList.get(0).length;


        for (int i = 0; i < numberOfAttributes; i++) {

            while(alreadySplitted.contains(i)||i==labelindex){i++;} //skips already split attributes and labelindex

            if(i>numberOfAttributes-1)break;   //avoids index out of bounds if labelindex is last attribute

           tempSplit = splitList(i,currentList);
           entropies = getEachSplitEntropy(labelindex,tempSplit);
           combinedEntropies = getCombinedSplitEntropy(entropies, currentList.size(), tempSplit);
           listOfEntropies.add(combinedEntropies);
           checkIfBestEntropy(combinedEntropies,tempSplit,i);
        }

        //printList(listOfEntropies);
        return listOfEntropies;

    }







    //------ InformationGain Calculation
    private static void checkIfBestInformationGain(double informationGain){
        if(informationGain>bestInformatioGain){
            bestInformatioGain=informationGain;

        }

    }

    /**
     * Calculates the information gain for all attributes
     *
     * @param matrix     Matrix of the training data (example data), e.g. ArrayList<String[]>
     * @param labelIndex the index of the attribute that contains the class. If the dataset is [Temperature,Weather,PlayFootball] and you want to predict playing
     *                   football, than labelIndex is 2
     * @return the information gain for each attribute. if entropy = 0 or bestinfogain = 0 return null;
     */

    public static List<Double> calcInformationGain(List<CSVAttribute[]> matrix, int labelIndex, List<Integer> alreadySplitted) {
        List<Double> infoGains = new ArrayList<>();

        double matrixEntropy = getEntropy(labelIndex,matrix);
        if(matrixEntropy==0)return null; // list is pure no need to further split node


        List<Double> allSplitEntropies = getAllCombinedSplitEntropies(labelIndex, matrix, alreadySplitted);
        for (int i = 0; i < allSplitEntropies.size() ; i++) {
            infoGains.add(matrixEntropy-allSplitEntropies.get(i));
            checkIfBestInformationGain(matrixEntropy - allSplitEntropies.get(i));
        }
        if(bestInformatioGain==0){return null;}

        return infoGains;
    }


}
