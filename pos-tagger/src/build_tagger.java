import com.sun.org.apache.xpath.internal.operations.Mod;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class build_tagger {
    private static Map<String, Integer> wordCount = new HashMap<>();
    private static Map<String, Integer> tagCount = new HashMap<>();
    private static Model transitionProbability = new Model();
    private static Model observationLikelihood = new Model();

    public static void main(String[] args) throws IOException {
        args = new String[3];
        args[0] = "data/short.train";
        args[1] = "data/short.devt";
        args[2] = "model";

        File trainFile = new File(args[0]);
        File develFile = new File(args[1]);
        File modelFile = new File(args[2]);

        if (modelFile.exists()) {
            modelFile.delete();
        }

        buildModel(trainFile, develFile, modelFile);
    }

    public static void buildModel(File trainFile, File develFile, File modelFile) throws IOException {

        updateCount(trainFile);
        updateModel(transitionProbability);
        updateModel(observationLikelihood);

        System.out.println("Done");
        // TODO: Tune the model using develFile
    }


    public static void updateModel(Model model) {
        for (ConditionalProbability condProb : model.keySet()) {
            Double numerator = model.get(condProb);
            String given = condProb.getGiven();
            Integer denominator = tagCount.get(given);
            model.put(condProb, numerator / denominator);
        }
    }

    public static void updateCount(File inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        while (line != null) {
            updateCount(line);
            line = reader.readLine();
        }
    }

    public static void updateCount(String line) {
        String[] wordTagPairs = line.split(" ");
        String previousTag = "<s>";
        updateCount(tagCount, previousTag);

        for (String wordTagPair : wordTagPairs) {
            String[] parts = wordTagPair.split("/");
            if (parts.length != 2) {
                System.err.println(wordTagPair + " is not in word/tag format");
                continue;
            }
            String word = parts[0];
            String tag = parts[1];
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
        Double value = model.containsProb(event, given) ?
                model.get(event, given) + 1 : 1;
        model.put(event, given, value);
    }
}
