package com.meehien.graph;

import java.util.*;

public class ProbORGraph extends RoutedGraph{
	
	RawGraph graph = null;
	private Edge minusEdge = null;

	private String profileProp = null;

	public ProbORGraph(RawGraph inGraph, int dir, String vertex, Set<String> dataTypes, String inProfileProp){
		super(inGraph, dir, vertex,dataTypes);

		String profileMin = dataTypes.iterator().next();
		
		profileProp = inProfileProp;
		minusEdge = inGraph.minusEdge;
		minusEdge.setMinusEdge(profileMin);

		minusEdge.setDataProfile(profileMin,profileProp,-1.0);
	}

	//probabilstic OR operation implementation.
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


	//Checks if a virtual edge is part of a given virtual path. Returns true if the vEdge is in a virtual path, false otherwise.
	private Boolean edgeInPath(Edge edge, Set<Edge> path){
		Iterator<Edge> pathItr = path.iterator();
		while (pathItr.hasNext()){
			Edge thisEdge = pathItr.next();
			if (thisEdge.equals(edge)){
				return true;
			}
		}
		return false;
	}

	// generates the object based equation for the "path probabilistic OR" operation
	// multiply path1 and path2 by iterating through the equation in its current state.
	// Each element of a Set<VEdge>represents an element of the product.
	// Each element of the Set<Set<VEdge>> represents a term in the sum.
	public LinkedList<LinkedList<Edge>> algPathOR (LinkedList<LinkedList<Edge>> path1, LinkedList<Edge> path2){
		//add current state to the outEq equation
		LinkedList<LinkedList<Edge>> outEq = new LinkedList<LinkedList<Edge>>(path1);
		LinkedList<Edge> path1set = null;
		Edge edgeP2 = null;
		Edge tempEdge = null;
		//add the new path path2 to outEq
		outEq.add(path2);
		
		//create a path1 * path2 element.
		Iterator<LinkedList<Edge>> path1Itr = path1.iterator();
		while (path1Itr.hasNext()) {
			path1set = path1Itr.next();
			//copies paths in path1set into a newpath 
			LinkedList<Edge> newpath1 = new LinkedList<Edge>(path1set);

			newpath1.add(minusEdge);
			/*if(!newpath1.add(minusEdge)){
				newpath1.remove(minusEdge);
			}/**/

			//"multiplies" vpath2 with the elements in newpath1 (=path1).
			// this way of performing the operation ensures that elements in
			// VPath2 are not added to the product if they are already part of newpath1
			Iterator<Edge> path2Itr = path2.iterator();
			while (path2Itr.hasNext()) {
				edgeP2 = path2Itr.next();
				//if (!edgeInPath(edgeP2,newpath1)){
					newpath1.add(edgeP2);
				//}
			}
			//append the multiplication element to the outEq
			outEq.add(newpath1);
		}
		//return
		return outEq;
	}

	//replaces the final object path probabilistic OR equation with numerical values and computes the result 
	private Double computeProbOrEq(LinkedList<LinkedList<Edge>> eq){
		Double sum=0.0;

		//iterate through all the sum elements
		Iterator<LinkedList<Edge>> eqItr = eq.iterator();
		while (eqItr.hasNext()) {
			LinkedList<Edge> sumElement = eqItr.next();
			Iterator<Edge> sumElementItr = sumElement.iterator();
			Double prod = 1.0;
			
			//for each sum element compute the product of elememts
			if (sumElementItr.hasNext()){
				prod = edgeProbability(sumElementItr.next());
			}
			
			while (sumElementItr.hasNext()) {
				Edge prodElement = null;
				try{
					prodElement = sumElementItr.next();
					prod=prod*edgeProbability(prodElement);
				} catch (Exception e){
					System.out.println("Edge ["+prodElement.getSource().getName()+"]->["+prodElement.getTarget().getName()+"] does not have any valid data profile." );
					System.exit(1);
				}
			}
			
			//add the sum elements togather
			sum=sum+prod;
		}
		return sum;
	}

	//computes the overall likelihood for a pathSet 
	//(integrator method for the program; i.e., the main function for computing the overall probability for a path set)
	public Double computeProbability(Set<LinkedList<Vertex>> pathsSet){		
		LinkedList<LinkedList<Edge>> overallProbEq = new LinkedList<LinkedList<Edge>>();
		LinkedList<Edge> edges = null;
		
		Iterator<LinkedList<Vertex>> pathsSetIterator = pathsSet.iterator();
		LinkedList<Vertex> path;
		
		if (pathsSetIterator.hasNext()){
			path = pathsSetIterator.next();
			LinkedList<Edge> temp = new LinkedList<Edge>(buildEdgesPath(path));
			overallProbEq.add(temp);
		}
		while (pathsSetIterator.hasNext()){
			path = pathsSetIterator.next();
			edges = new LinkedList<Edge>(buildEdgesPath(path));
			overallProbEq=algPathOR(overallProbEq,edges);
		}
		return computeProbOrEq(overallProbEq);
	}
}