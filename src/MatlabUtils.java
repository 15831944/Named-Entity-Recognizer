import java.util.ArrayList;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;


public class MatlabUtils {
	public static ArrayList<String> predictSoftwareNames(String option) throws MatlabConnectionException, MatlabInvocationException {
		boolean verbose = false;
		if(option.equalsIgnoreCase("verbose"))
			verbose = true;
		
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
	    .setUsePreviouslyControlledSession(true)
	    .setHidden(true)
	    .setMatlabLocation(null).build(); 

	    MatlabProxyFactory factory = new MatlabProxyFactory(options);
	    MatlabProxy proxy = factory.getProxy();
	    String matlabWorkingDirectory = "C:\\Ethan\\Dropbox\\Computer Science\\ICS 499 NLP\\Java";
	    
	    
	    //Create a proxy, which we will use to control MATLAB
	    //MatlabProxyFactory factory = new MatlabProxyFactory();
	    //MatlabProxy proxy = factory.getProxy();

	    //Set a variable, add to it, retrieve it, and print the result
	    //proxy.setVariable("a", 5);
	    //proxy.eval("a = a + 6;");
	    //proxy.eval("disp('hello world')");
	    proxy.eval("addpath('" + matlabWorkingDirectory + "')");
	    proxy.feval("loadData");
	    proxy.feval("ml");
	    
	    int candidateCount = (int) ((double[]) proxy.getVariable("candidateCount"))[0];
	    int realTrueCount = (int) ((double[]) proxy.getVariable("correctCount"))[0];
	    int realFalseCount = candidateCount - realTrueCount;

	    String truePositives[] = ((String[]) proxy.getVariable("truePositivePrediction"));
	    String falseNegatives[] = ((String[]) proxy.getVariable("falseNegativePrediction"));
	    String falsePositives[] = ((String[]) proxy.getVariable("falsePositivePrediction"));
	    ArrayList<String> allPredictions = new ArrayList<String>();

	    int fpCount = (int) ((double[]) proxy.getVariable("totalFP"))[0];
	    int fnCount = (int) ((double[]) proxy.getVariable("totalFN"))[0];
	    int tpCount = (int) ((double[]) proxy.getVariable("totalTP"))[0];
	    
	    for(String word : truePositives)
	    	allPredictions.add(word);
	    for(String word : falsePositives)
	    	allPredictions.add(word);
	    
	    if(verbose) {
		    System.out.println("\tNamed Entity Recognizer Report:");
		    System.out.println("\nIdentified " + tpCount + " out of " + realTrueCount + " names of software. Success rate: " + 
		    		((float) tpCount/realTrueCount * 100) + "%");
		    
		    System.out.println("They were: ");
		    if(truePositives.length == 0)
		    	System.out.println("\tNone");
		    else
			    for(String word : truePositives)
			    	System.out.println("\t" + word);
		    
		    System.out.println("Missed software names were: ");
		    if(falseNegatives.length == 0)
		    	System.out.println("\tNone");
		    else
		    	for(String word : falseNegatives)
			    	System.out.println("\t" + word);
		    
		    System.out.println("\nIdentified " + (realFalseCount - fpCount) + " out of " + realFalseCount + " regular nouns. Success rate: " +
		    		(((float) realFalseCount - fpCount)/realFalseCount * 100 + "%"));
	
		    System.out.println("Nouns misclassified as software names were: ");
		    if(falsePositives.length == 0)
		    	System.out.println("\tNone");
		    else
			    for(String word : falsePositives)
			    	System.out.println("\t" + word);
	    }
	    
	    //Disconnect the proxy from MATLAB
	    proxy.disconnect();
	    
	    return allPredictions;
	}

}
