/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.stack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author rafaeldaddio
 */
public class PredictionStackingSingleAlphaForEachItem extends PredictionStackingSingleAlpha {

    protected HashMap<String, Double> alpha;

    public PredictionStackingSingleAlphaForEachItem(String validationMetadata1, String validationMetadata2, String validationTest, String testMetadata1, String testMetadata2, String finalTest, double learningrate) {
        super(validationMetadata1, validationMetadata2, validationTest, testMetadata1, testMetadata2, finalTest, learningrate);
        alpha = new HashMap<>();
        fillRandomItemAlpha(alpha);
    }

    protected void fillRandomItemAlpha(HashMap<String, Double> alpha) {
        for (String key : finalTest.keySet()) {
            String[] split = key.split(",");
            String item = split[1];
            double alpha_i = 0.5;
            alpha.put(item, alpha_i);
        }
        for (String key : validationTest.keySet()) {
            String[] split = key.split(",");
            String item = split[1];
            double alpha_i = 0.5;
            if (!alpha.containsKey(item)) {
                alpha.put(item, alpha_i);
            }
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
                String[] split = key.split(",");
                String item = split[1];
                double alpha_i = alpha.get(item);

                eui = validationTest.get(key) - (alpha_i * validationMetadata1.get(key) + (1 - alpha_i) * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - (1 - alpha) * ^rui_meta2
                rmse += eui * eui;
                count++;

                alpha_i = alpha_i - learningrate * (eui * (validationMetadata2.get(key) - validationMetadata1.get(key) + lambda*alpha_i)); //update alpha: alpha = alpha - learningrate * (eui * (^rui_meta2 - ^rui_meta1))
                alpha.put(item, alpha_i);
            }
            rmse = Math.sqrt(rmse / count);
            System.out.println(epoch + ": " + rmse);
            diffRmse = oldRmse - rmse;
            oldRmse = rmse;
            epoch++;

        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Training time: " + (recEndTime - recStarTime));
        //System.out.println("Final alpha: " + alpha);

        rmse = 0;
        count = 0;
        for (String key : validationTest.keySet()) {
            String[] split = key.split(",");
            String item = split[1];
            double alpha_i = alpha.get(item);
            eui = validationTest.get(key) - (alpha_i * validationMetadata1.get(key) + (1 - alpha_i) * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - (1 - alpha) * ^rui_meta2
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
                String[] split = key.split(",");
                String user = split[0];
                String item = split[1];
                double alpha_i = alpha.get(item);
                double prediction = alpha_i * testMetadata1.get(key) + (1 - alpha_i) * testMetadata2.get(key);

                bufferedWriter.write(user + "\t" + item + "\t" + prediction + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing recommenations.");
        }
    }

}
