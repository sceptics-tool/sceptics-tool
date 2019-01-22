package com.meehien.graph;
import java.util.*;


public class ClsVertex extends Vertex{
	Set<Edge> inEdges;
	Set<Edge> outEdges;
	String id;
	String name;
	Set<String> dataTypes;
	Map<String,Set<String>> bridges;

	public ClsVertex(){
		inEdges = new LinkedHashSet<Edge>();
		outEdges = new LinkedHashSet<Edge>();
		id = null;
		name = null;
		dataTypes = new LinkedHashSet<String>();
		bridges = new HashMap<String,Set<String>>();
	}

	public boolean equals(Object obj){
		if (obj == this){
			return true;
		}
		
		if (obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		ClsVertex vertex = (ClsVertex) obj;
		
		return (id == vertex.id || (id != null && id.equals(vertex.getId())));
	}

	public Set<Edge> getIn(){
		return inEdges;
	}
	
	public Set<Edge> getOut(){
		return outEdges;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public void setId(String val){
		id=val;
	}

	public void setName(String val){
		name=val;
	}

	public void addIn(Edge edge){
		inEdges.add(edge);
	}

	
	public void addOut(Edge edge){
		outEdges.add(edge);
	}

	public void addDataType(String s){
		dataTypes.add(s);
	}

	public Set<String> getDataTypes(){
		return dataTypes;
	}

	public void addBridge(String from, Set<String> to){
		bridges.put(from,to);
	}

	public Map<String,Set<String>> getBridges(){
		return bridges;
	}

	public Set<String> getBridge(String from){
		return bridges.get(from);
	}

	public void setDataProfile(String profile, Object... val){
	}
}