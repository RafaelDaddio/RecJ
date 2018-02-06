package recommender.ratings;

import java.util.HashMap;

/**
 * Class that implements the test matrix
 * 
 * @author Rafael D'Addio
 */
public class TestMatrix extends RatingMatrix {

    /**
     * Constructor
     *   
     * @param indexUserDbSystem the object containing the mapping of users dataset ids to internal representation
     * @param indexItemDbSystem the object containing the mapping of items dataset ids to internal representation
     * @param indexUserSystemDb the object containing the mapping of users internal representation to dataset ids
     * @param indexItemSystemDb the object containing the mapping of items internal representation to dataset ids
     */
    public TestMatrix(HashMap<Integer, Integer> indexUserDbSystem, HashMap<Integer, Integer> indexItemDbSystem, int[] indexUserSystemDb, int[] indexItemSystemDb) {
        super();
        this.indexUserDbSystem = indexUserDbSystem;
        this.indexItemDbSystem = indexItemDbSystem;
        nItems = indexItemDbSystem.size();
        nUsers = indexUserDbSystem.size();
        this.indexUserSystemDb = indexUserSystemDb;
        this.indexItemSystemDb = indexItemSystemDb;
        ratingMatrix = new double[nUsers][nItems];
        fillPredictionMatrix();
    }

    private void fillPredictionMatrix() {

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                ratingMatrix[i][j] = -1;
            }
        }
    }

}
