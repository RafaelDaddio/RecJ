package recommender.algorithms.ratingprediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import recommender.ratings.PredictionMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author ads
 */
public class ItemAttributeKNNMultiMetadata extends ItemAttributeKNN {

    private ArrayList<Integer> knnPartial1, knnPartial2;
    private double[][] itemxItemSimilarityMatrix2;
    private HashMap<Integer, Double> knnFinal;
    int combination;

    /*     
     predictionOption
     0 = all predictions
     1 = only test
    
     combination
     1 = intersect Rankings taking the Average Value
     2 = intersect Rankings maintaining the Best Value
     3 = combine Rankings taking the Average Value in ties
     4 = combine Rankings maintaining the Best Value in ties     
     */
    public ItemAttributeKNNMultiMetadata(String testFile, TrainingMatrix trainingMatrix, int k, int predictionOption, double itemxItemSimilarityMatrix[][], double itemxItemSimilarityMatrix2[][], int combination) {

        super(testFile, trainingMatrix, k, predictionOption, itemxItemSimilarityMatrix);

        this.itemxItemSimilarityMatrix2 = itemxItemSimilarityMatrix2;
        knnPartial1 = new ArrayList<>();
        knnPartial2 = new ArrayList<>();
        knnFinal = new HashMap<>();
        this.combination = combination;
    }

    @Override
    public void recommender() {
        long recStarTime = System.currentTimeMillis();
        for (int i = 0; i < nUsers; i++) {
            recommendItems(i);
            //System.out.println("Recomendou para usuário " + i);
        }
        long recEndTime = System.currentTimeMillis();
        executionTime = recEndTime - recStarTime;
    }

    private void recommendItems(int user) {
        if (predictionOption == 0) {
            for (int i = 0; i < nItems; i++) {
                if (trainingMatrix.getValue(user, i) == -1) {
                    generateUserItemScore(user, i);
                }
            }
        } else {
            for (int i = 0; i < nItems; i++) {
                if (testMatrix.getValue(user, i) == 1) {
                    generateUserItemScore(user, i);
                }
            }
        }

    }

    private void obtainNeighborhood(int user, int item, ArrayList<Integer> knnPartial, double[][] itemxItemSimilarityMatrix) {
        //obtem vizinhos ordenados por distancia p/ primeiro metadado
        for (int i = 0; i < k; i++) {
            idneighbour = -1;
            for (int j = 0; j < nItems; j++) {
                if ((itemxItemSimilarityMatrix[item][j] >= small) && (itemxItemSimilarityMatrix[item][j] < big)) {
                    idneighbour = j;
                    small = itemxItemSimilarityMatrix[item][j];
                }
            }

            small = -2;
            if (idneighbour != -1) {
                //System.out.println("Entrou vizinho");
                big = itemxItemSimilarityMatrix[item][idneighbour];
                if (trainingMatrix.getValue(user, idneighbour) != -1) {
                    knnPartial.add(idneighbour);
                }
            }
        }

    }

