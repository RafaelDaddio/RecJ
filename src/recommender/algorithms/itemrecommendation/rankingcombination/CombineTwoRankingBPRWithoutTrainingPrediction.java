package recommender.algorithms.itemrecommendation.rankingcombination;

import evaluation.Rating;
import java.io.*;
import java.util.*;

/**
 * Ranking combination based on Bayesian Personalized Ranking (BPR).
 *
 * This ranking combination based on BPR is based on papers [1,2], which are
 * based on the original paper [3]. The main objective is to learn &alpha
 * weights that represent the relevance in each ranking in combination. In this
 * version, the recommenders' parameter learning is disregarded, considering
 * only the produced rankings and their weights. Also, in this version we
 * consider the known pairs ranking scores as always "1".
 *
 * Literature:
 *
 * [1] Arthur Fortes da Costa and Marcelo G. Manzato: <a
 * href="https://dl.acm.org/citation.cfm?id=2664556"> Ensemble Learning in
 * Recommender Systems: Combining Multiple User Interactions for Ranking
 * Personalization</a>. WebMedia 2014.
 *
 * [2] Marcelo G. Manzato, Marcos A. Domingues, Arthur C. Fortes, Camila V.
 * Sundermann, Rafael M. D'addio, Merley S. Conrado, Solange O. Rezende, and
 * Maria G. Pimentel: <a href="https://dl.acm.org/citation.cfm?id=2975240">
 * Mining unstructured content for recommender systems: an ensemble approach
 * </a>. Inf. Retr. 19, 4 (August 2016).
 *
 * [3] Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars
 * Schmidt-Thieme: <a
 * href="https://dl.acm.org/citation.cfm?id=1795114.1795167">BPR: Bayesian
 * Personalized Ranking from Implicit Feedback </a>. UAI 2009.
 *
 *
 * @author Rafael D'Addio
 */
public class CombineTwoRankingBPRWithoutTrainingPrediction extends RankingCombiner {

    protected HashMap<Integer, HashMap<Integer, Double>> userTrain, userRanking1, userRanking2; // training and the partial rankings, which were produced by a recommender system
    protected double alpha1 = 0.5, alpha2 = 0.5, regRank1 = 0.0025, regRank2 = 0.0025, learnRate = 0.05; //parameters of alpha, regularization and learning rate
    protected int epochs = 30, nInteractions = 0; //parameters of number of epochs, interactions and the ranking size
    protected ArrayList<Integer> usersIDs; //list with users IDs to facilitate random subsampling

    /**
     * Constructor.
     *
     * @param fileTrain the file containing the training
     * @param fileRanking1 the first ranking file
     * @param fileRanking2 the second ranking file
     * @param rankingSize the size of the final ranking
     * @param normalize select true to perform ranking normalization
     */
    public CombineTwoRankingBPRWithoutTrainingPrediction(String fileTrain, String fileRanking1, String fileRanking2, int rankingSize, boolean normalize) {
        super(rankingSize);

        userTrain = new HashMap<>();
        userRanking1 = new HashMap<>();
        userRanking2 = new HashMap<>();
        usersIDs = new ArrayList<>();
        readRankings(fileTrain, userTrain);
        readRankings(fileRanking1, userRanking1);
        readRankings(fileRanking2, userRanking2);
        if (normalize) {
            normalizeRanking(userRanking1);
            normalizeRanking(userRanking2);
        }
        balanceRankings();
        fillRankings();
        createUsersID();
        calculateNumberInteractions();
    }

    /**
     * Creates a list of user IDs to facilitate random sampling.
     */
    private void createUsersID() {
        usersIDs = new ArrayList<>(userTrain.keySet());
    }

    /**
     * Reads a ranking from a file.
     *
     * @param fileRanking the file containing the ranking
     * @param userRankings the structure where the ranking will be stored
     */
    protected final void readRankings(String fileRanking, HashMap<Integer, HashMap<Integer, Double>> userRankings) {
        try {

            File file = new File(fileRanking);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                if (!userRankings.containsKey(user)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    ranking.put(item, rating);
                    userRankings.put(user, ranking);
                } else {
                    userRankings.get(user).put(item, rating);
                }
                scannerLine.close();
            }
            scannerFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }

