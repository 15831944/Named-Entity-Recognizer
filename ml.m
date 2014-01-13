svmStruct = svmtrain(X, Y);
svmPredict = svmclassify(svmStruct,unknownData);

positivePrediction = svmPredict .* correctPrediction;

truePositivePredictV = positivePrediction;
falseNegativePredictV = correctPrediction - positivePrediction;

falsePositivePredictV = svmPredict;
falsePositivePredictV(correctIndexes,:) = 0;

truePositivePrediction = candidateList(logical(truePositivePredictV));
falseNegativePrediction = candidateList(logical(falseNegativePredictV));
falsePositivePrediction = candidateList(logical(falsePositivePredictV));

totalFP = sum(falsePositivePredictV); 
totalFN = size(falseNegativePrediction,1);
totalTP = size(truePositivePrediction,1);

candidateList(correctIndexes,:);