package recommender.algorithms.ratingprediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import recommender.metadata.Metadata;
import recommender.ratings.PredictionMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author rafaeldaddio
 */
/*----------

 Usage example: ItemNSVD1
                
 DatabaseMatrix dbMatrix = new DatabaseMatrix(matrixFile); //le base de dados completa e seta matriz
 TrainingMatrix training = new TrainingMatrix(train, dbMatrix);
 String metadataFile = "/home/rafaeldaddio/Documents/ExperimentoSAC/Baseline/item_genre2.txt"; //le representações de item e/ou usuário
 Metadata metadata = new Metadata(metadataFile, dbMatrix.getIndexItemDbSystem());
 ItemNSVD1 i = new ItemNSVD1(training, test, metadata, 1, k); //k = numero de fatores
 i.recommender();
 i.writeRecommendations("/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/recItemNSVD1.dat");
 RatingPredictionMetrics eval = new RatingPredictionMetrics(test, "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/recItemNSVD1.dat");
 System.out.println(eval.RMSE());
        
 ------------*/

public class ItemNSVD1 {

    private int factors = 10;
    private TrainingMatrix rui;
    private PredictionMatrix testMatrix;
    private Metadata x;
    private double[][] predictions;
    private double[][] p;
    private double[][] q;
    private double[][] w;
    private double[] b;
    private double[] c;
    private double[] d;
    private double learningRate = 0.01, learningRate2 = 0.01;
    private double lambda = 0.015, lambda2 = 0.015;
    private int nUser, nItem, nMetadata, nEpochs = 30, n2 = 10;
    private double diffRmse = 100;

    public ItemNSVD1(TrainingMatrix trainingMatrix, String testFile, Metadata x, int predictionOption, int factors, int learningRate, int learningRate2, int lambda, int lambda2) {
        this.rui = trainingMatrix;        
        nUser = rui.getnUsers();
        nItem = rui.getnItems();
        if (predictionOption == 1) {
            testMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        predictions = new double[nUser][nItem];
        this.factors = factors;
        this.learningRate = learningRate;
        this.learningRate2 = learningRate2;
        this.lambda = lambda;
        this.lambda = lambda2;
        this.x = x;
        nMetadata = x.getMetadataSize();
        instantiateModel();
        train();
    }

    public ItemNSVD1(TrainingMatrix trainingMatrix, String testFile, Metadata x, int predictionOption) {
        this.rui = trainingMatrix;        
        nUser = rui.getnUsers();
        nItem = rui.getnItems();
        if (predictionOption == 1) {
            testMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        predictions = new double[nUser][nItem];
        this.x = x;
        nMetadata = x.getMetadataSize();
        instantiateModel();
        train();
    }
    
    public ItemNSVD1(TrainingMatrix trainingMatrix, String testFile, Metadata x, int predictionOption, int factors) {
        this.rui = trainingMatrix;        
        nUser = rui.getnUsers();
        nItem = rui.getnItems();
        if (predictionOption == 1) {
            testMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
            fillPredictionMatrix(testFile);
        }
        predictions = new double[nUser][nItem];
        this.factors = factors;
        this.x = x;
        nMetadata = x.getMetadataSize();
        instantiateModel();
        train();
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
                double rating = scannerLine.nextDouble();
                testMatrix.setValuematrix(rui.getIndexUserDbSystem().get(user), rui.getIndexItemDbSystem().get(item), 1);
            }
        } catch (IOException e) {
            System.out.println("Test file NOT FOUND.");
        }
    }