    /**
     * Normalizes each user's ranking scores with max-min normalization.
     *
     * @param userRankings the rankings to be normalized
     */
    protected final void normalizeRanking(HashMap<Integer, HashMap<Integer, Double>> userRankings) {
        for (HashMap<Integer, Double> ranking : userRankings.values()) {
            double max = 0, min = 1000;
            for (Map.Entry<Integer, Double> entry : ranking.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                }
                if (entry.getValue() < min) {
                    min = entry.getValue();
                }
            }
            min = min - 0.0000001;
            double maxMinusMin = max - min;
            for (Map.Entry<Integer, Double> entry : ranking.entrySet()) {
                double currentRating = entry.getValue();
                entry.setValue((currentRating - min) / maxMinusMin);
            }
        }
    }

    /**
     * Balances the rankings by adding missing users in each mapping.
     */
    private void balanceRankings() {
        if (userRanking1.size() > userRanking2.size()) {
            for (Integer key : userRanking1.keySet()) {
                if (!userRanking2.containsKey(key)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    userRanking2.put(key, ranking);
                }

            }
        }
        if (userRanking2.size() > userRanking1.size()) {
            for (Integer key : userRanking2.keySet()) {
                if (!userRanking1.containsKey(key)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    userRanking1.put(key, ranking);
                }

            }
        }
    }

    /**
     * Makes sure that both rankings have the same items, by adding the item
     * with score zero.
     *
     * This is required for the training step.
     *
     */
    private void fillRankings() {

        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking1.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                if (!userRanking2.get(user.getKey()).containsKey(item.getKey())) {
                    userRanking2.get(user.getKey()).put(item.getKey(), 0.0);
                }
            }
        }

        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking2.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                if (!userRanking1.get(user.getKey()).containsKey(item.getKey())) {
                    userRanking1.get(user.getKey()).put(item.getKey(), 0.0);
                }
            }
        }

    }

    /**
     * Calculates the number of interactions, which is the number of user-item
     * pairs in the training.
     */
    private void calculateNumberInteractions() {
        for (HashMap<Integer, Double> ranking : userTrain.values()) {
            nInteractions += ranking.size();
        }
    }

    /**
     * Samples a single triple of random user, item_i (from training) and item_j
     * (from rankings)
     *
     * @return a triple
     */
    protected String sampleTriple() {
        String triple;

        Random r = new Random();

        int user = usersIDs.get(r.nextInt(usersIDs.size())); // a random user

        ArrayList<Integer> keys_i = new ArrayList<>(userTrain.get(user).keySet());
        int item_i = keys_i.get(r.nextInt(keys_i.size())); //a random item_i (from training)
        ArrayList<Integer> keys_j = new ArrayList<>(userRanking1.get(user).keySet());
        int item_j = keys_j.get(r.nextInt(keys_j.size())); //a random item_j (from ranking)

        triple = user + "," + item_i + "," + item_j;

        return triple;
    }

    /**
     * Computes the loss, which is used to adjust the learning rate.
     *
     * @param sample_triple a list containing the sampled triples for the loss
     * computation
     * @return the loss
     */
    private double computeLoss(ArrayList<String> sample_triple) {
        double loss = 0;
        for (int i = 0; i < sample_triple.size(); i++) {
            String[] triple = sample_triple.get(i).split(",");
            int user = Integer.parseInt(triple[0]);
            int item_j = Integer.parseInt(triple[2]);

            double x_uij = alpha1 * (1 - userRanking1.get(user).get(item_j)) + alpha2 * (1 - userRanking2.get(user).get(item_j)); //x_(uij) = alpha_1 * (1 - ruj_1) + alpha_2 * (1 - ruj_2)
            loss += Math.log(1 / (1 + Math.exp(-x_uij))) - regRank1 * Math.pow(alpha1, 2) - regRank2 * Math.pow(alpha2, 2); // loss = sum( log(1 / (1 + e^(-x_(uij))) - regRank_1 * alpha_1^2 - regRank_2 * alpha_2^2) 
        }

        loss = Math.abs(loss); //convert the loss to positive
        return loss;
    }

    /**
     * Updates the alpha weights given a sample.
     *
     * @param sample a sample of user, item_i and item_j
     */
    private void update(String sample) {
        String[] triple = sample.split(",");
        int user = Integer.parseInt(triple[0]);
        int item_j = Integer.parseInt(triple[2]);

        double x_uij = alpha1 * (1 - userRanking1.get(user).get(item_j)) + alpha2 * (1 - userRanking2.get(user).get(item_j)); //x_(uij) = alpha_1 * (1 - ruj_1) + alpha_2 * (1 - ruj_2)

        alpha1 += learnRate * (1 / (1 + Math.exp(x_uij)) * (1 - userRanking1.get(user).get(item_j)) - regRank1 * alpha1); //alpha_1 += learnRate * (1 / 1 + e^x_(uij) * (1 - ruj_1) - regRank_1 * alpha_1)
        alpha2 += learnRate * (1 / (1 + Math.exp(x_uij)) * (1 - userRanking2.get(user).get(item_j)) - regRank2 * alpha2); //alpha_2 += learnRate * (1 / 1 + e^x_(uij) * (1 - ruj_2) - regRank_2 * alpha_2)

    }

    /**
     * Trains the model.
     */
    public void train() {

        double loss = 0;
        int num_sample_triples = (int) (Math.sqrt(userTrain.size()) * 100); //number of sample triples for the loss computation
        ArrayList<String> sample_triple = new ArrayList<>();

        //stores a list of sampled triples for loss computation
        for (int i = 0; i < num_sample_triples; i++) {
            sample_triple.add(sampleTriple());
        }
        loss = computeLoss(sample_triple); //computes the previous loss

        for (int i = 0; i < epochs; i++) { //for the number of epochs
            for (int j = 0; j < nInteractions; j++) { //for the number of interactions
                String x_uij = sampleTriple(); //samples a triple
                update(x_uij); //updates
            }

            //computes the loss ate the end of a epoch and adjust it accordingly
            double currentLoss = computeLoss(sample_triple);
            if (currentLoss > loss) {
                learnRate *= 0.5;
            } else if (currentLoss < loss) {
                learnRate *= 1.1;
            }
            loss = currentLoss;
            System.out.println("Epoch: " + i + "; Loss: " + loss + "\nAlpha1: " + alpha1 + " Alpha2: " + alpha2);
        }
    }

    /**
     * Combines both rankings applying the trained alphas, then sorts the final
     * ranking.
     *
     */
    public void generateFinalRanking() {
        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking1.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                double score = alpha1 * userRanking1.get(user.getKey()).get(item.getKey()) + alpha2 * userRanking2.get(user.getKey()).get(item.getKey()); // score = alpha_1 * sui_1 + alpha_2 * sui_2
                Rating r = new Rating(user.getKey(), item.getKey(), score);

                if (!userRankingsFinal.containsKey(user.getKey())) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    ranking.add(r);
                    userRankingsFinal.put(user.getKey(), ranking);
                } else {
                    userRankingsFinal.get(user.getKey()).add(r);
                }
            }
        }
        sort(userRankingsFinal);
    }

    /**
     * @param regRank1 the regularization of ranking 1 to set
     */
    public void setRegRank1(double regRank1) {
        this.regRank1 = regRank1;
    }

    /**
     * @param regRank2 the regularization of ranking 2 to set
     */
    public void setRegRank2(double regRank2) {
        this.regRank2 = regRank2;
    }

    /**
     * @param learnRate the learn rate to set
     */
    public void setLearnRate(double learnRate) {
        this.learnRate = learnRate;
    }

    /**
     * @param epochs the number of epochs to set
     */
    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

}
