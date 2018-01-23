/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class Metadata {

    private HashMap<Integer, Integer> indexMetadataDbSystem;
    private float[][] metadataMatrix;
    protected int[] indexMetadataSystemDb;
    private int entitySize;
    private int metadataSize;

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

    private void fillMetadataIndexArray() {

        for (Integer key : getIndexMetadataDbSystem().keySet()) {
            indexMetadataSystemDb[ getIndexMetadataDbSystem().get(key)] = key;

        }
    }

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
            }

        } catch (FileNotFoundException e) {
            System.out.println("Metadata file NOT FOUND.");
        }
        //System.out.println("Populou a matriz de Metadados");
    }

    public void printMatrix(HashMap<Integer, Integer> indexEntityDbSystem, int[] indexItemSystemDb) {
        int item;
        int metadata;
        float score;
        for (int i = 0; i < getEntitySize(); i++) {
            for (int j = 0; j < getMetadataSize(); j++) {
                score = metadataMatrix[i][j];
                item = indexItemSystemDb[i];
                metadata = indexMetadataSystemDb[j];
                System.out.println(item + " " + metadata + " " + score);

            }
        }
    }

    public float[] getEntity(int i) {
        return metadataMatrix[i];
    }

    public int getEntitySize() {
        return entitySize;
    }

    public int getMetadataSize() {
        return metadataSize;
    }
    
    public float[][] getMatrix(){
        return metadataMatrix;
    }

    public HashMap<Integer, Integer> getIndexMetadataDbSystem() {
        return indexMetadataDbSystem;
    }
}
