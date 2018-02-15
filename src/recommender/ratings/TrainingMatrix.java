package recommender.ratings;

import java.io.*;
import java.util.*;

/**
 * Class that implements the training matrix.
 *
 * Constructs a training user x item matrix based on internal IDs whose are
 * obtained in a DatabaseMatrix object.
 *
 * @author Rafael D'Addio
 */
public class TrainingMatrix extends RatingMatrix {

    /**
     * Constructor.
     *
     * @param trainingFile the file containing the training user/item pairs
     * @param dbMatrix the DatabaseMatrixobject containing the dataset-internal
     * representation mapping
     */
    public TrainingMatrix(String trainingFile, DatabaseMatrix dbMatrix) {
        super();

        this.indexItemDbSystem = dbMatrix.getIndexItemDbSystem();
        this.indexUserDbSystem = dbMatrix.getIndexUserDbSystem();
        nItems = indexItemDbSystem.size();
        nUsers = indexUserDbSystem.size();
        this.indexItemSystemDb = dbMatrix.getIndexItemSystemDb();
        this.indexUserSystemDb = dbMatrix.getIndexUserSystemDb();

        ratingMatrix = new double[nUsers][nItems];
        populateRatingMatrix(trainingFile);
        createUserLists();
        createItemLists();
    }

    /**
     * Populates the rating matrix with the user-item pairs in the training.
     *
     * @param ratingsFile the training file
     */
    private void populateRatingMatrix(String ratingsFile) {

        try {

            File file = new File(ratingsFile);
            Scanner scannerFile = new Scanner(file);
            scannerFile.useLocale(Locale.US);
            int id = 0;
            
            //reads files and fills matrix using mappings for internal IDs
            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                scannerLine.useLocale(Locale.US);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                int rating = (int) scannerLine.nextFloat();
                ratingMatrix[getIndexUserDbSystem().get(user)][getIndexItemDbSystem().get(item)] = rating;
                id++;
                scannerLine.close();
            }
            scannerFile.close();

            // sets to -1 pairs that do not have ratings
            double rating;
            for (int i = 0; i < nUsers; i++) {
                for (int j = 0; j < nItems; j++) {
                    rating = ratingMatrix[i][j];
                    if (rating < 1) {
                        ratingMatrix[i][j] = -1;
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println("Ratings file NOT FOUND.");
        }

    }

}
