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
public class Cosine implements SimilarityMeasures {
    
    public double calcSimilarity(float[] itemA, float[] itemB) {

        double cosine = 0;
        double sumAxB = 0;
        double sumA2 = 0;
        double sumB2 = 0;
        for (int i = 0; i < itemA.length; i++) {
            sumAxB += itemA[i] * itemB[i];
            sumA2 += itemA[i] * itemA[i];
            sumB2 += itemB[i] * itemB[i];
        }
        sumA2 = Math.sqrt(sumA2);
        sumB2 = Math.sqrt(sumB2);
        double sumA2xB2 = sumA2 * sumB2;
        if (sumA2xB2 == 0) {
            sumA2xB2 = 1;
        }
        cosine = sumAxB / (sumA2xB2);

        return cosine;
    }
    
}
