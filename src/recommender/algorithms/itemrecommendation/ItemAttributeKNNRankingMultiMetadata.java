/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.itemrecommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class ItemAttributeKNNRankingMultiMetadata extends ItemAttributeKNNRanking {

    private double[][] itemxItemSimilarityMatrix2;
    private int combination;

    public ItemAttributeKNNRankingMultiMetadata(TrainingMatrix trainingMatrix, int k, double itemxItemSimilarityMatrix[][], double itemxItemSimilarityMatrix2[][], int rankingSize, int combination) {
        super(trainingMatrix, k, itemxItemSimilarityMatrix, rankingSize);

        this.itemxItemSimilarityMatrix2 = itemxItemSimilarityMatrix2;
        this.combination = combination;
    }

    @Override
    public void recommender() {
        long recStarTime = System.currentTimeMillis();
        
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < nUsers; i++) {
            final int idUser = i;
            es.execute(new Runnable() {
                @Override
                public void run() {
                    recommendItems(idUser);
                }
            });
            //System.out.println("Recomendou para usuário " + i);
        }
        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Threading problem.");
        }
        
        long recEndTime = System.currentTimeMillis();
        executionTime = recEndTime - recStarTime;
    }

    private void recommendItems(int user) {
        for (int i = 0; i < nItems; i++) {
            if (trainingMatrix.getValue(user, i) == -1) {
                generateUserItemScore(user, i);
            }
        }
    }

    private void generateUserItemScore(int user, int item) {
        ArrayList<Integer> knnPartial1 = new ArrayList<>();
        ArrayList<Integer> knnPartial2 = new ArrayList<>();
        HashMap<Integer, Double> knnFinal = new HashMap<>();
        ArrayList<Integer> knn = new ArrayList<>();

        obtainNeighborhood(user, item, knnPartial1, itemxItemSimilarityMatrix);
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
        if (!knnFinal.isEmpty()) {
            for (Map.Entry<Integer, Double> neighbour : knnFinal.entrySet()) {
                //System.out.println("Item: "+ neighbour.getKey() + "Similarity: " + neighbour.getValue());
                scoreMatrix[user][item] += neighbour.getValue();
               

            }
        } else {
            scoreMatrix[user][item] = 0;
        }

        //System.out.println(" User " + user + " Item " + item + " Score " + scoreMatrix[user][item]);
    }

    private void obtainNeighborhood(int user, int item, ArrayList<Integer> knnPartial, double[][] itemxItemSimilarityMatrix) {
        double big = 2;
        double small = -2;
        int id;
        //obtem vizinhos ordenados por distancia p/ primeiro metadado
        for (int i = 0; i < k; i++) {
            id = -1;
            for (int j = 0; j < nItems; j++) {
                if ((itemxItemSimilarityMatrix[item][j] >= small) && (itemxItemSimilarityMatrix[item][j] < big)) {
                    id = j;
                    small = itemxItemSimilarityMatrix[item][j];
                }
            }

            small = -2;
            if (id != -1) {
                //System.out.println("Entrou vizinho");
                big = itemxItemSimilarityMatrix[item][id];
                if (trainingMatrix.getValue(user, id) != -1) {
                    knnPartial.add(id);
                }
            }
        }

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
                        knnFinal.remove(knnPartial1.get(m));
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
                        knnFinal.remove(knnPartial2.get(n));
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
                    knnFinal.remove(knnPartial1.get(m));
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
                    knnFinal.remove(knnPartial2.get(n));
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
                    knnFinal.remove(knnPartial1.get(m));
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
                    knnFinal.remove(knnPartial2.get(n));
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
                knnFinal.remove(knnPartial1.get(m));
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
                knnFinal.remove(knnPartial2.get(n));
                double newSim = (previousSim + itemxItemSimilarityMatrix2[item][knnPartial2.get(n)]) / 2;
                knnFinal.put(knnPartial2.get(n), newSim);
            }
            n++;

        }
    }

}
