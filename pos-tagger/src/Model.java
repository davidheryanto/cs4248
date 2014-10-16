import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;


public class Model implements Serializable {
    private HashMap<ConditionalProbability, Double> map;

    private static double modelMin;

    public Model() {
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
        } else {
            // Haven't seen this conditional probability
            return 0.0000001;
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

    public Set<ConditionalProbability> keySet() {
        return map.keySet();
    }
}
