package recommender.algorithms.ratingprediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import recommender.ratings.PredictionMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author ads
 */
public class ItemAttributeKNNv2 extends ItemAttributeKNN {

    /*     
     predictionOption
     0 = all predictions
     1 = only test    
     */
    public ItemAttributeKNNv2(String testFile, TrainingMatrix trainingMatrix, int k, int predictionOption, double itemxItemSimilarityMatrix[][]) {
        super(testFile, trainingMatrix, k, predictionOption, itemxItemSimilarityMatrix);
    }

    @Override
    public void recommender() {
        long recStarTime = System.currentTimeMillis();
        for (int i = 0; i < nUsers; i++) {
            recommendItems(i);
            //System.out.println("Recomendou para usuário " + i);
        }
        long recEndTime = System.currentTimeMillis();
        executionTime = recEndTime - recStarTime;
    }

    private void recommendItems(int user) {
        if (predictionOption == 0) {
            for (int i = 0; i < nItems; i++) {
                if (trainingMatrix.getValue(user, i) == -1) {
                    generateUserItemScore(user, i);
                }
            }
        } else {
            for (int i = 0; i < nItems; i++) {
                if (testMatrix.getValue(user, i) == 1) {
                    generateUserItemScore(user, i);
                }
            }
        }

    }

    private void generateUserItemScore(int user, int item) {
        relevantItems.clear();
        knn.clear();
        big = 2;
        small = -2;

        //obtem vizinhos ordenados por distancia
        for (int i = 0; i < k; i++) {
            idneighbour = -1;
            for (int j = 0; j < nItems; j++) {
                if ((itemxItemSimilarityMatrix[item][j] >= small) && (itemxItemSimilarityMatrix[item][j] < big)) {
                    idneighbour = j;
                    small = itemxItemSimilarityMatrix[item][j];
                }
            }

            small = -2;
            if (idneighbour != -1) {
                //System.out.println("Entrou vizinho");
                big = itemxItemSimilarityMatrix[item][idneighbour];
                if (trainingMatrix.getValue(user, idneighbour) != -1) {
                    knn.add(idneighbour);
                }

            }

        }

        //calcula score final
        dividendo = 1;
        if (knn.size() > 0) {
            for (int i = 0; i < knn.size(); i++) {
                ruj = trainingMatrix.getValue(user, knn.get(i));
                buj = bui[user][knn.get(i)];
                
                scoreMatrix[user][item] += (ruj - buj) * itemxItemSimilarityMatrix[item][knn.get(i)];
                dividendo += itemxItemSimilarityMatrix[item][knn.get(i)];
                //System.out.println(scoreMatrix[user][item] + " " + dividendo);
            }
            scoreMatrix[user][item] = bui[user][item] + (scoreMatrix[user][item] / dividendo);
        }else {
            scoreMatrix[user][item] = bui[user][item];
        }
        if (scoreMatrix[user][item] < 1) {
            scoreMatrix[user][item] = 1;

        } else if (scoreMatrix[user][item] > 5) {
            scoreMatrix[user][item] = 5;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + scoreMatrix[user][item]);
    }

}
