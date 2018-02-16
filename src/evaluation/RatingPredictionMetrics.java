package evaluation;

import java.io.*;
import java.util.*;

/**
 * Class responsible to perform rating prediction evaluation.
 *
 * In this form of evaluation, the ratings predicted by a recommender are
 * compared against a ground truth, the test set. The more similar to the ground
 * truth, the less error a system may have.
 *
 * Metrics implemented:
 *
 * - Mean Absolute Error(MAE): <a
 * href="https://en.wikipedia.org/wiki/Mean_absolute_error">Wikipedia page</a>
 * for more details.
 *
 * - Root Mean Square Error(RMSE): <a
 * href="https://en.wikipedia.org/wiki/Root-mean-square_deviation">Wikipedia
 * page</a> for more details.
 *
 * @author Rafael D'Addio
 */
public class RatingPredictionMetrics {

    private ArrayList<Rating> test; // list of Rating objects with the test pairs
    private ArrayList<Rating> prediction; // list of Rating objects with the prediction pairs

    /**
     * Constructor.
     *
     * @param testFile the file with the test pairs
     * @param predictionFile the file with the prediction pairs
     */
    public RatingPredictionMetrics(String testFile, String predictionFile) {
        test = new ArrayList<>();
        prediction = new ArrayList<>();
        populateArrayList(testFile, test);
        populateArrayList(predictionFile, prediction);

    }

    /**
     * Populates either the test of the prediction lists by reading its file.
     *
     * @param fileString the prediction or test file
     * @param array the prediction or test list
     */
    private void populateArrayList(String fileString, ArrayList<Rating> array) {
        try {

            File file = new File(fileString);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                Rating r = new Rating(user, item, rating);
                array.add(r);
                scannerLine.close();
            }
            scannerFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Test or Prediction file NOT FOUND.");
        }
    }

    /**
     * Calculates the root-mean-square error between prediction and test.
     * 
     * @return the RMSE score
     */
    public double RMSE() {
        double rmse;
        double sum = 0;
        int count = 0;
        
        //for each pair in the test, finds its equivalent in the prediction and computes the square error
        for (int i = 0; i < test.size(); i++) {
            for (int j = 0; j < prediction.size(); j++) {
                if ((test.get(i).getUser() == prediction.get(j).getUser()) && (test.get(i).getItem() == prediction.get(j).getItem())) {
                    sum += (test.get(i).getRating() - prediction.get(j).getRating()) * (test.get(i).getRating() - prediction.get(j).getRating());
                    count++;
                    break;
                }
            }
        }
        rmse = sum / count; //calculates mean
        rmse = Math.sqrt(rmse); // takes the square root
        return rmse;
    }

    /**
     * Calculates the mean absolute error between prediction and test.
     * 
     * @return the MAE score
     */
    public double MAE() {
        double mae = 0, sum = 0;
        int count = 0;
        
        //for each pair in the test, finds its equivalent in the prediction and computes the absolute error
        for (int i = 0; i < test.size(); i++) {
            for (int j = 0; j < prediction.size(); j++) {
                if ((test.get(i).getUser() == prediction.get(j).getUser()) && (test.get(i).getItem() == prediction.get(j).getItem())) {
                    sum += Math.abs(test.get(i).getRating() - prediction.get(j).getRating());
                    count++;
                    break;
                }
            }
        }
        mae = sum / count; //calculates mean
        return mae;
    }

}
