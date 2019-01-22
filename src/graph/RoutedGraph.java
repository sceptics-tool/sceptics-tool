package com.meehien.graph;

import com.meehien.*;
import com.google.common.collect.*;
import java.util.*;

public class RoutedGraph{
	private SetMultimap<Vertex,LinkedList<Vertex>> pathsMap = LinkedHashMultimap.create();
	private SetMultimap<Vertex,Set<Edge>> edgePathsMap = LinkedHashMultimap.create();
	private int direction=0;
	private Vertex gVertex;

	private RawGraph graph = null;

	public static final int FWD = 1;
	public static final int REV = -1;

	public RoutedGraph(RawGraph inGraph, int dir, String vertex, Set<String> dataTypes){
		graph=inGraph;

		//set graph type
		direction=dir;
		gVertex=graph.getVertices().get(vertex);
		if (gVertex==null){
			System.out.println("Vertex non-existent");
			System.exit(0);
		}

		if (direction == 1){
			SearchPathsBFForward(gVertex,dataTypes);
		}else if (direction==(-1))
			SearchPathsBFReverse(gVertex,dataTypes);

		getEdgeGraph();
	}

	public SetMultimap<Vertex,LinkedList<Vertex>> SearchPathsBFForward(Vertex sVertex, Set<String> dataTypes) {
		//initialises a queue
		Deque<Vertex> queue = new ArrayDeque<Vertex>();
		Map<String,Vertex> vertices = graph.getVertices();
		
		//HERE CHECK IF NODE HAS BRIDGE

		//init queue
		boolean sourceHasData = false;
		Iterator<String> dataTypeItr = dataTypes.iterator();
		while (dataTypeItr.hasNext()) {
			if(sVertex.getDataTypes().contains(dataTypeItr.next())){
				sourceHasData=true;
				break;
			}
		}
		if (sourceHasData){
			queue.add(sVertex);
		} else {
			System.out.println("Data profiles are invalid for start node.");
			return pathsMap;
		}
		
		//init pathsMap
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(sVertex);
		pathsMap.put(sVertex,path);
		
		while(!queue.isEmpty()){
			Vertex vCurrent = queue.remove();
			
			Iterable<Edge> edgesOut = vCurrent.getOut();
			
			Iterator<Edge> edgesOutIterator = edgesOut.iterator();
			while (edgesOutIterator.hasNext()) {
				Edge edgeOut = edgesOutIterator.next();

				//validate edge supports dataType
				boolean hasData = false;
					/*
				Iterator<String> dataTypeItr = nodeDataTypes.iterator();
				while (dataTypeItr.hasNext()) {
					if(edgeOut.getDataProfiles().contains(dataTypeItr.next())){
						hasData=true;
						break;
					}
				}
				*/

				//Set<String> nodeDataTypes = vCurrent.getDataTypes();
				//HERE CHECK IF NODE HAS BRIDGE
				
				Map<String,Set<String>> bridges = vCurrent.getBridges();
				Set<String> nodeDataTypes = vCurrent.getDataTypes();
				
				Iterator<String> nodeDataTypeItr = nodeDataTypes.iterator();
				while (nodeDataTypeItr.hasNext()) {
					String nodeDataType = nodeDataTypeItr.next();
					Set<String> currentBridge = bridges.get(nodeDataType);
					if (currentBridge != null){
						Iterator<String> bridgeDataTypeItr = currentBridge.iterator();
						while (bridgeDataTypeItr.hasNext()) {
							if(edgeOut.getDataProfiles().contains(bridgeDataTypeItr.next())){
								hasData=true;
								break;
							}
						}
					}
				}
				/*
				Map<String,Set<String>> bridges = vCurrent.getBridges();
				for(String brFrom: bridges.keySet()){
					Set<String> currentBridge = bridges.get(brFrom);
					Iterator<String> dataTypeItr = currentBridge.iterator();
					while (dataTypeItr.hasNext()) {
						if(edgeOut.getDataProfiles().contains(dataTypeItr.next())){
							hasData=true;
							break;
						}
					}
				}
				*/

				if (hasData){
					Vertex vChild = edgeOut.getTarget();
					
					boolean childHasData = false;
					Set<String> childNodeDataTypes = vChild.getDataTypes();
					Iterator<String> childNodeDataTypeItr = childNodeDataTypes.iterator();
					while (childNodeDataTypeItr.hasNext()) {
						if(edgeOut.getDataProfiles().contains(childNodeDataTypeItr.next())){
							childHasData=true;
							break;
						}
					}

					if (childHasData) {
						boolean onStack = false;
						Set<LinkedList<Vertex>> vCurrentPaths = pathsMap.get(vCurrent);
						
						Iterator<LinkedList<Vertex>> vCurrentPathsItr = vCurrentPaths.iterator();
						while (vCurrentPathsItr.hasNext()){
							LinkedList<Vertex> vCurrentPath = vCurrentPathsItr.next();
							
							//check for loops
							boolean loop=false;
							//check if we went into the child (vChild) node before from here (vCurrent)
							Iterator<Vertex> vCurrentPathItr = vCurrentPath.iterator();
							while (vCurrentPathItr.hasNext()){
								Vertex cmpVertex=vCurrentPathItr.next();
								if (vChild.equals(cmpVertex)){
									loop=true;
									break;
								}
							}
							

							LinkedList<Vertex> vChildPath = new LinkedList<Vertex>(vCurrentPath);
							vChildPath.add(vChild);
							
							if (!loop){
								if (pathsMap.put(vChild, vChildPath)){
									//add to queue
									if(!onStack){
										queue.add(vChild);
										onStack=true;
									}
								}
							}
						}
					}
				}
			}
		}
		return pathsMap;
	}

