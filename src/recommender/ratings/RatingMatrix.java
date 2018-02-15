package recommender.ratings;

import java.util.*;

/**
 * Superclass that contains generic methods for all rating matrices classes.
 *
 * @author Rafael D'Addio
 */
public class RatingMatrix {

    protected HashMap<Integer, Integer> indexUserDbSystem, indexItemDbSystem; // the item and user dataset - internal ID mappings
    protected int[] indexUserSystemDb, indexItemSystemDb; // the item and user internal - dataset mappings
    protected double[][] ratingMatrix; // the rating matrix
    private ArrayList<ArrayList<Integer>> itemInteractionLists, userInteractionLists; // item and user interaction lists
    protected int nItems, nUsers; //the number of items and users

    /**
     * Constructor.
     *
     * Instantiates objects responsible for converting user and items ids into
     * internal sequential ids, and lists of user/item interactions.
     */
    public RatingMatrix() {
        indexUserDbSystem = new HashMap<Integer, Integer>();
        indexItemDbSystem = new HashMap<Integer, Integer>();
        itemInteractionLists = new ArrayList<>();
        userInteractionLists = new ArrayList<>();

    }

    /**
     * Creates users lists of interactions.
     */
    public void createUserLists() {
        for (int u = 0; u < nUsers; u++) {
            ArrayList<Integer> user = new ArrayList<>();
            for (int i = 0; i < nItems; i++) {
                if (ratingMatrix[u][i] != -1) {
                    user.add(i);
                }
            }
            getUserInteractionLists().add(user);
        }
    }

    /**
     * Creates items lists of interactions.
     */
    public void createItemLists() {
        for (int i = 0; i < nItems; i++) {
            ArrayList<Integer> item = new ArrayList<>();
            for (int u = 0; u < nUsers; u++) {
                if (ratingMatrix[u][i] != -1) {
                    item.add(u);
                }
            }
            getItemInteractionLists().add(item);
        }
    }

    /**
     *
     * @return the rating matrix
     */
    public double[][] getMatrix() {
        return ratingMatrix;
    }

    /**
     * Returns a value for a user/item pair.
     *
     * @param user the user id
     * @param item the item id
     * @return the rating of the pair
     */
    public double getValue(int user, int item) {
        return ratingMatrix[user][item];
    }

    /**
     * Prints the whole matrix on console in the format: userID \t itemID \t
     * ratings
     */
    public void printMatrix() {
        int user;
        int item;
        double rating;
        for (int u = 0; u < nUsers; u++) {
            for (int i = 0; i < nItems; i++) {
                rating = ratingMatrix[u][i];
                user = getIndexUserSystemDb()[u];
                item = getIndexItemSystemDb()[i];
                System.out.println(user + "\t" + item + "\t" + rating);

            }
        }
    }

    /**
     *
     * @return the hashmap containing the conversion of dataset user id to
     * internal id
     */
    public HashMap<Integer, Integer> getIndexUserDbSystem() {
        return indexUserDbSystem;
    }

    /**
     *
     * @return the hashmap containing the conversion of dataset item id to
     * internal id
     */
    public HashMap<Integer, Integer> getIndexItemDbSystem() {
        return indexItemDbSystem;
    }

    /**
     *
     * @return the hashmap containing the conversion of internal user id to
     * dataset id
     */
    public int[] getIndexUserSystemDb() {
        return indexUserSystemDb;
    }

    /**
     *
     * @return the hashmap containing the conversion of internal item to dataset
     * id
     */
    public int[] getIndexItemSystemDb() {
        return indexItemSystemDb;
    }

    /**
     *
     * @return the number of items
     */
    public int getnItems() {
        return nItems;
    }

    /**
     *
     * @return the number of users
     */
    public int getnUsers() {
        return nUsers;
    }

    /**
     * sets a rating for a given user/item pair.
     *
     * @param user the user id
     * @param item the item id
     * @param rating the rating score
     */
    public void setValuematrix(int user, int item, int rating) {
        ratingMatrix[user][item] = rating;
    }

    /**
     *
     * @return the item interactions lists
     */
    public ArrayList<ArrayList<Integer>> getItemInteractionLists() {
        return itemInteractionLists;
    }

    /**
     *
     * @return the user interaction lists
     */
    public ArrayList<ArrayList<Integer>> getUserInteractionLists() {
        return userInteractionLists;
    }

}
