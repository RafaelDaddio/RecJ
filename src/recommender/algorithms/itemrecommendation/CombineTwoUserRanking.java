/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.algorithms.itemrecommendation;

import evaluation.Rating;
import evaluation.UserRanking;
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
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class CombineTwoUserRanking {

    protected HashMap<Integer, ArrayList<Rating>> userRankings1, userRankings2, userRankingsFinal;
    protected int rankingSize, option;

    public CombineTwoUserRanking(String fileRanking1, String fileRanking2, int rankingSize, int option) {
        this.rankingSize = rankingSize;
        userRankings1 = new HashMap<>();
        userRankings2 = new HashMap<>();
        userRankingsFinal = new HashMap<>();
        readRanking(fileRanking1, userRankings1);
        readRanking(fileRanking2, userRankings2);
        this.option = option;
        balanceRankings();
    }

    private void readRanking(String fileRanking, HashMap<Integer, ArrayList<Rating>> userRankings) {

        int found = 0;
        try {

            File file = new File(fileRanking);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                Rating r = new Rating(user, item, rating);
                found++;
                if (!userRankings.containsKey(user)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    ranking.add(r);
                    userRankings.put(user, ranking);
                } else {
                    userRankings.get(user).add(r);
                }

            }
            //System.out.println(found + " lines");
            sort(userRankings);
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }

    private void balanceRankings() {
        if (userRankings1.size() > userRankings2.size()) {
            for (Integer key : userRankings1.keySet()) {
                if (!userRankings2.containsKey(key)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    userRankings2.put(key, ranking);
                }

            }
        }
        if (userRankings1.size() < userRankings2.size()) {
            for (Integer key : userRankings2.keySet()) {
                if (!userRankings1.containsKey(key)) {
                    ArrayList<Rating> ranking = new ArrayList<>();
                    userRankings1.put(key, ranking);
                }

            }

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

    public void recommend() {
        switch (option) {
            case 1:
                for (Integer key : userRankings1.keySet()) {
                    combineRankingBestValue(key);
                }
                break;
            case 2:
                for (Integer key : userRankings1.keySet()) {
                    combineRankingSumValue(key);
                }
                break;
            default:
                System.out.println("Error: combination method not valid.");
        }
    }

    private void combineRankingBestValue(int user) {
        int m = 0, n = 0;
        HashMap<Integer, Double> partialUserRanking = new HashMap<>();

        //mergesort, adicionando no rank final priorizando o valor do ranking
        //até atingir o final de uma das duas listas
        while (m < userRankings1.get(user).size() && n < userRankings2.get(user).size()) {

            if (userRankings1.get(user).get(m).getRating() >= userRankings2.get(user).get(n).getRating()) {
                int item = userRankings1.get(user).get(m).getItem();
                double score = userRankings1.get(user).get(m).getRating();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score);
                } else {
                    double previousScore = partialUserRanking.get(item);
                    if (score > previousScore) {
                        partialUserRanking.remove(item);
                        partialUserRanking.put(item, score);
                    }
                }
                m++;
            } else {
                int item = userRankings2.get(user).get(n).getItem();
                double score = userRankings2.get(user).get(n).getRating();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score);
                } else {
                    double previousScore = partialUserRanking.get(item);
                    if (score > previousScore) {
                        partialUserRanking.remove(item);
                        partialUserRanking.put(item, score);
                    }
                }
                n++;
            }
        }
        //se sobrou elementos no primeiro rank, adiciona-os ou combina-os
        while (m < userRankings1.get(user).size()) {
            int item = userRankings1.get(user).get(m).getItem();
            double score = userRankings1.get(user).get(m).getRating();
            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                double previousScore = partialUserRanking.get(item);
                if (score > previousScore) {
                    partialUserRanking.remove(item);
                    partialUserRanking.put(item, score);
                }
            }
            m++;
        }
        //se sobrou elementos no segundo rank, adiciona-os ou combina-os
        while (n < userRankings2.get(user).size()) {
            int item = userRankings2.get(user).get(n).getItem();
            double score = userRankings2.get(user).get(n).getRating();

            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                double previousScore = partialUserRanking.get(item);
                if (score > previousScore) {
                    partialUserRanking.remove(item);
                    partialUserRanking.put(item, score);
                }
            }
            n++;
        }

        produceRanking(user, partialUserRanking);
    }

    private void combineRankingSumValue(int user) {
        int m = 0, n = 0;
        HashMap<Integer, Double> partialUserRanking = new HashMap<>();

        //mergesort, adicionando no rank final priorizando o valor do ranking
        //até atingir o final de uma das duas listas
        while (m < userRankings1.get(user).size() && n < userRankings2.get(user).size()) {

            if (userRankings1.get(user).get(m).getRating() >= userRankings2.get(user).get(n).getRating()) {
                int item = userRankings1.get(user).get(m).getItem();
                double score = userRankings1.get(user).get(m).getRating();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score);
                } else {
                    score += partialUserRanking.get(item);
                    score = score / 2;
                    partialUserRanking.put(item, score);
                }
                m++;
            } else {
                int item = userRankings2.get(user).get(n).getItem();
                double score = userRankings2.get(user).get(n).getRating();
                if (!partialUserRanking.containsKey(item)) {
                    partialUserRanking.put(item, score);
                } else {
                    score += partialUserRanking.get(item);
                    score = score / 2;
                    partialUserRanking.put(item, score);
                }
                n++;
            }
        }
        //se sobrou elementos no primeiro rank, adiciona-os ou combina-os
        while (m < userRankings1.get(user).size()) {
            int item = userRankings1.get(user).get(m).getItem();
            double score = userRankings1.get(user).get(m).getRating();
            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                score += partialUserRanking.get(item);
                score = score / 2;
                partialUserRanking.put(item, score);
            }
            m++;
        }
        //se sobrou elementos no segundo rank, adiciona-os ou combina-os
        while (n < userRankings2.get(user).size()) {
            int item = userRankings2.get(user).get(n).getItem();
            double score = userRankings2.get(user).get(n).getRating();

            if (!partialUserRanking.containsKey(item)) {
                partialUserRanking.put(item, score);
            } else {
                score += partialUserRanking.get(item);
                score = score / 2;
                partialUserRanking.put(item, score);
            }
            n++;
        }

        produceRanking(user, partialUserRanking);
    }

    private void produceRanking(int user, HashMap<Integer, Double> partialUserRanking) {
        double big = 10000, small = -10000;
        int id = -1;
        for (int i = 0; i < rankingSize; i++) {
            id = -1;
            for (Map.Entry<Integer, Double> entry : partialUserRanking.entrySet()) {
                int item = entry.getKey();
                double score = entry.getValue();
                if ((score >= small) && (score <= big)) {
                    small = score;
                    id = item;
                }
            }
            small = -20000;
            if (id != -1) {
                big = partialUserRanking.get(id);
                Rating r = new Rating(user, id, big);
                if (!userRankingsFinal.containsKey(user)) {
                    ArrayList<Rating> rank = new ArrayList<>();
                    rank.add(r);
                    userRankingsFinal.put(user, rank);
                } else {
                    userRankingsFinal.get(user).add(r);
                }
                partialUserRanking.remove(id);
            }
        }
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
            for (ArrayList<Rating> rankings : userRankingsFinal.values()) {
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

}
