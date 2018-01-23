/*----------

 Usage example: userknn without enriched user profile
                
 TrainingMatrix training = new TrainingMatrix(matrixFile, train);
 UserAttributeKNN u = new UserAttributeKNN(metadata, train, training, 50, 1, 0);        
 u.writeRecommendations(recommendations, "", "", test, 0);
 RatingPredictionMetrics evalRatings = new RatingPredictionMetrics(test, recommendations);
 System.out.println(evalRatings.RMSE());
        
 ------------*/



/*----------

 Usage example: ItemNSVD1
                
 DatabaseMatrix dbMatrix = new DatabaseMatrix(matrixFile); //le base de dados completa e seta matriz
 TrainingMatrix training = new TrainingMatrix(train, dbMatrix);
 String metadataFile = "/home/rafaeldaddio/Documents/ExperimentoSAC/Baseline/item_genre2.txt"; //le representações de item e/ou usuário
 Metadata metadata = new Metadata(metadataFile, dbMatrix.getIndexItemDbSystem());
 ItemNSVD1 i = new ItemNSVD1(training, test, metadata, 1, k); //k = numero de fatores
 i.recommender();
 i.writeRecommendations("/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/recItemNSVD1.dat");
 RatingPredictionMetrics eval = new RatingPredictionMetrics(test, "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/recItemNSVD1.dat");
 System.out.println(eval.RMSE());
        
 ------------*/

/*------------ 

 Usage example: ItemSoftClustering
      
 DatabaseMatrix dbMatrix = new DatabaseMatrix(databaseFile);
 TrainingMatrix training = new TrainingMatrix(trainFile, dbMatrix);
 ItemSoftClustering i = new ItemSoftClustering(testFile, training, 1, persistencyFile, 7);
 i.recommend();
 i.writeRecommendations(outputFile);
 RatingPredictionMetrics eval = new RatingPredictionMetrics(testFile, outputFile);
 System.out.println(eval.RMSE());

 ------------*/

/*------------ 

 Usage example: Prediction Stacking
      
 String validationMetadata1 = path + i + "/Pearson/" + k[j] + "/Combinação/PostCombinationValidacaoTFIDF.dat"
 String validationMetadata2 = path + i + "/Pearson/" + k[j] + "/Combinação/PostCombinationValidacaoSentimento.dat"
 String validationTest = path + i + "/ValidationSet/30-70/test.dat";
 String testMetadata1 = path + i + "/Pearson/" + k[j] + "/TF_IDFBabelfyPearson.dat"; 
 String testMetadata2 = path + i + "/Pearson/" + k[j] + "/SentimentBabelfyCOMPESO.dat"; 
 String finalTest = path + i + "/test.dat"; 
 String recommendations = path + i + "/Pearson/" + k[j] + "/Combinação/PostSingleAlphaForEachItem0_001.dat";
 
 PredictionStackingSingleAlphaForEachItem p = new PredictionStackingSingleAlphaForEachItem(validationMetadata1, validationMetadata2, validationTest, testMetadata1, testMetadata2, finalTest, 0.001); 
 p.train(); 
 p.writePredictions(recommendations); 
 RatingPredictionMetrics eval = new RatingPredictionMetrics(finalTest, recommendations);  
 bufferedWriter.write("      " + eval.RMSE() + "\n");  
 bufferedWriter.flush();

 ------------*/
package recommender;

