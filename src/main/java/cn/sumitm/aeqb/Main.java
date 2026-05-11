package cn.sumitm.aeqb;

import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A=B esolang interpreter — Java 25 + picocli.
 *
 * <p>Usage examples:
 * <pre>{@code
 *   java -jar AequalsB.jar -i code1.txt --fio -d
 *   java -jar AequalsB.jar -i code2.txt --fio -d -t 200
 *   java -jar AequalsB.jar -c
 * }</pre>
 */
@Command(name = "AeqB", mixinStandardHelpOptions = true, version = "2.0.0",
         description = "A=B — an esolang interpreter with only one instruction: A=B.")
public class Main implements Callable<Integer> {

    @Option(names = {"-c", "--cli"},
            description = "Ignore args, prompt interactively in terminal")
    boolean cli;

    @Option(names = {"-d", "--detail"},
            description = "Detailed output (show per-step running log)")
    boolean detail;

    @Option(names = {"--fio", "--using-file-io"},
            description = "Use file-based test cases (<program>_io.txt)")
    boolean usingFileIo;

    @Option(names = {"-i", "--file"},
            description = "Input program file path (.txt)")
    String file;

    @Option(names = {"-o", "--output"},
            description = "Output log file path (default: <program>.log)")
    String output;

    @Option(names = {"-t", "--time"},
            description = "Maximum execution steps (default: 1000)")
    int time = 1000;

    // ---- runtime state ----
    private String filePath;
    private boolean isInteractive;
    private List<String> testcaseMap = List.of();
    private final StringBuilder runHistory = new StringBuilder();

    @Override
    public Integer call() {
        filePath = file != null ? file : "";
        String outPath = output != null ? output : "";
        isInteractive = cli;   // only -c enters interactive mode

        if (filePath.isEmpty() && cli) {
            filePath = IO.readln("Enter input file path below.\n> ");
        }

        if (filePath.isEmpty() || !filePath.endsWith(".txt") || filePath.endsWith("_io.txt")) {
            Utils.printError("Fatal: Illegal input detected.");
            return 1;
        }

        if (outPath.isEmpty()) {
            outPath = filePath.replace(".txt", ".log");
        }

        // ---- CLI interactive mode ----
        if (cli) {
            detail = IO.readln("Need detail infos?(y/n): ").contains("y");
            var t = IO.readln("Time limit: ");
            if (Utils.isNumeric(t)) time = Integer.parseInt(t);
            IO.println("Using time limit: " + time);
            usingFileIo = IO.readln("Use testcase?(y/n): ").contains("y");
        }

        // ---- Load test cases ----
        if (usingFileIo) {
            String ioPath = filePath.replace(".txt", "_io.txt");
            String content = Utils.readFile(ioPath);
            if (!content.isEmpty()) {
                testcaseMap = content.lines().toList();
            }
        }

        IO.println("Read file: " + filePath);
        executeCodeBlock();

        // ---- Write log ----
        if (!outPath.isEmpty()) {
            Utils.writeFile(outPath, runHistory.toString());
            IO.println("\n\nLog output at:\n> " + outPath);
        }

        return 0;
    }

    private void executeCodeBlock() {
        boolean isToReload = true;
        List<Rule> rules = List.of();
        int testcaseNow = 0;
        boolean prevInputWasEmpty = false;

        while (true) {
            if (isToReload) {
                String codeBlock = Utils.readFile(filePath);
                runHistory.append("Program Loaded> ").append(filePath).append('\n');
                if (codeBlock.isBlank()) return;

                try {
                    rules = Parser.parse(codeBlock);
                } catch (Parser.ParseException e) {
                    Utils.printError(e.getMessage());
                    return;
                }

                String formatted = Parser.formatRules(codeBlock, rules);
                runHistory.append(formatted);
                if (detail) IO.print(formatted);
                IO.println("Program Loaded.");
                isToReload = false;

                // Non-interactive without test cases: exit immediately after loading
                if (!isInteractive && !usingFileIo) return;
            }

            // ---- Get input ----
            String inputString, expectedResult;
            if (usingFileIo) {
                int idx = testcaseNow * 2;
                if (idx + 1 >= testcaseMap.size()) return;
                inputString = testcaseMap.get(idx);
                expectedResult = testcaseMap.get(idx + 1);
                testcaseNow++;
                runHistory.append("\nTestcase ").append(testcaseNow)
                          .append("\nInput> ").append(inputString).append('\n');
            } else {
                String prompt = prevInputWasEmpty ? "\n(Quit) Input> " : "\nInput> ";
                inputString = IO.readln(prompt);
                expectedResult = "";
                runHistory.append("\nInput> ").append(inputString).append('\n');
            }

            // ---- Handle special commands ----
            switch (inputString) {
                case "" -> {
                    if (isInteractive) {
                        if (prevInputWasEmpty) return; // second empty → quit
                        prevInputWasEmpty = true;
                    }
                    continue;
                }
                case "exit" -> { return; }
                case "reload" -> { isToReload = true; prevInputWasEmpty = false; continue; }
            }
            prevInputWasEmpty = false;

            String mainString = inputString;
            boolean timedOut;
            List<StepLog> steps = List.of();

            if (Utils.isIllegalInput(mainString)) {
                System.err.println("Line Input: Illegal statement found.");
            } else {
                var engine = new Engine(rules, time);
                var result = engine.execute(mainString);
                mainString = result.output();
                timedOut = result.timedOut();
                steps = result.log();
                if (timedOut) IO.println("Time Limit Exceed: " + time);
            }

            runHistory.append("Output>\t").append(mainString).append('\n');
            if (usingFileIo) runHistory.append("OutStd>\t").append(expectedResult).append('\n');
            runHistory.append(formatLog(steps));

            // ---- Display results ----
            IO.println("");
            if (usingFileIo) IO.println("\nTestcase " + testcaseNow + " >>>");
            IO.println("Input:\t" + inputString + "\nOutput:\t" + mainString);
            if (usingFileIo) {
                IO.println("OutStd:\t" + expectedResult);
                if (!mainString.equals(expectedResult)) {
                    runHistory.append("Case failed.\n");
                    System.err.println("Fail at case " + testcaseNow);
                }
            }
            if (detail && !steps.isEmpty()) {
                IO.print("Running log >>>\n" + formatLog(steps));
            }
        }
    }

    private static String formatLog(List<StepLog> steps) {
        var sb = new StringBuilder();
        for (var s : steps) sb.append(s.format());
        return sb.toString();
    }

    // ---- Entry point ----
    static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
