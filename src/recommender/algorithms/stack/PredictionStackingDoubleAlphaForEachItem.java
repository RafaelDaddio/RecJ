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
public class PredictionStackingDoubleAlphaForEachItem extends PredictionStackingSingleAlphaForEachItem {

    private HashMap<String, Double> alpha2;
    protected double lambda2 = 0.015;

    public PredictionStackingDoubleAlphaForEachItem(String validationMetadata1, String validationMetadata2, String validationTest, String testMetadata1, String testMetadata2, String finalTest, double learningrate) {
        super(validationMetadata1, validationMetadata2, validationTest, testMetadata1, testMetadata2, finalTest, learningrate);
        alpha2 = new HashMap<>();
        fillRandomItemAlpha(alpha2);
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
                double alpha_i1 = alpha.get(item);
                double alpha_i2 = alpha2.get(item);

                eui = validationTest.get(key) - (alpha_i1 * validationMetadata1.get(key) + alpha_i2 * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - alpha2 * ^rui_meta2
                rmse += eui * eui;
                count++;

                alpha_i1 = alpha_i1 + learningrate * (eui * validationMetadata1.get(key) - lambda*alpha_i1); //update alpha: alpha = alpha + learningrate * (eui * ^rui_meta1)
                alpha.put(item, alpha_i1);
                alpha_i2 = alpha_i2 + learningrate * (eui * validationMetadata2.get(key) - lambda2*alpha_i2); //update alpha: alpha = alpha + learningrate * (eui * ^rui_meta1)
                alpha2.put(item, alpha_i2);
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
            double alpha_i2 = alpha2.get(item);
            eui = validationTest.get(key) - (alpha_i * validationMetadata1.get(key) + alpha_i2 * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - (1 - alpha) * ^rui_meta2
            rmse += eui * eui;
            count++;

        }
        rmse = Math.sqrt(rmse / count);
        System.out.println("RMSE: " + rmse);
    }

    @Override
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
                double alpha_i2 = alpha2.get(item);
                double prediction = alpha_i * testMetadata1.get(key) + alpha_i2 * testMetadata2.get(key);

                bufferedWriter.write(user + "\t" + item + "\t" + prediction + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing recommenations.");
        }
    }

    /**
     * @param lambda2 the lambda2 to set
     */
    public void setLambda2(double lambda2) {
        this.lambda2 = lambda2;
    }

}
