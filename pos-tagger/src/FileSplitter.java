import java.io.*;
import java.util.ArrayList;

public class FileSplitter {
    /**
     *
     * @param file
     * @param foldCount
     * @return List of (trainData, testData)
     * @throws IOException
     */
    public static ArrayList<Pair<ArrayList<String>, ArrayList<String>>> split(File file, int foldCount) throws IOException {
        int lineCount = getLineCount(file);
        int lineCountForTesting = lineCount / foldCount;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> validationSets = new ArrayList<>();

        for (int i = 0; i < foldCount; i++) {
            int startIndexForTesting = i * lineCountForTesting;
            int endIndexForTesting = startIndexForTesting + lineCountForTesting;

            ArrayList<String> training = new ArrayList<>();
            ArrayList<String> testing = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            int lineIndex = 0;

            while (line != null) {
                if (lineIndex >= startIndexForTesting && lineIndex <= endIndexForTesting) {
                    testing.add(line);
                } else {
                    training.add(line);
                }
                lineIndex += 1;
                line = reader.readLine();
            }

            reader.close();
            validationSets.add(new Pair<ArrayList<String>, ArrayList<String>>(training, testing));
        }

        // test(validationSets);

        return validationSets;
    }

    private static int getLineCount(File file) throws IOException {
        // http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
        LineNumberReader  lnr = new LineNumberReader(new FileReader(file));
        lnr.skip(Long.MAX_VALUE);
        return  lnr.getLineNumber() + 1;
    }

    private static void test(ArrayList<Pair<ArrayList<String>, ArrayList<String>>> validationSets) throws FileNotFoundException {
        int index = 1;
        for (Pair<ArrayList<String>, ArrayList<String>> set : validationSets) {
            ArrayList<String> training = set.getFirst();
            ArrayList<String> testinng = set.getSecond();

            File trainingFile = new File(index + "-training");
            File testingFile = new File(index + "-testing");

            write(trainingFile, training);
            write(testingFile,testinng);

            index += 1;
        }
    }

    private static void write(File file, ArrayList<String> content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(file));
        writer.println(content);
        writer.close();
    }
}
