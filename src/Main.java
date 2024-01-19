import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static int illegalProgramLine = 0;
    private static final ArrayList<String> programBody = new ArrayList<>();
    private static String mainString;
    private static boolean isEnd=false;
    private static int programLines = 0;
    private static int programLineNow = 0;
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

        //read in the program body
        while (program.hasNextLine()){
            programBody.add(program.nextLine());
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
        printProgram();
        System.out.print("Program Initialization done.\n" +
                "Input your test case below(1 line):");

        //read in the input
        String inputString = input.nextLine();
        mainString= inputString;
        if (isIllegalInput(mainString)){
            System.out.println("Line Input: Illegal statement found.");
            return;
        }
        System.out.println("\nInput: "+ inputString);

        //execute the program
        do {
            A_equals_B();
        } while (!isEnd);

        System.out.println("Output: "+mainString);
    }
    private static void printProgram(){
        for (var i:programBody) System.out.println(i);
    }
    private static void A_equals_B(){
        if (programLineNow>=programLines) {
            isEnd=true;
            return;
        }
        String regex = matchStrings.get(programLineNow);
        String stringReplaceTo = replaceToStrings.get(programLineNow);
        if (mainString.contains(regex)){
            mainString = mainString.replaceAll(regex,stringReplaceTo);
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
