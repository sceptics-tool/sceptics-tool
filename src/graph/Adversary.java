package com.meehien.graph;
import java.util.*;

public class Adversary{
	String id;
	Set<String> edgeTypes;
	Set<String> entryNodes;


	public Adversary(){
		id = null;
		edgeTypes = new LinkedHashSet<String>();
		entryNodes = new LinkedHashSet<String>();
	}

	// create an Adversary object, but we only care about setting the ID.
	public Adversary(String val){
		id = val;
		edgeTypes = new LinkedHashSet<String>();
		entryNodes = new LinkedHashSet<String>();
	}

	public String getID(){
		return id;
	}
	
	public void setID(String val){
		id=val;
	}

	public Set<String> getEdgeTypes(){
		return edgeTypes;
	}

	public void setEdgeTypes(Set<String> val){
		edgeTypes = val;
	}
	
	public Set<String> getEntryNodes(){
		return entryNodes;
	}

	public void setEntryNodes(Set<String> val){
		entryNodes = val;
	}
}