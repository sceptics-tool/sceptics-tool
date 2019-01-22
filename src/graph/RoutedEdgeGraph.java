package com.meehien.graph;

import com.meehien.*;
import com.google.common.collect.*;
import java.util.*;

public class RoutedEdgeGraph{
	private SetMultimap<Vertex,LinkedList<Vertex>> pathsMap = LinkedHashMultimap.create();
	private SetMultimap<Vertex,Set<Edge>> edgePathsMap = LinkedHashMultimap.create();
	private int direction=0;
	private Vertex gVertex;

	private RawGraph graph = null;

	public static final int FWD = 1;
	public static final int REV = -1;

	public RoutedEdgeGraph(RawGraph inGraph, int dir, String vertex){
		graph=inGraph;

		//set grap type
		direction=dir;
		gVertex=graph.getVertices().get(vertex);

		if (direction == 1){
			SearchPathsBFForward(gVertex);
		}else if (direction==(-1)){
			SearchPathsBFReverse(gVertex,null);
			getEdgeGraph();
		}
	}

	public SetMultimap<Vertex,LinkedList<Vertex>> SearchPathsBFForward(Vertex sVertex) {
		//initialises a queue
		Deque<Vertex> queue = new ArrayDeque<Vertex>();
		Map<String,Vertex> vertices = graph.getVertices();
		
		//init queue
		queue.add(sVertex);
		
		//init pathsMap
		Set<Edge> ePath = new LinkedHashSet<Edge>();
		edgePathsMap.put(sVertex,ePath);
		
		while(!queue.isEmpty()){
			//pop queue;
			Vertex vCurrent = queue.remove();
			
			Iterable<Edge> edgesOut = vCurrent.getOut();
			Iterator<Edge> edgesOutIterator = edgesOut.iterator();
			while (edgesOutIterator.hasNext()) {
				boolean onStack = false;

				Edge edgeOut = edgesOutIterator.next();
				

				//get parent paths
				Set<Set<Edge>> eCurrentPaths = edgePathsMap.get(vCurrent);
				Iterator<Set<Edge>> eCurrentPathsItr = eCurrentPaths.iterator();

				//get child (once)
				Vertex vChild = edgeOut.getTarget();
				
				//iterate parent paths
				while (eCurrentPathsItr.hasNext()){
					Set<Edge> eCurrentPath = eCurrentPathsItr.next();
					
					//check for loops
					boolean loop=false;
					//check if we went into the child (vChild) node before from here (vCurrent)
					
					
					Iterator<Edge> eCurrentPathItr = eCurrentPath.iterator();
					while (eCurrentPathItr.hasNext()){
						Edge cmpEdge=eCurrentPathItr.next();
						if (edgeOut.equals(cmpEdge)){
							loop=true;
							break;
						}
					}

					Set<Edge> eChildPath = new LinkedHashSet<Edge>(eCurrentPath);
					eChildPath.add(edgeOut);
					
					if (!loop){
						if (edgePathsMap.put(vChild, eChildPath)){
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
		return pathsMap;
	}

	public SetMultimap<Vertex,LinkedList<Vertex>> SearchPathsBFReverse(Vertex eVertex, Set<String> dataTypes) {
		//initialises a queue
		Deque<Vertex> queue = new ArrayDeque<Vertex>();
		
		Map<String,Vertex> vertices = graph.getVertices();
		
		//HERE CHECK IF NODE HAS BRIDGE
		//init queue
		queue.add(eVertex);
		
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
				if (dataTypes == null){
					hasData=true;
				} else {
					Iterator<String> dataTypeItr = dataTypes.iterator();
					while (dataTypeItr.hasNext()) {
						if(edgeIn.getDataProfiles().contains(dataTypeItr.next())){
							hasData=true;
							break;
						}
					}
				}
				
				if (hasData){
					Vertex vChild = edgeIn.getSource();
					
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

						//HERE CHECK IF NODE HAS BRIDGE
						
						@SuppressWarnings("unchecked")
						LinkedList<Vertex> vChildPath = (LinkedList<Vertex>) vCurrentPath.clone();
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

	public void printPathFromEdgePath(Set<Edge> ePath){
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
				printPathFromEdgePath(ePathSetIterator.next());
			}
		}
	}
}