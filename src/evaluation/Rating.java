/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package evaluation;

/**
 *
 * @author rafaeldaddio
 */
public class Rating {
    
    private final int user;
    private final int item;
    protected double rating;
    
    public Rating(int user, int item, double rating){    
        this.user = user;
        this.item = item;
        this.rating = rating;
    }

    public int getUser() {
        return user;
    }

    public int getItem() {
        return item;
    }

    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
}
