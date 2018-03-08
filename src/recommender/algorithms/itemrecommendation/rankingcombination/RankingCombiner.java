package recommender.algorithms.itemrecommendation.rankingcombination;

import evaluation.Rating;
import java.io.*;
import java.util.*;

/**
 * Class that implements attributes and methods common to all ranking
 * combination algorithms.
 *
 * @author Rafael D'Addio
 */
public class RankingCombiner {

    protected HashMap<Integer, ArrayList<Rating>> userRankingsFinal; // the final ranking
    protected int rankingSize; //the size of the ranking

    /**
     * Constructor.
     *
     * @param rankingSize the size of the final ranking
     */
    public RankingCombiner(int rankingSize) {
        userRankingsFinal = new HashMap<>();
        this.rankingSize = rankingSize;

    }

    /**
     * Sorts the user rankings by highest scores.
     *
     * @param userRankings the mapping of users with their rankings
     */
    protected void sort(HashMap<Integer, ArrayList<Rating>> userRankings) {
        for (ArrayList<Rating> ranking : userRankings.values()) {
            Collections.sort(ranking, new Comparator<Rating>() {
                @Override
                public int compare(Rating r1, Rating r2) {
                    return Double.compare(r2.getRating(), r1.getRating());
                }
            });
        }
    }

    /**
     * Writes the rankings in a file.
     *
     * Writes the rankings in the format user \t item \t score.
     *
     * @param recomendationPath the file which the rankings will be written
     */
    public void writeRecommendations(String recomendationPath) {
        int rank;

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
            for (ArrayList<Rating> rankings : userRankingsFinal.values()) {
                rank = rankingSize;
                if (rank > rankings.size()) {
                    rank = rankings.size();
                }
                for (int i = 0; i < rank; i++) {
                    bufferedWriter.write(rankings.get(i).getUser() + "\t" + rankings.get(i).getItem() + "\t" + rankings.get(i).getRating());
                    bufferedWriter.write("\n");
                }

            }
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing rankings.");
        }
    }

}
