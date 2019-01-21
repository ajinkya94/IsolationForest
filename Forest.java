//P1() main - Takes Input, Find Anomalies, Set Sampling Size based on the predefined percentage, Set number of Trees
//	| calls
//iForest() - Sets Maximum Height, Generate Random Samples from input data
//	| calls								\		\ calls 
//	|								 \		AnomalyCalc- for all input generate a sample and store in HashMap						
//iTrees() - Builds Trees, Recurrsive, set nodes Xl and Xr		  \			\ calls	1						\ calls 2
//									   \ calls	 	 \							avgCalc() - returns for all generating final value e
//					    				    \		PathLength() - generate value of e and populate e_final for all samples  
//									CalcMaxTen() - Final Output Generates

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;


public class Forest {
	//self defined number of trees and sampling size
	public static int t;
	public static int sampling_size;
	public static HashMap<List<Double>, Double> anomalymap = new HashMap<>();
	public static List<Double> e_final = new ArrayList<>();
	public static int anomaliesToFind;
	public static void main(String args[]) throws IOException {
		//input from stdin with error handling based on the user input.
		BufferedReader br=null;
        List<List<Double>> inputArray = new ArrayList<>();
        final String DELIMITER=",";
        try {
        	String line="";
        	br = new BufferedReader(new InputStreamReader(System.in));
        	while((line=br.readLine())!=null) {
        		List<Double> temp = new ArrayList<>();
        		String[] tokens = line.split(DELIMITER);
        		for(String token:tokens) {
        			try {temp.add(Double.valueOf(token));}
        			catch(NumberFormatException nfe){System.out.println("Input not a number");System.exit(0);}
        			}
        		inputArray.add(temp);
        	}
        }
        catch(IndexOutOfBoundsException e) {
        	System.out.println("No Input Found");
        	System.exit(0);
        }
        catch(FileNotFoundException e) {
        	System.out.println("Error occured, File not found.");
        }
        catch(Exception e) {System.out.println("Error occured while taking input");}
        finally {br.close(); if(inputArray.isEmpty()==true) {System.out.println("No Input");System.exit(0);}}
        
        anomaliesToFind = (int)Double.parseDouble((inputArray.get(0).get(0).toString()));
        inputArray.remove(0);
        if(inputArray.isEmpty()==true) {System.out.println("No Input Found in Data Points");System.exit(0);}
        int firstitemsize=0;
        try {
        	firstitemsize=inputArray.get(0).size();
        }
        catch(IndexOutOfBoundsException e) {
        	System.out.println("No Input");
        	System.exit(0);
        }
        
        
        //check if all input parameters are of same size
        for(int checksize=0;checksize<inputArray.size();checksize++) {
        	if(firstitemsize!=inputArray.get(checksize).size()) {
        		System.out.println("Incorrect number of parameters");
        		System.exit(0);
        	}
        }
        if(anomaliesToFind>inputArray.size()) {
        	System.out.println("Number of anomalies to be found are more than input data size which is not possible.");
        	System.exit(0);
        }
        
        
      //calling iForest after defining sub sampling data and iTrees
        sampling_size = (int) Math.abs(((0.4)*inputArray.size()));
        t = (int) Math.abs(((0.3)*inputArray.size()));
        iForest(inputArray,t,sampling_size);
	}
	
	
	public static void iForest(List<List<Double>> inputArray, int t, int sampling_size) {
		//Maximum Height of Tree
		int L = (int) Math.ceil(Math.log(sampling_size)/Math.log(2));
		int e=0;
		Random rand = new Random();
		List<List<Double>> sample = new ArrayList<>();
		List<Node> iForest = new ArrayList<>();
		int randomindex;
		//generate subsample and call iTree
		for(int i=1;i<=t;i++) {
			for(int j=1;j<=sampling_size;j++) {
				randomindex=rand.nextInt(inputArray.size());
				sample.add(inputArray.get(randomindex));
			} 
			iForest.add(iTree(sample,e,L));
			sample.clear();
		}
		//find anomalies for all input data
		for(List<Double> instance:inputArray) {
			e_final.clear();
			anomalymap.put(instance, AnomalyCalc(inputArray,instance,iForest,0));
		}
		
		CalcMaxTen(anomalymap);
		
	}


