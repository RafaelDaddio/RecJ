package recommender.algorithms.itemrecommendation.rankingcombination;

import java.util.*;

/**
 * Ranking combination based on Bayesian Personalized Ranking (BPR).
 *
 * This ranking combination based on BPR is based on papers [1,2], which are
 * based on the original paper [3]. The main objective is to learn &alpha
 * weights that represent the relevance in each ranking in combination. In this
 * version, the recommenders' parameter learning is disregarded, considering
 * only the produced rankings and their weights. This version considers the
 * known pairs predictions (scores) in the calculation.
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
public class CombineTwoRankingBPRWithTrainingPrediction extends CombineTwoRankingBPRWithoutTrainingPrediction {

    private HashMap<Integer, HashMap<Integer, Double>> userTrain2;

    /**
     * Constructor.
     *
     * @param fileTrain the file containing the first training
     * @param fileTrain2 the file containing the second training
     * @param fileRanking1 the first ranking file
     * @param fileRanking2 the second ranking file
     * @param rankingSize the size of the final ranking
     * @param normalize select true to perform ranking normalization
     */
    public CombineTwoRankingBPRWithTrainingPrediction(String fileTrain, String fileTrain2, String fileRanking1, String fileRanking2, int rankingSize, boolean normalize) {
        super(fileTrain, fileRanking1, fileRanking2, rankingSize, normalize);
        userTrain2 = new HashMap<>();
        readRankings(fileTrain2, userTrain2);
        if (normalize) {
            normalizeRanking(userTrain);
            normalizeRanking(userTrain2);
        }
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
            int item_i = Integer.parseInt(triple[1]);
            int item_j = Integer.parseInt(triple[2]);

            double x_uij = (alpha1 * userTrain.get(user).get(item_i) + alpha2 * userTrain2.get(user).get(item_i)) - (alpha1 * userRanking1.get(user).get(item_j) + alpha2 * userRanking2.get(user).get(item_j));
            loss += Math.log(1 / (1 + Math.exp(-x_uij))) - regRank1 * Math.pow(alpha1, 2) - regRank2 * Math.pow(alpha2, 2);
        }

        loss = loss * (-1);
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
        int item_i = Integer.parseInt(triple[1]);
        int item_j = Integer.parseInt(triple[2]);

        double x_uij = alpha1 * (userTrain.get(user).get(item_i) - userRanking1.get(user).get(item_j)) + alpha2 * (userTrain2.get(user).get(item_i) - userRanking2.get(user).get(item_j)); //x_(uij) = alpha_1 * (rui_1 - ruj_1) + alpha_2 * (rui_2 - ruj_2)

        alpha1 += learnRate * (1 / (1 + Math.exp(x_uij)) * (userTrain.get(user).get(item_i) - userRanking1.get(user).get(item_j)) - regRank1 * alpha1); //alpha_1 += learnRate * (1 / 1 + e^x_(uij) * (rui_1 - ruj_1) - regRank_1 * alpha_1)
        alpha2 += learnRate * (1 / (1 + Math.exp(x_uij)) * (userTrain2.get(user).get(item_i) - userRanking2.get(user).get(item_j)) - regRank2 * alpha2); //alpha_2 += learnRate * (1 / 1 + e^x_(uij) * (rui_2 - ruj_2) - regRank_2 * alpha_2)

    }

    /**
     * Trains the model.
     */
    @Override
    public void train() {

        double loss = 0;
        int num_sample_triples = (int) (Math.sqrt(userTrain.size()) * 100);
        ArrayList<String> sample_triple = new ArrayList<>();

        for (int i = 0; i < num_sample_triples; i++) {
            sample_triple.add(sampleTriple());
        }
        loss = computeLoss(sample_triple);

        for (int i = 0; i < epochs; i++) {
            for (int j = 0; j < nInteractions; j++) {
                String x_uij = sampleTriple();
                update(x_uij);
            }
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
}
