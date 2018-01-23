/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.ratings;

import java.util.HashMap;

/**
 *
 * @author rafaeldaddio
 */
public class PredictionMatrix extends RatingMatrix {

    public PredictionMatrix(HashMap<Integer, Integer> indexUserDbSystem, HashMap<Integer, Integer> indexItemDbSystem, int[] indexUserSystemDb, int[] indexItemSystemDb) {
        super();
        this.indexUserDbSystem = indexUserDbSystem;
        this.indexItemDbSystem = indexItemDbSystem;
        nItems = indexItemDbSystem.size();
        nUsers = indexUserDbSystem.size();
        this.indexUserSystemDb = indexUserSystemDb;
        this.indexItemSystemDb = indexItemSystemDb;
        ratingMatrix = new double[nUsers][nItems];
        fillPredictionMatrix();
    }

    private void fillPredictionMatrix() {

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                ratingMatrix[i][j] = -1;
            }
        }
        //System.out.println("Populou a matriz de Predições");
    }

}
