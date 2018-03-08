/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.itemrecommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import recommender.ratings.TrainingMatrix;

/**
 * Item Attribute k-NN that accepts two similarity matrices from different
 * metadata.
 *
 * Combines the two neighborhoods produced by the similarity matrices in four
 * different manners:
 *
 * 1 - Intersect the two neighborhoods and compute the average similarity in
 * case the item is present in both k-NN
 *
 * 2 - Intersect the two neighborhoods and selects the highest similarity in
 * case the item is present in both k-NN
 *
 * 3 - Unify the two neighborhoods up to K and compute the average similarity in
 * case the item is present in both k-NN
 *
 * 4 - Unify the two neighborhoods up to K and selects the highest similarity in
 * case the item is present in both k-NN
 *
 * @author Rafael D'Addio
 */
public class ItemAttributeKNNRankingMultiMetadata extends ItemAttributeKNNRanking {

    private double[][] itemxItemSimilarityMatrix2; // the second item similarity matrix
    private final int combination; // the combination option

    /**
     * Constructor.
     *
     * @param trainingMatrix the object containing the training matrix
     * @param k the number of neighbors
     * @param itemxItemSimilarityMatrix the first similarity matrix
     * @param itemxItemSimilarityMatrix2 the second similarity matrix
     * @param rankingSize the size of the ranking that will be produced
     * @param combination the combination option: 1 - intersect and average
     * similarity; 2 - intersect and highest similarity; 3 - unify and average
     * similarity; 4 - unify and highest similarity
     */
    public ItemAttributeKNNRankingMultiMetadata(TrainingMatrix trainingMatrix, int k, double itemxItemSimilarityMatrix[][], double itemxItemSimilarityMatrix2[][], int rankingSize, int combination) {
        super(trainingMatrix, k, itemxItemSimilarityMatrix, rankingSize);

        this.itemxItemSimilarityMatrix2 = itemxItemSimilarityMatrix2;
        this.combination = combination;
    }

    /**
     * Iterates over all users and calls the method which will generate scores
     * for the items.
     *
     * Each user is treated as a separate thread. This method is thread safe.
     */
    @Override
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
            System.out.println("Threading problem.");
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
     * neighbors, which are constructed by the combination of two neighborhoods.
     *
     * @param user the user internal ID
     * @param item the item internal ID
     */
    private void generateUserItemScore(int user, int item) {
        ArrayList<Integer> knnPartial1 = computeknn(user, item, itemxItemSimilarityMatrix);
        ArrayList<Integer> knnPartial2 = computeknn(user, item, itemxItemSimilarityMatrix2);
        HashMap<Integer, Double> knnFinal = new HashMap<>();

        //selects which combination method to employ
        if ((combination == 1) || (combination == 2)) {
            knnFinal = intersectNeighborhood(item, knnPartial1, knnPartial2);
        } else if ((combination == 3) || (combination == 4)) {
            knnFinal = combineNeighborhood(item, knnPartial1, knnPartial2);
        }
        else{
            System.out.println("Error: combination method not valid.");
        }

        //the final score is a sum of the k-NN's similarities
        if (!knnFinal.isEmpty()) {
            for (Map.Entry<Integer, Double> neighbour : knnFinal.entrySet()) {
                scoreMatrix[user][item] += neighbour.getValue();
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
     * @param itemxItemSimilarityMatrix the item similarity matrix
     * @return the k nearest neighbors
     */
    private ArrayList<Integer> computeknn(int user, int item, double[][] itemxItemSimilarityMatrix) {
        ArrayList<Integer> knnPartial = new ArrayList<>();
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
                    knnPartial.add(id);
                }
            }
        }

        return knnPartial;

    }

    /**
     * Intersects two neighborhoods, either maintaining the highest or the
     * average similarity value.
     *
     * @param item the item internal ID
     * @param knnPartial1 the first neighborhood
     * @param knnPartial2 the second neighborhood
     * @param knnFinal the final neighborhood
     */
    private HashMap<Integer, Double> intersectNeighborhood(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2) {

        HashMap<Integer, Double> knnFinal = new HashMap<>();
        //faz a interseção dos dois ranks, obtendo o maior 
        for (int i = 0; i < knnPartial1.size(); i++) {
            for (int j = 0; j < knnPartial2.size(); j++) {
                if (knnPartial1.get(i) == knnPartial2.get(j)) {
                    if (combination == 1) {
                        double finalValue = calculateAverageValue(itemxItemSimilarityMatrix[item][knnPartial1.get(i)], itemxItemSimilarityMatrix2[item][knnPartial2.get(j)]);
                        knnFinal.put(knnPartial1.get(i), finalValue);
                    } else if (combination == 2) {
                        double finalValue = calculateHighestValue(itemxItemSimilarityMatrix[item][knnPartial1.get(i)], itemxItemSimilarityMatrix2[item][knnPartial2.get(j)]);
                        knnFinal.put(knnPartial1.get(i), finalValue);
                    }
                }
            }
        }

        return knnFinal;
    }

