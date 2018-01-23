/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.stack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class PredictionStackingSingleAlpha {

    protected HashMap<String, Double> validationMetadata1, validationMetadata2, validationTest;
    protected HashMap<String, Double> testMetadata1, testMetadata2, finalTest;
    protected double alpha, learningrate, nEpochs = 30, lambda = 0.015;

    public PredictionStackingSingleAlpha(String validationMetadata1, String validationMetadata2, String validationTest, String testMetadata1, String testMetadata2, String finalTest, double learningrate) {
        this.validationMetadata1 = new HashMap<>();
        this.validationMetadata2 = new HashMap<>();
        this.validationTest = new HashMap<>();
        this.testMetadata1 = new HashMap<>();
        this.testMetadata2 = new HashMap<>();
        this.finalTest = new HashMap<>();
        populateHashMap(validationMetadata1, this.validationMetadata1);
        populateHashMap(validationMetadata2, this.validationMetadata2);
        populateHashMap(validationTest, this.validationTest);
        populateHashMap(testMetadata1, this.testMetadata1);
        populateHashMap(testMetadata2, this.testMetadata2);
        populateHashMap(finalTest, this.finalTest);
        this.learningrate = learningrate;
        alpha = 0.5;
    }

    private void populateHashMap(String fileString, HashMap<String, Double> array) {
        try {

            File file = new File(fileString);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                String ui = user + "," + item;
                array.put(ui, rating);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Test or Prediction file NOT FOUND.");
        }
    }

    public void train() {

        long recStarTime = System.currentTimeMillis();
        double eui, rmse, oldRmse = 10000, diffRmse = 1;
        int count = 0, epoch = 0;
        while (diffRmse > 0.00001 && epoch < nEpochs) {
            rmse = 0;
            count = 0;

            for (String key : validationTest.keySet()) {
                eui = validationTest.get(key) - (alpha * validationMetadata1.get(key) + (1 - alpha) * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - (1 - alpha) * ^rui_meta2
                rmse += eui * eui;
                count++;

                setAlpha(alpha - learningrate * (eui * (validationMetadata2.get(key) - validationMetadata1.get(key)) + lambda*alpha)); //update alpha: alpha = alpha - learningrate * (eui * (^rui_meta2 - ^rui_meta1))
            }
            rmse = Math.sqrt(rmse / count);
            System.out.println(epoch + ": " + rmse);
            diffRmse = oldRmse - rmse;
            oldRmse = rmse;
            epoch++;

        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Training time: " + (recEndTime - recStarTime));
        System.out.println("Final alpha: " + alpha);

        rmse = 0;
        count = 0;
        for (String key : validationTest.keySet()) {
            eui = validationTest.get(key) - (alpha * validationMetadata1.get(key) + (1 - alpha) * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - (1 - alpha) * ^rui_meta2
            rmse += eui * eui;
            count++;

        }
        rmse = Math.sqrt(rmse / count);
        System.out.println("RMSE: " + rmse);
    }

    public void writePredictions(String recomendationPath) {
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
            for (String key : finalTest.keySet()) {
                double prediction = alpha * testMetadata1.get(key) + (1 - alpha) * testMetadata2.get(key);
                String[] split = key.split(",");
                String user = split[0];
                String item = split[1];
                bufferedWriter.write(user + "\t" + item + "\t" + prediction + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing recommenations.");
        }
    }

    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @param learningrate the learningrate to set
     */
    public void setLearningrate(double learningrate) {
        this.learningrate = learningrate;
    }

    /**
     * @param nEpochs the nEpochs to set
     */
    public void setnEpochs(double nEpochs) {
        this.nEpochs = nEpochs;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

}