	public SetMultimap<Vertex,LinkedList<Vertex>> SearchPathsBFReverse(Vertex eVertex, Set<String> dataTypes) {
		//initialises a queue
		Deque<Vertex> queue = new ArrayDeque<Vertex>();
		
		Map<String,Vertex> vertices = graph.getVertices();
		
		//init queue
		boolean sourceHasData = false;
		Iterator<String> dataTypeItr = dataTypes.iterator();
		while (dataTypeItr.hasNext()) {
			if(eVertex.getDataTypes().contains(dataTypeItr.next())){
				sourceHasData=true;
				break;
			}
		}
		if (sourceHasData){
			queue.add(eVertex);
		} else {
			System.out.println("Data profiles are invalid for end node.");
			return pathsMap;
		}
		
		//init pathsMap
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(eVertex);
		pathsMap.put(eVertex,path);
		
		while(!queue.isEmpty()){
			Vertex vCurrent = queue.remove();
			
			Iterable<Edge> edgesIn = vCurrent.getIn();
			
			Iterator<Edge> edgesInIterator = edgesIn.iterator();
			while (edgesInIterator.hasNext()) {
				Edge edgeIn = edgesInIterator.next();
				//System.out.println(edgeIn.getLinkType());

				//validate edge supports dataType
				boolean hasData = false;
				Set<String> nodeDataTypes = vCurrent.getDataTypes();
				Iterator<String> nodeDataTypeItr = nodeDataTypes.iterator();
				while (nodeDataTypeItr.hasNext()) {
					if(edgeIn.getDataProfiles().contains(nodeDataTypeItr.next())){
						hasData=true;
						break;
					}
				}
				
				if (hasData){
					Vertex vChild = edgeIn.getSource();
					
					boolean childHasData = false;

					Map<String,Set<String>> bridges = vChild.getBridges();
					Set<String> childDataTypes = vChild.getDataTypes();
					
					Iterator<String> childDataTypesItr = childDataTypes.iterator();
					while (childDataTypesItr.hasNext()) {
						String childDataType = childDataTypesItr.next();
						Set<String> currentBridge = bridges.get(childDataType);
						if (currentBridge != null){
							Iterator<String> bridgeDataTypeItr = currentBridge.iterator();
							while (bridgeDataTypeItr.hasNext()) {
								if(edgeIn.getDataProfiles().contains(bridgeDataTypeItr.next())){
									childHasData=true;
									break;
								}
							}
						}
					}

					if (childHasData) {
						boolean onStack = false;
						Set<LinkedList<Vertex>> vCurrentPaths = pathsMap.get(vCurrent);
						
						Iterator<LinkedList<Vertex>> vCurrentPathsItr = vCurrentPaths.iterator();
						while (vCurrentPathsItr.hasNext()){
							LinkedList<Vertex> vCurrentPath = vCurrentPathsItr.next();
							
							//check for loops
							boolean loop=false;
							Iterator<Vertex> vCurrentPathItr = vCurrentPath.iterator();
							while (vCurrentPathItr.hasNext()){
								Vertex cmpVertex=vCurrentPathItr.next();
								if (vChild.equals(cmpVertex)){
									loop=true;
									break;
								}
							}

							LinkedList<Vertex> vChildPath = new LinkedList<Vertex>(vCurrentPath);
							vChildPath.addFirst(vChild);
							
							if (!loop){
								if (pathsMap.put(vChild, vChildPath)){
									//add to queue
									if(!onStack){
										queue.addFirst(vChild);
										onStack=true;
									}
								}
							}
						}
					}
				}
			}//while end
		}
		return pathsMap;
	}

	public Set<Edge> buildEdgesPath(LinkedList<Vertex> path){
		Set<Edge> edgePath = new LinkedHashSet<Edge>();
		Iterator<Vertex> pathIterator = path.iterator();
		
		Vertex previousVertex = null;
		Vertex currentVertex = pathIterator.next();
		while (pathIterator.hasNext()){
			previousVertex = currentVertex;
			currentVertex = pathIterator.next();
			
			Iterable<Edge> edgesOut = previousVertex.getOut();
			Iterator<Edge> edgesOutIterator = edgesOut.iterator();
			while (edgesOutIterator.hasNext()){
				Edge edge=edgesOutIterator.next();
				if (edge.getTarget().equals(currentVertex)){
					edgePath.add(edge);
				}
			}
		}
		return edgePath;
	}

