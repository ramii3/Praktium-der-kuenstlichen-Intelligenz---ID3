package de.uni_trier.wi2.pki.io;

import de.uni_trier.wi2.pki.postprocess.CrossValidator;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.io.IOException;

public class Start {

    /**
     * global labelindex
     */
    private static int labelindex=0;
    public static int listsize;
    private static int maxDepth=4;

    private static DecisionTreeNode bestTree;

    public static DecisionTreeNode getBestTree(){
        return bestTree;
    }

    public static int getMaxDepth() {
        return maxDepth;
    }
    //
    public static int getLabelindex(){
        return labelindex;
    };

    /**
     *
     * @param labelindex
     * @param listsize
     * @param maxDepth 0 means unlocked depth
     * @param inputPath
     * @param delimiter
     * @param ignoreHeader
     * @throws IOException
     */
    public static void start(int labelindex, int listsize, int maxDepth, String inputPath ,String delimiter, boolean ignoreHeader) throws IOException {
        Start.labelindex=labelindex;
        Start.listsize=listsize;
        if(maxDepth<1) Start.maxDepth=Integer.MAX_VALUE;
        else Start.maxDepth=maxDepth;

        CSVReader.readCsvToArray(inputPath,delimiter,ignoreHeader);
        CSVReader.initializeMatrix();
        BinningDiscretizer.shuffleMatrix();
        BinningDiscretizer.makeSublist(0,listsize);
    }

    /**
     *
     * @param discretizer 0 = no discretizing   1 = Equal-Width    2 = Equal-Frequency
     * @param numberOfBins
     */
    public static void startPreProcess( int discretizer, int numberOfBins) {
        if(discretizer==1) BinningDiscretizer.discretizeAllEqualWidth(numberOfBins);
        if(discretizer==2) BinningDiscretizer.discretizeAllEqualFrequency(numberOfBins);

    }

    /**
     * Also performs reduced error pruning both with and without accuracies get displayed
     * @param numberOfFolds recommend 5
     * @param outputPath
     * @throws Exception
     */
    public static void startAll(int numberOfFolds,String outputPath) throws Exception {
        DecisionTreeNode model=CrossValidator.performCrossValidation(BinningDiscretizer.getMatrix(),labelindex,CrossValidator.trainFunction,numberOfFolds);
        XMLWriter.writeXML(outputPath,model);
        bestTree=model;
    }
}
