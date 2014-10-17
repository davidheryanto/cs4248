import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class build_tagger {
    private static Map<String, Integer> wordCount = new HashMap<>();
    private static Map<String, Integer> tagCount = new HashMap<>();

    private static Model transitionProbability = new Model(Model.TRANSITION_PROBABILITY);
    private static Model observationLikelihood = new Model(Model.OBSERVATION_LIKELIHOOD);

    private static Model affixProbability = new Model(Model.AFFIX_PROBABILITY);  // For unknown words
    private static Model tagProbability = new Model(Model.TAG_PROBABILITY);      // For smoothing

    public static Map<String, Integer> getTagCount() {
        return tagCount;
    }
    public static Map<String, Integer> getWordCount() {
        return wordCount;
    }
    public static Model getTransitionProbability() {
        return transitionProbability;
    }
    public static Model getObservationLikelihood() {
        return observationLikelihood;
    }
    public static Model getAffixProbability() {
        return affixProbability;
    }
    public static Model getTagProbability() {
        return tagProbability;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File trainFile = new File(args[0]);
        File develFile = new File(args[1]);
        File modelFile = new File(args[2]);
        File modelTransitionTxtFile = new File(args[2] + "-transition-probability.txt");
        File modelObservationTxtFile = new File(args[2] + "-observation-likelihood.txt");
        File affixProbabilityFile = new File(args[2] + "-affix-probability.txt");

        deleteExistingFile(modelFile);
        deleteExistingFile(modelTransitionTxtFile);
        deleteExistingFile(modelObservationTxtFile);

        buildModel(trainFile, develFile);
        saveModel(modelFile, modelTransitionTxtFile, modelObservationTxtFile, affixProbabilityFile);
    }

    public static void deleteExistingFile(File fileToDelete) {
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    public static void buildModel(File trainFile, File develFile) throws IOException {
        updateCount(trainFile);
        updateModel(transitionProbability);
        updateModel(observationLikelihood);
        updateModel(affixProbability);

        // Update tag probability -- for smoothing purpose
        int tagCountSum = getTagCountSum();
        for (String tag : tagCount.keySet()) {
            int count = tagCount.get(tag);
            double prob = (double) count / tagCountSum;
            tagProbability.put(tag, "", prob);
        }

        System.out.println("Finish building model. Saving to model_file ... ");
        // TODO: Tune the model using develFile
    }

    public static void saveModel(File modelBinaryFile,
                                 File modelTransitionTxtFile,
                                 File modelObservationTxtFile,
                                 File modelAffixTxtFile) throws IOException {
        // Save serialized binary format
        ArrayList<Model> models = new ArrayList<>();
        models.add(transitionProbability);
        models.add(observationLikelihood);
        models.add(affixProbability);
        models.add(tagProbability);

        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(modelBinaryFile));
        oos.writeObject(models);
        oos.close();

        // Save to readable format
        PrintWriter writer = new PrintWriter(new FileOutputStream(modelTransitionTxtFile));
        writer.println("TransitionProbability");
        writer.println(getSeparator(30));
        writer.println(transitionProbability);
        writer.close();

        writer = new PrintWriter(new FileOutputStream(modelObservationTxtFile));
        writer.println("ObservationLikelihood");
        writer.println(getSeparator(30));
        writer.println(observationLikelihood);

        writer = new PrintWriter(new FileOutputStream(modelAffixTxtFile));
        writer.println("Affix Probability");
        writer.println(getSeparator(30));
        writer.println(affixProbability);
    }

    public static void updateCount(File inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        while (line != null) {
            updateCount(line);
            line = reader.readLine();
        }
    }

    public static void updateModel(Model model) {
        for (ConditionalProbability condProb : model.keySet()) {
            Double numerator = model.get(condProb);
            String given = condProb.getGiven();
            Integer denominator = tagCount.get(given);
            model.put(condProb, numerator / denominator);
        }
    }

    public static int getTagCountSum() {
        int sum = 0;
        for (String tag : tagCount.keySet()) {
            sum += tagCount.get(tag);
        }
        return sum;
    }

    public static String getSeparator(int charCount) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < charCount; i++) {
            stringBuilder.append("=");
        }
        return stringBuilder.toString();
    }

    public static void updateCount(String line) {
        String[] wordTagPairs = line.split(" ");
        String previousTag = "<s>";
        updateCount(tagCount, previousTag);

        for (String wordTagPair : wordTagPairs) {
            // ASSUMPTIONS: each wordTagPair is separated by 'space' character
            int slashIndex = wordTagPair.lastIndexOf('/');
            String word = wordTagPair.substring(0, slashIndex).toLowerCase();
            String tag = wordTagPair.substring(slashIndex + 1);

            updateCount(wordCount, word);
            updateCount(tagCount, tag);
            updateCount(transitionProbability, tag, previousTag);
            updateCount(observationLikelihood, word, tag);

            ArrayList<String> affixes = getAffixes(word);
            for (String affix : affixes) {
                updateCount(affixProbability, affix, tag);
            }

            previousTag = tag;
        }

        String endTag = "</s>";
        updateCount(tagCount, endTag);
        updateCount(transitionProbability, endTag, previousTag);
    }

    public static void updateCount(Map<String, Integer> count, String key) {
        Integer value = count.containsKey(key) ? count.get(key) + 1 : 1;
        count.put(key, value);
    }

    public static void updateCount(Model model, String event, String given) {
        // Update the count of (given, event)
        Double value = model.containsProb(event, given) ?
                model.get(event, given) + 1 : 1;
        model.put(event, given, value);
    }

    /**
     * Get the affixes of a word
     * Currently following this rule:
     * 1. If word.length is <= 3, we don't consider its affixes
     * 2. If 4 <= word.length <= 5, we consider suffix and prefix of length 3 until word.length-1
     * 3. If 7 <= word.length, consider suffix and prefix and suffix of length 1 until 5
     *
     * @param word
     * @return
     */
    public static ArrayList<String> getAffixes(String word) {
        ArrayList<String> affixes = new ArrayList<>();

        if (word.length() <= 3) {
            return affixes;
        }

        ArrayList<String> prefixes = new ArrayList<>();
        ArrayList<String> suffixes = new ArrayList<>();

        int maxLen = word.length() <= 5 ? word.length() - 1 : 5;

        for (int i = 2; i < maxLen; i++) {
            prefixes.add(word.substring(0, i + 1));
            suffixes.add(word.substring(word.length() - 1 - i));
        }

        affixes.addAll(prefixes);
        affixes.addAll(suffixes);
        return affixes;
    }

    public static void buildModel(ArrayList<String> trainData, File develFile) {
        updateCount(trainData);
        updateModel(transitionProbability);
        updateModel(observationLikelihood);
        updateModel(affixProbability);

        // Update tag probability -- for smoothing purpose
        int tagCountSum = getTagCountSum();
        for (String tag : tagCount.keySet()) {
            int count = tagCount.get(tag);
            double prob = (double) count / tagCountSum;
            tagProbability.put(tag, "", prob);
        }
    }

    public static void updateCount(ArrayList<String> trainData) {
        for (String line : trainData) {
            updateCount(line);
        }
    }

    public static void resetModel() {
        wordCount = new HashMap<>();
        tagCount = new HashMap<>();
        transitionProbability = new Model(Model.TRANSITION_PROBABILITY);
        observationLikelihood = new Model(Model.OBSERVATION_LIKELIHOOD);
        affixProbability = new Model(Model.AFFIX_PROBABILITY);
        tagProbability = new Model(Model.TAG_PROBABILITY);
    }
}
