/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender.ratings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author rafaeldaddio
 */
public class DatabaseMatrix extends RatingMatrix {

    public DatabaseMatrix(String matrixFile) {
        super();
        convertUserItemID(matrixFile);
        nItems = indexItemDbSystem.size();
        nUsers = indexUserDbSystem.size();
        indexUserSystemDb = new int[nUsers];
        indexItemSystemDb = new int[nItems];
        fillUserItemIndexArray();
    }

    private void convertUserItemID(String ratingsFile) {
        int uCount = 0;
        int iCount = 0;
        try {

            File file = new File(ratingsFile);
            Scanner scannerFile = new Scanner(file);

            while (scannerFile.hasNextLine()) {
                String line = scannerFile.nextLine();
                Scanner scannerLine = new Scanner(line);
                int user = scannerLine.nextInt();
                int item = scannerLine.nextInt();
                if (!indexUserDbSystem.containsKey(user)) {
                    getIndexUserDbSystem().put(user, uCount);
                    uCount++;
                }
                if (!indexItemDbSystem.containsKey(item)) {
                    getIndexItemDbSystem().put(item, iCount);
                    iCount++;
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Ratings file NOT FOUND.");
        }
    }

    private void fillUserItemIndexArray() {

        for (Integer key : getIndexUserDbSystem().keySet()) {
            indexUserSystemDb[getIndexUserDbSystem().get(key)] = key;

        }

        for (Integer key : getIndexItemDbSystem().keySet()) {
            indexItemSystemDb[getIndexItemDbSystem().get(key)] = key;

        }

    }

}
