import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static int illegalProgramLine = 0;
    private static boolean isDetailedDisplay = false;
    private static final ArrayList<String> programBody = new ArrayList<>();
    private static String mainString;
    private static boolean isEnd;
    private static int programLines = 0;
    private static int executedLines;
    private static int programLineNow;
    private static final ArrayList<String> matchStrings = new ArrayList<>();
    private static final ArrayList<String> replaceToStrings = new ArrayList<>();
    public static void main(String[] args) {
        Scanner program;
        try {
            program = new Scanner(new File("programBody.txt"));
        } catch (Exception e) {
            program = new Scanner("a=b");
        }
        Scanner input = new Scanner(System.in);
        System.out.print("Do you need detailed information?(y/n): ");
        var tmp = input.nextLine().toLowerCase().charAt(0);
        if (tmp=='y') isDetailedDisplay=true;

        //read in the program body
        while (program.hasNextLine()){
            String line = program.nextLine();
            if (line.contains("#")){
                int ignoreIndex = line.indexOf("#");
                if (ignoreIndex==0) continue;
                else line = line.substring(0,ignoreIndex);
                if (!line.contains("=")) continue;
            }
            programBody.add(line);
        }
        if (isIllegalProgram()){
            System.out.println("Line "+illegalProgramLine+": Illegal statement found.");
            return;
        }
        programLines=programBody.size();
        for (var i:programBody){
            var m = i.split("=");
            matchStrings.add(i.indexOf("=")!=0?m[0]:"");
            replaceToStrings.add(i.lastIndexOf("=")!=i.length()-1?m[1]:"");
        }
        if (isDetailedDisplay) printProgram();
        System.out.println("Program Initialization done.");

        while (true) {
            //initialize the execute block
            programLineNow = 0;
            isEnd=false;
            executedLines=0;
            //read in the input
            System.out.print("\nType input below: ");
            String inputString = input.nextLine();
            if (inputString.isEmpty()) continue;
            if (inputString.equals("exit")) return;
            mainString = inputString;
            if (isIllegalInput(mainString)) {
                System.out.println("Line Input: Illegal statement found.");
                return;
            }
            System.out.println("\nInput: " + inputString);

            //execute the program
            do A_equals_B(); while (!isEnd);
            System.out.println("Output: " + mainString);
        }
    }
    private static void printProgram(){
        for (int i = 0; i < programBody.size(); i++) System.out.printf("%-2d %s\n", i + 1, programBody.get(i));
    }
    protected static void A_equals_B(){
        if (programLineNow>=programLines) {
            isEnd=true;
            return;
        }
        String regex = matchStrings.get(programLineNow);
        String stringReplaceTo = replaceToStrings.get(programLineNow);
        if (mainString.contains(regex)){
            mainString = mainString.replaceFirst(regex,stringReplaceTo);
            executedLines++;
            if (isDetailedDisplay) System.out.printf("%-2d %-2d %s  %s\n",executedLines,programLineNow,programBody.get(programLineNow),mainString);
            programLineNow=0;
        }
        else programLineNow++;
    }
    private static boolean isIllegalProgram(){
        illegalProgramLine=0;
        if (programBody.isEmpty()) return true;
        for (int j = 0; j < programBody.size(); j++) {
            var i = programBody.get(j);
            if (!i.isEmpty() && !i.contains("=") || i.indexOf("=") != i.lastIndexOf("=")) {
                illegalProgramLine = j;
                return true;
            }
        }
        return false;
    }
    private static boolean isIllegalInput(String mainString){
        return !mainString.contains("#") && mainString.contains("=") || mainString.contains("(") || mainString.contains(")");
    }
}