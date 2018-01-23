/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class RatingPredictionMetrics {
    
    ArrayList<Rating> test;
    ArrayList<Rating> prediction;
    
    public RatingPredictionMetrics(String testFile, String predictionFile){
        test = new ArrayList<>();
        prediction = new ArrayList<>();
        populateArrayList(testFile, test);        
        populateArrayList(predictionFile, prediction);
        
    }
    
    private void populateArrayList(String fileString, ArrayList<Rating> array){
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
            }
        } catch (FileNotFoundException e) {
            System.out.println("Test or Prediction file NOT FOUND.");
        }        
    }
    
    public double RMSE(){
        double rmse;
        double sum = 0;
        int count = 0;
        for(int i = 0; i < test.size(); i++){
            for(int j = 0; j < prediction.size(); j++){
                if((test.get(i).getUser() == prediction.get(j).getUser()) && (test.get(i).getItem() == prediction.get(j).getItem())){
                    sum += (test.get(i).getRating() - prediction.get(j).getRating())*(test.get(i).getRating() - prediction.get(j).getRating());
                    count++;
                    break;
                }
            }
        }
        rmse = sum/count;
        rmse = Math.sqrt(rmse);
        return rmse;
    }
    
}