	public static void CalcMaxTen(HashMap<List<Double>, Double> anomalymap2) {
		//sort the hashmap based on the values i.e anomalyscores
		Set<Entry<List<Double>, Double>> set = anomalymap2.entrySet();
        List<Entry<List<Double>, Double>> list = new ArrayList<Entry<List<Double>, Double>>(
                set);
        
        Collections.sort(list,new Comparator<Map.Entry<List<Double>,Double>>() {
            public int compare(Map.Entry<List<Double>,Double> o1,Map.Entry<List<Double>,Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        int i = list.size();
        List<List<Double>> toptenpoints = new ArrayList<>();
        List<Double> toptenindex= new ArrayList<>();
        
        
        for (Entry<List<Double>, Double> entry : list) {
        		toptenpoints.add(entry.getKey());
        		toptenindex.add(entry.getValue());
        		i=i-1;
        }
        
        int check_num=0;
        int j = toptenpoints.size()-1;
	System.out.println();
        System.out.println("Top "+anomaliesToFind+" Anomalies: ");
        System.out.println();
        
        while(j>=0) {
        	System.out.println(toptenpoints.get(j)+"-->"+toptenindex.get(j));
        	System.out.println();
        	check_num=check_num+1;
        	if(check_num==anomaliesToFind) {
        		j=0;
        	}
        	j=j-1;
        }      
	}


	public static Node iTree(List<List<Double>> X, int e, int l) {
		//starting from height =0 we find the nodes until height is l which is the height prefined based on sample size.
		Node n = new Node(0.0,0,null,null);
		List<List<Double>> Xl = new ArrayList<>();
		List<List<Double>> Xr = new ArrayList<>();
		
		if(e>=l || X.size()<=1) {	
			//exit condition.. size is stored at this point
			n.left=null;
			n.right=null;
			n.set_size(X.size());
		}
		else {
			//check conditions and determines if the input is Xl or Xr
			int q = (int)(Math.random()*X.get(0).size());
			Double mini=X.get(0).get(q);
        	Double maxi=X.get(0).get(q);
        	for(int k=0;k<X.size();k++) {
        		if(maxi<X.get(k).get(q)) { maxi=X.get(k).get(q);}
        		if(mini>X.get(k).get(q)) { mini=X.get(k).get(q);}
        		}
        	Double p = mini + (int)(Math.random() * ((maxi - mini) + 1));
        	for(int i=0;i<X.size();i++) {
        		if(X.get(i).get(q)>=p){ Xr.add(X.get(i));}
        		else { Xl.add(X.get(i));}
        	}
        	n.q = q;
        	n.p = p;
        	n.right= iTree(Xr,e+1,l);
    		n.left= iTree(Xl,e+1,l);
		}
		return n;
	}
	
	public static double AnomalyCalc(List<List<Double>> inputArray, List<Double> instance, List<Node> iForest, int i) {
		double instance_e=0;
		for(Node node:iForest) {
			PathLength(instance,node,0);
		}
		instance_e = avgcalc(e_final);
		//find the anomaly score for every instance
		double anomalyscore = Math.pow(2,-(instance_e)/c_calc(inputArray.size()));
		return anomalyscore;
	}




	private static double avgcalc(List<Double> e_final) {
		double tot_e =0;
		//calc the avg e for every a particular instance
		for(Double item:e_final) {
			tot_e = tot_e+item;
		}

		return tot_e/e_final.size();
	}


	public static double PathLength(List<Double> instance, Node node, int e) {
		//finds pathlength of a particular instance in a tree
		double ef;
		int size_tree;
		if (node.left == null && node.right==null) {
			//exit condition .. calculates c(size) and adds it to the array e_final
            size_tree = node.get_size();
            ef=e+c_calc(size_tree);

            e_final.add(ef);
            return e;
        }
        int coord=node.q;
        //based on the value of instance co-ordinate it is compared to the node value and based on this condition the traversal is done in the tree until exit condition i.e tree ends
        if(node.p>instance.get(coord)){
            PathLength(instance,node.left,e+1);
        }
        else{
            PathLength(instance,node.right,e+1);
        }
        return e;
	}
	public static double c_calc(int size_tree) {
		//func to calculate C based on the formula in the document.
		if(size_tree<=1) { return 0;}
		return 2*((Math.log(size_tree-1)+0.5772156649)-2*(size_tree-1)/size_tree);
	}
	
}

