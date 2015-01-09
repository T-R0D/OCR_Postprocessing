package NGramSpellCorrection;

import java.util.*;

/**
 * Created by Terence
 * on 12/5/2014.
 */
public class SingleWordSpellCheckDictionary extends HashMap<String, Integer> {

    public SingleWordSpellCheckDictionary() {
        super();
    }

    public SingleWordSpellCheckDictionary addWord(String newWord, Integer frequency) {
        Integer previousFrequency = this.getOrDefault(newWord, 1);
        this.put(newWord, previousFrequency + frequency);

        return this; // for call chaining
    }

    public String dictionaryToString() {
        StringBuilder dictionaryString = new StringBuilder();
        String newline = System.getProperty("line.separator");

        ArrayList<String> sortedKeys = new ArrayList<String>(this.keySet());
        Collections.sort(sortedKeys);

        for (String word : sortedKeys) {
            dictionaryString.append("'")
                            .append(word)
                            .append("'")
                            .append(": ")
                            .append(this.get(word))
                            .append(newline);
        }

        return dictionaryString.toString();
    }
}
