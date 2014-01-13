Named-Entity-Recognizer
=======================

Uses bags of words to recognize the category words belong in.

This is a Named Entity Recognizer. The goal was to be able to input a website URL and have it automatically download all the software described in the pages on that site. The core of this application is a Natural Language Processing (NLP) Named Entity Recognizer (NER) which can recognize the names of software. The NER uses the words found in Wikipedia articles to judge if a word software or not. Basically we get a bag of words, turn that into a vector of 0s and 1s representing the words. This vector is then classified using a Support Vector Machine (SVM). SVMs handle large numbers of features well.

This version assumes you have a local MySQL database with Wikipedia installed. It has the functionality to use the official Wikipedia webpage for the data, but you might get banned if you ping them too much.

Future versions will add a general web search to get text about terms that simply dont exist in Wikipedia.

The current output is just the list of the software you should download yourself. Automatically finding the download page for software is going to be a separate project.
