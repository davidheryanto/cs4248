public class ConditionalProbability {
    private String event;
    private String given;

    public ConditionalProbability(String event, String given) {
        this.event = event;
        this.given = given;
    }

    public String getEvent() {
        return event;
    }

    public String getGiven() {
        return given;
    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + (given != null ? given.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionalProbability)) return false;

        ConditionalProbability that = (ConditionalProbability) o;

        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        if (given != null ? !given.equals(that.given) : that.given != null) return false;

        return true;
    }

}
