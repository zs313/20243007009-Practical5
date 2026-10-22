package com.example.practicalfive;

/** Dependency-free regression checks. Compile alongside TextProcessor with javac. */
public final class TextProcessorTest {
    public static void main(String[] args) {
        TextProcessor processor = new TextProcessor();
        String[][] valid = {
                {"Beijing", "Beijing"}, {"beijing", "Beijing"}, {"BEIJING", "Beijing"},
                {"  Beijing  ", "Beijing"}, {"Shanghai", "Shanghai"}, {"SHANGHAI", "Shanghai"},
                {"tianjin", "Tianjin"}, {"Tianjin", "Tianjin"},
                {"Chongqing", "Chongqing"}, {"ChOnGqInG", "Chongqing"},
                {"\tshanghai\n", "Shanghai"}
        };
        int checked = 0;
        for (String[] example : valid) {
            if (!example[1].equals(processor.processMunicipality(example[0]))) {
                throw new AssertionError("Incorrect canonical name for: " + example[0]);
            }
            checked++;
        }
        String[] invalid = {null, "", " ", "   ", "\t\r\n", "Guangzhou", "Beijing City",
                "Bei jing", "Shangha", "Tianjin123", "Chongqing/Beijing"};
        for (String input : invalid) {
            if (processor.processMunicipality(input) != null) {
                throw new AssertionError("Accepted unsupported input: " + input);
            }
            checked++;
        }
        if (!TextProcessor.isBlank(null) || !TextProcessor.isBlank(" ")
                || TextProcessor.isBlank("Beijing")) {
            throw new AssertionError("Blank input classification failed");
        }
        System.out.println("PASS: " + checked + " municipality cases and blank-input checks");
    }
}
