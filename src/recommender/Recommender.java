package recommender;

import evaluation.*;
import recommender.algorithms.itemrecommendation.*;
import recommender.algorithms.ratingprediction.*;
import recommender.metadata.*;
import recommender.ratings.*;
import java.io.*;

/**
 *
 * @author Rafael D'Addio
 *
 * This is the main class for the RecJ framework. For its current version visit:
 * <a
 * href="https://github.com/RafaelDaddio/RecJ">https://github.com/RafaelDaddio/RecJ</a>
 *
 * Here in the main method you should put all coding in order to use the
 * algorithms implemented in this framework. We provide a commented example of a
 * rating prediction ItemAttributeKNN execution for one train/test setting. We
 * do not have data splitting or cross-folding routines implemented yet, you
 * should provide the file paths in the appropriated space.
 */
public class Recommender {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String matrixFile = ""; //Place here the full database file, since it will be used to create the DatabaseMatrix object
        String train = ""; //Place here the train file
        String test = "";  //Place here the test file
        String metadataFile = ""; //Place here the metadata file

        /*database and train setting*/
        DatabaseMatrix dbMatrix = new DatabaseMatrix(matrixFile); //reads the matrixFile and converts all user and items IDs to system indexes
        TrainingMatrix training = new TrainingMatrix(train, dbMatrix); //creates the training matrix

        /*metadata setting*/
        Metadata metadata = new Metadata(metadataFile, dbMatrix.getIndexItemDbSystem()); //reads the metadata file
        EntitySimilarity similarityPearson = new EntitySimilarity(metadata, 1, true); //calculates the similarity: 0 - cosine; 1 - pearson; apply shrinkage: true or false

        /*running a recommender - rating prediction ItemAttributeKNN example (for further examples of how to run each algorithm, refer to its class)*/
        ItemAttributeKNN knn = new ItemAttributeKNN(test, training, 20, 1, similarityPearson.getEntityxEntitySimilarityMatrix()); //ItemAttributeKNN receives: test, k, predictionOption (0: all, 1: only test), and the similarity metrics
        knn.recommender();
        String recommendations = ""; //place here the file where recommendation will be stored
        knn.writeRecommendations(recommendations);

        RatingPredictionMetrics r = new RatingPredictionMetrics(test, recommendations); //Evaluation class, receives the test and the recommendations
        System.out.println(r.RMSE()); //prints the root mean squared error

    }
}