import evaluation.ItemRecommendationMetrics;
import recommender.algorithms.ratingprediction.ItemAttributeKNN;
import evaluation.RatingPredictionMetrics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import recommender.algorithms.itemrecommendation.CombineTwoUserRanking;
import recommender.algorithms.itemrecommendation.CombineTwoUserRankingWithBPR;
import recommender.algorithms.itemrecommendation.CombineTwoUserRankingWithBPRV2;
import recommender.algorithms.itemrecommendation.ItemAttributeKNNRanking;
import recommender.algorithms.itemrecommendation.ItemAttributeKNNRankingMultiMetadata;
import recommender.algorithms.ratingprediction.ItemAttributeKNNMultiMetadata;
import recommender.algorithms.ratingprediction.ItemNSVD1;
import recommender.algorithms.ratingprediction.ItemSoftClustering;
import recommender.algorithms.ratingprediction.UserAttributeKNN;
import recommender.algorithms.ratingprediction.ItemAttributeKNNv2;
import recommender.algorithms.stack.PredictionStackingDoubleAlpha;
import recommender.algorithms.stack.PredictionStackingDoubleAlphaForEachItem;
import recommender.algorithms.stack.PredictionStackingSingleAlpha;
import recommender.algorithms.stack.PredictionStackingSingleAlphaForEachItem;
import recommender.metadata.EntitySimilarity;
import recommender.metadata.Metadata;
import recommender.ratings.DatabaseMatrix;
import recommender.ratings.TrainingMatrix;

/**
 *
 * @author rafaeldaddio
 */
public class Recommender {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String matrixFile = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Apps_for_Android-ratings.dat"; //base de dados completa (sem divisão de treinamento/teste)
        String termSimilarity = ""; //desconsidera, WordNet

        //se for fazer 10-fold cross-validation, caminho com os folds
        String path = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Folds/folds/";

        //se for usar arquivo unico de treino e teste
        //String train = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/0/train.dat";
        //String test = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/0/test.dat";
        int k[] = {20, 40, 60, 80, 100};

