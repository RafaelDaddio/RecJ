package recommender.algorithms.itemrecommendation;

import java.io.*;
import recommender.ratings.TrainingMatrix;

/**
 * Class that implements attributes and methods common to all item
 * recommendation algorithms.
 *
 *
 * @author Rafael D'Addio
 */
public class RankingRecommender {

    protected TrainingMatrix trainingMatrix; // the training matrix 
    protected double[][] scoreMatrix; // the matrix with scores to rank
    protected final int nItems, nUsers; //total number of items and users
    protected int rankingSize; // the ranking size
    protected long executionTime; // execution time, measured in milliseconds

    /**
     * Constructor.
     *
     * @param trainingMatrix the object containing the training matrix
     * @param rankingSize the size of the ranking that will be produced
     */
    public RankingRecommender(TrainingMatrix trainingMatrix, int rankingSize) {
        this.trainingMatrix = trainingMatrix;
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        scoreMatrix = new double[nUsers][nItems];
        fillScoreMatrix();
        this.rankingSize = rankingSize;
    }

    /**
     * Fills the score matrix with zero.
     *
     */
    private void fillScoreMatrix() {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                scoreMatrix[i][j] = 0;
            }
        }
    }

    /**
     * Writes the rankings in a file.
     *
     * Sorts scores for each user and writes those with higher scores up to the
     * size of the ranking. Writes the rankings in the format user \t item \t
     * score.
     *
     * @param recomendationFile the file path which the rankings will be written
     */
    public void writeRecommendations(String recomendationFile) {
        double big, small;
        int id;

        try {
            File recomendation = new File(recomendationFile);
            if (!recomendation.exists()) {
                recomendation.createNewFile();
            } else {
                recomendation.delete();
                recomendation.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(recomendation, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (int u = 0; u < nUsers; u++) {
                big = 10000;
                small = -10000;
                id = -1;
                for (int n = 0; n < rankingSize; n++) {
                    id = -1;
                    for (int i = 0; i < nItems; i++) {
                        if (scoreMatrix[u][i] != 0) {
                            if ((scoreMatrix[u][i] >= small) && (scoreMatrix[u][i] <= big)) {
                                small = scoreMatrix[u][i];
                                id = i;
                            }
                        }
                    }
                    small = -20000;
                    if (id != -1) {
                        bufferedWriter.write(trainingMatrix.getIndexUserSystemDb()[u] + "\t" + trainingMatrix.getIndexItemSystemDb()[id] + "\t" + scoreMatrix[u][id]);
                        bufferedWriter.write("\n");

                        big = scoreMatrix[u][id];
                        scoreMatrix[u][id] = 0;
                    }
                }
            }
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing rankings.");
        }
    }

    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return executionTime;
    }

}
