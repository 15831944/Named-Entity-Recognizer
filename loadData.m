goodData = csvread('positiveSampleData.txt');
goodData = horzcat(goodData, ones(size(goodData,1), 1));
badData = csvread('negativeSampleData.txt');
badData = horzcat(badData, zeros(size(badData,1), 1));

%figure;
%plot(sum(goodData(:,1:end-1)));
%figure;
%plot(sum(badData));
%figure;
%plot(sum(goodData(:,1:end-1)) - sum(badData(:,1:end-1)));

X = vertcat(goodData,badData);
Y = X(:,end);
X = X(:,1:end-1);

unknownData = csvread('unclassifiedCandidateData.txt');
candidateCount = size(unknownData,1);
%correctIndexes = [26; 76; 190; 335; 344];
%correctIndexes = [15;45;114;186]; % for ics 462
correctIndexes = [118;121;122;124]; % for ics 321
correctPrediction = zeros(candidateCount,1);
correctPrediction(correctIndexes,:) = 1;
correctCount = size(correctIndexes,1);

unknownCorrect = zeros(size(unknownData,1),1);
unknownCorrect(correctIndexes,:) = 1;



fid = fopen('vocabList.txt');
C = textscan(fid,'%s','delimiter','\n');
carray = char(C{:});
vocabList = C{1,1};

fid = fopen('candidateWordList.txt');
C = textscan(fid,'%s','delimiter','\n');
carray = char(C{:});
candidateList = C{1,1};
