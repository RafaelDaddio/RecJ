/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class ItemRecommendationMetrics {

    ArrayList<UserRanking> usersRankings;
    HashSet<String> test;

    public ItemRecommendationMetrics(String rankingsFile, String testFile) {
        usersRankings = new ArrayList<>();
        test = new HashSet<>();
        populateRankings(rankingsFile);
        populateTest(testFile);
    }

    private void populateRankings(String rankingFile) {
        int found = 0;
        try {

            File file = new File(rankingFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                double rating = scannerLine.nextDouble();
                Rating r = new Rating(user, item, rating);
                found = 0;
                for (int i = 0; i < usersRankings.size(); i++) {
                    if (usersRankings.get(i).getId() == r.getUser()) {
                        usersRankings.get(i).addRanking(r);
                        found = 1;
                    }
                }
                if (found == 0) {
                    UserRanking ur = new UserRanking(r.getUser());
                    ur.addRanking(r);
                    usersRankings.add(ur);
                }
            }
            for (int i = 0; i < usersRankings.size(); i++) {
                usersRankings.get(i).sortRanking();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ranking file NOT FOUND.");
        }
    }

    private void populateTest(String testFile) {
        try {

            File file = new File(testFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                String tuple = user + "," + item;
                if (!test.contains(tuple)) {
                    test.add(tuple);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Test file NOT FOUND.");
        }
    }

    public void printRankings() {
        for (int i = 0; i < usersRankings.size(); i++) {

            System.out.println("User " + usersRankings.get(i).getId() + ":");
            for (int j = 0; j < usersRankings.get(i).getRanking().size(); j++) {
                System.out.println("Item " + usersRankings.get(i).getRanking().get(j).getItem() + " - score: " + usersRankings.get(i).getRanking().get(j).getRating());
            }

        }
    }

    public double precisionAtK(int rankSize) {
        double prec = 0;

        for (int i = 0; i < usersRankings.size(); i++) {
            double precUser = 0;
            if (rankSize > usersRankings.get(i).getRanking().size()) {
                rankSize = usersRankings.get(i).getRanking().size();
            }
            
            for (int j = 0; j < rankSize; j++) {
                String candidate = usersRankings.get(i).getRanking().get(j).getUser() + "," + usersRankings.get(i).getRanking().get(j).getItem();
                if (test.contains(candidate)) {
                    precUser++;
                }

            }
            precUser = precUser / rankSize;
            prec = prec + precUser;
        }
        prec = prec / usersRankings.size();

        return prec;

    }

    public double mapAtK(int rankSize) {
        double map = 0;

        for (int i = 0; i < usersRankings.size(); i++) {
            double apUser = 0, hits = 0;
            if (rankSize > usersRankings.get(i).getRanking().size()) {
                rankSize = usersRankings.get(i).getRanking().size();
            }
            for (int j = 0; j < rankSize; j++) {
                String candidate = usersRankings.get(i).getRanking().get(j).getUser() + "," + usersRankings.get(i).getRanking().get(j).getItem();
                if (test.contains(candidate)) {
                    hits++;
                    apUser += hits / (j+1);
                }

            }
            if (hits != 0) {
                apUser = apUser / hits;
            } else {
                apUser = 0;
            }
            map += apUser;
        }
        map = map / usersRankings.size();

        return map;

    }

}