        try {
            File results = new File("/home/rafaeldaddio/Dropbox/resultadosSentimentBabelfyAMAZON.txt"); //arquivo onde escreve RMSE
            if (!results.exists()) {
                results.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(results, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            DatabaseMatrix dbMatrix = new DatabaseMatrix(matrixFile); //le base de dados completa e seta matriz
            System.out.println("Leu matriz geral");
            
//            /*Ratings*/         
//            bufferedWriter.write("\nRatings:\n");
//            for (int j = 0; j < k.length; j++) {
//                bufferedWriter.write("k = " + k[j] + "\n");
//                for (int i = 0; i < 10; i++) {
//                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
//                    String test = path + i + "/test.dat";
//                    
//                    String metadataFile = path + i + "/trainInverted.dat"; //le representações de item e/ou usuário
//                    Metadata metadata = new Metadata(metadataFile, dbMatrix.getIndexItemDbSystem());
//                    System.out.println("Leu metadados");
//                    EntitySimilarity similarityPearson = new EntitySimilarity(metadata, 1, termSimilarity);
//
//                    ItemAttributeKNNRanking knn = new ItemAttributeKNNRanking(training, k[j], similarityPearson.getItemxItemSimilarityMatrix(), 10); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
//                    knn.recommender(); //rotinas de recomendação
//                    String recommendations = path + i + "/Pearson/" + k[j] + "/Combinação/RankingRatings.dat";
//                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
//                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
//                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
//                    bufferedWriter.flush();
//
//                }
//            }
            
            /*Heuristic Terms
            String metadataFile2 = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Reviews/Item_Reviews/Vocabulários/Heuristic Terms/matrizFilteredIF30.txt"; //le representações de item e/ou usuário
            Metadata metadata2 = new Metadata(metadataFile2, dbMatrix.getIndexItemDbSystem());
            System.out.println("Leu metadados");
            EntitySimilarity similarityPearson2 = new EntitySimilarity(metadata2, 1, termSimilarity);
            
            bufferedWriter.write("\nHeuristic Terms:\n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRanking knn = new ItemAttributeKNNRanking(training, k[j], similarityPearson2.getItemxItemSimilarityMatrix(), 100); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingHeuristicSentiment.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }*/
            
            /*BabelFY SF-IDF*/
            String metadataFile3 = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Reviews/Item_Reviews/Vocabulários/Babelfy/matriz_amazon_limiar10.txt"; //le representações de item e/ou usuário
            Metadata metadata3 = new Metadata(metadataFile3, dbMatrix.getIndexItemDbSystem());
            System.out.println("Leu metadados");
            EntitySimilarity similarityPearson3 = new EntitySimilarity(metadata3, 1, termSimilarity);
            
            //bufferedWriter.write("\nBabelFy SF-IDF:\n");
            for (int j = 0; j < k.length; j++) {
                //bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNv2 knn = new ItemAttributeKNNv2(path + i + "/train.dat",training, k[j], 1, similarityPearson3.getItemxItemSimilarityMatrix()); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDFTrain.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    //ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    //bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    //bufferedWriter.flush();

                }
            }
            
            /*BabelFY Sentiment*/      
            String metadataFile4 = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Reviews/Item_Reviews/Vocabulários/Babelfy/matrizBabelfySentiment.dat"; //le representações de item e/ou usuário
            Metadata metadata4 = new Metadata(metadataFile4, dbMatrix.getIndexItemDbSystem());
            System.out.println("Leu metadados");
            EntitySimilarity similarityPearson4 = new EntitySimilarity(metadata4, 1, termSimilarity); //setar qual tipo de similaridade: 1;pearson 0:cosine 2:wordnet

            //bufferedWriter.write("\nBabelFy Sentiment: \n");
            for (int j = 0; j < k.length; j++) {
               // bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                   TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNv2 knn = new ItemAttributeKNNv2(path + i + "/train.dat",training, k[j], 1, similarityPearson4.getItemxItemSimilarityMatrix()); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentimentTrain.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    //ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    //bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    //bufferedWriter.flush();

                }
            }            
            
            /*Pre-comb multiplication*           
            String metadataFile5 = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Reviews/Item_Reviews/Vocabulários/Babelfy/amazonBabelfy_multiplied.txt"; //le representações de item e/ou usuário
            Metadata metadata5 = new Metadata(metadataFile5, dbMatrix.getIndexItemDbSystem());
            System.out.println("Leu metadados");
            EntitySimilarity similarityPearson5 = new EntitySimilarity(metadata5, 1, termSimilarity); //setar qual tipo de similaridade: 1;pearson 0:cosine 2:wordnet

            bufferedWriter.write("\nPre-comb multiplication: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRanking knn = new ItemAttributeKNNRanking(training, k[j], similarityPearson5.getItemxItemSimilarityMatrix(), 100); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/Ranking_precomb_multiplication.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
            /*Pre-comb concatenation*         
            String metadataFile6 = "/media/rafaeldaddio/e76ea905-7a8d-4280-96cb-c9781fced728/Datasets_full/Amazon/Apps/Filtrado_10/Reviews/Item_Reviews/Vocabulários/Babelfy/amazonBabelfy_concatenated.txt"; //le representações de item e/ou usuário
            Metadata metadata6 = new Metadata(metadataFile6, dbMatrix.getIndexItemDbSystem());
            System.out.println("Leu metadados");
            EntitySimilarity similarityPearson6 = new EntitySimilarity(metadata6, 1, termSimilarity); //setar qual tipo de similaridade: 1;pearson 0:cosine 2:wordnet

            bufferedWriter.write("\nPre-comb concatenation: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRanking knn = new ItemAttributeKNNRanking(training, k[j], similarityPearson6.getItemxItemSimilarityMatrix(), 100); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/Ranking_precomb_concatenation.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
            /*Neighbor comb 1
            bufferedWriter.write("\nNeighbor comb 1: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRankingMultiMetadata knn = new ItemAttributeKNNRankingMultiMetadata(training, k[j], similarityPearson4.getItemxItemSimilarityMatrix(), similarityPearson3.getItemxItemSimilarityMatrix(), 100, 1); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/NeighborComb1.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
            /*Neighbor comb 2*
            bufferedWriter.write("\nNeighbor comb 2: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRankingMultiMetadata knn = new ItemAttributeKNNRankingMultiMetadata(training, k[j], similarityPearson4.getItemxItemSimilarityMatrix(), similarityPearson3.getItemxItemSimilarityMatrix(), 100, 2); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/NeighborComb2.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
            /*Neighbor comb 3*
            bufferedWriter.write("\nNeighbor comb 3: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRankingMultiMetadata knn = new ItemAttributeKNNRankingMultiMetadata(training, k[j], similarityPearson4.getItemxItemSimilarityMatrix(), similarityPearson3.getItemxItemSimilarityMatrix(), 100, 3); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/NeighborComb3.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
            /*Neighbor comb 4*
            bufferedWriter.write("\nNeighbor comb 4: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    TrainingMatrix training = new TrainingMatrix(path + i + "/train.dat", dbMatrix); //cria matriz de treinamento, recebendo: caminho pro treino e matriz completa
                    String test = path + i + "/test.dat";

                    ItemAttributeKNNRankingMultiMetadata knn = new ItemAttributeKNNRankingMultiMetadata(training, k[j], similarityPearson4.getItemxItemSimilarityMatrix(), similarityPearson3.getItemxItemSimilarityMatrix(), 100, 4); //item kkn recebe: teste, o k, tipo predição, matriz de similaridade)
                    knn.recommender(); //rotinas de recomendação
                    String recommendations = path + i + "/Pearson/" + k[j] + "/NeighborComb4.dat";
                    knn.writeRecommendations(recommendations); //salvo recomendações no arquivo
                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
            
              /*Pos comb BPR
            bufferedWriter.write("\nPos comb BPR: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    String test = path + i + "/test.dat";
                    String train = path + i + "/train.dat";
                    String rankingfile1 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDF.dat";
                    String rankingfile2 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentiment.dat";

                    CombineTwoUserRankingWithBPR c = new CombineTwoUserRankingWithBPR(train, rankingfile1, rankingfile2, 10);
                   c.train();
                    c.generateFinalRanking();
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingPosCombBRP.dat";
                    c.writeRecommendations(recommendations); //salvo recomendações no arquivo

                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }     
            
            /*Pos comb 1
            bufferedWriter.write("\nPos comb 1: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    String test = path + i + "/test.dat";
                    String rankingfile1 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDF.dat";
                    String rankingfile2 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentiment.dat";

                    CombineTwoUserRanking c = new CombineTwoUserRanking(rankingfile1, rankingfile2, 10, 1);
                    c.recommend();
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingPosComb1.dat";
                    c.writeRecommendations(recommendations); //salvo recomendações no arquivo

                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            } 
            
             /*Pos comb 2
            bufferedWriter.write("\nPos comb 2: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    String test = path + i + "/test.dat";
                    String rankingfile1 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDF.dat";
                    String rankingfile2 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentiment.dat";

                    CombineTwoUserRanking c = new CombineTwoUserRanking(rankingfile1, rankingfile2, 10, 2);
                    c.recommend();
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingPosComb2.dat";
                    c.writeRecommendations(recommendations); //salvo recomendações no arquivo

                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                    bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }             
            
                 
            Pos comb BPR v2 (arthur)*/
           bufferedWriter.write("\nPos comb BPRv2: \n");
            for (int j = 0; j < k.length; j++) {
                bufferedWriter.write("k = " + k[j] + "\n");
                for (int i = 0; i < 10; i++) {
                    String test = path + i + "/test.dat";
                    String train1 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDFTrain.dat";
                    String train2 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentimentTrain.dat";
                    String rankingfile1 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySFIDF.dat";
                    String rankingfile2 = path + i + "/Pearson/" + k[j] + "/RankingBabelFySentiment.dat";

                    CombineTwoUserRankingWithBPRV2 c = new CombineTwoUserRankingWithBPRV2(train1, train2, rankingfile1, rankingfile2, 10);
                    c.train();
                    c.generateFinalRanking();
                    String recommendations = path + i + "/Pearson/" + k[j] + "/RankingPosCombBRPv2.dat";
                    c.writeRecommendations(recommendations); //salvo recomendações no arquivo

                    ItemRecommendationMetrics it = new ItemRecommendationMetrics(recommendations, test);
                   bufferedWriter.write("      " + it.mapAtK(1) + "\t" + it.mapAtK(3) + "\t" + it.mapAtK(5) + "\t" + it.mapAtK(10) + "\n");
                    bufferedWriter.flush();

                }
            }
     } catch (IOException e) {
        }
    }
}