    private void instantiateModel() {
        p = new double[nUser][factors];
        q = new double[nItem][factors];
        w = new double[nMetadata][factors];
        b = new double[nUser];
        c = new double[nItem];
        d = new double[nItem];

        for (int i = 0; i < nUser; i++) {
            for (int j = 0; j < factors; j++) {
                p[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nItem; i++) {
            for (int j = 0; j < factors; j++) {
                q[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nMetadata; i++) {
            for (int j = 0; j < factors; j++) {
                w[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nUser; i++) {
            b[i] = Math.random();
        }
        for (int i = 0; i < nItem; i++) {
            c[i] = Math.random();
        }

        for (int i = 0; i < nItem; i++) {
            for (int l = 0; l < nMetadata; l++) {
                d[i] += x.getMatrix()[i][l] * x.getMatrix()[i][l];
            }
            if (d[i] != 0) {
                d[i] = 1 / d[i];
            } else {
                d[i] = 1;
            }
        }

    }

    private void computeQ() {
        double qTemp = 0;
        for (int i = 0; i < nItem; i++) {
            for (int k = 0; k < factors; k++) {
                qTemp = 0;
                for (int l = 0; l < nMetadata; l++) {
                    if (x.getMatrix()[i][l] != 0) {
                        qTemp += x.getMatrix()[i][l] * w[l][k];
                    }
                }
                q[i][k] = qTemp;
            }
        }
    }

    private double predict(int u, int i) {
        double puTqi = 0;
        for (int k = 0; k < factors; k++) {
            puTqi += p[u][k] * q[i][k];
        }

        return b[u] + c[i] + puTqi;
    }

    private void update(double eui, int u, int i) {
        double b1[] = new double[factors];
        for (int k = 0; k < factors; k++) {
            b1[k] = p[u][k];
            p[u][k] = p[u][k] + learningRate * (eui * q[i][k] - lambda * p[u][k]);
            q[i][k] = q[i][k] + learningRate * (eui * b1[k] - lambda * q[i][k]);
        }
        b[u] = b[u] + learningRate * (eui - lambda * b[u]);
        c[i] = c[i] + learningRate * (eui - lambda * c[i]);

    }

    private void updateW() {
        double xWTemp = 0;
        for (int i = 0; i < nItem; i++) {
            double e[] = new double[factors];
            for (int k = 0; k < factors; k++) {
                xWTemp = 0;
                for (int l = 0; l < nMetadata; l++) {                    
                    if (x.getMatrix()[i][l] != 0) {
                        xWTemp +=  x.getMatrix()[i][l] * w[l][k];
                    }
                }
                e[k] = q[i][k] - xWTemp;
            }
            for (int l = 0; l < nMetadata; l++) {
                for (int k = 0; k < factors; k++) {
                    double DXiE = d[i] * x.getMatrix()[i][l] * e[k];
                    w[l][k] += learningRate2 * (DXiE - lambda2 * w[l][k]);
                    }
            }
        }
    }

    private void train() {
        long recStarTime = System.currentTimeMillis();
        double eui, rmse, oldRmse = 10000;
        int count = 0, count2 = 0;
        while (diffRmse > 0.001 && count2 < nEpochs) {
            rmse = 0;
            count = 0;
            computeQ();
            for (int u = 0; u < rui.getUserInteractionLists().size(); u++) {
                for (int i = 0; i < rui.getUserInteractionLists().get(u).size(); i++) {
                    int item = rui.getUserInteractionLists().get(u).get(i);
                    eui = rui.getMatrix()[u][item] - predict(u, item);
                    //System.out.println(rmse+ "+= " + eui + " * " + eui);
                    rmse += eui * eui;
                    
                    count++;
                    update(eui, u, item);
                }
            }
            rmse = Math.sqrt(rmse / count);
            System.out.println(count2 + ": " + rmse);
            diffRmse = oldRmse - rmse;
            oldRmse = rmse;
            for (int i = 0; i < n2; i++) {
                updateW();
            }
            computeQ();
            count2++;
        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Training time: " + (recEndTime - recStarTime));
    }

    public void recommender() {
        long recStarTime = System.currentTimeMillis();
        for (int i = 0; i < nUser; i++) {
            recommendItems(i);
            //System.out.println("Recomendou para usuário " + i);
        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Test time: " + (recEndTime - recStarTime));
    }

    private void recommendItems(int user) {
        double rmse = 0, eui;
        for (int i = 0; i < nItem; i++) {
            if (testMatrix.getValue(user, i) == 1) {
                predictions[user][i] = predict(user, i);
            }
        }

    }

    public void writeRecommendations(String recomendationPath) {

        try {
            File recomendation = new File(recomendationPath);
            if (!recomendation.exists()) {
                recomendation.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(recomendation, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (int u = 0; u < nUser; u++) {
                for (int i = 0; i < nItem; i++) {
                    if (predictions[u][i] != 0) {
                        bufferedWriter.write(rui.getIndexUserSystemDb()[u] + " " + rui.getIndexItemSystemDb()[i] + " " + predictions[u][i]);
                        bufferedWriter.write("\n");
                    }
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing recommenations.");
        }

    }
}
