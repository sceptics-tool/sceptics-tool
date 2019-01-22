package com.meehien.graph;
import java.util.*;

public abstract class Vertex{
	public abstract boolean equals(Object obj);
	public abstract Set<Edge> getIn();
	public abstract Set<Edge> getOut();
	public abstract String getName();
	public abstract String getId();
	public abstract void setId(String val);
	public abstract void setName(String val);
	public abstract void addIn(Edge edge);
	public abstract void addOut(Edge edge);
	public abstract void addDataType(String s);
	public abstract void addBridge(String from, Set<String> to);
	public abstract Set<String> getDataTypes();
	public abstract Set<String> getBridge(String from);
	public abstract Map<String,Set<String>> getBridges();

	public abstract void setDataProfile(String profile, Object... val);
}