package de.uni_trier.wi2.pki.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Is used to read in CSV files.
 */
public class CSVReader {
    public static List<String[]> matrix = null;
    public static boolean[] isContinuous =null;
    public static List<String> attributeNames = new ArrayList<>();

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
                System.out.println(ignoreHeader);
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
        
        matrix=Data;
        isContinuous = new boolean[matrix.get(0).length];
        for (int i = 0; i < matrix.get(0).length; i++) {
            getNumberRealm(i);
        }

        return Data;
    }

    public static void toList(String line,String delimiter){
        List<String> attributeNames= new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(line,delimiter);
        int iterator = stringTokenizer.countTokens();
        //System.out.println(stringTokenizer.countTokens());
        for (int i = 0; i < iterator; i++) {
            attributeNames.add(stringTokenizer.nextToken());
        }

        CSVReader.attributeNames=attributeNames;
        System.out.println("hier");
        System.out.println(CSVReader.attributeNames.size());
    }

    private static void getNumberRealm(int attribute){

        for (int i = 0; i < matrix.size(); i++) {
            double a = Double.parseDouble(matrix.get(i)[attribute]);
            int b = (int) a;
            if(b!=a){
                isContinuous[attribute]=true;
                break;
            }
        }

    }

    public static void displayNumberRealm(){
        for (int i = 0; i < isContinuous.length; i++) {
            System.out.println(isContinuous[i]);
        }
    }



    public static void createSublist(int sublistlength){

        boolean largerthanlist= false;
        List<String[]> newmatrix = new ArrayList<>();
        String[] str;

        if(matrix==null){
            System.out.println("Run readCsvToArray first before running displaySublist");
            return;
        }
        if(sublistlength>matrix.size()){
            sublistlength=matrix.size();
            largerthanlist=true;
        }
        for (int i = 0; i < sublistlength; i++) {
            str=matrix.get(i);
            newmatrix.add(str);
        }
        matrix = newmatrix;

    }
    public static void displaySublist(int sublistlength){
        String temp="";
        boolean largerthanlist= false;
        if(matrix==null){
            System.out.println("Run readCsvToArray first before running displaySublist");
            return;
        }
        if(sublistlength>matrix.size()){
            sublistlength=matrix.size();
            largerthanlist=true;
        }
        for(int i=0;i<sublistlength;i++){
            for(int j=0;j<matrix.get(i).length;j++){
                System.out.print(matrix.get(i)[j] + ", ");
                temp=temp + matrix.get(i)[j] + ", ";

            }
            System.out.println(temp.substring(0,temp.length()-2));
            temp="";
        }
        if(largerthanlist){
            System.out.println("The given sublistlength exceeds the given listlength and is set to the given listlength");
            System.out.println("If you ran createSubList just run readCSVtoArray again to fetch original List");
        }

    }
}
