package recommender.algorithms.ratingprediction;

import java.io.*;
import java.util.*;
import recommender.ratings.*;

/**
 *
 * @author ads
 */
public class ItemAttributeKNN extends RatingPredictionBase {

    protected ArrayList<Integer> relevantItems;
    protected ArrayList<Integer> knn;
    protected int k;
    protected double[][] itemxItemSimilarityMatrix;
    protected double[][] bui;
    protected double[] bu, bi;
    protected double mi;
    protected double big, small;
    protected int idneighbour;
    protected double dividend, ruj, buj;
    protected final int regBi = 10;
    protected final int regBu = 15;
    protected final int iterations = 10;
    protected long executionTime;

    public ItemAttributeKNN(String testFile, TrainingMatrix trainingMatrix, int k, int predictionOption, double itemxItemSimilarityMatrix[][]) {
        super(testFile, trainingMatrix, predictionOption);
        relevantItems = new ArrayList<>();
        knn = new ArrayList<>();
        this.k = k;
        this.itemxItemSimilarityMatrix = itemxItemSimilarityMatrix;
        trainBaselines();
    }

    private void computeMi() {

        int count = 0;

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                if (trainingMatrix.getValue(i, j) != -1) {
                    mi += trainingMatrix.getValue(i, j);
                    count++;
                }
            }
        }
        mi = mi / count;
        //System.out.println(mi);
    }

    private void trainBaselines() {
        bu = new double[nUsers];
        bi = new double[nItems];
        bui = new double[nUsers][nItems];

        computeMi();

        for (int i = 0; i < iterations; i++) {
            fillBiVector();
            fillBuVector();
        }

        fillBuiMatrix();
    }

    private void fillBiVector() {
        int count;
        for (int i = 0; i < bi.length; i++) {
            bi[i] = 0;
            count = 0;
            for (int j = 0; j < nUsers; j++) {
                if (trainingMatrix.getValue(j, i) != -1) {
                    bi[i] += (trainingMatrix.getValue(j, i) - mi - bu[j]);
                    count++;
                }
            }
            if (count > 0) {
                bi[i] = bi[i] / (regBi + count);
            }
            //System.out.println("Bi do item " + i + " : " + bi[i] + " com " + count + " usuarios");
        }
        //System.out.println("Computou bi");
    }

    private void fillBuVector() {
        int count;
        for (int i = 0; i < bu.length; i++) {
            bu[i] = 0;
            count = 0;
            for (int j = 0; j < nItems; j++) {
                if (trainingMatrix.getValue(i, j) != -1) {
                    bu[i] += (trainingMatrix.getValue(i, j) - mi - bi[j]);
                    count++;
                }
            }
            if (count > 0) {
                bu[i] = bu[i] / (regBu + count);
            }
            //System.out.println("Bu do usuario " + i + " : " + bu[i] + " com " + count + " itens");
        }
        //System.out.println("Computou bu");
    }

    private void fillBuiMatrix() {

        for (int i = 0; i < bu.length; i++) {
            for (int j = 0; j < bi.length; j++) {
                bui[i][j] = mi + bu[i] + bi[j];
            }
        }
    }

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

        //recupera os ids dos itens avaliados pelo usuário
        for (int i = 0; i < nItems; i++) {
            if (trainingMatrix.getValue(user, i) != -1) {
                relevantItems.add(i);
            }
        }

        //obtem vizinhos ordenados por distancia
        for (int i = 0; i < k; i++) {
            idneighbour = -1;
            for (int j = 0; j < relevantItems.size(); j++) {
                //System.out.println(itemxItemScoresMatrix[item][relevantItems.get(j)]);
                if ((itemxItemSimilarityMatrix[item][relevantItems.get(j)] >= small) && (itemxItemSimilarityMatrix[item][relevantItems.get(j)] < big)) {
                    idneighbour = relevantItems.get(j);
                    small = itemxItemSimilarityMatrix[item][relevantItems.get(j)];
                }
            }

            small = -2;
            if (idneighbour != -1) {
                //System.out.println("Entrou vizinho");
                big = itemxItemSimilarityMatrix[item][idneighbour];
                knn.add(idneighbour);
            }

            if (i == relevantItems.size()) {
                break;
            }

        }

        //calcula score final
        dividend = 1;
        if (knn.size() > 0) {
            for (int i = 0; i < knn.size(); i++) {
                ruj = trainingMatrix.getValue(user, knn.get(i));
                buj = bui[user][knn.get(i)];
                predictions[user][item] += (ruj - buj) * itemxItemSimilarityMatrix[item][knn.get(i)];
                dividend += itemxItemSimilarityMatrix[item][knn.get(i)];
                //System.out.println(predictions[user][item] + " " + dividend);
            }
            predictions[user][item] = bui[user][item] + (predictions[user][item] / dividend);
        } else {
            predictions[user][item] = bui[user][item];
        }
        if (predictions[user][item] < 1) {
            predictions[user][item] = 1;

        } else if (predictions[user][item] > 5) {
            predictions[user][item] = 5;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + predictions[user][item]);
    }

    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
