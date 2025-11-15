import org.apache.commons.cli.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Main {
    private static final ArrayList<String> testcaseMap = new ArrayList<>();
    static boolean isEnd;
    private static boolean isDetailedDisplay = false;
    private static String mainString;
    private static int timeLimit = 1000;
    private static boolean isToReload = true;
    private static int programLines;
    private static int executedLines;
    private static int lineIndex;
    private static HashSet<Integer> ignoredLineSet;
    private static ArrayList<String> leftExprs;
    private static ArrayList<String> rightExprs;
    private static String runHistory = "", runHistoryTemp = "";
    private static int testcaseNow = 0;
    private static boolean isUsingFileCases;
    private static boolean isCliInput, isLogOutput;

    static void main(String[] args) {
        String filePath = "", outPath = "";

        if (args.length != 0 && args[0].endsWith(".txt")) filePath = args[0];

        Options options = new Options();
        options.addOption("c", "cli", false, "忽略参数运行(将在命令行请求)");
        options.addOption("d", "detail", false, "需要详细输出");
        options.addOption("fio", "using-file-io", false, "使用文件输入");
        options.addOption("i", "file", true, "指定文件路径");
        options.addOption("o", "output", true, "指定输出路径");
        options.addOption("t", "time", true, "最大执行行数");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            isCliInput = cmd.hasOption("c");
            isDetailedDisplay = cmd.hasOption("d");
            isUsingFileCases = cmd.hasOption("fio");
            isLogOutput = cmd.hasOption("o");
            if (cmd.hasOption("i")) filePath = cmd.getOptionValue("i");
            if (cmd.hasOption("o")) outPath = cmd.getOptionValue("o");
            if (cmd.hasOption("t")) timeLimit = Integer.parseInt(cmd.getOptionValue("t"));
        } catch (ParseException e) {
            System.err.println("解析命令行参数失败: " + e.getMessage());
        }

        if (filePath.isEmpty())
            filePath = IO.readln("Enter input file path below.\n> ");
        if (!filePath.endsWith(".txt") || filePath.endsWith("_io.txt")) {
            Utils.printError("Fatal: Illegal input detected.");
            return;
        }
        if (outPath.isEmpty())
            outPath = filePath.replace(".txt", ".log");

        if (isCliInput) {
            isDetailedDisplay = IO.readln("Need detail infos?(y/n): ").contains("y");

            var input = IO.readln("Time limit: ");
            if (Utils.isNumeric(input)) timeLimit = Integer.parseInt(input);
            IO.println("Using time limit: " + timeLimit);

            isUsingFileCases = IO.readln("Use testcase?(y/n): ").contains("y");
        }
        if (isUsingFileCases) {
            String ioPath = filePath.replace(".txt", "_io.txt");
            var ioFile = Utils.readFile(ioPath);
            testcaseMap.addAll(List.of(ioFile.split("\n")));
        }

        IO.println("Read file: " + filePath);

        executeCodeBlock(filePath);

        if (isLogOutput) {
            Utils.writeFile(outPath, runHistory);
            IO.println("\n\nLog output at:\n> " + outPath);
        }
    }

    /**
     * 接受并处理全部代码块
     */
    static void executeCodeBlock(String filePath) {
        while (true) {
            ignoredLineSet = new HashSet<>();

            if (isToReload) {
                leftExprs = new ArrayList<>();
                rightExprs = new ArrayList<>();
                programLines = 0;

                String codeBlock = Utils.readFile(filePath).replace(" ", "");
                runHistory += "Program Loaded> " + filePath + "\n";
                if (codeBlock.isEmpty()) return;
                ArrayList<String> codeLines = new ArrayList<>(List.of(codeBlock.split("\n")));
                ArrayList<String> lines = removeComment(codeLines);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    lineIndex = i + 1;
                    if (Utils.isIllegalProgramLine(line)) {
                        Utils.printError("Illegal statement found: Not having a single '='.");
                        Utils.printfError(lineIndex, line);
                        return;
                    }
                    int splitIndex = line.indexOf('=');
                    String leftExpr = line.substring(0, splitIndex);
                    String rightExpr = line.substring(splitIndex + 1);
                    leftExprs.add(leftExpr);
                    rightExprs.add(rightExpr);

                    if (Utils.isIllegalProgramLine(leftExpr, rightExpr)) {
                        Utils.printError("Illegal statement found: Unsupported statement.");
                        Utils.printfError(lineIndex, line);
                        return;
                    }
                }
                programLines = leftExprs.size();
                String codeFormatted = getCodeFormatted();
                runHistory += codeFormatted;
                if (isDetailedDisplay) IO.println(codeFormatted);
                IO.println("Program Loaded.");
                isToReload = false;
            }

            //initialize the execute block
            lineIndex = 0;
            isEnd = false;
            executedLines = 0;
            runHistoryTemp = "";

            String inputString, expectedResult = "";
            if (isUsingFileCases) {
                try {
                    inputString = testcaseMap.get(testcaseNow * 2);
                    expectedResult = testcaseMap.get(testcaseNow * 2 + 1);
                    testcaseNow++;
                    runHistory += "\nTestcase " + testcaseNow + "\nInput> " + inputString + "\n";
                } catch (Exception e) {
                    return;
                }
            } else {
                inputString = IO.readln("\nInput> ");
                runHistory += "\nInput> " + inputString + "\n";
            }

            switch (inputString) {
                case "" -> {
                    continue;
                }
                case "exit" -> {
                    return;
                }
                case "reload" -> {
                    isToReload = true;
                    continue;
                }
            }
            mainString = inputString;
            if (Utils.isIllegalInput(mainString)) {
                System.err.println("Line Input: Illegal statement found.");
                isEnd = true;
            }

            //execute the program
            IO.println();
            while (!isEnd) executeCodeLine();
            runHistory += "Output>\t" + mainString + "\n";
            if (isUsingFileCases) runHistory += "OutStd>\t" + expectedResult + "\n";
            runHistory += runHistoryTemp;

            if (isUsingFileCases) IO.println("\nTestcase " + testcaseNow + " >>>");
            IO.println("Input:\t" + inputString + "\nOutput:\t" + mainString);
            if (isUsingFileCases) {
                IO.println("OutStd:\t" + expectedResult);
                if (!mainString.equals(expectedResult)) {
                    runHistory += "Case failed.\n";
                    System.err.println("Fail at case " + testcaseNow);
                }
            }
            if (isDetailedDisplay && !runHistoryTemp.isEmpty()) {
                IO.print("Running log >>>\n" + runHistoryTemp);
            }
        }
    }

    private static void executeCodeLine() {
        if (lineIndex >= programLines) {
            isEnd = true;
            return;
        }
        if (executedLines > timeLimit) {
            IO.println("Time Limit Exceed: " + timeLimit);
            isEnd = true;
            return;
        }
        String leftExpr = leftExprs.get(lineIndex);
        String rightExpr = rightExprs.get(lineIndex);
        String regex = Utils.quote(Utils.trimKey(leftExpr)).trim();
        String target = Utils.quote(Utils.trimKey(rightExpr)).trim();
        String mainStringQuote = Utils.quote(mainString).trim();
        if (regex.contains("*")) System.out.println(Arrays.toString(new String[]{regex, target, mainString}));
        if (mainStringQuote.contains(regex)) {
            String key1 = Utils.getKey(leftExpr), key2 = Utils.getKey(rightExpr);

            if (key1 == null || key1.equals("once")) {
                if (ignoredLineSet.contains(lineIndex)) {
                    lineIndex++;
                    return;
                }
                if (key1 != null) ignoredLineSet.add(lineIndex);
                if (key2 == null) mainString = mainString.replaceFirst(regex, target);
                else mainString = doReplace(regex, target, key1, key2, mainString);
            } else if (key1.equals("start") && mainStringQuote.indexOf(regex) == 0) {
                if (key2 == null) mainString = mainString.replaceFirst(regex, target);
                else {
                    mainString = mainString.replaceFirst(regex, "");
                    mainString = doReplace(regex, target, key1, key2, mainString);
                }
            } else if (key1.equals("end") && mainStringQuote.lastIndexOf(regex) + regex.length() == mainStringQuote.length()) {
                if (key2 == null) mainString = mainString.substring(0, mainString.length() - regex.length()) + target;
                else {
                    mainString = mainString.substring(0, mainString.length() - regex.length());
                    mainString = doReplace(regex, target, key1, key2, mainString);
                }
            } else {
                lineIndex++;
                return;
            }
            mainString = Utils.unquote(mainString).trim();
            executedLines++;
            runHistoryTemp += String.format("%-2d %-2d %s=%s\t%s\n",
                    executedLines, lineIndex + 1,
                    leftExprs.get(lineIndex), rightExprs.get(lineIndex), mainString);
            lineIndex = 0;
        } else lineIndex++;
    }

    static ArrayList<String> removeComment(ArrayList<String> codeLines) {
        for (int i = 0; i < codeLines.size(); i++) {
            var line = codeLines.get(i);
            if (line.isEmpty()) {
                codeLines.remove(i);
                i--;
            }
            if (line.contains("#")) {
                int ignoreIndex = line.indexOf("#");
                if (ignoreIndex != 0) codeLines.set(i, line.substring(0, ignoreIndex));
                else {
                    codeLines.remove(i);
                    i--;
                }
            }
        }
        return codeLines;
    }

    private static String doReplace(String left, String right, String key1, String key2, String mainString) {
        Map<String, Function<String, String>> replaceHandlers = new HashMap<>();

        replaceHandlers.put("", s -> s.replaceFirst(left, right));
        replaceHandlers.put("start", s -> right + s.replaceFirst(left, ""));
        replaceHandlers.put("end", s -> s.replaceFirst(left, "") + right);
        replaceHandlers.put("return", _ -> {
            isEnd = true;
            return right;
        });

        for (Map.Entry<String, Function<String, String>> entry : replaceHandlers.entrySet()) {
            if (key2.equals(entry.getKey())) return entry.getValue().apply(mainString);
        }
        return mainString;
    }

    private static String getCodeFormatted() {
        var ref = new Object() {
            String bash = "";
        };
        IntStream.range(0, leftExprs.size()).forEach(i -> ref.bash += Utils.unquote(String.format("%-2d %s=%s\n", i + 1, leftExprs.get(i), rightExprs.get(i))));
        return ref.bash;
    }

}