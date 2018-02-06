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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import recommender.ratings.TestMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class ItemAttributeKNNRanking extends RankingRecommender {

    protected int k;
    protected double[][] itemxItemSimilarityMatrix;

    public ItemAttributeKNNRanking(TrainingMatrix trainingMatrix, int k, double itemxItemSimilarityMatrix[][], int rankingSize) {
        super(trainingMatrix, rankingSize);
        this.k = k;
        this.itemxItemSimilarityMatrix = itemxItemSimilarityMatrix;
    }

    public void recommender() {
        long recStarTime = System.currentTimeMillis();

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < nUsers; i++) {
            final int idUser = i;
            es.execute(new Runnable() {
                @Override
                public void run() {
                    recommendItems(idUser);
                }
            });
            //System.out.println("Recomendou para usuário " + i);
        }
        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Threading problem.");
        }

        long recEndTime = System.currentTimeMillis();
        executionTime = recEndTime - recStarTime;
    }

    private void recommendItems(int user) {

        for (int i = 0; i < nItems; i++) {
            if (trainingMatrix.getValue(user, i) == -1) {
                generateUserItemScore(user, i);
            }
        }

    }

    private void generateUserItemScore(int user, int item) {
        ArrayList<Integer> knn = new ArrayList<>();
        double big = 2;
        double small = -2;
        int id;

        //obtem vizinhos ordenados por distancia
        for (int i = 0; i < k; i++) {
            id = -1;
            for (int j = 0; j < nItems; j++) {
                if ((itemxItemSimilarityMatrix[item][j] >= small) && (itemxItemSimilarityMatrix[item][j] < big)) {
                    id = j;
                    small = itemxItemSimilarityMatrix[item][j];
                }
            }

            small = -2;
            if (id != -1) {
                big = itemxItemSimilarityMatrix[item][id];
                if (trainingMatrix.getValue(user, id) != -1) {
                    knn.add(id);
                }

            }

        }

        //calcula score final
        if (knn.size() > 0) {
            for (int i = 0; i < knn.size(); i++) {
                scoreMatrix[user][item] += itemxItemSimilarityMatrix[item][knn.get(i)];
            }
        } else {
            scoreMatrix[user][item] = 0;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + scoreMatrix[user][item]);
    }
}
