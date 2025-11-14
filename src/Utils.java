import java.io.*;
import java.util.Stack;

public class Utils {
    static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) file = new File(filePath);
            } catch (IOException _) {
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
        }
        return content.toString();
    }

    static void writeFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException _) {
        }
    }

    static int getEndBracket(String expr, int start) {
        Stack<Integer> stack = new Stack<>();
        for (int i = start; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') {
                stack.push(i);
            } else if (expr.charAt(i) == ')') {
                if (stack.isEmpty()) return -1; // Mismatched closing bracket
                stack.pop();
                if (stack.isEmpty()) return i; // Found the matching closing bracket
            }
        }
        return -1; // No matching closing bracket found
    }

    static String getKey(String regex) {
        if (!regex.contains("(")) return null;
        return regex.substring(regex.indexOf("(") + 1, regex.indexOf(")"));
    }

    static String trimKey(String regex) {
        if (!regex.contains("(")) return regex;
        return regex.substring(regex.indexOf(")") + 1).trim();
    }

    static boolean isIllegalProgramLine(String line) {
        return !line.contains("=") || line.indexOf("=") != line.lastIndexOf("=");
    }

    static boolean isIllegalProgramLine(String matchString, String replaceToString) {
        return isIllegalStatement(matchString, "start", "end", "once") ||
                isIllegalStatement(replaceToString, "start", "end", "return");
    }

    private static boolean isIllegalStatement(String statement, String... allowedKeys) {
        if (statement.contains("(")) {
            int keyStart = statement.indexOf("(");
            int keyEnd = getEndBracket(statement, keyStart);

            if (keyStart == 0 && keyEnd != -1) {
                String key = statement.substring(keyStart + 1, keyEnd).toLowerCase();
                for (var i : allowedKeys) if (key.equals(i)) return false;
            }
            return true;
        } else return statement.contains(")");
    }

    static boolean isIllegalInput(String mainString) {
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
