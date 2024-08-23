package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.io.Start;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import de.uni_trier.wi2.pki.util.ID3Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Contains util methods for performing a cross-validation.
 */
public class CrossValidator {

    /**
     *
     * @param dataset
     * @param numberOfFolds
     * @return the folds if number is 5 f.e. returns 5 diffrent lists
     */
    public static List<List<CSVAttribute[]>> splitDatasetIntoFolds(List<CSVAttribute[]> dataset, int numberOfFolds){
        List<List<CSVAttribute[]>> folds = new ArrayList<>();

        int totalSize = dataset.size();
        int foldSize = totalSize / numberOfFolds;
        int startIndex, endIndex;

        for (int i = 0; i < numberOfFolds; i++) {
            startIndex = i * foldSize;
            // Für den letzten Fold, nimm alle übrigen Datenpunkte
            endIndex = (i == numberOfFolds - 1) ? totalSize : startIndex + foldSize;

            List<CSVAttribute[]> foldData = new ArrayList<>();
            for (int j = startIndex; j < endIndex; j++) {
                foldData.add(dataset.get(j));
            }
            folds.add(foldData);
        }

        return folds;
    }
    public static BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction = (data,labelindex)->{
        try {
            return ID3Utils.createTree(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    /**
     * when tree gets validated the node which gets passed by an element
     * gets checked if it would predict it right and counts up the right guesses
     * @param node
     * @param element
     */
    public static void countCorrectGuessesPerNode(DecisionTreeNode node, CSVAttribute[] element){
        int attributeValue =element[Start.getLabelindex()].getValueAsInteger();
        if(node.getNodeClass() == attributeValue) node.setCorrectGuesses(node.getCorrectGuesses()+1);
    }

    /**
     * traverser walks through tree until it reached a leaf or the element attribute is dissimilar to every child
     * @param traverser
     * @param element
     * @return
     */
    public static DecisionTreeNode walkThroughTree(DecisionTreeNode traverser,CSVAttribute[] element){
        boolean proceed=true;
        while(!traverser.isLeafNode() && proceed){
            countCorrectGuessesPerNode(traverser,element);
            List<DecisionTreeNode> children = new LinkedList<>(traverser.getSplits().values());
            int attributeValue = element[traverser.getAttributeIndex()].getValueAsInteger();

            for (int j = 0; j < children.size(); j++) {
                proceed=false;

                if(children.get(j).getSplitValue() ==attributeValue){
                    traverser=children.get(j);
                    proceed=true;
                    break;
                }
            }
        }
        return traverser;
    }

    /**
     * evaluates every Element in the testSet
     * @param model
     * @param testSet
     * @return accuracy
     */
    public static double evaluateModel(DecisionTreeNode model,List<CSVAttribute[]> testSet){

        int correct=0, wrong=0;

        for (int i = 0; i < testSet.size(); i++) {
            CSVAttribute[] element =testSet.get(i);
            DecisionTreeNode traverser=model;

            traverser=walkThroughTree(traverser,element);


            int elementAttributValue= element[Start.getLabelindex()].getValueAsInteger();

            if(traverser.getNodeClass() ==elementAttributValue) correct++;
            if(traverser.getNodeClass() !=elementAttributValue) wrong++;

        }

        return ((double) correct) /((double)correct+wrong);
    }

    /**
     * Performs a cross-validation with the specified dataset and the function to train the model.
     * also prunes the model best model gets returned
     * @param dataset        the complete dataset to use.
     * @param labelAttribute the label attribute.
     * @param trainFunction  the function to train the model with.
     * @param numFolds       the number of data folds.
     */
    public static DecisionTreeNode performCrossValidation(List<CSVAttribute[]> dataset, int labelAttribute,
                                                          BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction,
                                                          int numFolds) {
        List<List<CSVAttribute[]>> folds = splitDatasetIntoFolds(dataset, numFolds);
        DecisionTreeNode bestModel = null;
        double bestAccuracy = 0;

        for (int i = 0; i < numFolds; i++) {
            List<CSVAttribute[]> trainSet = new ArrayList<>();
            List<CSVAttribute[]> testSet = folds.get(i);

            // Erstelle den Trainingsdatensatz
            for (int j = 0; j < numFolds; j++) {
                if (j != i) {
                    trainSet.addAll(folds.get(j));
                }
            }

            // Trainiere das Modell
            DecisionTreeNode model = trainFunction.apply(trainSet, labelAttribute);

            // Bewerte das Modell
            double accuracy = evaluateModel(model, testSet);
            System.out.println("accuracy before Pruning: " + accuracy);
            ReducedErrorPruner.pruneOnValidatedSet(model);
            accuracy = evaluateModel(model,testSet);
            System.out.println("accuracy after Pruning: " + accuracy);
            // Wähle das beste Modell aus
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestModel = model;
            }
        }
        return bestModel;

    }


}


