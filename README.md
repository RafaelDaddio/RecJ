# RecJ
Recommender Systems framework in Java

This framework contains several recommendation algorithms implemented for both rating prediction and item recommendation scenarios, as well as evaluation metrics.

DOCUMENTATION IS CURRENTLY UNDER CONSTRUCTION.` 

# Recommenders
Rating Prediction
- ItemAttributeKNN.java -> the well known item-knn algorithm, which receives as input item-item similarity matrices.They can be based on metadata or one can use it as its original form by passing as metadata the item-user interactions.
  - ItemAttributeKNNv2.java -> a second version of the ItemAttributeKNN where neighbors are first selected as the most similar itens and then intersected with the list of items known to the user.
  - ItemAttributeKNNMultiMetadata.java -> a version of the KNN where it receives two item similarity matrices and combines the neighbors with intersect and union operations.
- ItemNSVD1.java -> a matrix factorization algorithm that uses item metadata in its calculation.
-ItemSoftClustering.java -> a collaborative filtering algorithm that receives as input a soft clustering solution of items.
-UserAttributeKNN.java -> the user-knn algorithm, implemented in the same form as ItemAttributeKNN.java.

Item Recommendation
- ItemAttributeKNNRanking.java -> the item-knn version for the item recommendation scenario. In here, we use the ItemAttributeKNNv2.java implementation but consider only similarities in the score calculation, disregarding ratings and baseline estimates.
  - ItemAttributeKNNRankingMultiMetadata.java -> the ItemAttributeKNNMultiMetadata.java version for the item recommendation scenario.
- CombineTwoUserRanking.java -> given two rankings, this algorithm combines them with union operations.
- CombineTwoUserRankingWithBPR.java -> given two rankings, this algorithm combines them with a BPR Learning-based approach, but regards known-pairs predictions as 1.
- CombineTwoUserRankingWithBPR.java -> given two rankings, this algorithm combines them with a BPR Learning-based approach, but requires known-pairs predictions (scores).

# Evaluation
This framework does not have support for cross-fold validation yet, requiring the user to provide files for each fold in the main.
Nevertheless, several evaluation metrics are implemented:
- Rating Prediction: RMSE
- Item Recommendation: Precision@K and MAP@K

# Metadata
Metadata is handled separately from the recommender algorithms. Some built-in similarity metrics are also implemented as independent modules:
- Pearson correlation coefficient 
- Cosine similarity

# General Guidelines

All coding must be performed in the main method of the class Recommender.java. 

The recommenders and metadata objects require files organized as triples in the form: 
"user item  rating"
"user metadata  score"
"item metadata  score"

Before initiating recommender objects, there must be instantiated an object of the class DataBaseMatrix, which must receive the full database containing user-item interactions in the format of triples. This is important so the system is able to convert each user/item ID into an internal and sequential ID.
Training and test objects must receive the DatabaseMatrix object so they can be instantiated. Some recommenders only require the TraningMatrix object, receiving the test as a file.

Metadata is handled independently of the recommender algorithm. First one must instantiate an Metadata object receiving the metadata file and the listing of items or users from the DatabaseMatrix object. Next, one must instantiate a EntitySimilarity object passing the Metadata object and the similarity option.

Evaluation is also handled independently of the recommender algorithm. Every algorithm must write in a file the recommendations (with the .writeRecommendations(path) method). This file is then passed to an ItemRecommendationMetrics or a RatingPredictionMetrics object, which will handle the evaluation.

An execution example will be released soon.
