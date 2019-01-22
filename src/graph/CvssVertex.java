package com.meehien.graph;
import java.util.*;
import com.cvss.*;

public class CvssVertex extends Vertex{
	Set<Edge> inEdges;
	Set<Edge> outEdges;
	String id;
	String name;
	Set<String> dataTypes;
	Map<String,Set<String>> bridges;
	Map<String,CvssVertexDataProfile> dataProfiles;
	
	public static final String CVSSVECTOR = "vector";
	public static final String CVSSVALUE="value";

	public CvssVertex(){
		inEdges = new LinkedHashSet<Edge>();
		outEdges = new LinkedHashSet<Edge>();
		id = null;
		name = null;
		dataTypes = new LinkedHashSet<String>();
		bridges = new HashMap<String,Set<String>>();
		dataProfiles = new HashMap<String,CvssVertexDataProfile>();
	}

	public boolean equals(Object obj){
		if (obj == this){
			return true;
		}
		
		if (obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		CvssVertex vertex = (CvssVertex) obj;
		
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
		inEdges.add((CvssEdge)edge);
	}

	
	public void addOut(Edge edge){
		outEdges.add((CvssEdge)edge);
	}

	public void addDataType(String s){
		dataTypes.add(s);
	}

	public Set<String> getDataType(){
		return dataTypes;
	}

	public void addBridge(String from, Set<String> to){
		bridges.put(from,to);
	}

	public Map<String,Set<String>> getBridge(){
		return bridges;
	}

	public Set<String> getBridge(String from){
		return bridges.get(from);
	}

	public void setDataProfile(String profile, Object... val){
		CvssVertexDataProfile dp = new CvssVertexDataProfile();
		dp.set("vector",(Cvss)val[0]);
		dp.set("value",(Double)val[1]);
		dp.set("tweak",(Double)val[2]);
		dataProfiles.put(profile,dp);
	}

	public void setDataProfile(String profile, String type, Object val){
		CvssVertexDataProfile dp = dataProfiles.get(profile);
		if (dp==null)
			dp = new CvssVertexDataProfile();
		dp.set(type,val);
		dataProfiles.put(profile,dp);
	}
	
	public Object getDataProfile(String profile, String type){
		if (dataProfiles.get(profile)!=null)
			return dataProfiles.get(profile).get(type);
		else
			return null;
	}

	public Set<String> getDataProfiles(){
		return dataProfiles.keySet();
	}

	public Map<String,Set<String>> getBridges(){
		return bridges;
	}

	public Set<String> getDataTypes(){
		return dataTypes;
	}
}