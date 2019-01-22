package com.meehien.graph;

import java.util.*;

public class ProbORGraphAprx extends RoutedGraph {

	RawGraph graph = null;
	private Edge minusEdge = null;
	private String profileProp = null;

	public ProbORGraphAprx(RawGraph inGraph, int dir, String vertex, Set<String> dataTypes, String inProfileProp) {
		super(inGraph, dir, vertex, dataTypes);
		String profileMin = dataTypes.iterator().next();

		profileProp = inProfileProp;

		minusEdge = inGraph.minusEdge;
		minusEdge.setMinusEdge(profileMin);

		minusEdge.setDataProfile(profileMin, profileProp, -1.0);
	}

	//probabilstic OR operation implementation.
	private Double probOR(Double x, Double y) {
		;
		return x + y - x * y;
	}

	private Double edgeProbability(Edge edge) {
		Double edgeProb = 0.0;

		Set<String> edgeProfiles = edge.getDataProfiles();
		Iterator<String> profilesItr = edgeProfiles.iterator();
		while (profilesItr.hasNext()) {
			String currentProfile = profilesItr.next();
			edgeProb = probOR(edgeProb, (Double) edge.getDataProfile(currentProfile, profileProp));

			// RJT - Need to look at, because this is not particularly great.
			// this is a cludge because if the edge prob was -1 (this is hardcoded),
			// we need to fix it.
			if (edgeProb == -1.0)
				edgeProb = 0.0;
			//System.out.println("Edge Profile is "+edgeProb);
		}
		return edgeProb;
	}

	//Checks if a virtual edge is part of a given virtual path. Returns true if the vEdge is in a virtual path, false otherwise.
	private Boolean edgeInPath(Edge edge, Set<Edge> path) {
		Iterator<Edge> pathItr = path.iterator();
		while (pathItr.hasNext()) {
			Edge thisEdge = pathItr.next();
			if (thisEdge.equals(edge)) {
				return true;
			}
		}
		return false;
	}

	// generates the object based equation for the "path probabilistic OR" operation
	// multiply path1 and path2 by iterating through the equation in its current state.
	// Each element of a Set<VEdge>represents an element of the product.
	// Each element of the Set<Set<VEdge>> represents a term in the sum.
	public Set<Set<Edge>> algPathOR(Set<Set<Edge>> path1, Set<Edge> path2) {
		//add current state to the outEq equation
		Set<Set<Edge>> outEq = new LinkedHashSet<Set<Edge>>(path1);
		Set<Edge> path1set = null;
		Edge edgeP2 = null;
		Edge tempEdge = null;
		//add the new path path2 to outEq
		outEq.add(path2);

		//create a path1 * path2 element.
		Iterator<Set<Edge>> path1Itr = path1.iterator();
		while (path1Itr.hasNext()) {
			path1set = path1Itr.next();
			//copies paths in path1set into a newpath 
			Set<Edge> newpath1 = new LinkedHashSet<Edge>(path1set);

			if (!newpath1.add(minusEdge)) {
				newpath1.remove(minusEdge);
			}
			//"multiplies" vpath2 with the elements in newpath1 (=path1).
			// this way of performing the operation ensures that elements in
			// VPath2 are not added to the product if they are already part of newpath1
			Iterator<Edge> path2Itr = path2.iterator();
			while (path2Itr.hasNext()) {
				edgeP2 = path2Itr.next();
				if (!edgeInPath(edgeP2, newpath1)) {
					newpath1.add(edgeP2);
				}
			}
			//append the multiplication element to the outEq
			outEq.add(newpath1);
		}
		//return
		return outEq;
	}

	//replaces the final object path probabilistic OR equation with numerical values and computes the result 
	private Double computeProbOrEq(Set<Set<Edge>> eq) {
		Double sum = 0.0;

		//iterate through all the sum elements
		Iterator<Set<Edge>> eqItr = eq.iterator();
		while (eqItr.hasNext()) {
			Set<Edge> sumElement = eqItr.next();
			Iterator<Edge> sumElementItr = sumElement.iterator();
			Double prod = 1.0;

			//for each sum element compute the product of elememts
			if (sumElementItr.hasNext()) {
				prod = edgeProbability(sumElementItr.next());
			}

			while (sumElementItr.hasNext()) {
				Edge prodElement = null;
				try {
					prodElement = sumElementItr.next();
					prod = prod * edgeProbability(prodElement);
				} catch (Exception e) {
					System.out.println("Edge [" + prodElement.getSource().getName() + "]->["
							+ prodElement.getTarget().getName() + "] does not have any valid data profile.");
					System.exit(1);
				}
			}

			//add the sum elements togather
			sum = sum + prod;
		}
		return sum;
	}

	//computes the overall likelihood for a pathSet 
	//(integrator method for the program; i.e., the main function for computing the overall probability for a path set)
	public Double computeProbability(Set<LinkedList<Vertex>> pathsSet) {
		Set<Set<Edge>> overallProbEq = new LinkedHashSet<Set<Edge>>();
		Set<Edge> edges = null;

		Iterator<LinkedList<Vertex>> pathsSetIterator = pathsSet.iterator();
		LinkedList<Vertex> path;

		if (pathsSetIterator.hasNext()) {
			path = pathsSetIterator.next();
			overallProbEq.add(buildEdgesPath(path));
		}
		while (pathsSetIterator.hasNext()) {
			path = pathsSetIterator.next();
			edges = buildEdgesPath(path);
			overallProbEq = algPathOR(overallProbEq, edges);
		}
		return computeProbOrEq(overallProbEq);
	}

	//computes the overall likelihood for a path 
	//(integrator method for the program; i.e., the main function for computing the overall probability for a path set)
	public Double computeProbability(LinkedList<Vertex> path) {
		Set<Set<Edge>> overallProbEq = new LinkedHashSet<Set<Edge>>();
		Set<Edge> edges = null;

		overallProbEq.add(buildEdgesPath(path));
		edges = buildEdgesPath(path);
		overallProbEq = algPathOR(overallProbEq, edges);
		//System.out.println("Compute Probability: "+computeProbOrEq(overallProbEq));
		return computeProbOrEq(overallProbEq);
	}

	//display a path in console
	public void printPathScores(LinkedList<Vertex> path) {
		Iterator<Vertex> pathIterator = path.iterator();
		while (pathIterator.hasNext()) {
			Vertex vertex = pathIterator.next();
			if (pathIterator.hasNext())
				System.out.print("[" + vertex.getName() + "]->");
			else
				System.out.print("[" + vertex.getName() + "] has a probability of " + computeProbability(path));
		}
		System.out.println();
	}

	//display a path in console
	public Object[] getOutputVals(LinkedList<Vertex> path) {
		String path_string = "";
		Double probability = 0.0;
		Iterator<Vertex> pathIterator = path.iterator();
		while (pathIterator.hasNext()) {
			Vertex vertex = pathIterator.next();
			if (pathIterator.hasNext()) {
				// System.out.print("[" + vertex.getName() + "]->");
				path_string += "[" + vertex.getName() + "]->";
			} else {
				// System.out.println("[" + vertex.getName() + "] has a probability of " + computeProbability(path));
				path_string += "[" + vertex.getName() + "]";
				probability = computeProbability(path);
			}
		}
		return new Object[]{path_string,probability};
	}
}