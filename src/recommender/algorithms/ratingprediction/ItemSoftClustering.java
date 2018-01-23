/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author rafaeldaddio
 */
public class ItemSoftClustering {

    private TrainingMatrix trainingMatrix;
    private PredictionMatrix predictionMatrix;
    private double[][] scoreMatrix;
    private double[][] itemPersistencyMatrix;
    private double[][] contributionMatrix;
    int nClusters;
    double big;
    double small;
    private double dividendo;
    private double ruj;
    private int nItems;
    private int nUsers;
    private int predictionOption;
    private double[][] bui;
    private double[] bu;
    private double[] bi;
    private double mi;
    private final int regBi = 10;
    private final int regBu = 15;
    private final int iterations = 10;

    /*     
     predictionOption
     0 = all predictions
     1 = only test    
     */
    public ItemSoftClustering(String testFile, TrainingMatrix trainingMatrix, int predictionOption, String persistencyFile, int nClusters) {

        this.trainingMatrix = trainingMatrix;
        this.predictionOption = predictionOption;
        if (predictionOption == 1) {
            predictionMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        scoreMatrix = new double[nUsers][nItems];
        fillScoreMatrix();
        this.nClusters = nClusters;
        itemPersistencyMatrix = new double[nItems][this.nClusters];
        contributionMatrix = new double[nItems][this.nClusters];
        fillPersistencyMatrix(persistencyFile);
        trainBaselines();
    }

    private void fillPredictionMatrix(String testFile) {
        try {
            File file = new File(testFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                predictionMatrix.setValuematrix(trainingMatrix.getIndexUserDbSystem().get(user), trainingMatrix.getIndexItemDbSystem().get(item), 1);
            }
        } catch (IOException e) {
            System.out.println("Erro na leitura do teste.");
        }
    }

    private void fillPersistencyMatrix(String testFile) {
        try {
            File file = new File(testFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int item = scannerLine.nextInt();
                int cluster = scannerLine.nextInt();
                double persistency = scannerLine.nextDouble();
                itemPersistencyMatrix[trainingMatrix.getIndexItemDbSystem().get(item)][cluster] = persistency;
            }
        } catch (IOException e) {
            System.out.println("Erro na leitura das persistencias.");
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
                //System.out.println("Bui de " + i + " " + j + " : " + bui[i][j]);
            }
        }
        //System.out.println("Computou bui");
    }

    public void recommend() {
        long recStarTime = System.currentTimeMillis();
        for (int i = 0; i < nUsers; i++) {
            recommendItems(i);
            //System.out.println("Recomendou para usuário " + i);
        }
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
                if (predictionMatrix.getValue(user, i) == 1) {
                    generateUserItemScore(user, i);
                }
            }
        }
    }

    private void generateUserItemScore(int user, int item) {
        double sumContributions = 0;
        double sumPersistencies = 0;

        for (int i = 0; i < nClusters; i++) {
            sumContributions += itemPersistencyMatrix[item][i] * computeContribution(i, user, item);
            sumPersistencies += itemPersistencyMatrix[item][i];
        }
        if (sumContributions == 0) {
            scoreMatrix[user][item] = bui[user][item];
        } else {
            scoreMatrix[user][item] = bui[user][item] + (sumContributions / sumPersistencies);
        }
    }

    private double computeContribution(int cluster, int user, int item) {
        double contribution = 0;
        double sumPonderedRUJ = 0;
        double sumPersistencies = 0;

        ArrayList<Integer> relevantItems = new ArrayList<>();

        for (int i = 0; i < nItems; i++) {
            if (item != i) {
                if (trainingMatrix.getValue(user, i) > 0) {
                    relevantItems.add(i);
                }
            }
        }

        for (int i = 0; i < relevantItems.size(); i++) {
            sumPonderedRUJ += itemPersistencyMatrix[i][cluster] * (trainingMatrix.getValue(user, relevantItems.get(i)) - bui[user][relevantItems.get(i)]);
            sumPersistencies += itemPersistencyMatrix[i][cluster];
        }

        if (sumPersistencies == 0) {
            contribution = 0;
        } else {
            contribution = sumPonderedRUJ / sumPersistencies;
        }
        return contribution;

    }

    public void writeRecommendations(String recomendationPath) {

        try {
            File recomendation = new File(recomendationPath);
            if (!recomendation.exists()) {
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
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error.");
        }

    }

}
