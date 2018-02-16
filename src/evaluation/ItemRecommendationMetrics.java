package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Class responsible to perform item recommendation evaluation.
 *
 * In this form of evaluation, the main focus is to check if the system is
 * returning relevant lists (rankings) of suggestions. Thus, a list of the top N
 * items scored by the recommender is evaluated against a ground truth, the test
 * set. The quality of the list is directly proportional to the number of items
 * in common with the test set. Some metrics implemented here, such as the MAP,
 * also consider ranking position as well.
 *
 * Metrics implemented:
 *
 * - Precision At K (prec@K): <a
 * href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#Precision_at_K">Wikipedia
 * page</a>
 * for more details.
 *
 * - Mean Average Precision at K (MAP@K): <a
 * href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#Mean_average_precision">Wikipedia
 * page</a>
 * for more details.
 *
 * @author Rafael D'Addio
 */
public class ItemRecommendationMetrics {

    ArrayList<UserRanking> usersRankings; // a list of user rankings
    HashSet<String> test; // a set containing the the test

    /**
     * Constructor.
     *
     * @param rankingsFile the file containing user rankings, in the form: user
     * \t item \t score
     * @param testFile the test file
     */
    public ItemRecommendationMetrics(String rankingsFile, String testFile) {
        usersRankings = new ArrayList<>();
        test = new HashSet<>();
        populateRankings(rankingsFile);
        populateTest(testFile);
    }

    /**
     * Populates the user rankings.
     *
     * @param rankingFile the file containing user rankings, in the form: user
     * \t item \t score
     */
    private void populateRankings(String rankingFile) {
        int found = 0;
        try {

            File file = new File(rankingFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                Rating r = new Rating(user, item, rating);
                found = 0;
                for (int i = 0; i < usersRankings.size(); i++) {
                    if (usersRankings.get(i).getId() == r.getUser()) {
                        usersRankings.get(i).addRanking(r);
                        found = 1;
                    }
                }
                if (found == 0) {
                    UserRanking ur = new UserRanking(r.getUser());
                    ur.addRanking(r);
                    usersRankings.add(ur);
                }
                scannerLine.close();
            }
            scannerFile.close();
            for (int i = 0; i < usersRankings.size(); i++) {
                usersRankings.get(i).sortRanking();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }

    /**
     * Populates the test set.
     *
     * @param testFile the test file
     */
    private void populateTest(String testFile) {
        try {

            File file = new File(testFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                String tuple = user + "," + item;
                if (!test.contains(tuple)) {
                    test.add(tuple);
                }
                scannerLine.close();
            }
            scannerFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Test file NOT FOUND.");
        }
    }

    /**
     * Prints the ranking on console.
     */
    public void printRankings() {
        for (int i = 0; i < usersRankings.size(); i++) {

            System.out.println("User " + usersRankings.get(i).getId() + ":");
            for (int j = 0; j < usersRankings.get(i).getRanking().size(); j++) {
                System.out.println("Item " + usersRankings.get(i).getRanking().get(j).getItem() + " - score: " + usersRankings.get(i).getRanking().get(j).getRating());
            }

        }
    }

    /**
     * calculates Precision for a given ranking of size K.
     *
     * If the ranking stored is higher than K, it ignores the rest of it. If it
     * is smaller, K is set as the size of the stored ranking.
     *
     * @param rankSize the size K of the ranking
     * @return the precision score
     */
    public double precisionAtK(int rankSize) {
        double prec = 0;

        //for each user ranking, computes its precision
        for (int i = 0; i < usersRankings.size(); i++) {
            double precUser = 0;
            // if K is larger than the size of the ranking, K = ranking size
            if (rankSize > usersRankings.get(i).getRanking().size()) {
                rankSize = usersRankings.get(i).getRanking().size();
            }

            // for each position of the rank, check if it is relevant
            for (int j = 0; j < rankSize; j++) {
                String candidate = usersRankings.get(i).getRanking().get(j).getUser() + "," + usersRankings.get(i).getRanking().get(j).getItem();
                if (test.contains(candidate)) {
                    precUser++;
                }
            }
            precUser = precUser / rankSize; // calculates precision of the user
            prec = prec + precUser;
        }
        prec = prec / usersRankings.size(); //calculates the mean of all users' precisions

        return prec;

    }

    /**
     * Calculates Mean Average Precision for a given ranking of size K.
     *
     * If the ranking stored is higher than K, it ignores the rest of it. If it
     * is smaller, K is set as the size of the stored ranking.
     *
     * @param rankSize the size K of the ranking
     * @return the map score
     */
    public double mapAtK(int rankSize) {
        double map = 0;

        //for each user ranking, computes its MAP
        for (int i = 0; i < usersRankings.size(); i++) {
            double apUser = 0, hits = 0;
            // if K is larger than the size of the ranking, K = ranking size
            if (rankSize > usersRankings.get(i).getRanking().size()) {
                rankSize = usersRankings.get(i).getRanking().size();
            }
            //for each position in the ranking, checks if it is relevant
            //if it is, calculates the precision at that point and sum it with the acumulated precisions
            for (int j = 0; j < rankSize; j++) {
                String candidate = usersRankings.get(i).getRanking().get(j).getUser() + "," + usersRankings.get(i).getRanking().get(j).getItem();
                if (test.contains(candidate)) {
                    hits++;
                    apUser += hits / (j + 1);
                }

            }
            if (hits != 0) {
                apUser = apUser / hits; // calculates the average precision
            } else {
                apUser = 0;
            }
            map += apUser;
        }
        map = map / usersRankings.size(); //calculates the mean average precision

        return map;

    }

}
