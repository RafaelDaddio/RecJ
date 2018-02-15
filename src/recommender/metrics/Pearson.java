package recommender.metrics;

/**
 * Implementation of the Pearson correlation coefficient, which produces a
 * similarity score between -1 and 1.
 *
 * See <a
 * href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Wikipedia
 * page</a> for more details about this metric.
 *
 * @author Rafael D'Addio
 */
public class Pearson implements SimilarityMeasures {

    @Override
    public double calcSimilarity(float[] entityA, float[] entityB) {
        double pearson = 0;
        double meanA = 0;
        double meanB = 0;
        double sumAxB = 0;
        double sumA2 = 0;
        double sumB2 = 0;

        //calculate mean value for entityA and entityB
        for (int i = 0; i < entityA.length; i++) {
            meanA += entityA[i];
            meanB += entityB[i];
        }
        meanA = meanA / entityA.length;
        meanB = meanB / entityB.length;

        // calculate formula
        for (int i = 0; i < entityA.length; i++) {
            sumAxB += (entityA[i] - meanA) * (entityB[i] - meanB); // the dividend
            sumA2 += (entityA[i] - meanA) * (entityA[i] - meanA); //part of entityA of the factor
            sumB2 += (entityB[i] - meanB) * (entityB[i] - meanB); //part of entityB of the factor
        }

        double sqrtA2xB2 = Math.sqrt(sumA2 * sumB2); // the factor
        if (sqrtA2xB2 == 0) {
            sqrtA2xB2 = 1;
        }
        pearson = sumAxB / sqrtA2xB2;

        return pearson;

    }

}
