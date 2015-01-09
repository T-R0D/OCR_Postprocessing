package NGramSpellCorrection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Terence
 * on 12/5/2014.
 */
public class NGramSpellChecker {
    public static final Integer MAX_EDIT_DISTANCE = 2;
    public static final Integer DEFAULT_FREQUENCY = 1;
    public static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public final String mAlphabet;
    private final SingleWordSpellCheckDictionary mCorrectWords;

    public NGramSpellChecker() {
        mAlphabet = DEFAULT_ALPHABET;
        mCorrectWords = new SingleWordSpellCheckDictionary();
    }

    public String findMostLikelyReplacementFor(String word) {
        String lowerCasedWord = word.toLowerCase();

        if (mCorrectWords.containsKey(lowerCasedWord)) {
            return lowerCasedWord;
        } else {
            String bestCandidate = lowerCasedWord;
            Map<Integer, String> candidates = new HashMap<Integer, String>();

            List<String> oneEdits = generateMutationsOfWord(lowerCasedWord, mAlphabet);
            for (String mutation : oneEdits) {
                candidates.put(mCorrectWords.getOrDefault(mutation, 0), mutation);
            }
            candidates.remove(0);

            if (candidates.size() == 0) {
                for (String mutation : oneEdits) {
                    for (String mutation2 : generateMutationsOfWord(mutation, mAlphabet)) {
                        candidates.put(mCorrectWords.getOrDefault(mutation2, 0), mutation2);
                    }
                }
                candidates.remove(0);
            }

            if (candidates.size() > 0) {
                Integer maxFrequency = Collections.max(candidates.keySet());
                bestCandidate = candidates.getOrDefault(maxFrequency, lowerCasedWord);
            }

            return bestCandidate;
        }
    }

    public NGramSpellChecker addCorrectWordToDictionary(String correctWord) {
        mCorrectWords.addWord(correctWord, 1);
        return this; // for call chaining
    }

    public NGramSpellChecker addCorrectWordWithFrequency(String word, Integer frequency) {
        mCorrectWords.addWord(word, frequency);
        return this; // for call chaining
    }

    public Boolean addNGramsFromFileToDictionary(String fileName) {
        Boolean result = false;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fileName));

            String line = reader.readLine();
            while (line != null) {

                line = reader.readLine();
            }

            reader.close();
            result = true;
        } catch (Exception e) {
            System.out.println(e.toString());
            result = false;
        }

        return result;
    }

    public Boolean addTextFromFileToDictionary(String fileName) {
        Boolean result = false;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fileName));

            Pattern pattern = Pattern.compile("\\w+");
            String line = reader.readLine();
            while (line != null) {
                Matcher matcher = pattern.matcher(line.toLowerCase());
                while (matcher.find()) {
                    String nextWord = matcher.group();
                    mCorrectWords.addWord(nextWord, 1);
                }
                line = reader.readLine();
            }

            reader.close();
            result = true;
        } catch (Exception e) {
            System.out.println(e.toString());
            result = false;
        }

        return result;
    }

    public static List<String> generateMutationsOfWord(String word, String alphabet) {
        int numMutations = (54 * word.length()) + 25;
        List<String> transformedWords = new ArrayList<String>(numMutations);

        List<String[]> splits = new ArrayList<String[]>(word.length() + 1);
        for (int i = 0; i <= word.length(); i++) {
            String[] split = new String[2];
            split[0] = word.substring(0, i);
            split[1] = word.substring(i);
            splits.add(split);
        }

        // deletions
        for (String[] split : splits) {
            if (split[1].length() >= 1) {
                transformedWords.add(split[0] + split[1].substring(1));
            }
        }

        // insertions
        for (char c : alphabet.toCharArray()) {
            for (String[] split : splits) {
                transformedWords.add(split[0] + c + split[1]);
            }
        }

        // replacements
        for (char c : alphabet.toCharArray()) {
            for (String[] split : splits) {
                if (split[1].length() >= 1) {
                    transformedWords.add(split[0] + c + split[1].substring(1));
                }
            }
        }

        // transpositions
        for (String[] split : splits) {
            if (split[1].length() > 1) {
                transformedWords.add(
                    split[0] +
                        split[1].substring(1, 2) +
                        split[1].substring(0, 1) +
                        split[1].substring(2)
                );
            }
        }

        return transformedWords;
    }

    public SingleWordSpellCheckDictionary getDictionary() {
        return mCorrectWords;
    }
}