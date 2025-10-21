private static boolean isDetailedDisplay = false;
private static String mainString;
private static int timeLimit = 1000;
private static boolean isEnd;
private static boolean isToReloadProgram = true;
private static boolean isTestCase = false;
private static int programLines = 0;
private static int executedLines;
private static int programLineNow;
private static HashSet<Integer> ignoredLineSet;
private static ArrayList<String> testcaseMap;
private static ArrayList<String> matchStrings;
private static ArrayList<String> replaceToStrings;
private static int testcaseNow = 0;

void main() {
    Scanner input = new Scanner(System.in);
    IO.print("Do you need detailed information?(y/n): ");
    var tmp = input.nextLine().toLowerCase();
    if (tmp.contains("y")) isDetailedDisplay = true;
    IO.print("Input time limit(Positive integer): ");
    try {
        timeLimit = Integer.parseInt(input.nextLine());
    } catch (NumberFormatException e) {
        System.err.println("Invalid time limit: reset to 1000");
    }
    IO.print("Use testcases?(y/n): ");
    tmp = input.nextLine().toLowerCase();
    if (tmp.contains("y")) {
        isTestCase = true;
        if (!readTestCase()) {
            System.err.println("Error: testcase not found.");
            return;
        }
    }

    while (true) {
        ignoredLineSet = new HashSet<>();
        if (isToReloadProgram) {
            //read in the program body
            Scanner program;
            try {
                program = new Scanner(new File("code.txt"));
            } catch (Exception e) {
                System.err.println("Error: code not found.");
                return;
            }
            matchStrings = new ArrayList<>();
            replaceToStrings = new ArrayList<>();
            programLineNow = 0;
            programLines = 0;
            while (program.hasNextLine()) {
                String line = program.nextLine();
                if (line.isEmpty()) continue;
                if (line.contains("#")) {
                    int ignoreIndex = line.indexOf("#");
                    if (ignoreIndex == 0) continue;
                    else line = line.substring(0, ignoreIndex);
                }
                programLineNow++;
                if (isIllegalProgramLine(line)) {
                    System.err.println("Line " + programLineNow + ": Illegal statement found.");
                    System.out.printf("%-2d %s", programLineNow, line);
                    return;
                }
                var m = line.split("=");
                var matchString = (line.indexOf("=") != 0 ? m[0] : "").trim();
                var replaceToString = (line.lastIndexOf("=") != line.length() - 1 ? m[1] : "").trim();
                matchStrings.add(matchString);
                replaceToStrings.add(replaceToString);
                if (isIllegalProgramLine(matchString, replaceToString)) {
                    IO.println("Line " + programLineNow + ": Illegal statement found.");
                    System.err.printf("%-2d %s\n", programLineNow, line);
                    return;
                }
            }
            program.close();
            programLines = matchStrings.size();
            if (isDetailedDisplay) printProgram();
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
        if (isTestCase) {
            try {
                inputString = testcaseMap.get(testcaseNow * 2);
                expectedResult = testcaseMap.get(testcaseNow * 2 + 1);
                testcaseNow++;
            } catch (Exception e) {
                return;
            }
        } else {
            IO.print("\nType input below: ");
            inputString = input.nextLine();
            expectedResult = "";
        }
        switch (inputString) {
            case "" -> {
                continue;
            }
            case "exit" -> {
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
        if (isTestCase) IO.println("Testcase " + testcaseNow);
        IO.println("\nInput: " + inputString);
        if (isTestCase) IO.println("Expected: " + expectedResult);
        IO.println("Output: " + mainString);
        if (isTestCase) {
            boolean isValidOutput = mainString.equals(expectedResult);
            if (isValidOutput) IO.println("Pass.");
            else {
                System.err.println("Wrong Answer at case " + testcaseNow);
                return;
            }
        }
    }
}

private static void printProgram() {
    for (int i = 0; i < matchStrings.size(); i++) System.out.printf("%-2d %s\n", i + 1, getProgramBody(i));
}

private static void A_equals_B() {
    if (programLineNow >= programLines) {
        isEnd = true;
        return;
    }
    if (executedLines > timeLimit) {
        System.err.println("Time Limit Exceed(" + timeLimit + ")");
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
            System.out.printf("%-2d %-2d %s  %s\n", executedLines, programLineNow, getProgramBody(programLineNow), mainString);
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
        Scanner sc = new Scanner(new File("testcase.txt"));
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
        int keyStart = statement.indexOf("("), keyEnd = statement.indexOf(")");
        if (keyStart == 0 && keyEnd != -1 && keyStart == statement.lastIndexOf("(") &&
                keyEnd == statement.lastIndexOf(")")) {
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
    return !mainString.contains("#") && mainString.contains("=") || mainString.contains("(") || mainString.contains(")");
}