package de.uni_trier.wi2.pki.io;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Is used to read in CSV files.
 */
public class CSVReader {
    private static List<String[]> data = null;
    private static List<CSVAttribute[]> matrix = new ArrayList<>();

    /**
     * 0 = ganze Zahl; 1 = kontinuierliche Zahl; 2 = String
     */
    private static int[] dataType =null;
    /**
     * holds how many different attributes are present
     * */
    private static int numberOfAttributes;
    private static List<String> attributeNames = new ArrayList<>(); // header names
    private static HashMap<String,Double>[] lexicon;


    public static List<String[]> getData(){
        return data;
    }
    public static List<CSVAttribute[]> getMatrix(){
        return matrix;
    }
    public static int getNumberOfAttribute(){
        return numberOfAttributes;
    }
    public static List<String> getAttributeNames() {
        return attributeNames;
    }
    public static HashMap<String, Double>[] getLexicon() {
        return lexicon;
    }


    /**
     * Read a CSV file and return a list of string arrays.
     *
     * @param relativePath the path where the CSV file is located (has to be relative path!)
     * @param delimiter    the delimiter symbol which is used in the CSV
     * @param ignoreHeader A boolean that indicates whether to ignore the header line or not, i.e., whether to include the first line into the list or not
     * @return A list that contains string arrays. Each string array stands for one parsed line of the CSV file
     * @throws IOException if something goes wrong. Exception should be handled at the calling function.
     */
    public static List<String[]> readCsvToArray(String relativePath, String delimiter, boolean ignoreHeader) throws IOException {
        BufferedReader csvreader = new BufferedReader(new FileReader(relativePath));
        String line = new String();

        List<String[]> Data = new ArrayList<>();


        while ((line = csvreader.readLine()) != null) {
            if(ignoreHeader){
                toList(line,delimiter);
                line= csvreader.readLine();
                ignoreHeader=false;}


            StringTokenizer stringTokenizer = new StringTokenizer(line,delimiter);
            String[] column = new String[stringTokenizer.countTokens()];
            int columnNumber = stringTokenizer.countTokens();


            for (int i = 0; i < columnNumber; i++) {
                column[i]=stringTokenizer.nextToken();
            }
            Data.add(column);
        }
        
        data =Data;
        dataType = new int[data.get(0).length];
        for (int i = 0; i < data.get(0).length; i++) {
            fetchAttributeDataType(i);
        }

        return Data;
    }
    public static void toList(String line,String delimiter){
        List<String> attributeNames= new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(line,delimiter);
        int iterator = stringTokenizer.countTokens();
        for (int i = 0; i < iterator; i++) {
            attributeNames.add(stringTokenizer.nextToken());
        }

        CSVReader.attributeNames=attributeNames;


    }
    private static void fetchAttributeDataType(int attributeId) {
        if (setIsString(attributeId)) {
            dataType[attributeId] = 2; // Der Wert 2 steht für String
        } else {
            for (String[] row : data) {
                try {
                    double a = Double.parseDouble(row[attributeId]);
                    if (a != (int) a) {
                        dataType[attributeId] = 1; // Der Wert 1 steht für kontinuierliche Zahl
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Ignorieren, da es kein String ist (bereits überprüft)
                }
            }
            dataType[attributeId] = 0; // Der Wert 0 steht für ganze Zahl
        }

    }
    private static boolean setIsString(int attributeId) {
        try {
            Double.parseDouble(data.get(0)[attributeId]);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }




    //-- Methoden zur Matrix Erstellung

    public static class CategoricalAttribute implements CSVAttribute {

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
        public Integer getValueAsInteger() {
            return (int)(double)value;
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
    public static class ContinuousAttribute implements CSVAttribute{

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
            return value;
        }

        @Override
        public Integer getValueAsInteger() {
            return (int)(double)value;
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
    public static class StringAttribute implements CSVAttribute{

        static Double[] count =new Double[data.get(0).length];


        public void defineLexicon(){
            lexicon = new HashMap[data.get(0).length];
            for (int i = 0; i < data.get(0).length; i++) {
                lexicon[i]=new HashMap<>();
            }
        }
        public void countUniqueStrings(int attributeNumber){
            //System.out.println(lexicon[attributeNumber].get(this.str) ==null);
            if(!lexicon[attributeNumber].containsKey(this.str)){
                if(count[attributeNumber]==null)count[attributeNumber]=0d;
                lexicon[attributeNumber].put(this.str,count[attributeNumber]);
                //System.out.println(count[attributeNumber]);

                count[attributeNumber]+=1;
            }
            value = lexicon[attributeNumber].get(str);

        }
        String str;
        Double value;
        public void fetchAttributeValue(int row,int attributeNumber){
            this.str= String.valueOf(data.get(row)[attributeNumber]);
            if(lexicon==null)defineLexicon();
            countUniqueStrings(attributeNumber);

        }
        @Override
        public Integer getValueAsInteger() {
            return (int)(double)value;
        }

        @Override
        public void setValue(Object value) {

        }

        @Override
        public Object getValue() {
            return value;
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

    /**
     *
     * @param row
     * @return CSVAttribute[] instanceOf one of the classes
     */
    private static CSVAttribute[] createElement(int row){
        CategoricalAttribute categoricalAttribute = new CategoricalAttribute();
        ContinuousAttribute continuousAttribute = new ContinuousAttribute();
        StringAttribute stringAttribute = new StringAttribute();
        CSVAttribute[] attributeArray = new CSVAttribute[data.get(row).length];

        for (int i = 0; i < data.get(row).length; i++) {
            if(CSVReader.dataType[i]==1){
                continuousAttribute.fetchAttributeValue(row, i);
                attributeArray[i] = continuousAttribute;
                continuousAttribute = new ContinuousAttribute();
            }
            else if(CSVReader.dataType[i]==0){
                categoricalAttribute.fetchAttributeValue(row, i);
                attributeArray[i] = categoricalAttribute;
                categoricalAttribute = new CategoricalAttribute();

            } else if(CSVReader.dataType[i]==2) {
                stringAttribute.fetchAttributeValue(row,i);
                attributeArray[i] = stringAttribute;
                stringAttribute = new StringAttribute();
            }
        }
        return attributeArray;

    }

    /**
     * initializes matrix with matrix.get(row)[col] pattern
     * @return
     */
    public static List<CSVAttribute[]> initializeMatrix(){
        List<CSVAttribute[]> listOfArrays = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            listOfArrays.add(createElement(i));
        }
        matrix = listOfArrays;
        numberOfAttributes = matrix.get(0).length;
        return listOfArrays;
    }


}
