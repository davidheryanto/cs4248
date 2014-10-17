# Compilation (ignore unchecked warning)
>> javac *.java

# Build model
# Additional files:
# 1.model_file-observation-likelihood.txt and 
# 2.model_file-transition-probability.txt
# will be written for easier to see the model values.
# model_file is in hex format to retain Java object structure.
>> java build_tagger sents.train sents.dev model_file

# Run tagger
>> java run_tagger sents.test model_file sents.out

# Evaluate using 10-fold cross validation on training file
# May be slow because the additional affixes consume much more memory and
# require more processing.
# Alternatively, don't use affix estimation by commenting out the use of affix # in Model.java to speed up the process
>> java Evaluator