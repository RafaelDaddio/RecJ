/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import recommender.metadata.Metadata;

/**
 *
 * @author rafaeldaddio
 */
public class WordNetSemanticSimilarity implements SimilarityMeasures {

    double semanticSimTermsMatrix[][];
    Metadata metadata;
    ArrayList<ArrayList<Integer>> similarTermsIDList;
    ArrayList<ArrayList<Double>> similarTermsScoreList;
    int lambda = 4;
    double kterms = 101;

    public WordNetSemanticSimilarity(String semanticMatrixFile, Metadata metadata) {
        this.metadata = metadata;
        semanticSimTermsMatrix = new double[this.metadata.getMetadataSize()][this.metadata.getMetadataSize()];
        readTermsSimMatrix(semanticMatrixFile);
        similarTermsIDList = new ArrayList<>();
        similarTermsScoreList = new ArrayList<>();
        obtainSimilarTerms();
    }

    private void readTermsSimMatrix(String semanticMatrixFile) {
        int term1;
        int term2;
        try {

            File file = new File(semanticMatrixFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                term1 = scannerLine.nextInt();
                term2 = scannerLine.nextInt();
                double score = scannerLine.nextDouble();
                semanticSimTermsMatrix[metadata.getIndexMetadataDbSystem().get(term1)][metadata.getIndexMetadataDbSystem().get(term2)] = score;
                semanticSimTermsMatrix[metadata.getIndexMetadataDbSystem().get(term2)][metadata.getIndexMetadataDbSystem().get(term1)] = score;
                scannerLine.close();
            }
            scannerFile.close();

            for (int i = 0; i < metadata.getMetadataSize(); i++) {
                semanticSimTermsMatrix[i][i] = 1;
            }

        } catch (FileNotFoundException e) {
            System.out.println("Term Similarity file NOT FOUND.");
        }
    }

    /*
     private double getAverageSimilarity() {
     double min = 2;
     double max = -2;
     double average = 0;

     for (int i = 0; i < metadata.getMetadataSize(); i++) {
     for (int j = 0; j < metadata.getMetadataSize(); j++) {
     if (semanticSimTermsMatrix[i][j] < min) {
     min = semanticSimTermsMatrix[i][j];
     }
     if (semanticSimTermsMatrix[i][j] > max) {
     max = semanticSimTermsMatrix[i][j];
     }
     }
     }

     average = (max - min) / 2;

     return average;
     }*/
    private void obtainSimilarTerms() {
        double max;
        int maxIndex = -1;

        for (int i = 0; i < metadata.getMetadataSize(); i++) {
            ArrayList<Integer> similarTerms = new ArrayList<>();
            ArrayList<Double> similarTermsScore = new ArrayList<>();
            max = 0;
            for (int k = 0; k < kterms; k++) {
                for (int j = 0; j < metadata.getMetadataSize(); j++) {
                    if (semanticSimTermsMatrix[i][j] >= max) {
                        max = semanticSimTermsMatrix[i][j];
                        maxIndex = j;
                    }
                }
                similarTerms.add(maxIndex);
                similarTermsScore.add(max);
                semanticSimTermsMatrix[i][maxIndex] = -1;
                maxIndex = -1;
                max = 0;
                }
            similarTermsIDList.add(similarTerms);
            similarTermsScoreList.add(similarTermsScore);
        }
    }

    @Override
    public double calcSimilarity(float[] itemA, float[] itemB) {
        double similarity = 0;
        int count = 0, k;

        for (int i = 0; i < itemA.length; i++) {
            if (itemA[i] != 0) {
                for (int j = 0; j < similarTermsIDList.get(i).size(); j++) {
                    k = similarTermsIDList.get(i).get(j);
                    if (itemB[k] != 0) {
                        similarity += semanticSimTermsMatrix[i][k] * (1 - (Math.abs(itemA[i] - itemB[k]) / lambda));
                        count += semanticSimTermsMatrix[i][k];
                    }
                }
            }
        }
        if (count == 0) {
            similarity = 0;
        } else {
            similarity = similarity / count;
        }
        return similarity;
    }

}
