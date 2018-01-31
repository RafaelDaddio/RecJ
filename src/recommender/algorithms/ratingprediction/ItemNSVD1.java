package recommender.algorithms.ratingprediction;

import recommender.metadata.Metadata;
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
public class ItemNSVD1 extends RatingPredictionBase {

    private Metadata x;
    private int factors = 10;
    private double[][] p;
    private double[][] q;
    private double[][] w;
    private double[] b;
    private double[] c;
    private double[] d;
    private double learningRate = 0.01, learningRate2 = 0.01;
    private double lambda = 0.015, lambda2 = 0.015;
    private int nMetadata, nEpochs = 30, n2 = 10;
    private double diffRmse = 100;

    public ItemNSVD1(TrainingMatrix trainingMatrix, String testFile, Metadata x, int predictionOption) {
        super(testFile, trainingMatrix, predictionOption);
        this.x = x;
        nMetadata = x.getMetadataSize();
        instantiateModel();
        train();
    }

    private void instantiateModel() {
        p = new double[nUsers][factors];
        q = new double[nItems][factors];
        w = new double[nMetadata][factors];
        b = new double[nUsers];
        c = new double[nItems];
        d = new double[nItems];

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < factors; j++) {
                p[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nItems; i++) {
            for (int j = 0; j < factors; j++) {
                q[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nMetadata; i++) {
            for (int j = 0; j < factors; j++) {
                w[i][j] = Math.random();
            }
        }
        for (int i = 0; i < nUsers; i++) {
            b[i] = Math.random();
        }
        for (int i = 0; i < nItems; i++) {
            c[i] = Math.random();
        }

        for (int i = 0; i < nItems; i++) {
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
        for (int i = 0; i < nItems; i++) {
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
        for (int i = 0; i < nItems; i++) {
            double e[] = new double[factors];
            for (int k = 0; k < factors; k++) {
                xWTemp = 0;
                for (int l = 0; l < nMetadata; l++) {
                    if (x.getMatrix()[i][l] != 0) {
                        xWTemp += x.getMatrix()[i][l] * w[l][k];
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
        int dividend = 0, countEpoch = 0;
        while (diffRmse > 0.001 && countEpoch < nEpochs) {
            rmse = 0;
            dividend = 0;
            computeQ();
            for (int u = 0; u < trainingMatrix.getUserInteractionLists().size(); u++) {
                for (int i = 0; i < trainingMatrix.getUserInteractionLists().get(u).size(); i++) {
                    int item = trainingMatrix.getUserInteractionLists().get(u).get(i);
                    eui = trainingMatrix.getMatrix()[u][item] - predict(u, item);
                    //System.out.println(rmse+ "+= " + eui + " * " + eui);
                    rmse += eui * eui;

                    dividend++;
                    update(eui, u, item);
                }
            }
            rmse = Math.sqrt(rmse / dividend);
            System.out.println(countEpoch + ": " + rmse);
            diffRmse = oldRmse - rmse;
            oldRmse = rmse;
            for (int i = 0; i < n2; i++) {
                updateW();
            }
            computeQ();
            countEpoch++;
        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Training time: " + (recEndTime - recStarTime));
    }

    public void recommender() {
        long recStarTime = System.currentTimeMillis();
        for (int i = 0; i < nUsers; i++) {
            recommendItems(i);
        }
        long recEndTime = System.currentTimeMillis();
        System.out.println("Test time: " + (recEndTime - recStarTime));
    }

    private void recommendItems(int user) {
        double rmse = 0, eui;
        if (predictionOption == 0) {
            for (int i = 0; i < nItems; i++) {
                if (trainingMatrix.getValue(user, i) == -1) {
                    predictions[user][i] = predict(user, i);
                }
            }
        } else {
            for (int i = 0; i < nItems; i++) {
                if (testMatrix.getValue(user, i) == 1) {
                    predictions[user][i] = predict(user, i);
                }
            }
        }

    }

    /**
     * @param factors the factors to set
     */
    public void setFactors(int factors) {
        this.factors = factors;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * @param learningRate2 the learningRate2 to set
     */
    public void setLearningRate2(double learningRate2) {
        this.learningRate2 = learningRate2;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * @param lambda2 the lambda2 to set
     */
    public void setLambda2(double lambda2) {
        this.lambda2 = lambda2;
    }

    /**
     * @param nEpochs the nEpochs to set
     */
    public void setnEpochs(int nEpochs) {
        this.nEpochs = nEpochs;
    }

    /**
     * @param n2 the n2 to set
     */
    public void setN2(int n2) {
        this.n2 = n2;
    }
}
