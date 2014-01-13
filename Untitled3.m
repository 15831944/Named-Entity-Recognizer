
%strfind(vocabList, 'zoo');
%freqDelta = sum(goodData(:,1:end-1)) - sum(badData(:,1:end-1));
%goodVocabFreq = [vocabList, num2cell(sum(goodData(:,1:end-1)))];
%deltaVocabFreq = [vocabList, num2cell(freqDelta')];
%[~, indexFreqOrder] = sort(cell2mat(freqDelta(:,2)));
%deltaVocabFreq(indexFreqOrder',:);

%xlswrite('vocabrates.xls', deltaVocabFreq(indexFreqOrder',:));

tree = ClassificationTree.fit(X,Y);
treePredict = predict(tree, unknownData);
treePredict(correctIndexes,:)

wrongTreePredict = treePredict;
wrongTreePredict(correctIndexes,:) = 0;
treeTotalFP = sum(wrongTreePredict); 
treeFPRate = treeTotalFP / size(wrongTreePredict,1);

%sprintf('Tree False Positives %d Percent %f', treeTotalFP, treeFPRate*100)

candidateList(logical(wrongTreePredict))

svmStruct = svmtrain(X, Y);
svmPredict = svmclassify(svmStruct,unknownData);
svmPredict(correctIndexes,:)

wrongSvmPredict = svmPredict;
wrongSvmPredict(correctIndexes,:) = 0;
svmTotalFP = sum(wrongSvmPredict); 
svmFPRate = svmTotalFP / size(wrongSvmPredict,1);
%sprintf('SVM False Positives %d Percent %f', svmTotalFP, svmFPRate*100)

candidateList(logical(wrongSvmPredict))

candidateList(correctIndexes,:)

%nntrain
%nnpredict
