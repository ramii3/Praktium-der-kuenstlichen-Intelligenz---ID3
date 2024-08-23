package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;

import java.util.*;

/**
 * Contains methods that help with computing the entropy.
 */
public class EntropyUtils {

    public static List<List<CSVAttribute[]>> bestSplit; // ein Split ist besteht aus den einzelnen Listen welche durch den Multisplit entstehen
    public static double bestEntropy = Double.MAX_VALUE;

    public static int splitAttribute;
    public static double bestInformatioGain = Double.MIN_VALUE;

    /*public static List<List<CSVAttribute[]>> testEverySplit(int labelindex, List<CSVAttribute[]> currentList ){
        int currentListSize = currentList.size();
        List<List<CSVAttribute[]>> bestSplit = new LinkedList<>();
        List<List<CSVAttribute[]>> tempList;
        double bestEntropy = Double.MAX_VALUE;
        double currentEntropy = 0;


       int numberOfAttributes = currentList.get(0).length;
       for (int i = 0; i < numberOfAttributes; i++) {

           if(i==labelindex)i++;
           if(i>numberOfAttributes-1)break;

           tempList = splitList(i, currentList);

           for (int j = 0; j < tempList.size(); j++) {

               if(!tempList.get(j).isEmpty()){
               currentEntropy += getEntropy(labelindex,tempList.get(j)) * ((double) tempList.get(j).size() / (double)currentListSize);
                   //System.out.println(getEntropy(labelindex,tempList.get(j)));
               }
           }

           System.out.println(currentEntropy);

           if(currentEntropy < bestEntropy){
               bestEntropy = currentEntropy;
               bestSplit = tempList;
           }
           currentEntropy=0;
       }
       //System.out.println(bestEntropy);
       EntropyUtils.bestSplit = bestSplit;
       return bestSplit;
   }*///checked
    private static int[] giveNumberOfEachElement(int labelIndex , List<CSVAttribute[]> currentList){

        int max =(int)(double)  BinningDiscretizer.getMax(labelIndex);
        int[] numbers= new int[max+1];

        for (int i = 0; i < currentList.size(); i++) {
            numbers[(int)(double) currentList.get(i)[labelIndex].getValue()]++;
        }
        return numbers;
    }//checked

    public static Double getMax(int attribute, List<CSVAttribute[]> currentList){
        Double[] data= new Double[currentList.size()];
        for (int i = 0; i < currentList.size(); i++) {

            data[i] = (Double) currentList.get(i)[attribute].getValue(); //extracts the attributevalues of the Elements and writes them to a new array
        }
        if(Arrays.stream(data).findAny().isEmpty()){return 0d;}
        return Arrays.stream(data).max(Double::compare).get();

    }//checked

    public static double getEntropy(int labelIndex, List<CSVAttribute[]> currentList){
        double entropy=0;


        int listlength= currentList.size();

        int[] numbers = giveNumberOfEachElement(labelIndex, currentList);


        int max =(int)(double) getMax(labelIndex,currentList);
        /*String str="";
        for (int a:numbers
             ) {

            str+= a+" ";

        }
        System.out.println(str);*/
        for (int i = 0; i <= max; i++) {
            double probability = numbers[i] / (double) listlength;  //one of the Values needs to be double in order for the result to be a double
            //System.out.println(probability);
            if(probability!=0) //avoids NaN results
                entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }//checked

    public static List<List<CSVAttribute[]>> splitList(int attributeID, List<CSVAttribute[]> currentList){
        List<List<CSVAttribute[]>> nodeLists = new LinkedList<>();

        for (int i = 0; i <= getMax(attributeID,currentList); i++) {
            nodeLists.add(new ArrayList<>());
        }

        for (int i = 0; i < currentList.size(); i++) {


            nodeLists.get((int)(double)currentList.get(i)[attributeID].getValue()).add(currentList.get(i)); // get() receives the Value of the attribute of the current Element so the Element is added to the right Index
        }

        return nodeLists;
    }//checked

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

    private static double[] getEachSplitEntropy(int labelindex, int attributeID, List<List<CSVAttribute[]>> splits){
        double[] entropies= new double[splits.size()];
        for (int i = 0; i < splits.size(); i++) {
           entropies[i] = getEntropy(labelindex,splits.get(i));
        }
        /*for (double a:
             entropies) {
            System.out.println(a);
        }*/
        return entropies;
    }

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

    private static void checkIfBestSplit(double entropy, List<List<CSVAttribute[]>> currentSplit, int splitAttribute){
        if(EntropyUtils.bestEntropy>entropy){
            bestSplit=currentSplit;
            bestEntropy=entropy;
            EntropyUtils.splitAttribute=splitAttribute;
        }
    }


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
           entropies = getEachSplitEntropy(labelindex,i,tempSplit);
           combinedEntropies = getCombinedSplitEntropy(entropies, currentList.size(), tempSplit);
           listOfEntropies.add(combinedEntropies);
           checkIfBestSplit(combinedEntropies,tempSplit,i);
        }

        printList(listOfEntropies);
        return listOfEntropies;

    }

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



    private static void checkIfBestInformationGain(double informationGain){
        if(informationGain>bestInformatioGain) bestInformatioGain=informationGain;
    }

    /**
     * Calculates the information gain for all attributes
     *
     * @param matrix     Matrix of the training data (example data), e.g. ArrayList<String[]>
     * @param labelIndex the index of the attribute that contains the class. If the dataset is [Temperature,Weather,PlayFootball] and you want to predict playing
     *                   football, than labelIndex is 2
     * @return the information gain for each attribute
     */

    public static List<Double> calcInformationGain(List<CSVAttribute[]> matrix, int labelIndex, List<Integer> alreadySplitted) {
        List<Double> infoGains = new ArrayList<>();
        List<Double> allSplitEntropies = getAllCombinedSplitEntropies(labelIndex, matrix, alreadySplitted);
        double matrixEntropy = getEntropy(labelIndex,matrix);
        //System.out.println("current Entropy "+matrixEntropy);
        //System.out.println("best Entropy "+bestEntropy);
        if(matrixEntropy==0)return null;
        for (int i = 0; i < allSplitEntropies.size() ; i++) {
            infoGains.add(matrixEntropy-allSplitEntropies.get(i));
            checkIfBestInformationGain(matrixEntropy-allSplitEntropies.get(i));
        }

        return infoGains;
    }

}
