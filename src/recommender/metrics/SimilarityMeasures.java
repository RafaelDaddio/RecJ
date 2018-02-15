package recommender.metrics;

/**
 * Interface responsible to define the method which should be implemented in all
 * similarity metrics
 *
 * @author Rafael D'Addio
 */
public interface SimilarityMeasures {

    /**
     * Method for calculating similarity
     *
     * @param entityA either a user or an item metadata vector
     * @param entityB either a user or an item metadata vector
     * @return a similarity score
     */
    public double calcSimilarity(float[] entityA, float[] entityB);

}