    /**
     * Combines two neighborhoods, either maintaining the highest or the average
     * similarity value when there are item repetitions
     *
     * @param item the item internal ID
     * @param knnPartial1 the first neighborhood
     * @param knnPartial2 the second neighborhood
     * @param knnFinal the final neighborhood
     */
    private HashMap<Integer, Double> combineNeighborhood(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2) {
        HashMap<Integer, Double> knnFinal = new HashMap<>();
        int m = 0, n = 0;
        double sim1, sim2;

        //mergesort, adding into the final neighborhood by highest similarity until it reaches the end of one of the two lists
        while (m < knnPartial1.size() && n < knnPartial2.size()) {
            if (knnFinal.size() == k) { //if the size of the final neighborhood reaches k, leaves the while
                break;
            }
            
            sim1 = itemxItemSimilarityMatrix[item][knnPartial1.get(m)];
            sim2 = itemxItemSimilarityMatrix2[item][knnPartial2.get(n)];

            if (sim1 >= sim2) { //if the first neighborhood's item's similarity is higher than the second's
                //adds it to the final rank or combines it in case the item is already in the neighborhood
                if (!knnFinal.containsKey(knnPartial1.get(m))) {
                    knnFinal.put(knnPartial1.get(m), sim1);
                } else {
                    double previousSim = knnFinal.get(knnPartial1.get(m));
                    if (combination == 3) {
                        double finalValue = calculateAverageValue(sim1, previousSim);
                        knnFinal.put(knnPartial1.get(m), finalValue);
                    } else if (combination == 4) {
                        double finalValue = calculateHighestValue(sim1, previousSim);
                        knnFinal.put(knnPartial1.get(m), finalValue);
                    }
                }
                m++;
            } else { //if the second neighborhood's item's similarity is higher than the first's
                //adds it to the final rank or combines it in case the item is already in the neighborhood
                if (!knnFinal.containsKey(knnPartial2.get(n))) {
                    knnFinal.put(knnPartial2.get(n),sim2);
                } else {
                    double previousSim = knnFinal.get(knnPartial2.get(n));
                    if (combination == 3) {
                        double finalValue = calculateAverageValue(sim2, previousSim);
                        knnFinal.put(knnPartial2.get(n), finalValue);
                    } else if (combination == 4) {
                        double finalValue = calculateHighestValue(sim2, previousSim);
                        knnFinal.put(knnPartial2.get(n), finalValue);
                    }
                }
                n++;
            }

        }
        //if there's still items in the first neighborhood
        while (m < knnPartial1.size()) {
            if (knnFinal.size() == k) { //if the size of the final neighborhood reaches k, leaves the while
                break;
            }
            
            sim1 = itemxItemSimilarityMatrix[item][knnPartial1.get(m)];
            if (!knnFinal.containsKey(knnPartial1.get(m))) {
                knnFinal.put(knnPartial1.get(m), sim1);
            } else {
                double previousSim = knnFinal.get(knnPartial1.get(m));
                if (combination == 3) {
                    double finalValue = calculateAverageValue(sim1, previousSim);
                    knnFinal.put(knnPartial1.get(m), finalValue);
                } else if (combination == 4) {
                    double finalValue = calculateHighestValue(sim1, previousSim);
                    knnFinal.put(knnPartial1.get(m), finalValue);
                }
            }
            m++;
        }
        //if there's still items in the first neighborhood
        while (n < knnPartial2.size()) {
            if (knnFinal.size() == k) { //if the size of the final neighborhood reaches k, leaves the while
                break;
            }

            sim2 = itemxItemSimilarityMatrix2[item][knnPartial2.get(n)];
            if (!knnFinal.containsKey(knnPartial2.get(n))) {
                knnFinal.put(knnPartial2.get(n), sim2);
            } else {
                double previousSim = knnFinal.get(knnPartial2.get(n));
                if (combination == 3) {
                    double finalValue = calculateAverageValue(sim2, previousSim);
                    knnFinal.put(knnPartial2.get(n), finalValue);
                } else if (combination == 4) {
                    double finalValue = calculateHighestValue(sim2, previousSim);
                    knnFinal.put(knnPartial2.get(n), finalValue);
                }
            }
            n++;
        }
        return knnFinal;
    }

    /**
     * Calculates the highest similarity among two values.
     *
     * @param value1 the first similarity value to compare
     * @param value2 the second similarity value to compare
     * @return the highest similarity value
     */
    private double calculateHighestValue(double value1, double value2) {
        double finalValue = 0;
        if (value1 >= value2) {
            finalValue = value1;
        } else {
            finalValue = value2;
        }
        return finalValue;
    }

    /**
     * Calculates the average similarity among two values.
     *
     * @param value1 the first similarity value to compare
     * @param value2 the second similarity value to compare
     * @return the average similarity
     */
    private double calculateAverageValue(double value1, double value2) {
        double finalValue = (value1 + value2) / 2;
        return finalValue;
    }
}