    private void generateUserItemScore(int user, int item) {
        relevantItems.clear();
        knn.clear();
        knnPartial1.clear();
        knnPartial2.clear();
        knnFinal.clear();
        big = 2;
        small = -2;

        obtainNeighborhood(user, item, knnPartial1, itemxItemSimilarityMatrix);

        big = 2;
        small = -2;

        obtainNeighborhood(user, item, knnPartial2, itemxItemSimilarityMatrix2);

        switch (combination) {
            case 1:
                intersectRankingAverageValue(item, knnPartial1, knnPartial2, knnFinal);
                break;
            case 2:
                intersectRankingBestValue(item, knnPartial1, knnPartial2, knnFinal);
                break;
            case 3:
                combineRankingAverageValue(item, knnPartial1, knnPartial2, knnFinal);
                break;
            case 4:
                combineRankingBestValue(item, knnPartial1, knnPartial2, knnFinal);
                break;

        }

        //calcula score final
        dividendo = 1;
        //System.out.println(relevantItems.size() + " " + knnPartial1.size() + " " + knnPartial2.size() + " " + knnFinal.size());
        if (knnFinal.size() > 0) {
            //System.out.println("Entrou");
            for (Map.Entry<Integer, Double> neighbour : knnFinal.entrySet()) {
                //System.out.println("Item: "+ neighbour.getKey() + "Similarity: " + neighbour.getValue());
                ruj = trainingMatrix.getValue(user, neighbour.getKey());
                buj = bui[user][neighbour.getKey()];
                scoreMatrix[user][item] += (ruj - buj) * neighbour.getValue();
                dividendo += neighbour.getValue();

            }
            //System.out.println("Nota: "+ (scoreMatrix[user][item]/dividendo));
            scoreMatrix[user][item] = bui[user][item] + (scoreMatrix[user][item] / dividendo);
        } else {
            scoreMatrix[user][item] = bui[user][item];
        }
        if (scoreMatrix[user][item] < 1) {
            scoreMatrix[user][item] = 1;

        } else if (scoreMatrix[user][item] > 5) {
            scoreMatrix[user][item] = 5;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + scoreMatrix[user][item]);
    }

    private void intersectRankingBestValue(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2, HashMap<Integer, Double> knnFinal) {
        //faz a interseção dos dois ranks, obtendo o maior 
        for (int i = 0; i < knnPartial1.size(); i++) {
            for (int j = 0; j < knnPartial2.size(); j++) {
                if (knnPartial1.get(i) == knnPartial2.get(j)) {
                    if (itemxItemSimilarityMatrix[item][knnPartial1.get(i)] > itemxItemSimilarityMatrix2[item][knnPartial2.get(j)]) {
                        knnFinal.put(knnPartial1.get(i), itemxItemSimilarityMatrix[item][knnPartial1.get(i)]);
                    } else {
                        knnFinal.put(knnPartial2.get(j), itemxItemSimilarityMatrix2[item][knnPartial2.get(j)]);
                    }

                }
            }
        }
    }

    private void intersectRankingAverageValue(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2, HashMap<Integer, Double> knnFinal) {
        //faz a interseção dos dois ranks, obtendo a média 
        for (int i = 0; i < knnPartial1.size(); i++) {
            for (int j = 0; j < knnPartial2.size(); j++) {
                if (knnPartial1.get(i) == knnPartial2.get(j)) {
                    double average = (itemxItemSimilarityMatrix[item][knnPartial1.get(i)] + itemxItemSimilarityMatrix2[item][knnPartial2.get(j)]) / 2;
                    knnFinal.put(knnPartial1.get(i), average);
                }
            }
        }
    }

