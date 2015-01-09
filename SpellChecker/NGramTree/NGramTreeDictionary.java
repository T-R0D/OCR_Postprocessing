package NGramTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Terence
 * on 1/5/2015.
 */
public class NGramTreeDictionary extends Object{
    public static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final Integer DEFAULT_FREQUENCY = 1;
    public static final String endl = System.getProperty("line.separator");

    /**
     * Gives us nodes for the tree. Effectively creates an "n-ary" tree.
     */
    private class WordNode {
        final String mWord;
        Integer mSequenceFrequency;
        List<WordNode> mSubsequentWordNodes;

        public WordNode(String word, Integer sequenceFrequency) {
            mWord = new String(word); // want to make sure we get a copy of the word
            mSequenceFrequency = sequenceFrequency;
            mSubsequentWordNodes = new ArrayList<WordNode>();
        }

        /* This method will get the reference to the word node that follows
         * this one in sequence. As a side-effect, if there is not previous
         * instance of the nextWord, it is added as a 0 frequency node.
         *
         * Not sure if this is currently desirable.
         */
        public WordNode getSubsequentWordNode(String nextWord) {
            // TODO: improve on linear search
            for (WordNode wordNode : mSubsequentWordNodes) {
                if (wordNode.mWord.equals(nextWord)) {
                    return wordNode;
                }
            }

            // word not found, add it with a low frequency
            WordNode newWordNode = new WordNode(nextWord, DEFAULT_FREQUENCY);
            mSubsequentWordNodes.add(newWordNode);
            return newWordNode;
        }

        public List<String> getSubsequentWords() {
            List<String> subsequentWords = new ArrayList<String>();
            for (WordNode node : mSubsequentWordNodes) {
                subsequentWords.add(node.mWord);
            }
            return subsequentWords;
        }
    }

    private WordNode mRoot;

    /**
     * A basic constructor.
     */
    public NGramTreeDictionary() {
        mRoot = new WordNode("", 0);
        // this gives us somewhere to start without branching off of a single word
    }

    /**
     * Given an array of words, traces the tree down adding words and updating frequencies as
     * necessary.
     */
    public void addFrequencyOfSequence(Integer frequency, String... sequence) {
        if (sequence.length > 0) {
            WordNode firstWordNode = mRoot.getSubsequentWordNode(sequence[0]);
            addFrequencyOfSequence(firstWordNode, frequency, Arrays.asList(sequence));
        }
    }

    /**
     * A helper method for adding a sequence of words to the dictionary.
     * If a word is not the last word in the sequence, its frequency is incremented,
     * since, after all, the sub-sequence has appeared one more time than we first expected.
     */
    private WordNode addFrequencyOfSequence(WordNode wordNode, Integer frequency, List<String> sequence) {
        if (sequence.size() == 1) {
            wordNode.mSequenceFrequency += frequency;
        } else {
            wordNode.mSequenceFrequency += 1;

            WordNode nextWordNode = wordNode.getSubsequentWordNode(sequence.get(2));

            addFrequencyOfSequence(nextWordNode, frequency, sequence.subList(1, sequence.size()));
        }

        return wordNode;
    }

    /**
     * A list accepting version of the following function.
     */
    public String findMostLikelyReplacement(List<String> sequence) {
        return findMostLikelyReplacement((String[]) sequence.toArray());
    }

    /**
     * Returns the most likely replacement for the last word in a given sequence. If the
     * given sequence has 0 elements, an empty string ("") is returned. Follows the tree down
     * the "assumed" words in the sequence (all but the last word, these are assumed to be either
     * correct or already corrected), then chooses the most likely replacement word to end the
     * sequence.
     *
     * TODO: fortify this method against sequences of "assumed" words that are not in the dictionary
     */
    public String findMostLikelyReplacement(String... sequence) {

        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = sequence[i].toLowerCase();
        }

        WordNode predecessor = mRoot;

        if (sequence.length > 1) {
            Integer numAssumedWords = sequence.length - 1;
            Integer depth = 0;

            while (depth < numAssumedWords) {
                predecessor = predecessor.getSubsequentWordNode(sequence[depth]);
                depth++;
            }
        }

        String bestCandidate = "";

        if (sequence.length > 0) {
            String wordInQuestion = sequence[sequence.length - 1];

            Map<String, Integer> possibilities = new HashMap<String, Integer>();
            for (WordNode node : predecessor.mSubsequentWordNodes) {
                possibilities.put(node.mWord, node.mSequenceFrequency);
            }

            bestCandidate = findMostLikelyReplacement(wordInQuestion, possibilities);
        }

        return bestCandidate;
    }

    /**
     * Finds the most likely replacement for a word given a map of possibilities as keys and
     * relative frequencies as values. The implementation (helper) of the interface method above.
     */
    public static String findMostLikelyReplacement(
        String wordInQuestion,
        Map<String, Integer> possibilities) {

        String bestCandidate = wordInQuestion;

        if (!possibilities.containsKey(wordInQuestion)) {
            Map<Integer, String> candidates = new HashMap<Integer, String>();

            List<String> oneEdits = generateMutationsOfWord(wordInQuestion, DEFAULT_ALPHABET);
            for (String mutation : oneEdits) {
                candidates.put(
                    possibilities.getOrDefault(mutation, 0),
                    mutation
                );
            }
            candidates.remove(0);

            if (candidates.size() == 0) {
                for (String mutation : oneEdits) {
                    List<String> twoEdits = generateMutationsOfWord(mutation, DEFAULT_ALPHABET);
                    for (String mutation2 : twoEdits) {
                        candidates.put(
                            possibilities.getOrDefault(mutation2, 0),
                            mutation2
                        );
                    }
                }
                candidates.remove(0);
            }

            if (candidates.size() > 0) {
                Integer maxFrequency = Collections.max(candidates.keySet());
                bestCandidate = candidates.getOrDefault(maxFrequency, wordInQuestion);
            }
        }

        return bestCandidate;
    }

    /**
     * Generates a list of mutations that have an edit distance of 1 from the given word, using
     * the given 'alphabet' for replacement mutations.
     */
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

    /**
     * Given the name of a file, will add all n-grams with frequencies to the dictionary.
     */
    public Boolean addNGramsFromNGramFile(String fileName) {
        Boolean success = false;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                String[] sequenceAndFrequency = line.split(":");
                String[] sequence = sequenceAndFrequency[0].trim().split(",");
                Integer frequency = Integer.valueOf(sequenceAndFrequency[1].trim());

                addFrequencyOfSequence(frequency, sequence);

                line = reader.readLine();
            }
            reader.close();
            success = true;
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    /**
     * Creates a string that represents the tree structure of the dictionary.
     */
    public String toString() {
        return toString(mRoot, -1);
    }

    /**
     * A recursive helper method for toString().
     */
    private String toString(WordNode node, Integer depth) {
        StringBuilder treeStringBuilder = new StringBuilder();

        for (int i = 0; i < depth; i++) {
            treeStringBuilder.append("\t");
        }

        treeStringBuilder.append("'")
                         .append(node.mWord)
                         .append("': ")
                         .append(node.mSequenceFrequency)
                         .append(endl);
        for (WordNode subsequentNode : node.mSubsequentWordNodes) {
            treeStringBuilder.append(toString(subsequentNode, depth + 1));
        }

        return treeStringBuilder.toString();
    }
}
