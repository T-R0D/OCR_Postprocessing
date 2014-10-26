public class GoogleNGram {
    private Integer mNSize;
    private String[] mNGram;
    private Integer mYear;
    private Integer mNumAppearances;
    private Integer mNumPublications;

    public GoogleNGram(String pNGramAsString, int pNSize) {
        mNSize = pNSize;

        String[] parts = pNGramAsString.split("\t");
        int i = 0;

        mNGram = parts[0].split(" ");
        i++;

        mYear = Integer.parseInt(parts[i]);
        i++;

        mNumAppearances = Integer.parseInt(parts[i]);
        i++;

        mNumPublications = Integer.parseInt(parts[i]);
    }

    public Integer getN() {
        return mNSize;
    }

    public String[] getmNGram() {
        return mNGram;
    }

    public Integer getYear() {
        return mYear;
    }

    public Integer getNumAppearances() {
        return mNumAppearances;
    }

    public Integer getNumPublications() {
        return mNumPublications;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

//        for(String word : mNGram) {
//            stringBuilder.append(word);
//            stringBuilder.append("\t");
//        }
        stringBuilder.append(" ");
        for(int i = 1; i < mNGram.length; i++) {
            stringBuilder.append(" ");
            stringBuilder.append(mNGram[i]);
        }
        stringBuilder.append("\t");
        stringBuilder.append(mYear);
        stringBuilder.append("\t");
        stringBuilder.append(mNumAppearances);
        stringBuilder.append("\t");
        stringBuilder.append(mNumPublications);

        return stringBuilder.toString();
    }
}
