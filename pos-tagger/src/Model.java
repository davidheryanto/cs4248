import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Model implements Serializable {
    // These constants to distinguish diff model types
    public static final int TRANSITION_PROBABILITY = 1;
    public static final int OBSERVATION_LIKELIHOOD = 2;
    public static final int AFFIX_PROBABILITY = 3;
    public static final int TAG_PROBABILITY = 4;

    // Minimum probability in this model
    private static double modelMin;
    private int type;  // either transition prob /observation likelihood
    private HashMap<ConditionalProbability, Double> map;

    public Model(int type) {
        this.type = type;
        map = new HashMap<>();
        modelMin = -1;
    }

    public double get(String event, String given) {
        ConditionalProbability condProb = new ConditionalProbability(event, given);
        return get(condProb);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ConditionalProbability condProb : map.keySet()) {
            stringBuilder.append(condProb + " = " + map.get(condProb) + System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    public double get(ConditionalProbability condProb) {
        if (map.containsKey(condProb)) {
            return map.get(condProb);

            // FOR smoothing
            // ============
//                        if (type == TRANSITION_PROBABILITY) {
//                            String given = condProb.getGiven();
//                            double discount = 0.75;
//                            discount = discount / build_tagger.getTagCount().get(given);
//                            return map.get(condProb) - discount;
//
//                        } else {
//                            return map.get(condProb);
//                        }
        } else {
            // Haven't seen this conditional probability

            // Unknown words, use the affixes to get the probability measure
            if (type == OBSERVATION_LIKELIHOOD) {
                double val = 0;
                double normalisationFactor = 4; // The max len of affix
                Model affixProb = run_tagger.getAffixProbability();
                ArrayList<String> affixes = build_tagger.getAffixes(condProb.getEvent());
                String tag = condProb.getGiven();

                for (String affix : affixes) {
                    if (affixProb.containsProb(affix, tag)) {
                        double normalisation = Math.pow(normalisationFactor, 4 - affix.length() + 5);
                        val += affixProb.get(affix, tag) / normalisation;
                    }
                }

                if (val > 0) {
                    return val;
                }
            }

            // None of the affixes have been seen either.

            // Smoothing
//                        if (type == TRANSITION_PROBABILITY) {
            //                            String tag = condProb.getEvent();
            //                            Model tagProbablity = build_tagger.getTagProbability();
            //                            if (tagProbablity.containsProb(tag, "")) {
            //                                double alpha = 0.5;
            //                                return alpha * tagProbablity.get(tag, "");
            //                            }
            //                        }
            return 0.00000001;
        }
    }

    public double getMin() {
        if (modelMin > -1) {
            return modelMin;
        }

        double min = 1;
        for (ConditionalProbability condProb : map.keySet()) {
            if (map.get(condProb) < min) {
                min = map.get(condProb);
            }
        }
        modelMin = min;
        // System.out.println("Model min: " + min);
        return min;
    }

    public void put(String event, String given, Double value) {
        ConditionalProbability condProb = new ConditionalProbability(event, given);
        put(condProb, value);
    }

    public void put(ConditionalProbability condProb, Double value) {
        map.put(condProb, value);
    }

    public boolean containsProb(String event, String given) {
        ConditionalProbability condProb = new ConditionalProbability(event, given);
        return map.containsKey(condProb);
    }

    public boolean contains(ConditionalProbability condProb) {
        return map.containsKey(condProb);
    }

    public Set<ConditionalProbability> keySet() {
        return map.keySet();
    }
}
