/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.itemrecommendation;

import evaluation.Rating;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author rafaeldaddio
 */
public class CombineTwoUserRankingWithBPRV2 {

    private HashMap<Integer, ArrayList<Rating>> userRankingFinal;
    private HashMap<Integer, HashMap<Integer, Double>> userTrain1, userTrain2, userRanking1, userRanking2;
    private double alpha1 = 0.5, alpha2 = 0.5, regRank1 = 0.0025, regRank2 = 0.0025, learnRate = 0.05;
    private int epochs = 30, nInteractions = 0, rankingSize;
    private ArrayList<Integer> usersIDs;

    public CombineTwoUserRankingWithBPRV2(String fileTrain1, String fileTrain2, String fileRanking1, String fileRanking2, int rankingSize) {
        userTrain1 = new HashMap<>(); 
        userTrain2 = new HashMap<>();   
        userRanking1 = new HashMap<>();
        userRanking2 = new HashMap<>();
        this.rankingSize = rankingSize;
        userRankingFinal = new HashMap<>();
        usersIDs = new ArrayList<>();
        readRankings(fileTrain1, userTrain1);        
        readRankings(fileTrain2, userTrain2);
        readRankings(fileRanking1, userRanking1);
        readRankings(fileRanking2, userRanking2);
//        normalizeRanking(userTrain1);        
//        normalizeRanking(userTrain2);
//        normalizeRanking(userRanking1);
//        normalizeRanking(userRanking2);
        createUsersID();
        balanceRankings();
        fillRankings();
        calculateNumberInteractions();
    }

