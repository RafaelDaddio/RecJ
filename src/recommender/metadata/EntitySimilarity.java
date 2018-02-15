package recommender.metadata;

import java.io.*;
import recommender.metrics.*;
import recommender.ratings.DatabaseMatrix;

/**
 * Class responsible for calculating an entity-entity similarity matrix.
 *
 * There's an option available for applying a shrinkage towards zero in the
 * similarity score, depending on the number of features in common on two entity
 * vectors. The more features they have in common, less shrunk is their
 * similarity. This shrinkage is based on the Equation 2 of the following paper:
 *
 * Yehud Koren: <a href="https://dl.acm.org/citation.cfm?id=1644874">Factor in
 * the Neighbors: Scalable and Accurate Collaborative Filtering</a>,
 * Transactions on Knowledge Discovery from Data (TKDD), 2010.
 *
 * @author Rafael D'Addio
 */
public class EntitySimilarity {

    private Metadata metadata;  //metadata matrix
    private int[][] EntityxEntityNfeatures; //entity x entity matrix containing number of features in common for similarity shrinkage
    private double[][] EntityxEntitySimilarityMatrix; // entity x entity similarity matrix
    private SimilarityMeasures sim; //similarity metric
    private int regNeighbor = 100; // regularization constant

    /**
     * Constructor.
     *
     * @param metadata a Metadata object containing a metadata matrix
     * @param similarityOption option selecting which similarity to use: 0 =
     * cosine; 1 = pearson
     * @param shrinkOption option activating shrunk similarity: false =
     * inactive; true = active
     */
    public EntitySimilarity(Metadata metadata, int similarityOption, boolean shrinkOption) {

        this.metadata = metadata;
        EntityxEntitySimilarityMatrix = new double[metadata.getEntitySize()][metadata.getEntitySize()];
        EntityxEntityNfeatures = new int[metadata.getEntitySize()][metadata.getEntitySize()];
        switch (similarityOption) {
            case 0:
                sim = new Cosine();
                break;
            case 1:
                sim = new Pearson();
                break;
            default:
                System.out.println("Invalid similarity");
                break;
        }
        computeSimilarities(shrinkOption);
    }

    /**
     * Counts the number of features in common between two entities.
     *
     * @param entityA either a user or an item metadata vector
     * @param entityB either a user or an item metadata vector
     * @return the number of features
     */
    private int computeNFeatures(float[] entityA, float[] entityB) {
        int nfeatures = 0;
        for (int i = 0; i < entityA.length; i++) {
            if ((entityA[i] != 0) || (entityB[i] != 0)) {
                nfeatures++;
            }
        }
        return nfeatures;
    }

    /**
     * Fills the entity x entity matrix with the number of features in common.
     */
    private void fillNFeaturesMatrix() {

        for (int i = 0; i < metadata.getEntitySize(); i++) {
            for (int j = 0; j <= i; j++) {
                if (i != j) {
                    EntityxEntityNfeatures[i][j] = computeNFeatures(metadata.getEntity(i), metadata.getEntity(j));
                    EntityxEntityNfeatures[j][i] = EntityxEntityNfeatures[i][j];
                }
            }
        }
    }

    /**
     * Computes and fills the entity x entity similarity matrix, whether with or
     * without the shrinkage.
     *
     * @param shrinkOption option activating shrunk similarity: false =
     * inactive; true = active
     */
    private void computeSimilarities(boolean shrinkOption) {
        for (int i = 0; i < metadata.getEntitySize(); i++) {
            for (int j = 0; j <= i; j++) {
                EntityxEntitySimilarityMatrix[i][j] = 0;
                EntityxEntitySimilarityMatrix[j][i] = 0;
            }
        }
        //with shrinkage
        if (shrinkOption) {
            fillNFeaturesMatrix();
            double nfeatures = 0;

            //fills the matrix, with calculation being done only to the lower triangular matrix to boost processing
            for (int i = 0; i < metadata.getEntitySize(); i++) {
                for (int j = 0; j <= i; j++) {
                    if (i != j) {
                        nfeatures = EntityxEntityNfeatures[i][j];
                        EntityxEntitySimilarityMatrix[i][j] = (nfeatures / (nfeatures + regNeighbor)) * sim.calcSimilarity(metadata.getEntity(i), metadata.getEntity(j));
                        EntityxEntitySimilarityMatrix[j][i] = getEntityxEntitySimilarityMatrix()[i][j];
                    } else {
                        EntityxEntitySimilarityMatrix[i][j] = 0;
                    }
                }
            }

        } //without shrinkage
        else {

            //fills the matrix, with calculation being done only to the lower triangular matrix to boost processing
            for (int i = 0; i < metadata.getEntitySize(); i++) {
                for (int j = 0; j <= i; j++) {
                    if (i != j) {
                        EntityxEntitySimilarityMatrix[i][j] = sim.calcSimilarity(metadata.getEntity(i), metadata.getEntity(j));
                        EntityxEntitySimilarityMatrix[j][i] = getEntityxEntitySimilarityMatrix()[i][j];
                    } else {
                        EntityxEntitySimilarityMatrix[i][j] = 0;
                    }
                }
            }

        }
    }

    /**
     * Writes the similarity matrix in a file in the for entityA \t entityB \t
     * similarity
     *
     * @param similarityFile the file to save the similarity matrix
     * @param dbMatrix object with database - internal representation mappings
     * @param entityOption whether the entity it is 1 - item; 0 - user
     */
    public void writeSimilarity(String similarityFile, DatabaseMatrix dbMatrix, int entityOption) {
        try {
            File results = new File(similarityFile);
            if (!results.exists()) {
                results.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(results, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < metadata.getEntitySize(); i++) {
                for (int j = 0; j <= i; j++) {
                    if (j != i) {
                        //writes entity-entity similarity, converting internal item or user IDs to dataset IDs
                        if (entityOption == 1) {
                            bufferedWriter.write(dbMatrix.getIndexItemSystemDb()[i] + "\t" + dbMatrix.getIndexItemSystemDb()[j] + "\t" + EntityxEntitySimilarityMatrix[i][j]);
                        } else {
                            bufferedWriter.write(dbMatrix.getIndexUserSystemDb()[i] + "\t" + dbMatrix.getIndexUserSystemDb()[j] + "\t" + EntityxEntitySimilarityMatrix[i][j]);
                        }
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                    }
                }
            }
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
        }
    }

    /**
     *
     * @return the similarity matrix
     */
    public double[][] getEntityxEntitySimilarityMatrix() {
        return EntityxEntitySimilarityMatrix;
    }

    /**
     * @param regNeighbor the regularization constant value to set
     */
    public void setRegNeighbor(int regNeighbor) {
        this.regNeighbor = regNeighbor;
    }

}
