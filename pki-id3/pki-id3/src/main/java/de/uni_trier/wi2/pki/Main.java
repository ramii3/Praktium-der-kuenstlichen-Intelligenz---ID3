package de.uni_trier.wi2.pki;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.util.EntropyUtils;
import de.uni_trier.wi2.pki.util.ID3Utils;


public class Main {

    public static void main(String[] args) {
        // TODO: this should be the main executable method for your project

        try{
            CSVReader.readCsvToArray("src/main/java/de/uni_trier/wi2/pki/diabetes.csv",",",true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //CSVReader.createSublist(1000);
        System.out.println(CSVReader.attributeNames.get(2));


        //discretizer.displayHorizontalAttributeArrays();
        BinningDiscretizer.initializeMatrix();
        BinningDiscretizer.discretizeAll(4,0);
        BinningDiscretizer.sort(4);
        //BinningDiscretizer.displayMatrix();
        //System.out.println(EntropyUtils.getEntropy(4,BinningDiscretizer.matrix));
        //EntropyUtils.displayAllNodeLists(4,EntropyUtils.splitList(4,BinningDiscretizer.matrix));
        //EntropyUtils.testEverySplit(1,BinningDiscretizer.matrix);
        //EntropyUtils.testSplit(1,4,BinningDiscretizer.matrix);
        //EntropyUtils.getAllCombinedSplitListEntropies(1,BinningDiscretizer.matrix);
        //EntropyUtils.printList(EntropyUtils.calcInformationGain(BinningDiscretizer.matrix,1));
        ID3Utils.displayTree(ID3Utils.createTree(BinningDiscretizer.matrix,0));
        //ID3Utils.createTree(BinningDiscretizer.matrix,1);

        //discretizer.sortListCounting(4);

        //discretizer.displayHorizontalAttributeArrays();

        /*Double a;
        double b;
        int c=0;
        b=(double) c;
        a = Double.valueOf(b);
        System.out.println(a);*/










    }

}
