package recommender.ratings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Class that implements the training matrix
 * 
 * @author Rafael D'Addio
 */
public class TrainingMatrix extends RatingMatrix {

    /**
     *
     * @param trainingFile the file containing the training user/item pairs
     * @param dbMatrix the DatabaseMatrixobject containing the dataset->internal representation mapping
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
     * Populates the rating matrix with the user-item pairs in the training
     * 
     * @param ratingsFile the training file
     */
    private void populateRatingMatrix(String ratingsFile) {

        try {

            File file = new File(ratingsFile);
            Scanner scannerFile = new Scanner(file);
            scannerFile.useLocale(Locale.US);
            int id = 0;

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                scannerLine.useLocale(Locale.US);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                int rating = (int) scannerLine.nextFloat();
                ratingMatrix[getIndexUserDbSystem().get(user)][getIndexItemDbSystem().get(item)] = rating;
                id++;
            }
            System.out.println(id +" lines");
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
