/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package recommender.algorithms.itemrecommendation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class RankingRecommender {
    
    protected double[][] scoreMatrix;
    protected int nItems;
    protected int nUsers;
    protected int rankingSize;
    protected long executionTime;    
    protected TrainingMatrix trainingMatrix;

    public RankingRecommender(TrainingMatrix trainingMatrix, int rankingSize){
        this.trainingMatrix = trainingMatrix;
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        scoreMatrix = new double[nUsers][nItems];        
        fillScoreMatrix();        
        this.rankingSize = rankingSize;        
    }    
    
    private void fillScoreMatrix() {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                scoreMatrix[i][j] = 0;
            }
        }
    }
    
    public void writeRecommendations(String recomendationPath) {
        double big = 2;
        double small = -2;
        int id = -1;

        try {
            File recomendation = new File(recomendationPath);
            if (!recomendation.exists()) {
                recomendation.createNewFile();
            } else {
                recomendation.delete();
                recomendation.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(recomendation, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (int u = 0; u < nUsers; u++) {
                big = 10000;
                small = -10000;
                id = -1;
                for (int n = 0; n < rankingSize; n++) {
                    id = -1;
                    for (int i = 0; i < nItems; i++) {
                        if (scoreMatrix[u][i] != 0) {
                            if ((scoreMatrix[u][i] >= small) && (scoreMatrix[u][i] <= big)) {
                                small = scoreMatrix[u][i];
                                id = i;
                            }

                        }
                    }
                    small = -20000;
                    if (id != -1) {
                        bufferedWriter.write(trainingMatrix.getIndexUserSystemDb()[u] + " " + trainingMatrix.getIndexItemSystemDb()[id] + " " + scoreMatrix[u][id]);
                        bufferedWriter.write("\n");

                        big = scoreMatrix[u][id];
                        scoreMatrix[u][id] = 0;
                    }
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error.");
        }
    }
    
    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return executionTime;
    }
    
}
