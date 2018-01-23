/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import recommender.metrics.Cosine;
import recommender.metrics.Pearson;
import recommender.metrics.SimilarityMeasures;
import recommender.metrics.WordNetSemanticSimilarity;
import recommender.ratings.DatabaseMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class EntitySimilarity {

    private Metadata metadata;
    private int[][] itemxItemNfeatures;
    private double[][] itemxItemSimilarityMatrix;
    private SimilarityMeasures dist;
    private final int regNeighbor = 100;

    /*
     similarityOption:
     0 = cosine
     1 = pearson
     2 = semanticSimilarity
     */
    public EntitySimilarity(Metadata metadata, int similarityOption, String termSimilarityMatrixFile) {

        this.metadata = metadata;
        itemxItemSimilarityMatrix = new double[metadata.getEntitySize()][metadata.getEntitySize()];
        itemxItemNfeatures = new int[metadata.getEntitySize()][metadata.getEntitySize()];
        switch (similarityOption) {
            case 0:
                dist = new Cosine();
                break;
            case 1:
                dist = new Pearson();
                break;
            case 2:
                dist = new WordNetSemanticSimilarity(termSimilarityMatrixFile, metadata);
                break;
            default:
                System.out.println("Invalid similarity");
                break;
        }
        computeSimilarities();
    }

    private int computeNFeatures(float[] itemA, float[] itemB) {
        int nfeatures = 0;
        for (int i = 0; i < itemA.length; i++) {
            if ((itemA[i] != 0) || (itemB[i] != 0)) {
                nfeatures++;
            }
        }
        return nfeatures;
    }

    private void fillNFeaturesMatrix() {

        for (int i = 0; i < metadata.getEntitySize(); i++) {
            for (int j = 0; j <= i; j++) {
                if (i != j) {
                    itemxItemNfeatures[i][j] = computeNFeatures(metadata.getEntity(i), metadata.getEntity(j));
                    itemxItemNfeatures[j][i] = itemxItemNfeatures[i][j];
                }
            }
        }
    }

    private void computeSimilarities() {
        fillNFeaturesMatrix();
        for (int i = 0; i < metadata.getEntitySize(); i++) {
            for (int j = 0; j <= i; j++) {
                itemxItemSimilarityMatrix[i][j] = 0;
                itemxItemSimilarityMatrix[j][i] = 0;
            }
        }
        double nfeatures = 0;
        for (int i = 0; i < metadata.getEntitySize(); i++) {
            for (int j = 0; j <= i; j++) {
                if (i != j) {
                    nfeatures = itemxItemNfeatures[i][j];
                    itemxItemSimilarityMatrix[i][j] = (nfeatures / (nfeatures + regNeighbor)) * dist.calcSimilarity(metadata.getEntity(i), metadata.getEntity(j));
                    itemxItemSimilarityMatrix[j][i] = getItemxItemSimilarityMatrix()[i][j];
                } else {
                    itemxItemSimilarityMatrix[i][j] = 0;
                }

            }
            //System.out.println("Calculou similaridade de " + i);

        }
    }
    
    public void writeSimilarity(String similarityFile, DatabaseMatrix dbMatrix){
        try {
            File results = new File(similarityFile);
            if (!results.exists()) {
                results.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(results, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < metadata.getEntitySize(); i++) {
                for (int j = 0; j <= i; j++) {
                    if(j != i){
                        bufferedWriter.write(dbMatrix.getIndexItemSystemDb()[i]+ "\t"+ dbMatrix.getIndexItemSystemDb()[j] + "\t" + itemxItemSimilarityMatrix[i][j]);
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public double[][] getItemxItemSimilarityMatrix() {
        return itemxItemSimilarityMatrix;
    }

}
