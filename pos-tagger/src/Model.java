import java.util.HashMap;
import java.util.Set;

public class Model {
    private HashMap<ConditionalProbability, Double> map;

    public Model() {
        map = new HashMap<>();
    }

    public double get(String event, String given) {
        ConditionalProbability condProb = new ConditionalProbability(event, given);
        return get(condProb);
    }

    public double get(ConditionalProbability condProb) {
        if (map.containsKey(condProb)) {
            return map.get(condProb);
        } else {
            // Haven't seen this conditional probability
            // TODO apply smoothing?
            return 0;
        }
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
