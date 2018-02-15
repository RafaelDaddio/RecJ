package recommender.metadata;

import java.io.*;
import java.util.*;

/**
 *
 * Class that implements a metadata matrix in the format entity x metadata.
 *
 * Converts metadata dataset IDs into an internal representation. Requires the
 * entity's dataset-internal representation mapping.
 *
 * @author Rafael D'Addio
 */
public class Metadata {

    private HashMap<Integer, Integer> indexMetadataDbSystem; // metadata dataset - internal ID mapping
    private float[][] metadataMatrix; // metadata matrix
    private int[] indexMetadataSystemDb; // metadata internal - dataset ID mapping
    private int entitySize; // number of entities (item or user)
    private int metadataSize; // number of metadata (features)

    /**
     * Constructor.
     *
     * @param metadataFile file containing the metadata matrix in the form of
     * triples: entity \t metadata \t score
     * @param indexEntityDbSystem the entity's dataset-internal representation
     * mapping
     */
    public Metadata(String metadataFile, HashMap<Integer, Integer> indexEntityDbSystem) {
        indexMetadataDbSystem = new HashMap<>();
        mapMetadata(metadataFile);
        entitySize = indexEntityDbSystem.size();
        metadataSize = indexMetadataDbSystem.size();
        indexMetadataSystemDb = new int[metadataSize];
        fillMetadataIndexArray();
        metadataMatrix = new float[entitySize][metadataSize];
        fillMetadataMatrix(metadataFile, indexEntityDbSystem);
    }

    /**
     * Creates the dataset-internal representation mapping for the metadata.
     *
     * @param metadataFile file containing the metadata matrix in the form of
     * triples: entity \t metadata \t score
     */
    private void mapMetadata(String metadataFile) {
        int count = 0;
        int entity;
        try {

            File file = new File(metadataFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                entity = scannerLine.nextInt();
                int metadata = scannerLine.nextInt();
                if (!indexMetadataDbSystem.containsKey(metadata)) {
                    getIndexMetadataDbSystem().put(metadata, count);
                    count++;
                }
                scannerLine.close();
            }
            scannerFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("Metadata file NOT FOUND.");
        }

    }

    /**
     * Fills the arrays responsible to convert internal representation to
     * dataset IDs.
     */
    private void fillMetadataIndexArray() {

        for (Integer key : getIndexMetadataDbSystem().keySet()) {
            indexMetadataSystemDb[ getIndexMetadataDbSystem().get(key)] = key;

        }
    }

    /**
     * Fills the metadata matrix.
     *
     * @param metadataFile file containing the metadata matrix in the form of
     * triples: entity \t metadata \t score
     * @param indexEntityDbSystem the entity's dataset-internal representation
     * mapping
     */
    private void fillMetadataMatrix(String metadataFile, HashMap<Integer, Integer> indexEntityDbSystem) {
        try {

            File file = new File(metadataFile);
            Scanner scannerFile = new Scanner(file);

            for (int i = 0; i < getEntitySize(); i++) {
                for (int j = 0; j < getMetadataSize(); j++) {
                    metadataMatrix[i][j] = 0;
                }
            }

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                scannerLine.useLocale(Locale.US);
                int entity = scannerLine.nextInt();
                int metadata = scannerLine.nextInt();
                float rating = scannerLine.nextFloat();

                if (indexEntityDbSystem.containsKey(entity)) {
                    metadataMatrix[indexEntityDbSystem.get(entity)][getIndexMetadataDbSystem().get(metadata)] = rating;
                }
                scannerLine.close();
            }
            scannerFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("Metadata file NOT FOUND.");
        }
    }

    /**
     * Prints the metadata matrix on the console.
     *
     * @param indexEntityDbSystem the entity's dataset-internal representation
     * mapping
     * @param indexEntitySystemDb the entity's internal representation-dataset
     * mapping
     */
    public void printMatrix(HashMap<Integer, Integer> indexEntityDbSystem, int[] indexEntitySystemDb) {
        int entity;
        int metadata;
        float score;
        for (int i = 0; i < getEntitySize(); i++) {
            for (int j = 0; j < getMetadataSize(); j++) {
                score = metadataMatrix[i][j];
                entity = indexEntitySystemDb[i];
                metadata = indexMetadataSystemDb[j];
                System.out.println(entity + "\t" + metadata + "\t" + score);

            }
        }
    }

    /**
     * Returns an entity's metadata vector, defined by its ID.
     *
     * @param i the entity's internal ID
     * @return the entity's metadata vector
     */
    public float[] getEntity(int i) {
        return metadataMatrix[i];
    }

    /**
     *
     * @return the number of entities.
     */
    public int getEntitySize() {
        return entitySize;
    }

    /**
     *
     * @return the number of metadata features
     */
    public int getMetadataSize() {
        return metadataSize;
    }

    /**
     *
     * @return the metadata matrix
     */
    public float[][] getMatrix() {
        return metadataMatrix;
    }

    /**
     *
     * @return the metadata dataset-internal representation mapping
     */
    public HashMap<Integer, Integer> getIndexMetadataDbSystem() {
        return indexMetadataDbSystem;
    }
}
