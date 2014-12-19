import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by Terence
 * on 10/23/2014.
 */

public class GoogleNGramCleaner {

    public static class ProgramParameters {
        public String language;
        public Integer nGramSize;
        public String dataSetVersion;
        public String workingDirectory;
        public Integer beginYear;
        public Integer endYear;

        public ProgramParameters() {
        }
    }

    static final String BEGIN_YEAR_ARG = "begin";
    static final String END_YEAR_ARG = "end";
    static final String LANGUAGE_ARG = "language";
    static final String DATA_SET_ARG = "set";
    static final String WORKING_DIR_ARG = "wd";
    static final String N_SIZE_ARG = "n";

    static final String GOOGLE_NGRAM_VIEWER_BASE =
            "http://storage.googleapis.com/books/ngrams/books/googlebooks";
    static final String DATA_FROM_2012 = "20120701";
    static final String DATA_FROM_2009 = "20090715";
    static final String FRENCH = "fre";
    // -fre-all-3gram-20120701-qz
    //  lang    n     which    letters


    public static void main(String[] args) {
        try {
            System.out.println("Parsing aruments...");
            ProgramParameters programParameters = parseArguments(args);
            List<String> nGrams;

            if (programParameters.nGramSize == 1) {
                nGrams = createOneGramList();
                for (String oneGram : nGrams) {
                    System.out.println("Obtaining 1-grams for " + oneGram + "...");
                    createFilteredNGramFile(oneGram, programParameters);
                }
            } else {
                nGrams = createNGramList();
                for (String nGram : nGrams) {
                    System.out.println("Obtaining n-grams for " + nGram + "...");
                    createFilteredNGramFile(nGram, programParameters);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }


    public static ProgramParameters parseArguments(String[] pArguments) {
        ProgramParameters programParameters = new GoogleNGramCleaner.ProgramParameters();

        for (String argument : pArguments) {
            String[] flagAndValue = argument.split("=");
            String flag = flagAndValue[0];

            if (flag.equals(BEGIN_YEAR_ARG)) {
                programParameters.beginYear = Integer.valueOf(flagAndValue[1]);
            } else if (flag.equals(END_YEAR_ARG)) {
                programParameters.endYear = Integer.valueOf(flagAndValue[1]);
            } else if (flag.equals(LANGUAGE_ARG)) {
                String languageValue = flagAndValue[1];
                if (languageValue.equals("french")) {
                    programParameters.language = FRENCH;
                } else {
                    programParameters.language = FRENCH;
                }
            } else if (flag.equals(DATA_SET_ARG)) {
                switch (Integer.valueOf(flagAndValue[1])) {
                    case 2012:
                        programParameters.dataSetVersion = DATA_FROM_2012;
                        break;
                    case 2009:
                        programParameters.dataSetVersion = DATA_FROM_2009;
                        break;
                    default:
                        programParameters.dataSetVersion = DATA_FROM_2012;
                        break;
                }
            } else if (flag.equals(WORKING_DIR_ARG)) {
                programParameters.workingDirectory = flagAndValue[1];
            } else if (flag.equals(N_SIZE_ARG)) {
                programParameters.nGramSize = Integer.parseInt(flagAndValue[1]);
            }
        }

        return programParameters;
    }

    public static URL createGoogleNGramDataURL(ProgramParameters pParameters, String pFirstLetters) {
        URL nGramDataURL = null;
        // Ex: http://storage.googleapis.com/books/ngrams/books/googlebooks
        //                                                                 - fre -all- 2 gram- 20120701 - dr .gz
        try {
            nGramDataURL = new URL(
                    String.format(
                            "%s-%s-all-%dgram-%s-%s.gz",
                            GOOGLE_NGRAM_VIEWER_BASE,
                            pParameters.language,
                            pParameters.nGramSize,
                            pParameters.dataSetVersion,
                            pFirstLetters
                    )
            );
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return nGramDataURL;
    }

    public static String createFilteredNGramFileName(String pFirstLetters, ProgramParameters pParameters) {
        return String.format(
                "%s/%s-%dgrams-%d-%d.txt",
                pParameters.workingDirectory,
                pFirstLetters,
                pParameters.nGramSize,
                pParameters.beginYear,
                pParameters.endYear
        );
    }


    public static void createFilteredNGramFile(String pFirstLetters, ProgramParameters pParameters) {
        try {
            String fileName = createFilteredNGramFileName(pFirstLetters, pParameters);
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName))));

            URL nGramDataURL = createGoogleNGramDataURL(pParameters, pFirstLetters);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(nGramDataURL.openStream())));

