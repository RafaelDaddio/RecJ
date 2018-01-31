package recommender.algorithms.ratingprediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import recommender.metadata.Metadata;
import recommender.metrics.Cosine;
import recommender.metrics.Pearson;
import recommender.metrics.SimilarityMeasures;
import recommender.ratings.PredictionMatrix;
import recommender.ratings.RatingMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author ads
 */
public class UserAttributeKNN {

    ArrayList<Integer> relevantUsers;
    ArrayList<Integer> knn;
    private int k;
    private TrainingMatrix trainingMatrix;
    private PredictionMatrix predictionMatrix;
    private Metadata metadataRatings;
    private Metadata metadataProfile;
    private int[][] userxuserNfeaturesRatings;
    private double[][] userxUserSimilarityRatings;
    private double[][] scoreMatrixRatings;
    private int[][] userxuserNfeaturesMetadata;
    private double[][] userxUserSimilarityMetadata;
    private double[][] scoreMatrixMetadata;
    private int[][] userxuserNfeaturesBoth;
    private double[][] userxUserSimilarityBoth;
    private double[][] scoreMatrixBoth;
    private double[][] bui;
    private double[] bu;
    private double[] bi;
    private double mi;
    double big;
    double small;
    private double dividendo;
    private double rvi;
    private double bvi;
    private int nItems;
    private int nUsers;
    int idneighbour;
    private SimilarityMeasures dist;
    private final int regSimilarity = 100;
    private final int regBi = 10;
    private final int regBu = 15;
    private final int iterations = 10;

    public UserAttributeKNN(String metadataFile, String trainingFile, TrainingMatrix trainingMatrix, int k, int SimMeasureOption, int similarityOption) {
        //simMeasureOption -> metrica de similaridade
        //similarityOption -> 0 = userknn normal; 1 = matriz metadado; 2 = ambos
        relevantUsers = new ArrayList<>();
        knn = new ArrayList<>();
        this.trainingMatrix = trainingMatrix;
        this.k = k;
        predictionMatrix = new PredictionMatrix(trainingMatrix.getIndexUserDbSystem(), trainingMatrix.getIndexItemDbSystem(), trainingMatrix.getIndexUserSystemDb(), trainingMatrix.getIndexItemSystemDb());
        nItems = trainingMatrix.getnItems();
        nUsers = trainingMatrix.getnUsers();
        trainBaselines();
        switch(similarityOption){
            case 0: dist = new Cosine();
                    break;
            case 1: dist = new Pearson();
                    break;
            default: System.out.println("Invalid similarity");
                     break;
        }
        if (similarityOption == 0) {
            metadataRatings = new Metadata(trainingFile, trainingMatrix.getIndexUserDbSystem());
            System.out.println("Leu metadados");
            scoreMatrixRatings = new double[nUsers][nItems];
            fillScoreMatrix(scoreMatrixRatings);
            userxUserSimilarityRatings = new double[nUsers][nUsers];
            userxuserNfeaturesRatings = new int[nUsers][nUsers];
            computeSimilarities(SimMeasureOption, userxUserSimilarityRatings, userxuserNfeaturesRatings, metadataRatings);
            System.out.println("Computou Similaridade");
            recommender(scoreMatrixRatings, userxUserSimilarityRatings);
            System.out.println("Recomendou");

        } else if (similarityOption == 1) {
            metadataProfile = new Metadata(metadataFile, trainingMatrix.getIndexUserDbSystem());
            scoreMatrixMetadata = new double[nUsers][nItems];
            fillScoreMatrix(scoreMatrixMetadata);
            userxUserSimilarityMetadata = new double[nUsers][nUsers];
            userxuserNfeaturesMetadata = new int[nUsers][nUsers];
            computeSimilarities(SimMeasureOption, userxUserSimilarityMetadata, userxuserNfeaturesMetadata, metadataProfile);
            recommender(scoreMatrixMetadata, userxUserSimilarityMetadata);

        } else if (similarityOption == 2) {
            //recomenda com Sim Ratings
            metadataRatings = new Metadata(trainingFile, trainingMatrix.getIndexUserDbSystem());
            scoreMatrixRatings = new double[nUsers][nItems];
            fillScoreMatrix(scoreMatrixRatings);
            userxUserSimilarityRatings = new double[nUsers][nUsers];
            userxuserNfeaturesRatings = new int[nUsers][nUsers];
            computeSimilarities(SimMeasureOption, userxUserSimilarityRatings, userxuserNfeaturesRatings, metadataRatings);
            recommender(scoreMatrixRatings, userxUserSimilarityRatings);
            System.out.println("Recommendou Ratings");

            //recomenda com Sim Metadata
            metadataProfile = new Metadata(metadataFile, trainingMatrix.getIndexUserDbSystem());
            scoreMatrixMetadata = new double[nUsers][nItems];
            fillScoreMatrix(scoreMatrixMetadata);
            userxUserSimilarityMetadata = new double[nUsers][nUsers];
            userxuserNfeaturesMetadata = new int[nUsers][nUsers];
            computeSimilarities(SimMeasureOption, userxUserSimilarityMetadata, userxuserNfeaturesMetadata, metadataProfile);
            recommender(scoreMatrixMetadata, userxUserSimilarityMetadata);
            System.out.println("Recommendou Metadata");

            //recomenda com similaridade combinada
            scoreMatrixBoth = new double[nUsers][nItems];
            fillScoreMatrix(scoreMatrixBoth);
            userxUserSimilarityBoth = new double[nUsers][nUsers];
            combineSimilarities();
            recommender(scoreMatrixBoth, userxUserSimilarityBoth);
            System.out.println("Recommendou Combinado");
        }
    }

