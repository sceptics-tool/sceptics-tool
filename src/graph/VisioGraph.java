package com.meehien.graph;

import com.google.common.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.lang.Object;

import com.aspose.diagram.*;

public class VisioGraph extends RawGraph{

	private Map<String,Vertex> vertexList = new HashMap<String,Vertex>();
	private Set<Edge> edgeList = new LinkedHashSet<Edge>();

	static String version = "0.5a";

	private Page visioGraph = null;

	public VisioGraph(String visio_file, String page) throws Exception{
		InputStream inputStream = new FileInputStream(visio_file);
		Diagram vsdDiagram = new Diagram(inputStream);
		Map<Long,Shape> shapesMap = new HashMap<Long, Shape>();

		visioGraph = vsdDiagram.getPages().getPage(page);

		minusEdge = new ClsEdge();

		buildNodeList(shapesMap);
		buildEdgeList(shapesMap);
	}

	@SuppressWarnings("unchecked") //apose needs this
	private void buildNodeList(Map<Long,Shape> shapesMap){
		for (Shape shape : (Iterable<Shape>) visioGraph.getShapes()) {
			//build shapesMap
			shapesMap.put(shape.getID(), shape);
			String shapeName = shape.getText().getValue().getText().replaceAll("\\<.*?>", "").trim().replace("\n", "").replace("\r", " ");
			if (shapeName != "") {
				//System.out.println("Node: "+shape.getID()+" "+shapeName);

				if (!vertexList.keySet().contains(String.valueOf(shape.getID()))){
					//create node

					Vertex vertex = new ClsVertex();

					//get node id
					vertex.setId(String.valueOf(shape.getID()));
					//get node name
					vertex.setName((String)shapeName);
				
					setShapeDataTypes(vertex, shape);
					setShapeBridges(vertex, shape);

					vertexList.put(String.valueOf(shape.getID()),vertex);
					//System.out.println("Vertex: "+vertex.getId());
				} else {
					System.out.println("Duplicate node id "+String.valueOf(shape.getID()));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void buildEdgeList(Map<Long,Shape> shapesMap){
		Map<String,Edge> connectors = new HashMap<String,Edge>();
		
		// edges have two endpoints.
		// use a map to keep track of connectors.
		// first encounter initialize edge with start point.
		// second encounter add connector data.
		// optionally, create reverse edge.

		for (Connect connector : (Iterable<Connect>) visioGraph.getConnects()) {
			Edge edge = connectors.get(String.valueOf(connector.getFromSheet()));
			if (edge == null){
				//FW edge
				Edge newEdge = new ClsEdge();
				Vertex source = vertexList.get(String.valueOf(connector.getToSheet()));
				source.addOut(newEdge);
				newEdge.setSource(source);
				connectors.put(String.valueOf(connector.getFromSheet()),newEdge);
			} else {
				Vertex target = vertexList.get(String.valueOf(connector.getToSheet()));
				target.addIn(edge);
				edge.setTarget(target);

				String profileName = getConnectorString(shapesMap, connector, "profile");
				Double vulnerability	= getConnectorDouble(shapesMap,  connector, "vulnerability");
				Double confidentiality = getConnectorDouble(shapesMap, connector, "confidentiality");
				Double availability = getConnectorDouble(shapesMap, connector, "availability");
				Double integrity = getConnectorDouble(shapesMap, connector, "integrity");

				if (profileName==null){
					System.out.println("Connector ["+edge.getSource().getName()+"]->["+target.getName()+"] has no data profile." );
					System.exit(1);
				}

				edge.setDataProfile(profileName,vulnerability,confidentiality,availability,integrity);

				//TODO Verify direction and define direction in VISIO (maybe by adding new set of arrows).
				//for now duplicates direction

				//System.out.println(getConnectorDirection(shapesMap, connector));
				//System.out.println(getConnectorColor(shapesMap, connector));
				//System.out.println(getConnectorPattern(shapesMap, connector));

				Edge edgeRev = new ClsEdge();
				//updates nodes to link contain the edge
				edgeRev.setSource(edge.getTarget());
				edgeRev.setTarget(edge.getSource());
				edge.getSource().addIn(edgeRev);
				edge.getTarget().addOut(edgeRev);

				edgeRev.setDataProfile(profileName,vulnerability,confidentiality,availability,integrity);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setShapeDataTypes(Vertex vertex, Shape shape){
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("data_types")){
				String[] dataTypesList=propertyItr.getValue().getVal().split(",");
				for (int i=0; i<dataTypesList.length; i++){
						vertex.addDataType(dataTypesList[i].trim());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setShapeBridges(Vertex vertex, Shape shape){
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("bridge")){
				String[] bridges=propertyItr.getValue().getVal().split(":");
				String[] bridgesTo=bridges[1].split(",");
				Set<String> bridgesToSet = new LinkedHashSet<String>();
				for (int i=0; i<bridgesTo.length; i++){
					bridgesToSet.add(bridgesTo[i].trim());
				}
				vertex.addBridge(bridges[0].trim(),bridgesToSet);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Double getConnectorDouble(Map<Long,Shape> shapesMap, Connect connector, String property){
		Double ret = 0.0;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals(property)){
				try {
					ret = Double.parseDouble(propertyItr.getValue().getVal());
				} catch (NumberFormatException e) {
					ret = 0.0;
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private String getConnectorString(Map<Long,Shape> shapesMap, Connect connector, String property){
		String ret = null;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals(property)){
				ret = String.valueOf(propertyItr.getValue().getVal());
			}
		}
		return ret;
	}

	public Set<Edge> getEdges(){
		return edgeList;
	}

	public Map<String,Vertex> getVertices(){
		return vertexList;
	}

	public String getVertexIdByName(String value){
		String ret = null;
		for (String vertex : vertexList.keySet()) {
			if (vertexList.get(vertex).getName().equals(value))
				ret = vertex;
		}
		return ret;
	}

	public Vertex getVertexByName(String value){
		Vertex ret = null;
		for (String vertex : vertexList.keySet()) {
			if (vertexList.get(vertex).getName().equals(value))
				ret = vertexList.get(vertex);
		}
		return ret;
	}

	public Vertex getVertexById(String value){
		Vertex ret = null;
		ret = vertexList.get(value);
		return ret;
	}

	@SuppressWarnings("unchecked")
	private Direction getConnectorDirection(Map<Long,Shape> shapesMap,Connect connector){
		Direction direction = Direction.NODIRECTION;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		direction = getDirection(shape.getLine());
		return direction;
	}

	@SuppressWarnings("unchecked")
	private String getConnectorColor(Map<Long,Shape> shapesMap, Connect connector){
		String colour = "";
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		colour = shape.getLine().getLineColor().getValue();
		return colour;
	}

	@SuppressWarnings("unchecked")
	private int getConnectorPattern(Map<Long,Shape> shapesMap, Connect connector){
		int connectionPattern = 0;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		connectionPattern = shape.getLine().getLinePattern().getValue();
		return connectionPattern;
	}

	private Direction getDirection(Line l) {
		boolean beginningArrow = l.getBeginArrow().getValue() == 4;
		boolean endArrow = l.getEndArrow().getValue() == 4;

		Direction ret = Direction.NODIRECTION;
		if (!beginningArrow && endArrow)
			ret=Direction.FORWARD;
		if (beginningArrow && !endArrow)
			ret=Direction.BACKWARD;
		if (beginningArrow && endArrow)
			ret=Direction.BIDIRECTIONAL;

		return ret;
	}
}