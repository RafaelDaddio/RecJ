package recommender.ratings;

import java.io.*;
import java.util.*;

/**
 * Class that converts and stores user and item dataset and internal IDs
 * 
 * This class is required in order to construct training and test matrices. Extends RatingsMatrix class.
 * 
 * @author Rafael D'Addio
 */
public class DatabaseMatrix extends RatingMatrix {

    /**
     * Constructor
     * 
     * @param matrixFile the full dataset file
     */
    public DatabaseMatrix(String matrixFile) {
        super();
        convertUserItemID(matrixFile);
        nItems = indexItemDbSystem.size();
        nUsers = indexUserDbSystem.size();
        indexUserSystemDb = new int[nUsers];
        indexItemSystemDb = new int[nItems];
        fillUserItemIndexArray();
    }

    /**
     * Converts user and items dataset IDs to an internal representation.
     * 
     * @param ratingsFile the full dataset file
     */
    private void convertUserItemID(String ratingsFile) {
        int uCount = 0;
        int iCount = 0;
        try {

            File file = new File(ratingsFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                if (!indexUserDbSystem.containsKey(user)) {
                    getIndexUserDbSystem().put(user, uCount);
                    uCount++;
                }
                if (!indexItemDbSystem.containsKey(item)) {
                    getIndexItemDbSystem().put(item, iCount);
                    iCount++;
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Ratings file NOT FOUND.");
        }
    }

    /**
     * Fills the arrays responsible to convert internal representation to dataset IDs 
     */
    private void fillUserItemIndexArray() {

        for (Integer key : getIndexUserDbSystem().keySet()) {
            indexUserSystemDb[getIndexUserDbSystem().get(key)] = key;

        }

        for (Integer key : getIndexItemDbSystem().keySet()) {
            indexItemSystemDb[getIndexItemDbSystem().get(key)] = key;

        }

    }

}