            String line = inputReader.readLine();
            while (line != null) {
                GoogleNGram googleNGram = new GoogleNGram(line, pParameters.nGramSize);
                if (pParameters.beginYear <= googleNGram.getYear() && googleNGram.getYear() <= pParameters.endYear) {
                    writer.write(googleNGram.toString());
                    writer.newLine();
                }

                line = inputReader.readLine();
            }

            inputReader.close();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    static List<String> createOneGramList() {
        List<String> nGrams = new ArrayList<String>(50);

        for (int i = 0; i < 10; i++) {
            nGrams.add(String.valueOf(i));
        }
        for (char firstLetter = 'a'; firstLetter <= 'z'; firstLetter++) {
            nGrams.add(String.valueOf(firstLetter));

        }
        nGrams.add("other");
        nGrams.add("pos");
        nGrams.add("punctuation");

        return nGrams;
    }

    static List<String> createNGramList() {
        List<String> nGrams = new ArrayList<String>(800);

        for (int i = 0; i < 10; i++) {
            nGrams.add(String.valueOf(i));
        }
        for (char firstLetter = 'a'; firstLetter <= 'z'; firstLetter++) {
            nGrams.add(firstLetter + "_");

            for (char secondLetter = 'a'; secondLetter <= 'z'; secondLetter++) {
                nGrams.add(String.valueOf(firstLetter) + String.valueOf(secondLetter));
            }
        }
        nGrams.add("_ADJ_");
        nGrams.add("_ADP_");
        nGrams.add("_ADV_");
        nGrams.add("_CONJ_");
        nGrams.add("_DET_");
        nGrams.add("_NOUN_");
        nGrams.add("_PRON_");
        nGrams.add("_PRT_");
        nGrams.add("_VERB_");

        return nGrams;
    }

    static void createNGramEnumFile(int pNGramSize) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("NGramType.java"))));

            writer.write("public enum NGramTypes {" + System.getProperty("line.separator"));

            List<String> nGrams;
            if (pNGramSize == 1) {
                nGrams = createOneGramList();
            } else {
                nGrams = createNGramList();
            }
            for (int i = 0; i < (nGrams.size() - 1); i++) {
                writeNGramName(nGrams.get(i), ",", writer);
            }
            writeNGramName(nGrams.get(nGrams.size() - 1), ";" + System.getProperty("line.separator"), writer);

            writeNGramEnumToStringFunction(writer);

            writer.write("}" + System.getProperty("line.separator"));

            writer.close();
        } catch (Exception e) {
            System.out.println("exception: " + e.toString());
        }
    }

    public static void writeNGramName(String pName, String pDelimeter, Writer pWriter) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("X"); // this is here so that the enum names aren't just a number or single letter
        stringBuilder.append(pName);
        stringBuilder.append(pDelimeter);
        stringBuilder.append(System.getProperty("line.separator"));

        try {
            pWriter.write(stringBuilder.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void writeNGramEnumToStringFunction(Writer pWriter) {
        try {
            pWriter.write(
                    "   @Override" + System.getProperty("line.separator") +
                            "   public String toString() {" + System.getProperty("line.separator") +
                            "   return this.name().substring(1);" + System.getProperty("line.separator") +
                            "   }" + System.getProperty("line.separator")
            );

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