    private void readRankings(String fileRanking, HashMap<Integer, HashMap<Integer, Double>> userRankings) {
        try {

            File file = new File(fileRanking);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                if (!userRankings.containsKey(user)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    ranking.put(item, rating);
                    userRankings.put(user, ranking);
                } else {
                    userRankings.get(user).put(item, rating);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }
    
    private void createUsersID(){
        usersIDs = new ArrayList<>(userTrain1.keySet());
    }    

    private void normalizeRanking(HashMap<Integer, HashMap<Integer, Double>> userRankings) {
        for (HashMap<Integer, Double> ranking : userRankings.values()) {
            double max = 0, min = 1000;
            for (Map.Entry<Integer, Double> entry : ranking.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                }
                if (entry.getValue() < min) {
                    min = entry.getValue();
                }
            }
            min = min - 0.0000001;
            double maxMinusMin = max - min;
            for (Map.Entry<Integer, Double> entry : ranking.entrySet()) {
                double currentRating = entry.getValue();
                entry.setValue((currentRating - min) / maxMinusMin);
            }
        }
    }
    
    private void balanceRankings() {
        if (userRanking1.size() > userRanking2.size()) {
            for (Integer key : userRanking1.keySet()) {
                if (!userRanking2.containsKey(key)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    userRanking2.put(key, ranking);
                }

            }
        }
        if (userRanking2.size() > userRanking1.size()) {
            for (Integer key : userRanking2.keySet()) {
                if (!userRanking1.containsKey(key)) {
                    HashMap<Integer, Double> ranking = new HashMap<>();
                    userRanking1.put(key, ranking);
                }

            }
        }
    }

    private void fillRankings() {

        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking1.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                if (!userRanking2.get(user.getKey()).containsKey(item.getKey())) {
                    userRanking2.get(user.getKey()).put(item.getKey(), 0.0);
                }
            }
        }

        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking2.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                if (!userRanking1.get(user.getKey()).containsKey(item.getKey())) {
                    userRanking1.get(user.getKey()).put(item.getKey(), 0.0);
                }
            }
        }

    }

    private void calculateNumberInteractions() {
        for (HashMap<Integer, Double> ranking : userTrain1.values()) {
            nInteractions += ranking.size();
        }
    }

    private String sampleTriple() {
        String triple;
        
        Random r = new Random();
        
        int user = usersIDs.get(r.nextInt(usersIDs.size()));
        
        ArrayList<Integer> keys_i = new ArrayList<>(userTrain1.get(user).keySet());
        int item_i = keys_i.get(r.nextInt(keys_i.size()));
        ArrayList<Integer> keys_j = new ArrayList<>(userRanking1.get(user).keySet());
        int item_j = keys_j.get(r.nextInt(keys_j.size()));
        triple = user + "," + item_i + "," + item_j;

        return triple;
    }

    private double computeLoss(ArrayList<String> sample_triple) {
        double loss = 0;
        for (int i = 0; i < sample_triple.size(); i++) {
            String[] triple = sample_triple.get(i).split(",");
            int user = Integer.parseInt(triple[0]);
            int item_i = Integer.parseInt(triple[1]);
            int item_j = Integer.parseInt(triple[2]);

            double x_uij = (alpha1 * userTrain1.get(user).get(item_i) + alpha2 * userTrain2.get(user).get(item_i)) - (alpha1 * userRanking1.get(user).get(item_j) + alpha2 * userRanking2.get(user).get(item_j));
            loss += Math.log(1 / (1 + Math.exp(-x_uij))) - regRank1 * Math.pow(alpha1, 2) - regRank2 * Math.pow(alpha2, 2);
        }

        loss = loss * (-1);
        return loss;
    }

    private void update(String sample) {
        String[] triple = sample.split(",");
        int user = Integer.parseInt(triple[0]);
        int item_i = Integer.parseInt(triple[1]);
        int item_j = Integer.parseInt(triple[2]);

        double x_uij = (alpha1 * userTrain1.get(user).get(item_i) + alpha2 * userTrain2.get(user).get(item_i)) - (alpha1 * userRanking1.get(user).get(item_j) + alpha2 * userRanking2.get(user).get(item_j));

        alpha1 += learnRate * (1 / (1 + Math.exp(x_uij)) * (userTrain1.get(user).get(item_i) - userRanking1.get(user).get(item_j)) - regRank1 * alpha1);
        alpha2 += learnRate * (1 / (1 + Math.exp(x_uij)) * (userTrain2.get(user).get(item_i) - userRanking2.get(user).get(item_j)) - regRank2 * alpha2);

    }

    public void train() {

        double loss = 0;
        int num_sample_triples = (int) (Math.sqrt(userTrain1.size()) * 100);
        ArrayList<String> sample_triple = new ArrayList<>();

        for (int i = 0; i < num_sample_triples; i++) {
            sample_triple.add(sampleTriple());
        }
        loss = computeLoss(sample_triple);

        for (int i = 0; i < epochs; i++) {
            for (int j = 0; j < nInteractions; j++) {
                String x_uij = sampleTriple();
                update(x_uij);
            }
            double currentLoss = computeLoss(sample_triple);
            if (currentLoss > loss) {
                learnRate *= 0.5;
            } else if (currentLoss < loss) {
                learnRate *= 1.1;
            }
            loss = currentLoss;
            System.out.println("Epoch: " + i + "; Loss: " + loss +"\nAlpha1: "+ alpha1 + " Alpha2: " +alpha2);
        }
    }

    protected void sort(HashMap<Integer, ArrayList<Rating>> userRankings) {
        for (ArrayList<Rating> ranking : userRankings.values()) {
            Collections.sort(ranking, new Comparator<Rating>() {
                @Override
                public int compare(Rating r1, Rating r2) {
                    return Double.compare(r2.getRating(), r1.getRating());
                }
            });
        }
    }

    public void generateFinalRanking() {
        for (Map.Entry<Integer, HashMap<Integer, Double>> user : userRanking1.entrySet()) {
            for (Map.Entry<Integer, Double> item : user.getValue().entrySet()) {
                double score = alpha1 * userRanking1.get(user.getKey()).get(item.getKey()) + alpha2 * userRanking2.get(user.getKey()).get(item.getKey());
                Rating r = new Rating(user.getKey(), item.getKey(), score);

                if (!userRankingFinal.containsKey(user.getKey())) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    ranking.add(r);
                    userRankingFinal.put(user.getKey(), ranking);
                } else {
                    userRankingFinal.get(user.getKey()).add(r);
                }
            }
        }
        sort(userRankingFinal);
    }
    public void writeRecommendations(String recomendationPath) {
        int rank = 0;
        try {
            File recomendation = new File(recomendationPath);
            if (!recomendation.exists()) {
                recomendation.createNewFile();
            } else {
                recomendation.delete();
                recomendation.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(recomendation, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (ArrayList<Rating> rankings : userRankingFinal.values()) {
                rank = rankingSize;
                if (rank > rankings.size()) {
                    rank = rankings.size();
                }
                for (int i = 0; i < rank; i++) {
                    bufferedWriter.write(rankings.get(i).getUser() + "\t" + rankings.get(i).getItem() + "\t" + rankings.get(i).getRating());
                    bufferedWriter.write("\n");
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error.");
        }
    }

    /**
     * @param regRank1 the regularization of ranking 1 to set
     */
    public void setRegRank1(double regRank1) {
        this.regRank1 = regRank1;
    }

    /**
     * @param regRank2 the regularization of ranking 2 to set
     */
    public void setRegRank2(double regRank2) {
        this.regRank2 = regRank2;
    }

    /**
     * @param learnRate the learn rate to set
     */
    public void setLearnRate(double learnRate) {
        this.learnRate = learnRate;
    }

    /**
     * @param epochs the number of epochs to set
     */
    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

}
