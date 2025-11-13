import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
    private static boolean isDetailedDisplay = false;
    private static String mainString;
    private static int timeLimit = 1000;
    private static boolean isEnd;
    private static boolean isToReloadProgram = true;
    private static int programLines = 0;
    private static int executedLines;
    private static int programLineNow;
    private static HashSet<Integer> ignoredLineSet;
    private static ArrayList<String> testcaseMap;
    private static ArrayList<String> matchStrings;
    private static ArrayList<String> replaceToStrings;
    private static int testcaseNow = 0;
    private static boolean isUsingFileIO;
    private static String outPath = "";
    private static String ioPath = "sample_cases/code1_io.txt";
    private static boolean isCliInput;

    static void main(String[] args) {
        String filePath = "sample_cases/code1.txt";

//        if (args.length != 0) filePath = args[0];

        Options options = new Options();
        options.addOption("c", "cli", false, "无参数运行(将在命令行请求)");
        options.addOption("d", "detail", false, "需要详细输出");
        options.addOption("io", "using-file-io", false, "使用文件输入");
        options.addOption("i", "file", true, "指定文件路径");
        options.addOption("o", "output", true, "指定输出路径");
        options.addOption("t", "time", true, "最大执行行数");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            isCliInput = cmd.hasOption("c");
            isDetailedDisplay = cmd.hasOption("d");
            isUsingFileIO = cmd.hasOption("io");
            if (cmd.hasOption("i")) filePath = cmd.getOptionValue("i");
            if (cmd.hasOption("o")) outPath = cmd.getOptionValue("o");
            if (cmd.hasOption("t")) timeLimit = Integer.parseInt(cmd.getOptionValue("t"));
        } catch (ParseException e) {
            System.err.println("解析命令行参数失败: " + e.getMessage());
        }

        if (filePath.isEmpty())
            filePath = IO.readln("Enter input file path below.\n> ");
        if (filePath.isEmpty()) {
            IO.println("\u001B[31mNo input file is detected.\u001B[0m");
            return;
        }

        if (isCliInput) {
            isDetailedDisplay = IO.readln("Need detail infos?(y/n): ").contains("y");

            var input = IO.readln("Time limit: ");
            if (isNumeric(input)) timeLimit = Integer.parseInt(input);
            IO.println("Using time limit: " + timeLimit);

            isUsingFileIO = IO.readln("Use testcase?(y/n): ").contains("y");
            if (isUsingFileIO && !readTestCase()) {
                System.err.println("Error: testcase not found.");
                return;
            }
        }

        IO.println("Read file: " + filePath);

        convertCodeBlock(filePath);
    }

    /**
     * 接受并处理全部代码块
     */
    static void convertCodeBlock(String filePath) {
        while (true) {
            ignoredLineSet = new HashSet<>();

            //软重载
            if (isToReloadProgram) {
                String codeBlock = Utils.readFile(filePath);
                matchStrings = new ArrayList<>();
                replaceToStrings = new ArrayList<>();
                programLineNow = 0;
                programLines = 0;
                for (String line : codeBlock.split("\n")) {
                    if (line.isEmpty()) continue;
                    if (line.contains("#")) {
                        int ignoreIndex = line.indexOf("#");
                        if (ignoreIndex == 0) continue;
                        else line = line.substring(0, ignoreIndex);
                    }
                    programLineNow++;
                    if (isIllegalProgramLine(line)) {
                        System.err.println("Illegal statement found at line " + programLineNow);
                        System.out.printf("%-2d %s", programLineNow, line);
                        return;
                    }
                    String[] m = line.split("=", 2);
                    int splitIndex = line.indexOf('=');
                    var matchString = (splitIndex != 0 ? m[0] : "").trim();
                    var replaceToString = (splitIndex != line.length() - 1 ? m[1] : "").trim();
                    matchStrings.add(matchString);
                    replaceToStrings.add(replaceToString);

                    if (isIllegalProgramLine(matchString, replaceToString)) {
                        IO.println("Line " + programLineNow + ": Illegal statement found.");
                        System.err.printf("%-2d %s\n", programLineNow, line);
                        return;
                    }
                }
                programLines = matchStrings.size();
                if (isDetailedDisplay) printProgramDetail();
                IO.println("Program Initialization done.");
                isToReloadProgram = false;
            }

            //initialize the execute block
            programLineNow = 0;
            isEnd = false;
            executedLines = 0;
            //read in the input
            String inputString;
            String expectedResult;
            if (isUsingFileIO) {
                try {
                    inputString = testcaseMap.get(testcaseNow * 2);
                    expectedResult = testcaseMap.get(testcaseNow * 2 + 1);
                    testcaseNow++;
                } catch (Exception e) {
                    return;
                }
            } else {
                inputString = IO.readln("\nType input below: ");
                expectedResult = "";
            }
            switch (inputString) {
                case "" -> {
                    continue;
                }
                case "exit" -> {
                    if (!outPath.isEmpty()) {
                        Utils.writeFile(outPath, ""); //todo: write the program & io flow to file.
                        IO.println("Output execute log at\n> " + outPath);
                    }
                    return;
                }
                case "reload" -> {
                    isToReloadProgram = true;
                    continue;
                }
            }
            mainString = inputString;
            if (isIllegalInput(mainString)) {
                System.err.println("Line Input: Illegal statement found.");
                isEnd = true;
            }

            //execute the program
            IO.println();
            while (!isEnd) A_equals_B();
            if (isUsingFileIO) IO.println("Testcase " + testcaseNow);
            IO.println("\nInput: " + inputString);
            if (isUsingFileIO) IO.println("Expected: " + expectedResult);
            IO.println("Output: " + mainString);
            if (isUsingFileIO) {
                boolean isValidOutput = mainString.equals(expectedResult);
                if (isValidOutput) IO.println("Pass.");
                else {
                    System.err.println("Wrong Answer at case " + testcaseNow);
                    return;
                }
            }
        }
    }

    private static void printProgramDetail() {
        for (int i = 0; i < matchStrings.size(); i++) System.out.printf("%-2d %s\n", i + 1, getProgramBody(i));
    }

    private static void A_equals_B() {
        if (programLineNow >= programLines) {
            isEnd = true;
            return;
        }
        if (executedLines > timeLimit) {
            IO.println("Time Limit Exceed: " + timeLimit);
            isEnd = true;
            return;
        }
        String stringMatchTo = matchStrings.get(programLineNow);
        String stringReplaceTo = replaceToStrings.get(programLineNow);
        String regex = removeKey(stringMatchTo);
        String target = removeKey(stringReplaceTo);
        if (mainString.contains(regex)) {
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
            if (isDetailedDisplay)
                System.out.printf("%-2d %-2d %s  %s\n", executedLines, programLineNow, getProgramBody(programLineNow),
                        mainString);
            programLineNow = 0;
        } else programLineNow++;
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

    private static String keyOf(String regex) {
        if (!regex.contains("(")) return null;
        return regex.substring(regex.indexOf("(") + 1, regex.indexOf(")"));
    }

    private static String removeKey(String regex) {
        if (!regex.contains("(")) return regex;
        return regex.substring(regex.indexOf(")") + 1).trim();
    }

    private static boolean readTestCase() {
        testcaseMap = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(ioPath));
            while (sc.hasNextLine()) {
                testcaseMap.add(sc.nextLine());
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    private static boolean isIllegalProgramLine(String line) {
        return !line.contains("=") || line.indexOf("=") != line.lastIndexOf("=");
    }

    private static boolean isIllegalProgramLine(String matchString, String replaceToString) {
        return isIllegalStatement(matchString, "start", "end", "once") ||
                isIllegalStatement(replaceToString, "start", "end", "return");
    }

    private static boolean isIllegalStatement(String statement, String... allowedKeys) {
        if (statement.contains("(")) {
            int keyStart = statement.indexOf("(");
            int keyEnd = Utils.getEndBracket(statement, keyStart);

            if (keyStart == 0 && keyEnd != -1) {
                String key = statement.substring(keyStart + 1, keyEnd).toLowerCase();
                for (var i : allowedKeys) if (key.equals(i)) return false;
            }
            return true;
        } else return statement.contains(")");
    }

    private static String getProgramBody(int line) {
        return String.format("%s=%s", matchStrings.get(line), replaceToStrings.get(line));
    }

    private static boolean isIllegalInput(String mainString) {
        return !mainString.contains("#") && mainString.contains("=") || mainString.contains("(") || mainString.contains(
                ")");
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}