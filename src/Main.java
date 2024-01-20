import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
    private static boolean isDetailedDisplay = false;
    private static String mainString;
    private static int timeLimit = 20;
    private static boolean isEnd;
    private static boolean isToReloadProgram = true;
    private static int programLines = 0;
    private static int executedLines;
    private static int programLineNow;
    private static HashSet<Integer> ignoredLineSet;
    private static ArrayList<String> matchStrings;
    private static ArrayList<String> replaceToStrings;
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Do you need detailed information?(y/n): ");
        var tmp = input.nextLine().toLowerCase().charAt(0);
        if (tmp=='y') isDetailedDisplay=true;
        System.out.print("Input time limit(Positive integer): ");
        try {
            timeLimit = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException ignored) {}

        while (true) {
            ignoredLineSet = new HashSet<>();
            if (isToReloadProgram){
                //read in the program body
                Scanner program;
                try {
                    program = new Scanner(new File("programBody.txt"));
                } catch (Exception e) {
                    program = new Scanner("a=b");
                }
                matchStrings = new ArrayList<>();
                replaceToStrings = new ArrayList<>();
                programLineNow = 0;
                programLines=0;
                while (program.hasNextLine()){
                    String line = program.nextLine();
                    if (line.isEmpty()) continue;
                    if (line.contains("#")){
                        int ignoreIndex = line.indexOf("#");
                        if (ignoreIndex==0) continue;
                        else line = line.substring(0,ignoreIndex);
                    }
                    programLineNow++;
                    if (isIllegalProgramLine(line)){
                        System.err.println("Line "+programLineNow+": Illegal statement found.");
                        System.out.printf("%-2d %s",programLineNow,line);
                        return;
                    }
                    var m = line.split("=");
                    var matchString = (line.indexOf("=")!=0?m[0]:"").trim();
                    var replaceToString = (line.lastIndexOf("=")!=line.length()-1?m[1]:"").trim();
                    matchStrings.add(matchString);
                    replaceToStrings.add(replaceToString);
                    if (isIllegalProgramLine(matchString,replaceToString)){
                        System.out.println("Line "+programLineNow+": Illegal statement found.");
                        System.err.printf("%-2d %s\n",programLineNow,line);
                        return;
                    }
                }
                programLines=matchStrings.size();
                if (isDetailedDisplay) printProgram();
                System.out.println("Program Initialization done.");
                isToReloadProgram=false;
            }

            //initialize the execute block
            programLineNow = 0;
            isEnd=false;
            executedLines=0;
            //read in the input
            System.out.print("\nType input below: ");
            String inputString = input.nextLine();
            if (inputString.isEmpty()) continue;
            if (inputString.equals("exit")) return;
            if (inputString.equals("reload")) {
                isToReloadProgram=true;
                continue;
            }
            mainString = inputString;
            if (isIllegalInput(mainString)) {
                System.err.println("Line Input: Illegal statement found.");
                isEnd=true;
            }
            System.out.println("\nInput: " + inputString);

            //execute the program
            while (!isEnd) A_equals_B();
            System.out.println("Output: " + mainString);
        }
    }
    private static void printProgram(){
        for (int i = 0; i < matchStrings.size(); i++) System.out.printf("%-2d %s\n", i + 1,getProgramBody(i));
    }
    protected static void A_equals_B(){
        if (programLineNow>=programLines) {
            isEnd=true;
            return;
        }
        if (executedLines>timeLimit){
            System.err.println("Time Limit Exceed("+timeLimit+")");
            isEnd=true;
            return;
        }
        String stringMatchTo = matchStrings.get(programLineNow);
        String stringReplaceTo = replaceToStrings.get(programLineNow);
        String regex = removeKey(stringMatchTo);
        String target = removeKey(stringReplaceTo);
        if (mainString.contains(regex)){
            var key1 = keyOf(stringMatchTo);
            var key2 = keyOf(stringReplaceTo);
            if (key1 == null || key1.equals("once")) {
                if (ignoredLineSet.contains(programLineNow)) {
                    programLineNow++;
                    return;
                }
                if (key1 != null) ignoredLineSet.add(programLineNow);
                if (key2 == null) mainString = mainString.replaceFirst(regex, target);
                else doEqual(key2, regex, target);
            } else if (key1.equals("start") && mainString.indexOf(regex) == 0) {
                if (key2 == null) mainString = mainString.replaceFirst(regex, target);
                else {
                    mainString = mainString.replaceFirst(regex, "");
                    doEqual(key2, regex, target);
                }
            } else if (key1.equals("end") && mainString.lastIndexOf(regex) + regex.length() == mainString.length()) {
                if (key2 == null) mainString = mainString.substring(0, mainString.length() - regex.length()) + target;
                else {
                    mainString = mainString.substring(0, mainString.length() - regex.length());
                    doEqual(key2, regex, target);
                }
            } else {
                programLineNow++;
                return;
            }
            executedLines++;
            if (isDetailedDisplay) System.out.printf("%-2d %-2d %s  %s\n",executedLines,programLineNow,getProgramBody(programLineNow),mainString);
            programLineNow=0;
        }
        else programLineNow++;
    }

    private static void doEqual(String key2, String regex, String target) {
        switch (key2) {
            case "start" -> mainString = target + mainString.replaceFirst(regex, "");
            case "end" -> mainString = mainString.replaceFirst(regex, "") + target;
            case "return" -> {
                isEnd = true;
                mainString = target;
            }
        }
    }

    private static String keyOf(String regex){
        if (!regex.contains("(")) return null;
        return regex.substring(regex.indexOf("(")+1,regex.indexOf(")"));
    }
    private static String removeKey(String regex){
        if (!regex.contains("(")) return regex;
        return regex.substring(regex.indexOf(")")+1).trim();
    }
    private static boolean isIllegalProgramLine(String line){
        return !line.contains("=") || line.indexOf("=") != line.lastIndexOf("=");
    }
    private static boolean isIllegalProgramLine(String matchString,String replaceToString){
        return isIllegalStatement(matchString, "start", "end", "once") ||
                isIllegalStatement(replaceToString, "start", "end", "return");
    }
    private static boolean isIllegalStatement(String statement, String... allowedKeys){
        if (statement.contains("(")){
            int keyStart = statement.indexOf("("),keyEnd = statement.indexOf(")");
            if (keyStart==0 && keyEnd!=-1 && keyStart==statement.lastIndexOf("(") &&
                    keyEnd==statement.lastIndexOf(")")){
                String key = statement.substring(keyStart+1,keyEnd).toLowerCase();
                for (var i:allowedKeys) if (key.equals(i)) return false;
            }
            return true;
        }
        else return statement.contains(")");
    }
    private static String getProgramBody(int line){
        return String.format("%s=%s",matchStrings.get(line),replaceToStrings.get(line));
    }
    private static boolean isIllegalInput(String mainString){
        return !mainString.contains("#") && mainString.contains("=") || mainString.contains("(") || mainString.contains(")");
    }
}