package com.meehien.graph;

import java.util.*;
import java.io.*;
import java.util.stream.IntStream;

public class InclusionExclusionGraph extends RoutedGraph{
	
	RawGraph graph = null;
	private String profile = null;
	private String profileProp = null;

	public InclusionExclusionGraph(RawGraph inGraph, int dir, String vertex, Set<String> dataTypes, String inProfileProp){
		super(inGraph, dir, vertex, dataTypes);
		String profileMin = dataTypes.iterator().next();
		profile = profileMin;

		profileProp = inProfileProp;
	}

	private int[] getSubset(int[] input, int[] subset) {
		int[] result = new int[subset.length]; 
		for (int i = 0; i < subset.length; i++) 
			result[i] = input[subset[i]];
		return result;
	}

	private void generateSets (int[] input, List<int[]> output, int k){
		if (k <= input.length) {

			int[] s = new int[k];	// here we'll keep indices 
								// pointing to elements in input array

			// first index sequence: [ 0, 1, 2, ... ]
			for (int i = 0; (s[i] = i) < k - 1; i++);
			output.add(getSubset(input, s));
			for(;;) {
				int i;
				// find position of item that can be incremented
				for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--); 
				if (i < 0) {
					break;
				}
				s[i]++;						// increment this item
				for (++i; i < k; i++) {	// fill up remaining items
					s[i] = s[i - 1] + 1; 
				}
				output.add(getSubset(input, s));
			}
		} else {
			System.out.println("k is too large.");
		}
	}

	private Double computeSet (int[] set, int k, Map<Integer, Double> pathsProb){
		Double intersection = Math.pow((double)-1, (double)k-1);
		for(int value : set){
			intersection *= pathsProb.get(value);
		}
		return intersection;
	}

	private Double computeSets (int[] input, int k, Map<Integer, Double> pathsProb){
		Double sum = 0.0;
		
		System.out.println("Computing \\binom("+ input.length +","+k+").");

		if (k <= input.length) {

			int[] s = new int[k];	// here we'll keep indices 
										// pointing to elements in input array

			// first index sequence: 0, 1, 2, ...
			for (int i = 0; (s[i] = i) < k - 1; i++);

			sum = computeSet(getSubset(input, s), k, pathsProb);

			for(;;) {
				int i;
				// find position of item that can be incremented
				for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--); 
				if (i < 0) {
					break;
				}
				s[i]++;						// increment this item
				for (++i; i < k; i++) {	// fill up remaining items
					s[i] = s[i - 1] + 1; 
				}
				sum += computeSet(getSubset(input, s),k,pathsProb);
			}
		} else {
			System.out.println("k is too large.");
		}
		return sum;
	}

	private Double probOR(Double x, Double y){
		return x+y-x*y;
	}

	private Double edgeProbability(Edge edge){
		Double edgeProb = 0.0;
		
		Set<String> edgeProfiles = edge.getDataProfiles();
		Iterator<String> profilesItr = edgeProfiles.iterator();
		while (profilesItr.hasNext()) {
			String currentProfile = profilesItr.next();
			edgeProb = probOR(edgeProb, (Double)edge.getDataProfile(currentProfile,profileProp));
		}
		return edgeProb;
	}

	private void computePathsProbability(Set<LinkedList<Vertex>> pathsSet, Map<Integer, Double> pathsProb){
		Set<Edge> edgePath = new LinkedHashSet<Edge>();
		int index = -1;

		Iterator<LinkedList<Vertex>> pathsSetIterator = pathsSet.iterator();
		LinkedList<Vertex> path;
		
		while (pathsSetIterator.hasNext()){
			edgePath = buildEdgesPath(pathsSetIterator.next());
			Double prod = 1.0;

			Iterator<Edge> edgePathItr = edgePath.iterator();
			while (edgePathItr.hasNext()) {
				Edge currentEdge = edgePathItr.next();
				try{
					prod=prod*edgeProbability(currentEdge);
				} catch (Exception e){
					System.out.println("Edge ["+currentEdge.getSource().getName()+"]->["+currentEdge.getTarget().getName()+"] does not have any valid profile." );
					System.exit(1);
				}
			}
			index++;
			pathsProb.put(index, prod);
		}
	}

	private int[] SetToInt(Set<Integer> set) {
		int[] a = new int[set.size()];
		int i = 0;
		for (Integer val : set)
			a[i++] = val;
		return a;
	}

	public Double computeProbability(Set<LinkedList<Vertex>> pathsSet){
		Map<Integer, Double> pathsProb = new HashMap<Integer, Double>(); 
		computePathsProbability(pathsSet, pathsProb);

		int[] input = SetToInt(pathsProb.keySet());
		Double sum = 0.0;

		for(int k = 1; k<=input.length; k++){
			/*
			List<int[]> subsets = new ArrayList<>();
			Double sign = Math.pow((double)-1, (double)k-1);

			generateSets(input, subsets, k);
			for (int[] subset : subsets) {
				Double intersection = 1.0; //multiply elements in set to get intersection value
				for(int value : subset){
					intersection *= pathsProb.get(value);
				}
				sum += sign*intersection;
			}
			subsets.clear();
			*/
			sum += computeSets(input, k, pathsProb);
		}
		
		pathsProb.clear();
		return sum;
	}

}