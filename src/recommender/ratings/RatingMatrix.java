package recommender.ratings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rafaeldaddio
 */
public class RatingMatrix {

    protected HashMap<Integer, Integer> indexUserDbSystem;
    protected HashMap<Integer, Integer> indexItemDbSystem;
    protected int[] indexUserSystemDb;
    protected int[] indexItemSystemDb;
    protected double[][] ratingMatrix;
    private ArrayList<ArrayList<Integer>> itemInteractionLists;
    private ArrayList<ArrayList<Integer>> userInteractionLists;
    protected int nItems;
    protected int nUsers;

    public RatingMatrix() {
        indexUserDbSystem = new HashMap<Integer, Integer>();
        indexItemDbSystem = new HashMap<Integer, Integer>();
        itemInteractionLists = new ArrayList<>();
        userInteractionLists = new ArrayList<>();

    }

    public void createUserLists() {
        for (int u = 0; u < nUsers; u++) {
            ArrayList<Integer> user = new ArrayList<>();
            for (int i = 0; i < nItems; i++) {
                if(ratingMatrix[u][i] != -1){
                    user.add(i);
                }
            }
            getUserInteractionLists().add(user);
        }
    }
    
    public void createItemLists() {
        for (int i = 0; i < nItems; i++) {
            ArrayList<Integer> item = new ArrayList<>();
            for (int u = 0; u < nUsers; u++) {
                if(ratingMatrix[u][i] != -1){
                    item.add(u);
                }
            }
            getItemInteractionLists().add(item);
        }
    }

    public double[][] getMatrix() {
        return ratingMatrix;
    }

    public double getValue(int user, int item) {
        return ratingMatrix[user][item];
    }

    public void printMatrix() {
        int user;
        int item;
        double rating;
        for (int u = 0; u < nUsers; u++) {
            for (int i = 0; i < nItems; i++) {
                rating = ratingMatrix[u][i];
                user = getIndexUserSystemDb()[u];
                item = getIndexItemSystemDb()[i];
                System.out.println(user + " " + item + " " + rating);

            }
        }
    }

    public HashMap<Integer, Integer> getIndexUserDbSystem() {
        return indexUserDbSystem;
    }

    public HashMap<Integer, Integer> getIndexItemDbSystem() {
        return indexItemDbSystem;
    }

    public int[] getIndexUserSystemDb() {
        return indexUserSystemDb;
    }

    public int[] getIndexItemSystemDb() {
        return indexItemSystemDb;
    }

    public int getnItems() {
        return nItems;
    }

    public int getnUsers() {
        return nUsers;
    }

    public void setValuematrix(int user, int item, int rating) {
        ratingMatrix[user][item] = rating;
    }

    public ArrayList<ArrayList<Integer>> getItemInteractionLists() {
        return itemInteractionLists;
    }

    public ArrayList<ArrayList<Integer>> getUserInteractionLists() {
        return userInteractionLists;
    }

}
