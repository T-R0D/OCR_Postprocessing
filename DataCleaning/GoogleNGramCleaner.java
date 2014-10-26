import java.io.*;
import java.net.URL;
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

            if (programParameters.nGramSize == 1) {
                System.out.println("Obtaining 1-grams...");
                for (OneGramType oneGram : OneGramType.values()) {
                    createFilteredNGramFile(oneGram.toString(), programParameters);
                }
            } else {
                System.out.println("Obtaining n-grams...");
                for (NGramType nGram : NGramType.values()) {
                    createFilteredNGramFile(nGram.toString(), programParameters);
                    break;
                }
            }
        } catch(Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }


    public static ProgramParameters parseArguments(String[] pArguments) {
        ProgramParameters programParameters = new GoogleNGramCleaner.ProgramParameters();

        for(String argument : pArguments) {
            String[] flagAndValue = argument.split("=");

            switch(flagAndValue[0]) {
                case BEGIN_YEAR_ARG:
                    programParameters.beginYear = Integer.valueOf(flagAndValue[1]);
                    break;
                case END_YEAR_ARG:
                    programParameters.endYear = Integer.valueOf(flagAndValue[1]);
                    break;
                case LANGUAGE_ARG:
                    switch(flagAndValue[1]) {
                        case "french":
                            programParameters.language = FRENCH;
                            break;
                        default:
                            programParameters.language = FRENCH;
                            break;
                    }
                    break;
                case DATA_SET_ARG:
                    switch(Integer.valueOf(flagAndValue[1])) {
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
                    break;
                case WORKING_DIR_ARG:
                    programParameters.workingDirectory = flagAndValue[1];
                    break;
                case N_SIZE_ARG:
                    programParameters.nGramSize = Integer.parseInt(flagAndValue[1]);
                    break;
                default:
                    break;
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
            Writer writer =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName))));

            URL nGramDataURL = createGoogleNGramDataURL(pParameters, pFirstLetters);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(nGramDataURL.openStream())));

            String line = inputReader.readLine();
            while (line != null) {
                GoogleNGram googleNGram = new GoogleNGram(line, pParameters.nGramSize);
                writer.write(googleNGram.toString());
                writer.write(System.lineSeparator());

                line = inputReader.readLine();
            }

            inputReader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    public static void createOneGramEnumFile() {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("OneGramType.java"))));

            writer.write("public enum OneGramTypes {\n");

            for(int i = 0; i < 10; i++) {
                writeNGramName(String.valueOf(i), ",", writer);
            }
            for(char firstLetter = 'a'; firstLetter <= 'z'; firstLetter++) {
                writeNGramName(String.valueOf(firstLetter), ",", writer);
            }
            writeNGramName("other", ",", writer);
            writeNGramName("pos", ",", writer);
            writeNGramName("punctuation", ";\n", writer);

            writeToStringFunction(writer);

            writer.write("}" + System.lineSeparator());
            writer.flush();
            writer.close();
        } catch(Exception e) {
            System.out.println("exception: " + e.toString());
        }
    }

    static void createNGramEnumFile() {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("NGramType.java"))));

            writer.write("public enum NGramTypes {\n");

            for(int i = 0; i < 10; i++) {
                writeNGramName(String.valueOf(i), ",", writer);
            }
            for(char firstLetter = 'a'; firstLetter <= 'z'; firstLetter++) {
                writeNGramName(firstLetter + "_", ",", writer);

                for(char secondLetter = 'a'; secondLetter <= 'z'; secondLetter++) {
                    writeNGramName(String.valueOf(firstLetter) + String.valueOf(secondLetter), ",", writer);
                }
            }
            writeNGramName("_ADJ_", ",", writer);
            writeNGramName("_ADP_", ",", writer);
            writeNGramName("_ADV_", ",", writer);
            writeNGramName("_CONJ_", ",", writer);
            writeNGramName("_DET_", ",", writer);
            writeNGramName("_NOUN_", ",", writer);
            writeNGramName("_PRON_", ",", writer);
            writeNGramName("_PRT_", ",", writer);
            writeNGramName("_VERB_", ";\n", writer);

            writeToStringFunction(writer);

            writer.write("}\n");

            writer.close();
        } catch(Exception e) {
            System.out.println("exception: " + e.toString());
        }
    }

    public static void writeNGramName(String pName, String pDelimeter, Writer pWriter) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("X"); // this is here so that the enum names aren't just a number or single letter
        stringBuilder.append(pName);
        stringBuilder.append(pDelimeter);
        stringBuilder.append(System.lineSeparator());

        try {
            pWriter.write(stringBuilder.toString());
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void writeToStringFunction(Writer pWriter) {
        try {
            pWriter.write("\n@Override\n");
            pWriter.write("public String toString() {\n");
            pWriter.write("return this.name().substring(1);\n");
            pWriter.write("}\n");
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}
