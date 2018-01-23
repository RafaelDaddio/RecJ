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

/**
 *
 * @author rafaeldaddio
 */
public class PredictionStackingDoubleAlpha extends PredictionStackingSingleAlpha {

    protected double alpha2;
    protected double lambda2 = 0.015;

    public PredictionStackingDoubleAlpha(String validationMetadata1, String validationMetadata2, String validationTest, String testMetadata1, String testMetadata2, String finalTest, double learningrate) {
        super(validationMetadata1, validationMetadata2, validationTest, testMetadata1, testMetadata2, finalTest, learningrate);
        alpha2 = 0.5;
    }

    @Override
    public void train() {

        long recStarTime = System.currentTimeMillis();
        double eui, rmse, oldRmse = 10000, diffRmse = 1;
        int count = 0, epoch = 0;
        while (diffRmse > 0.00001 && epoch < nEpochs) {
            rmse = 0;
            count = 0;

            for (String key : validationTest.keySet()) {
                eui = validationTest.get(key) - (alpha * validationMetadata1.get(key) + alpha2 * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - alpha2 * ^rui_meta2
                rmse += eui * eui;
                count++;

                setAlpha(alpha + learningrate * (eui * validationMetadata1.get(key) - lambda*alpha)); //update alpha: alpha = alpha + learningrate * (eui * ^rui_meta1)
                setAlpha2(alpha2 + learningrate * (eui * validationMetadata2.get(key) - lambda2*alpha2)); //update alpha: alpha2 = alpha2 + learningrate * (eui * ^rui_meta2)
            }
            rmse = Math.sqrt(rmse / count);
            System.out.println(epoch + ": " + rmse);
            diffRmse = oldRmse - rmse;
            oldRmse = rmse;
            epoch++;

        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Training time: " + (recEndTime - recStarTime));
        System.out.println("Final alphas: " + alpha + "\nFinal alpha2: " + alpha2);

        rmse = 0;
        count = 0;
        for (String key : validationTest.keySet()) {
            eui = validationTest.get(key) - (alpha * validationMetadata1.get(key) + alpha2 * validationMetadata2.get(key)); //eui = rui - (alpha * ^rui_meta1 - alpha2 * ^rui_meta2
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
                double prediction = alpha * testMetadata1.get(key) + alpha2 * testMetadata2.get(key);
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
     * @param alpha2 the alpha2 to set
     */
    public void setAlpha2(double alpha2) {
        this.alpha2 = alpha2;
    }

    /**
     * @param lambda2 the lambda2 to set
     */
    public void setLambda2(double lambda2) {
        this.lambda2 = lambda2;
    }

}
