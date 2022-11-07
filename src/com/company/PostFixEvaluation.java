package com.company;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class PostFixEvaluation {

    private final static char ADD = '+';
    private final static char SUBTRACT = '-';
    private final static char MULTIPLY = '*';
    private final static char DIVIDE = '/';

    private final static String ERROR_MESSAGE =  "#ERR";

    private final Map<String, String> resultsMap = new HashMap<>();
    private final List<String> fixedCells = new ArrayList<>();
    private final Map<String, String[]> notSeenMap = new HashMap<>();

    private int rowCount = 0;
    private int maxColumns = 0;

    private final String cellContainsDigitsCheck = "^[0-9]*$";
    private final String cellContainsAlphabetLetterCheck = ".*[a-zA-Z].*";
    private final String cellContainsOperatorCheck = "\"[a-zA-Z]+";

    private final DecimalFormat df = new DecimalFormat("0.##########"); // 10 precision - not pretty

    public void evaluate(String inputFile) throws Exception {
        Scanner sc = new Scanner(new File(inputFile));
        sc.useDelimiter("\n");

        while (sc.hasNext()) {
            String rowData = sc.next();
            String[] cells = rowData.split("\\s*,\\s*");
            maxColumns = Math.max(cells.length, maxColumns);

            for(int i = 0; i < cells.length; i++){
                String[] splitCell = cells[i].split(" ");

                Queue<Double> queue = new ArrayDeque<>();
                computeSingleCell(splitCell, queue, i, rowCount);

                String key = getLocation(i, rowCount);
                if(queue.size() == 1){
                    resultsMap.put(key, df.format(queue.peek()));
                } else {
                    resultsMap.put(key, ERROR_MESSAGE);
                }
            }
            rowCount++;
        }

        int notSeenMapPreviousSize = 0;
        while(!notSeenMap.isEmpty() && notSeenMap.size() != notSeenMapPreviousSize) {
            for (Map.Entry<String, String[]> entry : notSeenMap.entrySet()) {
                Queue<Double> queue = new ArrayDeque<>();
                ComputerSingleCellForErrorList(entry, queue);
            }
            for(String fixedCell : fixedCells){
                notSeenMap.remove(fixedCell);
            }
            notSeenMapPreviousSize = notSeenMap.size();
        }

        if(notSeenMap.size() != 0){
            for (Map.Entry<String, String[]> entry : notSeenMap.entrySet()) {
                resultsMap.put(entry.getKey(), ERROR_MESSAGE);
            }
        }
        sc.close();
    }

    public void writeToFile(FileDescriptor location) throws IOException {
        writeToCSVFile(location, resultsMap, maxColumns);
    };

    private void computeSingleCell(String[] splitCell, Queue<Double> queue, int index, int rowCount) throws Exception {
        for(String currSymbol : splitCell){
            if(currSymbol.matches(cellContainsDigitsCheck)){
                // Number i.e. '10'
                queue.add(Double.parseDouble(currSymbol));
            } else if(Pattern.compile(cellContainsAlphabetLetterCheck).matcher(currSymbol).matches()){
                // row/col lookup i.e. 'a0'
                // Check if it exists as it may reference something ahead in the list
                String value = resultsMap.get(currSymbol);
                if(value != null && !value.equals(ERROR_MESSAGE)){
                    queue.add(Double.parseDouble(value));
                } else {
                    // It doesn't exist here so add to a new list to check again after
                    notSeenMap.put(getLocation(index, rowCount), splitCell);
                    queue.clear();
                    return;
                }
            } else if(splitCell.length > 1 && !currSymbol.contains(cellContainsOperatorCheck)){
                // operator + - / *
                double currentResultValue = queue.remove();

                while(!queue.isEmpty()){
                    currentResultValue = evaluateOperator(currSymbol.charAt(0), currentResultValue, queue.remove());
                }
                queue.add(currentResultValue);
            }
        }
    }

    private void ComputerSingleCellForErrorList(Map.Entry<String, String[]> entry, Queue<Double> queue) throws Exception {
        for (String currSymbol : entry.getValue()) {
            if(currSymbol.matches(cellContainsDigitsCheck)){
                // Number i.e. '10'
                queue.add(Double.parseDouble(currSymbol));
            } else if (Pattern.compile(cellContainsAlphabetLetterCheck).matcher(currSymbol).matches()) {
                // row/col lookup i.e. 'a0'
                // Check if it exists as it may reference something ahead in the list
                String value = resultsMap.get(currSymbol);
                if (value != null && !value.equals(ERROR_MESSAGE)) {
                    queue.add(Double.parseDouble(value));
                } else {
                    return;
                }
            } else if (entry.getValue().length > 1 && !currSymbol.contains(cellContainsOperatorCheck)) {
                // operator + - / *
                double currentResultValue = queue.remove();

                while (!queue.isEmpty()) {
                    currentResultValue = evaluateOperator(currSymbol.charAt(0), currentResultValue, queue.remove());
                }
                queue.add(currentResultValue);
            }
            if(queue.size() == 1){

                resultsMap.put(entry.getKey(), df.format(queue.peek()));
                fixedCells.add(entry.getKey());
            } else {
                resultsMap.put(entry.getKey(), ERROR_MESSAGE);
            }
        }
    }

    private double evaluateOperator(char operator, double operandOne, double operandTwo) throws Exception {
        switch (operator) {
            case ADD : { return operandOne + operandTwo; }
            case SUBTRACT : { return operandOne - operandTwo; }
            case DIVIDE : { return operandOne / operandTwo; }
            case MULTIPLY : { return operandOne * operandTwo; }
        }
       throw new Exception();
    }

    public String getLocation(int column, int row){
        column++;
        row++;
        String result;

        if(column > 0 && column < 27){
            result =  String.valueOf((char)(column + 64));
        } else if(column > 26){
            // Assumes we have a double character i.e. aa10
            int remainder = column % 26;
            int firstCharacter = column / 26;
            result = String.valueOf((char)(firstCharacter + 64)) + (char) (remainder + 64);
        } else {
            return null;
        }
        return (result + row).toLowerCase();
    }

    private void writeToCSVFile(FileDescriptor location, Map<String, String> resultForCSV, int maxColumnCount) throws IOException {
        List<String> resultKeys = new ArrayList<>(resultForCSV.keySet());
        resultKeys.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        FileWriter writer = new FileWriter(location);

        for(int i = 0; i < resultKeys.size() / maxColumnCount; i++){
            for(int j = 0; j < maxColumnCount; j++){
                writer.write(resultForCSV.get(resultKeys.get(i + j * maxColumnCount)));

                if(j == maxColumnCount - 1){
                    writer.write("\n");
                } else {
                    writer.write(",");
                }
            }
        }
        writer.close();
    }
}