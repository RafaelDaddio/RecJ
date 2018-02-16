package evaluation;

/**
 * Class that stores a single user-item rating.
 *
 * This class must be used for rating prediction evaluation only. It does not
 * provide mapping from dataset to internal IDs.
 *
 * @author Rafael D'Addio
 */
public class Rating {

    private final int user; // the user ID
    private final int item; // the item ID
    private double rating; // the rating

    /**
     * Constructor.
     *
     * @param user the user ID
     * @param item the item ID
     * @param rating the rating
     */
    public Rating(int user, int item, double rating) {
        this.user = user;
        this.item = item;
        this.rating = rating;
    }

    /**
     *
     * @return the user ID
     */
    public int getUser() {
        return user;
    }

    /**
     *
     * @return the item ID
     */
    public int getItem() {
        return item;
    }

    /**
     *
     * @return the rating
     */
    public double getRating() {
        return rating;
    }

    /**
     *
     * @param rating the rating to be set
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

}
