/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.ratingprediction;

import java.io.*;
import java.util.Scanner;
import recommender.ratings.*;

/**
 *
 * @author rafaeldaddio
 */
public class RatingPredictionBase {

    protected TrainingMatrix trainingMatrix;
    protected TestMatrix testMatrix;
    protected double[][] predictions;
    protected int nItems, nUsers;
    protected int predictionOption;

    /*     
     predictionOption
     0 = all predictions
     1 = only test    
     */
    public RatingPredictionBase(String testFile, TrainingMatrix trainingMatrix, int predictionOption) {
        this.trainingMatrix = trainingMatrix;
        this.predictionOption = predictionOption;
        if (predictionOption == 1) {
            testMatrix = new TestMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        predictions = new double[nUsers][nItems];
        fillScoreMatrix();
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
                scannerLine.close();
            }
            scannerFile.close();
        } catch (IOException e) {
            System.out.println("Error filling test matrix.");
        }
    }

    private void fillScoreMatrix() {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                predictions[i][j] = 0;
            }
        }
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
                    if (predictions[i][j] != 0) {
                        bufferedWriter.write(trainingMatrix.getIndexUserSystemDb()[i] + " " + trainingMatrix.getIndexItemSystemDb()[j] + " " + predictions[i][j]);
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
}
