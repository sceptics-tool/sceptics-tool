package com.meehien.graph;
import java.util.*;

public abstract class Edge{
	public abstract boolean equals(Object obj);
	public abstract void setSource(Vertex v);
	public abstract void setTarget(Vertex v);
	public abstract Vertex getSource();
	public abstract Vertex getTarget();
	public abstract void setLinkType(String s);
	public abstract String getLinkType();
	public abstract Set<String> getEdgeType();
	public abstract void setDirection(Direction d);
	public abstract Direction getDirection();
	public abstract Set<String> getDataProfiles();
	public abstract void setDataProfile(String profile, String type, Object val);

	public abstract Object getDataProfile(String profile, String type);
	public abstract void setDataProfile(String profile, Object... val);
	public abstract void setMinusEdge(String profile);
}
