package evaluation;

import java.util.*;

/**
 * Class responsible to maintain a user ranking.
 *
 * This class must be used for item recommendation evaluation only. It does not
 * provide mapping from dataset to internal IDs.
 *
 * @author Rafael D'Addio
 */
public class UserRanking {

    private final int id; // the user id
    private ArrayList<Rating> ranking; //a list of Ratings, which represent the user ranking

    /**
     * Constructor.
     * 
     * @param id the user id
     */
    public UserRanking(int id) {
        this.id = id;
        ranking = new ArrayList<>();
    }

    /**
     * Adds a Rating to the ranking.
     * 
     * @param r a Rating, which contains user/item IDs and a score
     */
    public void addRanking(Rating r) {
        ranking.add(r);
    }

    /**
     * Sorts the ranking by decreasing order.
     */
    public void sortRanking() {
        Collections.sort(ranking, new Comparator<Rating>() {
            @Override
            public int compare(Rating r1, Rating r2) {
                return Double.compare(r2.getRating(), r1.getRating());
            }

        });
    }

    /**
     * @return the user id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the ranking
     */
    public ArrayList<Rating> getRanking() {
        return ranking;
    }

}
