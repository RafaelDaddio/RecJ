/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import recommender.ratings.DatabaseMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class UserRanking {

    private int id;
    private ArrayList<Rating> ranking;

    public UserRanking(int id) {
        this.id = id;
        ranking = new ArrayList<>();
    }

    public void addRanking(Rating r) {
        ranking.add(r);
    }

    public void sortRanking() {
        Collections.sort(ranking, new Comparator<Rating>() {
            @Override
            public int compare(Rating r1, Rating r2) {
                return Double.compare(r2.getRating(), r1.getRating());
            }
            
        });
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the ranking
     */
    public ArrayList<Rating> getRanking() {
        return ranking;
    }

}
