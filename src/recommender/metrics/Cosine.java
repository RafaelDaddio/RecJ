package recommender.metrics;

/**
 * Implementation of the Cosine Similarity, which produces a score between 0 and
 * 1.
 *
 * See <a href="https://en.wikipedia.org/wiki/Cosine_similarity">Wikipedia
 * page</a> for more details about this metric.
 *
 * @author Rafael D'Addio
 */
public class Cosine implements SimilarityMeasures {

    public double calcSimilarity(float[] entityA, float[] entityB) {

        double cosine = 0;
        double sumAxB = 0;
        double sumA2 = 0;
        double sumB2 = 0;

        //calculate formula
        for (int i = 0; i < entityA.length; i++) {
            sumAxB += entityA[i] * entityB[i]; // the dividend
            sumA2 += entityA[i] * entityA[i]; // part of entitiyA of the factor
            sumB2 += entityB[i] * entityB[i]; // part of entityB of the factor
        }
        // their square roots
        sumA2 = Math.sqrt(sumA2);
        sumB2 = Math.sqrt(sumB2);

        double sumA2xB2 = sumA2 * sumB2; // the factor
        if (sumA2xB2 == 0) {
            sumA2xB2 = 1;
        }
        cosine = sumAxB / (sumA2xB2);

        return cosine;
    }

}
