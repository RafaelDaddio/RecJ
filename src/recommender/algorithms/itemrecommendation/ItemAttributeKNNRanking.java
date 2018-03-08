package recommender.algorithms.itemrecommendation;

import java.util.*;
import java.util.concurrent.*;
import recommender.ratings.*;

/**
 * The attribute-aware item k-nearest neighbors algorithm, adapted for the item
 * recommendation scenario.
 *
 * This algorithm uses item representations to calculate the similarity between
 * them. The score of a (u,i) pair is calculated as the sum of the item's k
 * nearest neighbors, as described in [1].
 *
 * [1] Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars
 * Schmidt-Thieme: <a
 * href="https://dl.acm.org/citation.cfm?id=1795114.1795167">BPR: Bayesian
 * Personalized Ranking from Implicit Feedback </a>. UAI 2009.
 *
 * @author Rafael D'Addio
 */
public class ItemAttributeKNNRanking extends RankingRecommender {

    protected int k; //the number of neighbors
    protected double[][] itemxItemSimilarityMatrix; //the similarity matrix

    /**
     * Constructor.
     *
     * @param trainingMatrix the object containing the training matrix
     * @param k the number of neighbors
     * @param itemxItemSimilarityMatrix the similarity matrix
     * @param rankingSize the size of the ranking that will be produced
     */
    public ItemAttributeKNNRanking(TrainingMatrix trainingMatrix, int k, double itemxItemSimilarityMatrix[][], int rankingSize) {
        super(trainingMatrix, rankingSize);
        this.k = k;
        this.itemxItemSimilarityMatrix = itemxItemSimilarityMatrix;
    }

    /**
     * Iterates over all users and calls the method which will generate scores
     * for the items.
     *
     * Each user is treated as a separate thread. This method is thread safe.
     */
    public void recommender() {
        long recStarTime = System.currentTimeMillis();

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < nUsers; i++) {
            final int idUser = i;
            es.execute(new Runnable() {
                @Override
                public void run() {
                    recommendItems(idUser);
                }
            });
        }
        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Thread problem.");
        }

        long recEndTime = System.currentTimeMillis();
        executionTime = recEndTime - recStarTime;
    }

    /**
     * Iterates over all items and calls the score calculation method for a
     * user-item tuple.
     *
     * @param user the user internal ID
     */
    private void recommendItems(int user) {

        for (int i = 0; i < nItems; i++) {
            if (trainingMatrix.getValue(user, i) == -1) {
                generateUserItemScore(user, i);
            }
        }

    }

    /**
     * Generates a score for a user-item tuple based on the item's k nearest
     * neighbors.
     *
     * @param user the user internal ID
     * @param item the item internal ID
     */
    private void generateUserItemScore(int user, int item) {
        ArrayList<Integer> knn = computeknn(user, item);

        //the final score is a sum of the k-NN's similarities
        if (knn.size() > 0) {
            for (int i = 0; i < knn.size(); i++) {
                scoreMatrix[user][item] += itemxItemSimilarityMatrix[item][knn.get(i)];
            }
        } else {
            scoreMatrix[user][item] = 0;
        }
    }

    /**
     * Calculates the k nearest neighbors of an item that a user evaluated.
     *
     * Ranks items by similarity up to k, and stores as k-NN those that were
     * evaluated by the user
     *
     * @param user the user internal ID
     * @param item the item internal ID
     * @return the k nearest neighbors
     */
    private ArrayList<Integer> computeknn(int user, int item) {
        ArrayList<Integer> knn = new ArrayList<>();
        double big = 2, small = -2;
        int id;

        //obtains the k-NN ordered by similarity
        for (int i = 0; i < k; i++) {
            id = -1;
            for (int j = 0; j < nItems; j++) {
                if ((itemxItemSimilarityMatrix[item][j] >= small) && (itemxItemSimilarityMatrix[item][j] < big)) {
                    id = j;
                    small = itemxItemSimilarityMatrix[item][j];
                }
            }
            small = -2;

            //if there's an item ID, checks wether it was evaluated by the user and if so, adds it to the k-NN
            if (id != -1) {
                big = itemxItemSimilarityMatrix[item][id];
                if (trainingMatrix.getValue(user, id) != -1) {
                    knn.add(id);
                }
            }
        }

        return knn;
    }
}
