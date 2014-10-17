# Compilation (Ignore the unchecked exception)
>> javac *.java

# Build model (extra file model_file-observation-likelihood.txt and 
# model_file-transition-probability.txt will be written as well for easy 
# checking because model_file store the moel in hexadecimal format, to 
# retain java object info)
>> java build_tagger sents.train sents.dev model_file

# Run the tagger
>> java run_tagger sents.test model_file sents.out

# Evaluate on the training set using 10-fold cross validation
# NOTE: can be slow because the affixes uses much more memory and process more probability
>> java Evaluator 