    private void fillScoreMatrix(double[][] scoreMatrix) {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                scoreMatrix[i][j] = 0;
            }
        }
    }

    private void computeMi() {

        int count = 0;

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nItems; j++) {
                if (trainingMatrix.getValue(i, j) != -1) {
                    mi += trainingMatrix.getValue(i, j);
                    count++;
                }
            }
        }
        mi = mi / count;
        //System.out.println(mi);
    }

    private void trainBaselines() {
        bu = new double[nUsers];
        bi = new double[nItems];
        bui = new double[nUsers][nItems];

        computeMi();

        for (int i = 0; i < iterations; i++) {
            fillBiVector();
            fillBuVector();
        }

        fillBuiMatrix();
    }

    private void fillBiVector() {
        int count;
        for (int i = 0; i < bi.length; i++) {
            bi[i] = 0;
            count = 0;
            for (int j = 0; j < nUsers; j++) {
                if (trainingMatrix.getValue(j, i) != -1) {
                    bi[i] += (trainingMatrix.getValue(j, i) - mi - bu[j]);
                    count++;
                }
            }
            if (count > 0) {
                bi[i] = bi[i] / (regBi + count);
            }
            //System.out.println("Bi do item " + i + " : " + bi[i] + " com " + count + " usuarios");
        }
        //System.out.println("Computou bi");
    }

    private void fillBuVector() {
        int count;
        for (int i = 0; i < bu.length; i++) {
            bu[i] = 0;
            count = 0;
            for (int j = 0; j < nItems; j++) {
                if (trainingMatrix.getValue(i, j) != -1) {
                    bu[i] += (trainingMatrix.getValue(i, j) - mi - bi[j]);
                    count++;
                }
            }
            if (count > 0) {
                bu[i] = bu[i] / (regBu + count);
            }
            //System.out.println("Bu do usuario " + i + " : " + bu[i] + " com " + count + " itens");
        }
        //System.out.println("Computou bu");
    }

    private void fillBuiMatrix() {

        for (int i = 0; i < bu.length; i++) {
            for (int j = 0; j < bi.length; j++) {
                bui[i][j] = mi + bu[i] + bi[j];
                //System.out.println("Bui de " + i + " " + j + " : " + bui[i][j]);
            }
        }
        //System.out.println("Computou bui");
    }

    private int computeNFeatures(float[] userA, float[] userB) {
        int nfeatures = 0;
        for (int i = 0; i < userA.length; i++) {
            if ((userA[i] != 0) || (userB[i] != 0)) {
                nfeatures++;
            }
        }
        return nfeatures;
    }

    private void fillNFeaturesMatrix(int[][] userxuserNfeatures, Metadata metadata) {

        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nUsers; j++) {
                if (userxuserNfeatures[i][j] == 0) {
                    if (i != j) {
                        userxuserNfeatures[i][j] = computeNFeatures(metadata.getEntity(i), metadata.getEntity(j));
                        userxuserNfeatures[j][i] = userxuserNfeatures[i][j];
                    }
                }
            }
        }
    }

    private void computeSimilarities(int SimMeasureOption, double[][] userxUserSimilarity, int[][] userxuserNfeatures, Metadata metadata) {
        fillNFeaturesMatrix(userxuserNfeatures, metadata);
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nUsers; j++) {
                userxUserSimilarity[i][j] = 0;
            }
        }
        double nfeatures = 0;
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nUsers; j++) {
                if (userxUserSimilarity[i][j] == 0) {
                    if (i != j) {
                        nfeatures = userxuserNfeatures[i][j];
                        if (SimMeasureOption == 0) {
                            userxUserSimilarity[i][j] = (nfeatures / (nfeatures + regSimilarity)) * dist.calcSimilarity(metadata.getEntity(i), metadata.getEntity(j));
                            userxUserSimilarity[j][i] = userxUserSimilarity[i][j];
                        } else {
                            userxUserSimilarity[i][j] = (nfeatures / (nfeatures + regSimilarity)) * dist.calcSimilarity(metadata.getEntity(i), metadata.getEntity(j));
                            userxUserSimilarity[i][j] = (userxUserSimilarity[i][j] + 1) / 2;
                            userxUserSimilarity[j][i] = userxUserSimilarity[i][j];
                        }
                    } else {
                        userxUserSimilarity[i][j] = 0;
                    }
                }
            }
        }
    }

    private void combineSimilarities() {
        for (int i = 0; i < nUsers; i++) {
            for (int j = 0; j < nUsers; j++) {
                if (userxUserSimilarityBoth[i][j] == 0) {
                    if (i != j) {
                        userxUserSimilarityBoth[i][j] = (userxUserSimilarityRatings[i][j] + userxUserSimilarityMetadata[i][j]) / 2;
                        userxUserSimilarityBoth[j][i] = userxUserSimilarityBoth[i][j];

                    } else {
                        userxUserSimilarityBoth[i][j] = 0;
                    }
                }
            }
        }
    }

    private void recommender(double[][] scoreMatrix, double[][] userxUserSimilarity) {
        for (int i = 0; i < nUsers; i++) {
            recommendItems(i, scoreMatrix, userxUserSimilarity);
            //System.out.println("Recomendou para usuário " + i);
        }
    }

    private void recommendItems(int user, double[][] scoreMatrix, double[][] userxUserSimilarity) {
        for (int i = 0; i < nItems; i++) {
            if (trainingMatrix.getValue(user, i) == -1) {
                generateUserItemScore(user, i, scoreMatrix, userxUserSimilarity);
            }
        }
    }

    private void generateUserItemScore(int user, int item, double[][] scoreMatrix, double[][] userxUserSimilarity) {
        relevantUsers.clear();
        knn.clear();
        big = 2;
        small = -2;

        //recupera os ids dos usuários que avaliaram o item
        for (int i = 0; i < nUsers; i++) {
            if (trainingMatrix.getValue(i, item) != -1) {
                relevantUsers.add(i);
            }
        }

        //obtem vizinhos ordenados por distancia
        for (int i = 0; i < k; i++) {
            idneighbour = -1;
            for (int j = 0; j < relevantUsers.size(); j++) {
                //System.out.println(itemxItemScoresMatrix[item][relevantItems.get(j)]);
                if ((userxUserSimilarity[user][relevantUsers.get(j)] >= small) && (userxUserSimilarity[user][relevantUsers.get(j)] < big)) {
                    idneighbour = relevantUsers.get(j);
                    small = userxUserSimilarity[user][relevantUsers.get(j)];
                }
            }

            small = -2;
            if (idneighbour != -1) {
                //System.out.println("Entrou vizinho");
                big = userxUserSimilarity[user][idneighbour];
                knn.add(idneighbour);
            }

            if (i == relevantUsers.size()) {
                break;
            }

        }

        //calcula score final
        dividendo = 0;
        if (knn.size() > 0) {
            for (int i = 0; i < knn.size(); i++) {
                rvi = trainingMatrix.getValue(knn.get(i), item);
                bvi = bui[knn.get(i)][item];
                scoreMatrix[user][item] += (rvi - bvi) * userxUserSimilarity[user][knn.get(i)];
                dividendo += userxUserSimilarity[user][knn.get(i)];
            }
            if (dividendo == 0) {
                dividendo = 1;
            }
            scoreMatrix[user][item] = bui[user][item] + (scoreMatrix[user][item] / dividendo);
        } else {
            scoreMatrix[user][item] = bui[user][item];
        }
        if (scoreMatrix[user][item] < 1) {
            scoreMatrix[user][item] = 1;

        } else if (scoreMatrix[user][item] > 5) {
            scoreMatrix[user][item] = 5;
        }

    }

    public void writeRecommendations(String recomendationFileRating, String recomendationFileMetadata, String recomendationFileBoth, String testFile, int option) {

        if (option == 0) {
            try {
                File recomendation = new File(recomendationFileRating);
                if (!recomendation.exists()) {
                    recomendation.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(recomendation, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                File file = new File(testFile);
                Scanner scannerFile = new Scanner(file);

                while (scannerFile.hasNextLine()) {
                    String line = scannerFile.nextLine();
                    Scanner scannerLine = new Scanner(line);
                    int user = scannerLine.nextInt();
                    int item = scannerLine.nextInt();
                    bufferedWriter.write(user + " " + item + " " + scoreMatrixRatings[trainingMatrix.getIndexUserDbSystem().get(user)][trainingMatrix.getIndexItemDbSystem().get(item)]);
                    bufferedWriter.write("\n");

                }

                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("Erro na escrita das recomendações.");
            }

        } else if (option == 1) {
            try {
                File recomendation = new File(recomendationFileMetadata);
                if (!recomendation.exists()) {
                    recomendation.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(recomendation, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                File file = new File(testFile);
                Scanner scannerFile = new Scanner(file);

                while (scannerFile.hasNextLine()) {
                    String line = scannerFile.nextLine();
                    Scanner scannerLine = new Scanner(line);
                    int user = scannerLine.nextInt();
                    int item = scannerLine.nextInt();
                    bufferedWriter.write(user + " " + item + " " + scoreMatrixMetadata[trainingMatrix.getIndexUserDbSystem().get(user)][trainingMatrix.getIndexItemDbSystem().get(item)]);
                    bufferedWriter.write("\n");

                }

                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("Erro na escrita das recomendações.");
            }

        } else if (option == 2) {
            try {
                File recomendation = new File(recomendationFileBoth);
                if (!recomendation.exists()) {
                    recomendation.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(recomendation, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                File file = new File(testFile);
                Scanner scannerFile = new Scanner(file);

                while (scannerFile.hasNextLine()) {
                    String line = scannerFile.nextLine();
                    Scanner scannerLine = new Scanner(line);
                    int user = scannerLine.nextInt();
                    int item = scannerLine.nextInt();
                    bufferedWriter.write(user + " " + item + " " + scoreMatrixBoth[trainingMatrix.getIndexUserDbSystem().get(user)][trainingMatrix.getIndexItemDbSystem().get(item)]);
                    bufferedWriter.write("\n");

                }

                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("Erro na escrita das recomendações.");
            }

        }

    }
}
