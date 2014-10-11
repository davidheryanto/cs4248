import java.io.*;
import java.util.ArrayList;

/**
 * TODO:
 * 1. Handle new words -> smoothing?
 * 2. Tune the model -> use dev -> how?
 */


public class run_tagger {
    private static Model transitionProbability;
    private static Model observationLikelihood;
    private static String[] states = {
            "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "$", "#", "``", "''", "(", ")", ",", ".", ":"
    };

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        args = new String[3];
        args[0] = "data/sents.test";
        args[1] = "model_file";
        args[2] = "short.out";

        File testFile = new File(args[0]);
        File modelFile = new File(args[1]);
        File outFile = new File(args[2]);

        readModel(modelFile);
        tagFile(testFile, outFile);
    }

    public static void readModel(File modelFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(modelFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<Model> models = (ArrayList<Model>) ois.readObject();
        ois.close();

        transitionProbability = models.get(0);
        observationLikelihood = models.get(1);
    }

    public static void tagFile(File testFile, File outFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String line = reader.readLine();

        while (line != null) {
            String result = getTagsForLine(line);
            writer.println(result);
            line = reader.readLine();
        }
    }

    public static String getTagsForLine(String line) {
        // Adapted from Speech and Language Processing by Jurafsky and Marting Sec 5.5.3

        line = line.toLowerCase();
        String regexForSplittingWords = " ";
        String[] words = line.split(regexForSplittingWords);
        int stateLen = states.length;
        int stepLen = words.length;

        double[][] viterbi = new double[stateLen + 2][stepLen + 1];
        int[][] backpointers = new int[stateLen + 2][stepLen + 1];

        // Initialization
        for (int i = 0; i < stateLen; i++) {
            viterbi[i][0] = transitionProbability.get(states[i], "<s>") *
                    observationLikelihood.get(words[0], states[i]);
            backpointers[i][0] = -1;
        }

        // Recursion step
        for (int i = 1; i < stepLen; i++) {
            for (int j = 0; j < stateLen; j++) {
                for (int k = 0; k < stateLen; k++) {
                    double val = viterbi[k][i - 1]
                            * transitionProbability.get(states[k], states[j])
                            * observationLikelihood.get(words[i], states[j]);
                    if (val > viterbi[j][i]) {
                        viterbi[j][i] = val;
                        backpointers[j][i] = k;
                    }
                }
            }
        }

        // Termination
        for (int i = 0; i < stateLen; i++) {
            double val = viterbi[i][stepLen - 1]
                    * transitionProbability.get("</s>", states[i]);
            if (val > viterbi[stateLen][stepLen]) {
                viterbi[stateLen][stepLen] = val;
                backpointers[stateLen][stepLen] = i;
            }
        }


        // System.out.println(line);
        ArrayList<String> tags = new ArrayList<>();
        tags.add("</s> ");
        int stateIndex = stateLen;

        for (int i = stepLen; i >= 0; i--) {
            if (backpointers[stateIndex][i] < 0) {
                tags.add("<s> ");
                continue;
            }

            tags.add(states[backpointers[stateIndex][i]]);
            stateIndex = backpointers[stateIndex][i];
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = tags.size() - 1; i >= 0; i--) {
            stringBuilder.append(tags.get(i) + " ");
        }

        System.out.println("==========================");
        System.out.println(tags.get(tags.size() - 1));
        for (int i = 0; i < words.length; i++) {
            System.out.println(words[i] + "/" + tags.get(tags.size() - 2 - i));
        }
        System.out.println(tags.get(0));

        return "";
    }
}
