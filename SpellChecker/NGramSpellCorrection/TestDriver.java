package NGramSpellCorrection;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.gauner.jSpellCorrect.spi.ToySpellingCorrector;

/**
 * Created by Terence
 * on 12/5/2014.
 */
public class TestDriver {
    public static final String TEST_TEXT =
        "C:\\Users\\Terence\\Desktop\\AI_Project\\Google N-gram Cleaner\\SpellingCorrection\\src\\NGramSpellCorrection\\test_text.txt";
    public static final String BIG_TXT = "C:\\Users\\Terence\\Desktop\\big.txt";
    public static final String TEST_CASES_1 = "C:\\Users\\Terence\\Desktop\\test_cases_1.txt";
    public static final String TEST_CASES_2 = "C:\\Users\\Terence\\Desktop\\test_cases_2.txt";


    public static void main(String[] args) {
//        TestTheOtherGuysThing(BIG_TXT, TEST_CASES_1);

        TestSpellCheckerOnWord(BIG_TXT, "cak", "cake");
        TestSpellCheckerOnWord(BIG_TXT, "Cads", "lads");
        TestSpellCheckerOnWord(BIG_TXT, "Dok", "ok");
        TestSpellCheckerOnWord(BIG_TXT, "Speling", "Spelling");

//        TestSpellCheckerOnFile(BIG_TXT, TEST_CASES_1, 1);
    }

    public static void TestTheOtherGuysThing(String dictionaryFileName, String testCaseFileName) {
        ToySpellingCorrector sc = new ToySpellingCorrector();
        // train some data from a text file
        sc.trainFile(dictionaryFileName);
        // train a single word
//        sc.trainSingle("some word");
        // get the best suggestion
        System.out.println(sc.correct("Cads"));
        System.out.println(sc.correct("Dok"));
        System.out.println(sc.correct("Speling"));

        MultiMap<String, String> testCases = ReadTestCasesFromFile(testCaseFileName);

        Integer correctMatches = 0;
        Integer incorrectMatches = 0;
        Double total = 0.0;
        for (String testWord : testCases.keySet()) {
            List<String> misspellings = (List<String>) testCases.get(testWord);
            for (String misspelling : misspellings) {
                String correction = sc.correct(misspelling);
                if (correction.equals(testWord)) {
                    correctMatches++;
                } else {
                    System.out.println(CreateCorrectionResultString(misspelling, correction, testWord));
                    incorrectMatches++;
                }
                total += 1.0;
            }
        }

        System.out.println("% correctly corrected: " + ((double) correctMatches / total));
        System.out.println("% incorrectly corrected: " + ((double) incorrectMatches / total));
    }

    public static void TestSpellCheckerOnWord(
        String dictionaryFileName,
        String word,
        String actual) {

        NGramSpellChecker testChecker = new NGramSpellChecker();
        testChecker.addTextFromFileToDictionary(dictionaryFileName);

        String correction = testChecker.findMostLikelyReplacementFor(word);
        System.out.println(CreateCorrectionResultString(word, correction, actual));
    }

    public static void TestSpellCheckerOnFile(
        String dictionaryFileName,
        String testCaseFileName,
        Integer correctWordBias) {

        NGramSpellChecker testChecker = new NGramSpellChecker();
        testChecker.addTextFromFileToDictionary(dictionaryFileName);

        MultiMap<String, String> testCases = ReadTestCasesFromFile(testCaseFileName);

        if (correctWordBias > 0) {
            for (String word : testCases.keySet()) {
                testChecker.addCorrectWordWithFrequency(word, correctWordBias);
            }
        }
//        System.out.println(testChecker.dictionaryToString());

        Integer correctMatches = 0;
        Integer incorrectMatches = 0;
        Double total = 0.0;
        for (String testWord : testCases.keySet()) {
            List<String> misspellings = (List<String>) testCases.get(testWord);
            for (String misspelling : misspellings) {
                String correction = testChecker.findMostLikelyReplacementFor(misspelling);
                if (correction.equals(testWord)) {
                    correctMatches++;
                } else {
                    System.out.println(CreateCorrectionResultString(misspelling, correction, testWord));
                    incorrectMatches++;
                }
                total += 1.0;
            }
        }

        System.out.println("% correctly corrected: " + ((double) correctMatches / total));
        System.out.println("num correct: " + correctMatches);
        System.out.println("% incorrectly corrected: " + ((double) incorrectMatches / total));
        System.out.println("num incorrect: " + incorrectMatches);
//        System.out.println(testChecker.getDictionary().dictionaryToString());
    }

    public static MultiMap<String, String> ReadTestCasesFromFile(String testCaseFileName) {
        MultiMap<String, String> testCases = new MultiValueMap<String, String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(testCaseFileName));
            String line = reader.readLine();
            while (line != null) {
                String[] testCaseEntry = line.split(":");
                String correctSpelling = testCaseEntry[0];
                String[] misspellings = testCaseEntry[1].split(",");
                for (String misspelling : misspellings) {
                    testCases.put(correctSpelling, misspelling);
                }

                line = reader.readLine();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return testCases;
    }

    public static String CreateCorrectionResultString(String input, String output, String actual) {
        return String.format(
            "RESULT: %s -> %s | ACTUAL: %s - %s",
            input,
            output,
            actual,
            output.toLowerCase().equals(actual.toLowerCase()) ? "CORRECT!" : "WRONG!"
        );
    }
}
