package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.Start;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.util.*;



/**
 * Class that holds logic for discretizing values.
 */

    public class BinningDiscretizer {

        private static List<CSVAttribute[]> matrix = CSVReader.getMatrix();
        private static int numberOfBins;


        public static List<CSVAttribute[]> getMatrix() {
            return matrix;
        }


        public static Double getMax(int attributeId){
            Double[] data= new Double[matrix.size()];
            for (int i = 0; i < matrix.size(); i++) {

                data[i] = (Double) matrix.get(i)[attributeId].getValue();
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


        //------Tools to manipulate Matrix------------

    /**
     * shuffles matrix with switching through the labelindex values
     */
        public static void shuffleMatrix(){
            sort(Start.getLabelindex());
            List<CSVAttribute[]> temp=new ArrayList<>();
            List<List<CSVAttribute[]>> separated= new LinkedList<>();
            for (int i = 0; i <= (int)(double)getMax(Start.getLabelindex()); i++) {
                separated.add(new ArrayList<>());
            }
            for (int i = 0; i < matrix.size(); i++) {
                separated.get(matrix.get(i)[Start.getLabelindex()].getValueAsInteger()).add(matrix.get(i));
            }
            while(!separated.isEmpty()) {

                for (int j = 0; j < separated.size(); j++) {
                    if(separated.get(j).isEmpty()){
                        separated.remove(j);
                        break;
                    }
                    temp.add(separated.get(j).get(0));
                    separated.get(j).remove(0);
                }
            }
            matrix=temp;
        }

    /**
     * makes sublist of the part of the matrix from start to end
     * @param start
     * @param end
     */
    public static void makeSublist(int start,int end){
            List<CSVAttribute[]> newMatrix=new ArrayList<>();
            if(end>=matrix.size()||end<1)end=matrix.size()-1;
            for(int i= start; i<=end;i++){
                newMatrix.add(matrix.get(i));
            }
            matrix=newMatrix;
        }

        public static void displayMatrix(){
            for (int i = 0; i < matrix.size(); i++) {
                for (int j = 0; j < matrix.get(i).length; j++) {
                    System.out.print(matrix.get(i)[j].getValue()+ " ");
                }
                System.out.println();
            }
        }



        //------ sorting algorithms --------

        /**
         *  under the assumption that only integer values are present we can use countingsort for O(n) performance
         */
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

        /**
         * sorts list with quick sort
         * @param attribute
         * @param listOfAttributeArrays
         * @return
         */
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

        /**
         *  decides which algorithm is used for sorting
         * @param attributeId
         */
        public static void sort(int attributeId){
                if( matrix.get(0)[attributeId] instanceof CSVReader.CategoricalAttribute )//determines which sorting algorithm is used in general categorical sorting is faster cause it uses counting sort thus using no swap operations and less comparisons
                {
                    sortListQuick(attributeId,BinningDiscretizer.matrix);
                }
                else sortListCounting(attributeId,BinningDiscretizer.matrix);
            }




        //---Binning preparations---
        private static double[] createBinRangeForEqualWidth(int numberOfBins, int attributeId){
            double[] bins =new double[numberOfBins];
            double maxValue = getMax(attributeId);
            double minValue = getMin(attributeId);
            double binRange = (maxValue - minValue)/numberOfBins;

            for (int i = 1; i <= numberOfBins; i++) {
                bins[i-1] = minValue+i*binRange;
            }

            return bins;
        }
        private static int[] createBinRangeForEqualFrequency(int numberOfBins){
            int[] bins = new int[numberOfBins];
            for (int i = 1; i < numberOfBins; i++) {
                bins[i-1] = i * (BinningDiscretizer.matrix.size()/numberOfBins);
            }

            bins[numberOfBins-1]=Integer.MAX_VALUE;
            return bins;
        }

        /**
         * Discretizes a collection of examples according to the number of bins and the respective attribute ID.
         *
         * @param numberOfBins Specifies the number of numeric ranges that the data will be split up in.
         * @param examples     The list of examples to discretize.
         * @param attributeId  The ID of the attribute to discretize.
         * @return the list of discretized examples.
         */


        /**
         * avoids boolean and Strings
         * @param attributeId
         * @return
         */
        private static boolean dontDiscretize(int attributeId){
                if( matrix.get(0)[attributeId] instanceof CSVReader.CategoricalAttribute) // ist kategorisch
                {

                    if((getMin(attributeId)>=0 && getMax(attributeId)<=1)) // ist boolean
                    {

                        return true;
                    }
                }
                if(matrix.get(0)[attributeId] instanceof CSVReader.StringAttribute) return true; // ist String
                if(attributeId == Start.getLabelindex()) return true; // ist LabelAttribut
                return false;
            }

        /**
         * discretizes specific attribute
          * @param numberOfBins
         * @param examples
         * @param attributeId
         * @return
         */
        public static List<CSVAttribute[]> discretizeEqualWidth(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {

                double[] bins = createBinRangeForEqualWidth(numberOfBins,attributeId);



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

        /**
         * discretizes all attributes and avoid non discretizable ones
         * @param numberOfBins
         * @return
         */
        public static List<CSVAttribute[]> discretizeAllEqualWidth(int numberOfBins){
                BinningDiscretizer.numberOfBins=numberOfBins;
                List<CSVAttribute[]> examples= matrix;
                int attributenumber = examples.get(0).length;
                for (int i = 0; i < attributenumber; i++) {
                    if(dontDiscretize(i))continue;
                    examples = discretizeEqualWidth(numberOfBins,examples,i);
                }
                System.out.println("Discretizing Complete");
                matrix = examples;
                return examples;
            }

        /**
         * discretizes specific attribute
         * @param numberOfBins
         * @param examples
         * @param attributeId
         * @return
         */
        public static List<CSVAttribute[]> discretizeEqualFrequency(int numberOfBins, List<CSVAttribute[]> examples, int attributeId){

            sort( attributeId);


            int[] binsize = createBinRangeForEqualFrequency(numberOfBins);

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

        /**
         * discretizes all attributes and avoid non discretizable ones
         * @param numberOfBins
         * @return
         */
        public static List<CSVAttribute[]> discretizeAllEqualFrequency(int numberOfBins ){
            BinningDiscretizer.numberOfBins=numberOfBins;
            List<CSVAttribute[]> examples= matrix;
            int attributenumber = examples.get(0).length;
            for (int i = 0; i < attributenumber; i++) {
                if(dontDiscretize(i))continue;
                examples = discretizeEqualFrequency(numberOfBins,examples,i);
            }
            System.out.println("Discretizing Complete");
            matrix = examples;
            return examples;
        }


    }


