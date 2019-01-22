package com.meehien.graph;
import java.util.*;

public class ClsEdge extends Edge{
	Vertex sourceVertex;
	Vertex targetVertex;
	Set<String> edgeType;
	String linkType;
	//Map<Set<String>,EdgeDataProfile> dataProfiles;
	Map<String,EdgeDataProfile> dataProfiles;
	Direction direction;

	public static final String VULNERABILITY="vulnerability";
	public static final String CONFIDENTIALITY="confidentiality";
	public static final String AVALABILITY="availability";
	public static final String INTEGRITY="integrity";

	public ClsEdge(){
		sourceVertex = null;
		targetVertex = null;
		Set<String> edgeType = new LinkedHashSet<String>();
		dataProfiles = new HashMap<String,EdgeDataProfile>();
	}

	public ClsEdge(String value){
		if (value!=null){
			sourceVertex = null;
			targetVertex = null;
			dataProfiles = new HashMap<String,EdgeDataProfile>();
			EdgeDataProfile dp = new EdgeDataProfile();
			dp.set((Double)(-1.0),(Double)(-1.0),(Double)(-1.0),(Double)(-1.0));
			dataProfiles.put(value,dp);
		} else
			System.out.println("edgeMinus err");
	}
	
	public ClsEdge(Vertex source, Vertex target){
		sourceVertex = source;
		targetVertex = target;
	}
	
	public boolean equals(Object obj){
		if (obj == this){
			return true;
		}
		
		if (obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		ClsEdge edge = (ClsEdge) obj;
		
		return (sourceVertex == edge.sourceVertex || (sourceVertex != null && sourceVertex.equals(edge.getSource())))
			&& (targetVertex == edge.targetVertex || (targetVertex != null && targetVertex.equals(edge.getTarget())));
	}

	public void setMinusEdge(String profile){
		if (profile!=null){
			sourceVertex = null;
			targetVertex = null;
			dataProfiles = new HashMap<String,EdgeDataProfile>();
			EdgeDataProfile dp = new EdgeDataProfile();
			dp.set((Double)(-1.0),(Double)(-1.0),(Double)(-1.0),(Double)(-1.0));
			dataProfiles.put(profile,dp);
		} else
			System.out.println("edgeMinus err");
	}

	public void setSource(Vertex v){
		sourceVertex = v;
	}
	
	public void setTarget(Vertex v){
		targetVertex = v;
	}
	
	public Vertex getSource(){
		return sourceVertex;
	}
	
	public Vertex getTarget(){
		return targetVertex;
	}

	public void setLinkType(String s){
		linkType = s;
	}
	
	public String getLinkType(){
		return linkType;
	}

	public Set<String> getEdgeType(){
		return edgeType;
	}
    
    public void setDirection(Direction d){
        this.direction=d;
    }
    
    public Direction getDirection(){
        return this.direction;
    }

	public void setDataProfile(String profile, Object... val){
		EdgeDataProfile dp = new EdgeDataProfile();
		dp.set((Double)val[0],(Double)val[1],(Double)val[2],(Double)val[3]);
		dataProfiles.put(profile,dp);
	}

	public void setDataProfile(String profile, String type, Object val){
		EdgeDataProfile dp = dataProfiles.get(profile);
		if (dp==null)
			dp = new EdgeDataProfile();
		dp.set(type,(Double)val);
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
}
