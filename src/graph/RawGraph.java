package com.meehien.graph;

import java.util.*;

public abstract class RawGraph{
	public Edge minusEdge = null;

	public abstract Set<Edge> getEdges();
	public abstract Map<String,Vertex> getVertices();
	public abstract String getVertexIdByName(String value);
	public abstract Vertex getVertexByName(String value);
	public abstract Vertex getVertexById(String value);
}