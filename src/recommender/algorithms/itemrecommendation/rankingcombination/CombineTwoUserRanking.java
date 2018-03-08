package recommender.algorithms.itemrecommendation.rankingcombination;

import evaluation.*;
import java.io.*;
import java.util.*;

/**
 * Heuristics for combining two user rankings.
 *
 * Combines two rankings for all users, using heuristics based on the union set
 * operation.
 *
 * Options:
 *
 * 1 - Unify the two rankings up to the ranking size and and selects the highest
 * score in case the item is present in both rankings
 *
 * 2 - Unify the two rankings up to the ranking size and and sums the scores in
 * case the item is present in both rankings
 *
 * @author Rafael D'Addio
 */
public class CombineTwoUserRanking extends RankingCombiner {

    private HashMap<Integer, ArrayList<Rating>> userRankings1, userRankings2; //the first, second and final mappings of users with their rankings    
    private final int option; //the combination option

    /**
     * Constructor.
     *
     * @param fileRanking1 the first ranking file
     * @param fileRanking2 the second ranking file
     * @param rankingSize the size of the final ranking
     * @param option combination option: 1 - unify rankings with highest scores;
     * 2 - unify rankings with sum of the scores
     */
    public CombineTwoUserRanking(String fileRanking1, String fileRanking2, int rankingSize, int option) {
        super(rankingSize);

        userRankings1 = new HashMap<>();
        userRankings2 = new HashMap<>();
        readRanking(fileRanking1, userRankings1);
        readRanking(fileRanking2, userRankings2);
        this.option = option;
        balanceRankings();
    }

    /**
     * Reads a file containing the users' rankings, storing them in a mapping.
     *
     * The file must be in the format user \t item \t score
     *
     * @param fileRanking the rankings file
     * @param userRankings the mapping of users with their rankings
     */
    private void readRanking(String fileRanking, HashMap<Integer, ArrayList<Rating>> userRankings) {

        try {

            File file = new File(fileRanking);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                Rating r = new Rating(user, item, rating);
                if (!userRankings.containsKey(user)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    ranking.add(r);
                    userRankings.put(user, ranking);
                } else {
                    userRankings.get(user).add(r);
                }
                scannerLine.close();
            }
            scannerFile.close();
            sort(userRankings);
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }

    /**
     * Balances the rankings by adding missing users in each mapping.
     */
    private void balanceRankings() {
        if (userRankings1.size() > userRankings2.size()) {
            for (Integer key : userRankings1.keySet()) {
                if (!userRankings2.containsKey(key)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    userRankings2.put(key, ranking);
                }

            }
        }
        if (userRankings1.size() < userRankings2.size()) {
            for (Integer key : userRankings2.keySet()) {
                if (!userRankings1.containsKey(key)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    userRankings1.put(key, ranking);
                }
            }
        }
    }

    /**
     * Performs the recommendation method by combining two rankings.
     *
     * For each user, combines its rankings according to the option provided.
     */
    public void recommend() {

        for (Integer key : userRankings1.keySet()) {
            combineRanking(key);
        }

    }

    /**
     * Combines the ranking, either maintaining the highest or the average
     * similarity value when there are item repetitions.
     *
     * @param user the user ID
     */
    private void combineRanking(int user) {
        int m = 0, n = 0;
        HashMap<Integer, Double> partialUserRanking = new HashMap<>();

        //mergesort, adding into the partial final ranking by highest similarity until it reaches the end of one of the two lists
        while (m < userRankings1.get(user).size() && n < userRankings2.get(user).size()) {

            double score1 = userRankings1.get(user).get(m).getRating();
            double score2 = userRankings2.get(user).get(n).getRating();
            if (score1 >= score2) {
                int item = userRankings1.get(user).get(m).getItem();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score1);
                } else {
                    double previousScore = partialUserRanking.get(item);
                    if (option == 1) {
                        double finalValue = calculateHighestValue(score1, previousScore);
                        partialUserRanking.remove(item);
                        partialUserRanking.put(item, finalValue);
                    } else if (option == 2) {
                        double finalValue = calculateSumValue(score1, previousScore);
                        partialUserRanking.put(item, finalValue);
                    }
                }
                m++;
            } else {
                int item = userRankings2.get(user).get(n).getItem();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score2);
                } else {
                    double previousScore = partialUserRanking.get(item);
                    if (option == 1) {
                        double finalValue = calculateHighestValue(score2, previousScore);
                        partialUserRanking.put(item, finalValue);
                    } else if (option == 2) {
                        double finalValue = calculateSumValue(score2, previousScore);
                        partialUserRanking.put(item, finalValue);
                    }
                }
                n++;
            }
        }
        //if there's still items in the first neighborhood
        while (m < userRankings1.get(user).size()) {
            int item = userRankings1.get(user).get(m).getItem();
            double score = userRankings1.get(user).get(m).getRating();
            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                double previousScore = partialUserRanking.get(item);
                if (option == 1) {
                    double finalValue = calculateHighestValue(score, previousScore);
                    partialUserRanking.put(item, finalValue);
                } else if (option == 2) {
                    double finalValue = calculateSumValue(score, previousScore);
                    partialUserRanking.put(item, finalValue);
                }
            }
            m++;
        }
        //if there's still items in the first neighborhood
        while (n < userRankings2.get(user).size()) {
            int item = userRankings2.get(user).get(n).getItem();
            double score = userRankings2.get(user).get(n).getRating();

            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                double previousScore = partialUserRanking.get(item);
                if (option == 1) {
                    double finalValue = calculateHighestValue(score, previousScore);
                    partialUserRanking.put(item, finalValue);
                } else if (option == 2) {
                    double finalValue = calculateSumValue(score, previousScore);
                    partialUserRanking.put(item, finalValue);
                }
            }
            n++;
        }

        produceRanking(user, partialUserRanking);
    }

    /**
     * Calculates the highest score among two values.
     *
     * @param value1 the first similarity value to compare
     * @param value2 the second similarity value to compare
     * @return the highest score value
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
     * Calculates the average score among two values.
     *
     * @param value1 the first similarity value to compare
     * @param value2 the second similarity value to compare
     * @return the average score
     */
    private double calculateSumValue(double value1, double value2) {
        double finalValue = value1 + value2;
        return finalValue;
    }

    /**
     * Sorts and prepares the final user ranking.
     *
     * @param user the user ID
     * @param partialUserRanking the partial final user ranking
     */
    private void produceRanking(int user, HashMap<Integer, Double> partialUserRanking) {
        double big = 10000, small = -10000;
        int id = -1;
        for (int i = 0; i < rankingSize; i++) {
            id = -1;
            for (Map.Entry<Integer, Double> entry : partialUserRanking.entrySet()) {
                int item = entry.getKey();
                double score = entry.getValue();
                if ((score >= small) && (score <= big)) {
                    small = score;
                    id = item;
                }
            }
            small = -20000;
            if (id != -1) {
                big = partialUserRanking.get(id);
                Rating r = new Rating(user, id, big);
                if (!userRankingsFinal.containsKey(user)) {
                    ArrayList<Rating> rank = new ArrayList<>();
                    rank.add(r);
                    userRankingsFinal.put(user, rank);
                } else {
                    userRankingsFinal.get(user).add(r);
                }
                partialUserRanking.remove(id);
            }
        }
    }

}