    private void combineRankingBestValue(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2, HashMap<Integer, Double> knnFinal) {
        int m = 0, n = 0;

        //mergesort, adicionando no rank final priorizando o valor de similaridade
        //até atingir o final de uma das duas listas
        while (m < knnPartial1.size() && n < knnPartial2.size()) {

            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (itemxItemSimilarityMatrix[item][knnPartial1.get(m)] >= itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]) { //se a similaridade do elemento do primeiro rank é maior que do elemento do segundo, 
                //adiciona ao rank final ou combina caso o elemento já tiver sido adicionado do segundo rank
                if (!knnFinal.containsKey(knnPartial1.get(m))) {
                    knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
                } else {
                    double previousSim = knnFinal.get(knnPartial1.get(m));
                    if (itemxItemSimilarityMatrix[item][knnPartial1.get(m)] > previousSim) {
                        knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
                    }
                }
                m++;
            } else { //se a similaridade do elemento do segundo rank é maior que do elemento do primeiro, 
                //adiciona ao rank final ou combina caso o elemento já tiver sido adicionado do primeiro rank

                if (!knnFinal.containsKey(knnPartial2.get(n))) {
                    knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
                } else {
                    double previousSim = knnFinal.get(knnPartial2.get(n));
                    if (itemxItemSimilarityMatrix2[item][knnPartial2.get(n)] > previousSim) {
                        knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
                    }
                }
                n++;
            }

        }
        //se sobrou elementos no primeiro rank, adiciona-os ou combina-os
        while (m < knnPartial1.size()) {

            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (!knnFinal.containsKey(knnPartial1.get(m))) {
                knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
            } else {
                double previousSim = knnFinal.get(knnPartial1.get(m));
                if (itemxItemSimilarityMatrix[item][knnPartial1.get(m)] > previousSim) {
                    knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
                }
            }
            m++;
        }
        //se sobrou elementos no segundo rank, adiciona-os ou combina-os
        while (n < knnPartial2.size()) {
            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (!knnFinal.containsKey(knnPartial2.get(n))) {
                knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
            } else {
                double previousSim = knnFinal.get(knnPartial2.get(n));
                if (itemxItemSimilarityMatrix2[item][knnPartial2.get(n)] > previousSim) {
                    knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
                }
            }
            n++;

        }
    }

    private void combineRankingAverageValue(int item, ArrayList<Integer> knnPartial1, ArrayList<Integer> knnPartial2, HashMap<Integer, Double> knnFinal) {
        int m = 0, n = 0;

        //mergesort, adicionando no rank final priorizando o valor de similaridade
        //até atingir o final de uma das duas listas
        while (m < knnPartial1.size() && n < knnPartial2.size()) {

            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (itemxItemSimilarityMatrix[item][knnPartial1.get(m)] >= itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]) { //se a similaridade do elemento do primeiro rank é maior que do elemento do segundo, 
                //adiciona ao rank final ou combina caso o elemento já tiver sido adicionado do segundo rank
                if (!knnFinal.containsKey(knnPartial1.get(m))) {
                    knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
                } else {
                    double previousSim = knnFinal.get(knnPartial1.get(m));
                    double newSim = (previousSim + itemxItemSimilarityMatrix[item][knnPartial1.get(m)]) / 2;
                    knnFinal.put(knnPartial1.get(m), newSim);
                }
                m++;
            } else { //se a similaridade do elemento do segundo rank é maior que do elemento do primeiro, 
                //adiciona ao rank final ou combina caso o elemento já tiver sido adicionado do primeiro rank

                if (!knnFinal.containsKey(knnPartial2.get(n))) {
                    knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
                } else {
                    double previousSim = knnFinal.get(knnPartial2.get(n));
                    double newSim = (previousSim + itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]) / 2;
                    knnFinal.put(knnPartial2.get(n), newSim);
                }
                n++;
            }

        }
        //se sobrou elementos no primeiro rank, adiciona-os ou combina-os
        while (m < knnPartial1.size()) {

            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (!knnFinal.containsKey(knnPartial1.get(m))) {
                knnFinal.put(knnPartial1.get(m), itemxItemSimilarityMatrix[item][knnPartial1.get(m)]);
            } else {
                double previousSim = knnFinal.get(knnPartial1.get(m));
                double newSim = (previousSim + itemxItemSimilarityMatrix[item][knnPartial1.get(m)]) / 2;
                knnFinal.put(knnPartial1.get(m), newSim);
            }
            m++;

        }

        //se sobrou elementos no segundo rank, adiciona-os ou combina-os
        while (n < knnPartial2.size()) {
            if (knnFinal.size() == k) { //se o tamanho do rank final já atingiu o tamanho necessário de vizinhos, sai do while
                break;
            }

            if (!knnFinal.containsKey(knnPartial2.get(n))) {
                knnFinal.put(knnPartial2.get(n), itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]);
            } else {
                double previousSim = knnFinal.get(knnPartial2.get(n));
                double newSim = (previousSim + itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]) / 2;
                knnFinal.put(knnPartial2.get(n), newSim);
            }
            n++;
        }
    }

}
