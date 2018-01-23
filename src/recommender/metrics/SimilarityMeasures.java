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
public interface SimilarityMeasures {
    
    public double calcSimilarity(float[] itemA, float[] itemB);
    
}
