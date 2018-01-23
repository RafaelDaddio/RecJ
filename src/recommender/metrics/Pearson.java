/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.metrics;

/**
 *
 * @author rafaeldaddio
 */
public class Pearson implements SimilarityMeasures {

    @Override
    public double calcSimilarity(float[] itemA, float[] itemB) {
        double pearson = 0;
        double meanA = 0;
        double meanB = 0;
        double sumAxB = 0;
        double sumA2 = 0;
        double sumB2 = 0;

        for (int i = 0; i < itemA.length; i++) {
            meanA += itemA[i];
            meanB += itemB[i];
        }
        meanA = meanA / itemA.length;
        meanB = meanB / itemB.length;

        for (int i = 0; i < itemA.length; i++) {
            sumAxB += (itemA[i] - meanA) * (itemB[i] - meanB);
            sumA2 += (itemA[i] - meanA) * (itemA[i] - meanA);
            sumB2 += (itemB[i] - meanB) * (itemB[i] - meanB);
        }

        double sqrtA2xB2 = Math.sqrt(sumA2 * sumB2);
        if (sqrtA2xB2 == 0) {
            sqrtA2xB2 = 1;
        }
        pearson = sumAxB / sqrtA2xB2;

        return pearson;

    }

}
