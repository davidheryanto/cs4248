import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class build_tagger {
    private static Map<String, Integer> wordCount = new HashMap<>();
    private static Map<String, Integer> tagCount = new HashMap<>();

    private static Model transitionProbability = new Model();
    private static Model observationLikelihood = new Model();

    public static Model getTransitionProbability() {
        return transitionProbability;
    }

    public static Model getObservationLikelihood() {
        return observationLikelihood;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        args = new String[3];
//        args[0] = "data/sents.train";
//        args[1] = "data/sents.devt";
//        args[2] = "model_file";

        File trainFile = new File(args[0]);
        File develFile = new File(args[1]);
        File modelFile = new File(args[2]);
        File modelTransitionTxtFile = new File(args[2] + "-transition-probability.txt");
        File modelObservationTxtFile = new File(args[2] + "-observation-likelihood.txt");

        deleteExistingFile(modelFile);
        deleteExistingFile(modelTransitionTxtFile);
        deleteExistingFile(modelObservationTxtFile);

        buildModel(trainFile, develFile);
        saveModel(modelFile, modelTransitionTxtFile, modelObservationTxtFile);
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

        System.out.println("Done");
        // TODO: Tune the model using develFile
    }

    public static void saveModel(File modelBinaryFile,
                                 File modelTransitionTxtFile,
                                 File modelObservationTxtFile) throws IOException {
        // Save serialized binary format
        ArrayList<Model> models = new ArrayList<>();
        models.add(transitionProbability);
        models.add(observationLikelihood);

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

    public static void buildModel(ArrayList<String> trainData, File develFile) {
        updateCount(trainData);
        updateModel(transitionProbability);
        updateModel(observationLikelihood);
    }

    public static void updateCount(ArrayList<String> trainData) {
        for (String line : trainData) {
            updateCount(line);
        }
    }

    public static void resetModel() {
        wordCount = new HashMap<>();
        tagCount = new HashMap<>();
        transitionProbability = new Model();
        observationLikelihood = new Model();
    }
}
