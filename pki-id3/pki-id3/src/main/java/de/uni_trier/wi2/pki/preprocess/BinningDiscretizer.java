package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.util.*;

/**
 * Class that holds logic for discretizing values.
 */


    /**
     * Discretizes a collection of examples according to the number of bins and the respective attribute ID.
     *
     * @param numberOfBins Specifies the number of numeric ranges that the data will be split up in.
     * @param examples     The list of examples to discretize.
     * @param attributeId  The ID of the attribute to discretize.
     * @return the list of discretized examples.
     */
    public class BinningDiscretizer {
        static List<String[]> data= CSVReader.matrix;
        public static List<CSVAttribute[]> matrix = new ArrayList<>();
        //public static List<CSVAttribute[]> matrix = new ArrayList<>();

        double[] bins;

        public static int numberOfAttributes;
        public BinningDiscretizer(){
            this.initializeMatrix();
        }

        /**
         * Discretizes a collection of examples according to the number of bins and the respective attribute ID.
         *
         * @param numberOfBins Specifies the number of numeric ranges that the data will be split up in.
         * @param examples     The list of examples to discretize.
         * @param attributeId  The ID of the attribute to discretize.
         * @return the list of discretized examples.
         */

        //for equal-frequency Discretization
        public static List<CSVAttribute[]> discretize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {

            if((!CSVReader.isContinuous[attributeId])&& getMin(attributeId)>=0 && getMax(attributeId)<=1) {
                //System.out.println("is ein boolean");
                return examples;
            }
            double[] bins = createBinRange(numberOfBins,attributeId);
            for(int i=0; i < examples.size(); i++){
                for (int j = 0; j < bins.length; j++) {
                    if((double)examples.get(i)[attributeId].getValue() <= bins[j]){
                        double temp = (double) j;


                        examples.get(i)[attributeId].setValue(temp);
                        break;
                    }
                }
            }
            matrix = examples;
            return examples;
        }

        public static List<CSVAttribute[]> discretizeAll(int numberOfBins, int labelindex){
            List<CSVAttribute[]> examples= matrix;
            int attributenumber = examples.get(0).length;
            for (int i = 0; i < attributenumber; i++) {
                if(i!=labelindex)
                examples = discretize(numberOfBins,examples,i);
            }
            System.out.println("Discretizing Complete");
            matrix = examples;
            return examples;
        }
        // for equal-width Discretization
        public static List<CSVAttribute[]> discretizeEqualDataSize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId){
            if((!CSVReader.isContinuous[attributeId]) && getMin(attributeId)>=0 && getMax(attributeId)<=1) {
                //System.out.println("is ein boolean");
                return examples;
            }
            if( CSVReader.isContinuous[attributeId] )//determines which sorting algorithm is used in general categorical sorting is faster cause it uses counting sort thus using no swap operations and less comparisons
            {
                sortListQuick(attributeId,examples);
            }
            else sortListCounting(attributeId,examples);

            int[] binsize = createBinRangeEqualDataSize(numberOfBins);

            for (int i = 0; i < matrix.size(); i++) {
                for (int j = 0; j < binsize.length; j++) {
                    if(i < binsize[j]){

                        examples.get(i)[attributeId].setValue((double) j);
                        break;
                    }
                }
            }
            matrix =examples;

            return examples;
        }

        public static List<CSVAttribute[]> discretizeEqualDataSizeAll(int numberOfBins, int labelindex ){
            List<CSVAttribute[]> examples= matrix;
            int attributenumber = examples.get(0).length;
            for (int i = 0; i < attributenumber; i++) {
                if(i!=labelindex)
                examples = discretizeEqualDataSize(numberOfBins,examples,i);
            }
            System.out.println("Discretizing Complete");
            matrix = examples;
            return examples;
        }



        private static double[] createBinRange(int numberOfBins,int attributeId){
            double[] bins =new double[numberOfBins];
            double maxValue = getMax(attributeId);
            double minValue = getMin(attributeId);
            double binRange = (maxValue - minValue)/numberOfBins;

            for (int i = 1; i <= numberOfBins; i++) {
                bins[i-1] = minValue+i*binRange;
            }

            return bins;
        }
        private static int[] createBinRangeEqualDataSize(int numberOfBins){
            int[] bins = new int[numberOfBins];
            for (int i = 1; i < numberOfBins; i++) {
               bins[i-1] = i * (BinningDiscretizer.matrix.size()/numberOfBins);
            }

            bins[numberOfBins-1]=Integer.MAX_VALUE;
            return bins;
        }


        // Erstellen einer Liste von Attribute Arrays welche als CSVAttribute[] gespeichert werden
        public static void displayMatrix(){
            for (int i = 0; i < matrix.size(); i++) {
                for (int j = 0; j < matrix.get(i).length; j++) {
                    System.out.print(matrix.get(i)[j].getValue()+ " ");
                }
                System.out.println();
            }
        }

        // Erstellt die Liste aller erstellten Objekte (in dem Fall Personen)
        public static List<CSVAttribute[]> initializeMatrix(){
            List<CSVAttribute[]> listOfArrays = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                listOfArrays.add(createHorizontalAttributeArray(i));
            }
            matrix = listOfArrays;
            numberOfAttributes = matrix.get(0).length;
            return listOfArrays;
        }

        // Erstellt ein Array aus allen Attributen eines Objekts (in dem Fall werden Personen erstellt)
        // Hier wird auch festgestellt ob es sich bei dem Attribut um ein Categorial oder Continuous attribut handelt
        private static CSVAttribute[] createHorizontalAttributeArray(int row){
            CategoricalAttribute categoricalAttribute = new CategoricalAttribute();
            ContinuousAttribute continuousAttribute = new ContinuousAttribute();
            CSVAttribute[] attributeArray = new CSVAttribute[data.get(row).length];

            for (int i = 0; i < data.get(row).length; i++) {
                if(CSVReader.isContinuous[i]){
                    continuousAttribute.fetchAttributeValue(row, i);
                    attributeArray[i] = continuousAttribute;
                    continuousAttribute = new ContinuousAttribute();
                }
               else {
                    categoricalAttribute.fetchAttributeValue(row, i);
                    attributeArray[i] = categoricalAttribute;
                    categoricalAttribute = new CategoricalAttribute();
                }
            }
            return attributeArray;

        }


        public static Double getMax(int attribute){
            Double[] data= new Double[matrix.size()];
            for (int i = 0; i < matrix.size(); i++) {

                data[i] = (Double) matrix.get(i)[attribute].getValue();
            }
            if(Arrays.stream(data).findAny().isEmpty()){return 0d;}
            return Arrays.stream(data).max(Double::compare).get();

        }

        public static Double getMin(int attribute){
            Double[] data= new Double[matrix.size()];
            for (int i = 0; i < matrix.size(); i++) {

                data[i] = (Double) matrix.get(i)[attribute].getValue();
            }
            if(Arrays.stream(data).findAny().isEmpty()){return 0d;}
            return Arrays.stream(data).min(Double::compare).get();

        }

        // under the assumption that only integer values are present we can use countingsort for O(n) performance
        public void sortListCounting(int attributeID){

           ArrayList<LinkedList<CSVAttribute[]>> data= new ArrayList<>();
           List<CSVAttribute[]> newArray = new ArrayList<>();

            for (int i = 0; i < getMax(attributeID)+1; i++) {
                LinkedList<CSVAttribute[]> temp = new LinkedList<>();
                data.add(temp);
            }

            int index;
            double doubleindex;

            for (int i = 0; i < matrix.size(); i++) {
                doubleindex= (double) matrix.get(i)[attributeID].getValue();
                index = (int) doubleindex;
                data.get(index).add(
                        this.matrix.get(i));
            }

            for (int i = 0; i < data.size(); i++) {
                while(!data.get(i).isEmpty()){
                    newArray.add(data.get(i).removeFirst());
                }
            }
            this.matrix = newArray;
            System.out.println("finished sorting");
        }

        private static List<CSVAttribute[]> sortListCounting(int attribute, List<CSVAttribute[]> listOfAttributeArrays){

            ArrayList<LinkedList<CSVAttribute[]>> data= new ArrayList<>();
            List<CSVAttribute[]> newArray = new ArrayList<>();

            for (int i = 0; i < getMax(attribute)+1; i++) {
                LinkedList<CSVAttribute[]> temp = new LinkedList<>();
                data.add(temp);
            }

            int index;
            double doubleindex;

            for (int i = 0; i < listOfAttributeArrays.size(); i++) {
                doubleindex= (double) listOfAttributeArrays.get(i)[attribute].getValue();
                index = (int) doubleindex;
                data.get(index).add(
                        listOfAttributeArrays.get(i));
            }

            for (int i = 0; i < data.size(); i++) {
                while(!data.get(i).isEmpty()){
                    newArray.add(data.get(i).removeFirst());
                }
            }
            //System.out.println("finished sorting");
            BinningDiscretizer.matrix =newArray;
            return newArray;

        }




        public static List<CSVAttribute[]> sortListQuick(int attribute, List<CSVAttribute[]> listOfAttributeArrays){
            Collections.sort(listOfAttributeArrays, new MyComparator(attribute));
            BinningDiscretizer.matrix =listOfAttributeArrays;
            return listOfAttributeArrays;
        }
        static class MyComparator implements Comparator<CSVAttribute[]>{
            public final int attribute;
            public  MyComparator(int attribute){
                this.attribute=attribute;
            }
            @Override
            public int compare(CSVAttribute[] o1, CSVAttribute[] o2) {

                return Double.compare((Double) o1[attribute].getValue(), (Double) o2[attribute].getValue());
            }
        }


        public static void sort(int attributeId){
            if( CSVReader.isContinuous[attributeId] )//determines which sorting algorithm is used in general categorical sorting is faster cause it uses counting sort thus using no swap operations and less comparisons
            {
                sortListQuick(attributeId,BinningDiscretizer.matrix);
            }
            else sortListCounting(attributeId,BinningDiscretizer.matrix);
        }



        static class CategoricalAttribute implements CSVAttribute {
            Integer attributeID;
            Double value;

            public void fetchAttributeValue(int row,int attributeNumber){
                this.value= Double.valueOf(data.get(row)[attributeNumber]);
            }

            @Override
            public void setValue(Object value) {

                this.value = (double) value;
            }

            @Override
            public Object getValue() {
                return this.value;
            }

            @Override
            public Object clone() {
                return null;
            }

            @Override
            public int compareTo(Object o) {
                return 0;
            }
        }

        static class ContinuousAttribute implements CSVAttribute{

            Double value;

            public void fetchAttributeValue(int row,int attributeNumber){
                this.value= Double.valueOf(data.get(row)[attributeNumber]);
            }
            @Override
            public void setValue(Object value) {

            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public Object clone() {
                return null;
            }

            @Override
            public int compareTo(Object o) {
                return 0;
            }
        }

    }


