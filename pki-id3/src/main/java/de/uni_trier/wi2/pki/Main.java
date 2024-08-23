package de.uni_trier.wi2.pki;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.Start;
import de.uni_trier.wi2.pki.postprocess.CrossValidator;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import de.uni_trier.wi2.pki.util.ID3Utils;


public class Main {

    public static void main(String[] args) throws Exception {
        // TODO: this should be the main executable method for your project
        // churn data src/main/java/de/uni_trier/wi2/pki/churn_data.csv
        // diabetes data src/main/java/de/uni_trier/wi2/pki/diabetes.csv
        Start.start(0,10000,0,"src/main/java/de/uni_trier/wi2/pki/diabetes.csv",",",true );
        //BinningDiscretizer.displayMatrix();
        Start.startPreProcess(1,4);
        Start.startAll(5,"result.csv");

    }

}
