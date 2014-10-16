import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Evaluator {
    static File develData = new File("sents.devt");

    public static void main(String[] args) throws IOException {
        int foldCount = 10;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> evaluationSets =
                FileSplitter.split(new File("sents.train"), foldCount);
        ArrayList<Double> scores = new ArrayList<>();
        double sum = 0;

        int index = 1;
        System.out.println(foldCount + "-fold Evaluation\n");
        System.out.println("Fold\tAccuracy\n=========================");
        for (Pair<ArrayList<String>, ArrayList<String>> evaluationSet : evaluationSets) {
            ArrayList<String> trainData = evaluationSet.getFirst();
            ArrayList<String> testData = evaluationSet.getSecond();
            double score = evaluate(trainData, testData);

            scores.add(score);
            sum += score;

            System.out.println(String.format("%4d\t%.5f", index, score));
            index += 1;
        }

        System.out.println(String.format("\nAverage accuracy : %f", sum / scores.size()));
    }


    /**
     *
     * @param trainData
     * @param testData
     * @return accuracy
     */
    public static double evaluate(ArrayList<String> trainData, ArrayList<String> testData) {
        int tagsCount = 0;
        int tagsMatchCount = 0;

        build_tagger.resetModel();
        build_tagger.buildModel(trainData, develData);
        run_tagger.setModel(build_tagger.getTransitionProbability(), build_tagger.getObservationLikelihood());

        for (String lineWithTags : testData) {
            String line = getLineWithoutTags(lineWithTags);
            String[] tagsActual = getTags(lineWithTags);
            String[] tagsPredicted = run_tagger.getTagsForLine(line);

            tagsCount += tagsActual.length;
            for (int i = 0; i < tagsActual.length; i++) {
                if (i > tagsPredicted.length - 1) {
                    // Error in parsing where tags and words not matching
                    continue;
                }
                if (tagsActual[i].equalsIgnoreCase(tagsPredicted[i])) {
                    tagsMatchCount += 1;
                }
            }
        }

        return (double)tagsMatchCount / tagsCount;
    }

    public static String getLineWithoutTags(String line) {
        StringBuilder builder = new StringBuilder();
        String[] wordTagPairs = line.split(" ");

        for (String wordTagPair : wordTagPairs) {
            int slashIndex = wordTagPair.lastIndexOf('/');
            String word = wordTagPair.substring(0, slashIndex);
            builder.append(" ").append(word);
        }

        return builder.substring(1).toString();
    }

    public static String[] getTags(String line) {
        ArrayList<String> tags = new ArrayList<>();
        String[] wordTagPairs = line.split(" ");

        for (String wordTagPair : wordTagPairs) {
            int slashIndex = wordTagPair.lastIndexOf('/');
            String tag = wordTagPair.substring(slashIndex + 1);
            tags.add(tag);
        }

        return tags.toArray(new String[tags.size()]);
    }
}
