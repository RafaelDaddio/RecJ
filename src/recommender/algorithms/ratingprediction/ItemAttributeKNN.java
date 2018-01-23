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
public class ItemAttributeKNN {

    protected ArrayList<Integer> relevantItems;
    protected ArrayList<Integer> knn;
    protected int k;
    protected TrainingMatrix trainingMatrix;
    protected PredictionMatrix testMatrix;
    protected double[][] itemxItemSimilarityMatrix;
    protected double[][] scoreMatrix;
    protected double[][] bui;
    protected double[] bu;
    protected double[] bi;
    protected double mi;
    protected double big;
    protected double small;
    protected double dividendo;
    protected double ruj;
    protected double buj;
    protected int nItems;
    protected int nUsers;
    protected int idneighbour;
    protected int predictionOption;
    protected final int regBi = 10;
    protected final int regBu = 15;
    protected final int iterations = 10;
    protected long executionTime;

    /*     
     predictionOption
     0 = all predictions
     1 = only test    
     */
    public ItemAttributeKNN(String testFile, TrainingMatrix trainingMatrix, int k, int predictionOption, double itemxItemSimilarityMatrix[][]) {

        relevantItems = new ArrayList<>();
        knn = new ArrayList<>();
        this.trainingMatrix = trainingMatrix;
        this.k = k;
        this.predictionOption = predictionOption;
        if (predictionOption == 1) {
            testMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        this.itemxItemSimilarityMatrix = itemxItemSimilarityMatrix;
        scoreMatrix = new double[nUsers][nItems];
        fillScoreMatrix();
        trainBaselines();

    }

    private void fillPredictionMatrix(String testFile) {
        try {
            File file = new File(testFile);
            Scanner scannerFile = new Scanner(file);

            int lines = 0;
            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                testMatrix.setValuematrix(trainingMatrix.getIndexUserDbSystem().get(user), trainingMatrix.getIndexItemDbSystem().get(item), 1);
                lines++;
            }
            System.out.println(lines + " lines");
        } catch (IOException e) {
            System.out.println("Error filling test matrix.");
        }
    }

    private void fillScoreMatrix() {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                scoreMatrix[i][j] = 0;
            }
        }
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
        } else {
            scoreMatrix[user][item] = bui[user][item];
        }
        if (scoreMatrix[user][item] < 1) {
            scoreMatrix[user][item] = 1;

        } else if (scoreMatrix[user][item] > 5) {
            scoreMatrix[user][item] = 5;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + scoreMatrix[user][item]);
    }

    public void writeRecommendations(String recomendationPath) {

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
            for (int i = 0; i < nUsers; i++) {
                for (int j = 0; j < nItems; j++) {
                    if (scoreMatrix[i][j] != 0) {
                        bufferedWriter.write(trainingMatrix.getIndexUserSystemDb()[i] + " " + trainingMatrix.getIndexItemSystemDb()[j] + " " + scoreMatrix[i][j]);
                        bufferedWriter.write("\n");
                    }
                }
                bufferedWriter.flush();
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
