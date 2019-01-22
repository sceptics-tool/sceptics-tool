package com.meehien.graph;
import java.util.*;
import com.cvss.*;

public class CvssEdge extends Edge{
	CvssVertex sourceVertex;
	CvssVertex targetVertex;
	Set<String> edgeType;
	String linkType;
	//Map<Set<String>,DataProfile> dataProfiles;
	Map<String,CvssEdgeDataProfile> dataProfiles;
	Direction direction;

	public static final String CVSS = "cvss";
	public static final String CVSSVECTOR = "vector";
	public static final String CVSSVALUE="value";
	public static final String CVSSTWEAK="tweak";

	public CvssEdge(){
		sourceVertex = null;
		targetVertex = null;
		Set<String> edgeType = new LinkedHashSet<String>();
		dataProfiles = new HashMap<String,CvssEdgeDataProfile>();
	}

	public CvssEdge(String value){
		if (value!=null){
			sourceVertex = null;
			targetVertex = null;
			dataProfiles = new HashMap<String,CvssEdgeDataProfile>();
		} else
			System.out.println("edgeMinus err");
	}

	public CvssEdge(Vertex source, Vertex target){
		sourceVertex = (CvssVertex) source;
		targetVertex = (CvssVertex) target;
	}
	
	public boolean equals(Object obj){
		if (obj == this){
			return true;
		}
		
		if (obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		CvssEdge edge = (CvssEdge) obj;
		
		return (sourceVertex == edge.sourceVertex || (sourceVertex != null && sourceVertex.equals(edge.getSource())))
			&& (targetVertex == edge.targetVertex || (targetVertex != null && targetVertex.equals(edge.getTarget())));
	}

	public void setMinusEdge(String profile){
		if (profile!=null){
			sourceVertex = null;
			targetVertex = null;
			dataProfiles = new HashMap<String,CvssEdgeDataProfile>();
		} else
			System.out.println("edgeMinus err");
	}
	

	public void setSource(Vertex v){
		sourceVertex = (CvssVertex) v;
	}
	
	public void setTarget(Vertex v){
		targetVertex = (CvssVertex) v;
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
		CvssEdgeDataProfile dp = new CvssEdgeDataProfile();
		dp.set("vector",(Cvss)val[0]);
		dp.set("value",(Double)val[1]);
		dp.set("tweak",(Double)val[2]);
		dataProfiles.put(profile,dp);
	}

	public void setDataProfile(String profile, String type, Object val){
		CvssEdgeDataProfile dp = dataProfiles.get(profile);
		if (dp==null)
			dp = new CvssEdgeDataProfile();
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