	public void getEdgeGraph(){
		for(Vertex vertex: pathsMap.keySet()){
			Set<LinkedList<Vertex>> vPathSet=pathsMap.get(vertex);
			Iterator<LinkedList<Vertex>> vPathSetIterator = vPathSet.iterator();
			//iterate paths of vertices between <vertex> and <inVertex>
			while (vPathSetIterator.hasNext()){
				//convert vertex path to edge path and 
				Set<Edge> edgePath = buildEdgesPath(vPathSetIterator.next());
				if (edgePath.size()!=0){
					//add to Multimap if not 0
					edgePathsMap.put(vertex,edgePath);
				}
			}
		}
	}

	//display a path in console
	public void printVertexPath(LinkedList<Vertex> path){
		Iterator<Vertex> pathIterator = path.iterator();
		while (pathIterator.hasNext()){
			Vertex vertex = pathIterator.next();
			if (pathIterator.hasNext())
				System.out.print("[" + vertex.getName() +"]->");
			else
				System.out.print("[" + vertex.getName()+"]");
		}
		System.out.println();
	}

	public void printEdgePath(Set<Edge> ePath){
		Iterator<Edge> pathIterator = ePath.iterator();
		while (pathIterator.hasNext()){
			Edge edge = pathIterator.next();
			if (pathIterator.hasNext()){
				System.out.print("[" + edge.getSource().getName() +"]->");
			} else {
				System.out.print("[" + edge.getSource().getName() +"]->");
				System.out.print("[" + edge.getTarget().getName() +"]");
			}
		}
		System.out.println();
	}

	public void printAllEdgePaths (){
		for(Vertex vertex: edgePathsMap.keySet()){
			if (direction == 1){
				System.out.println("From ["+gVertex.getName()+"] to ["+vertex.getName()+"]:");
			}else if (direction==(-1))
				System.out.println("From ["+vertex.getName()+"] to ["+gVertex.getName()+"]:");
			
			Set<Set<Edge>> ePathSet=edgePathsMap.get(vertex);
			Iterator<Set<Edge>> ePathSetIterator = ePathSet.iterator();
			while (ePathSetIterator.hasNext()){
				printEdgePath(ePathSetIterator.next());
			}
		}
	}

	public void printAllVertexPaths (){
		for(Vertex vertex: pathsMap.keySet()){
			if (direction == 1){
				System.out.println("From ["+gVertex.getName()+"] to ["+vertex.getName()+"]:");
			}else if (direction==(-1))
				System.out.println("From ["+vertex.getName()+"] to ["+gVertex.getName()+"]:");
			
			Set<LinkedList<Vertex>> vPathSet=pathsMap.get(vertex);
			Iterator<LinkedList<Vertex>> vPathSetIterator = vPathSet.iterator();
			while (vPathSetIterator.hasNext()){
				printVertexPath(vPathSetIterator.next());
			}
		}
	}


	//builds a set of paths (i.e. pathsSet) of all the paths starting in vertex, or ending in vertex.
	public Set<LinkedList<Vertex>> getPaths(Vertex vertex){
		Set<LinkedList<Vertex>> pathsSet = new LinkedHashSet<LinkedList<Vertex>>();
		Set<LinkedList<Vertex>> pathsToEndNode=pathsMap.get(vertex);

		Iterator<LinkedList<Vertex>> pathsToEndNodeItr = pathsToEndNode.iterator();
		while (pathsToEndNodeItr.hasNext()){
			LinkedList<Vertex> path = pathsToEndNodeItr.next();
			pathsSet.add(path);
		}
		return pathsSet;
	}

	/*
	//builds a set of paths (i.e. pathsSet) of all the paths as a list
	public Set<LinkedList<Vertex>> getPaths(Set<String> dataTypes){
		Set<LinkedList<Vertex>> pathsSet = new LinkedHashSet<LinkedList<Vertex>>();

		for(Vertex vertexKey: pathsMap.keySet()){
			boolean hasData = false;
			Iterator<String> dataTypeItr = dataTypes.iterator();
			while (dataTypeItr.hasNext()) {
				if(vertexKey.getDataTypes().contains(dataTypeItr.next())){
					hasData=true;
					break;
				}
			}

			if (hasData){
				Set<LinkedList<Vertex>> pathsToEndNode=pathsMap.get(vertexKey);

				Iterator<LinkedList<Vertex>> pathsToEndNodeItr = pathsToEndNode.iterator();
				while (pathsToEndNodeItr.hasNext()){
					LinkedList<Vertex> path = pathsToEndNodeItr.next();
					if (path.size() > 1)
						pathsSet.add(path);
				}
			}
		}
		return pathsSet;
	}*/
}