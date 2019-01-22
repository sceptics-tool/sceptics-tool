package com.meehien.graph;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import com.cvss.*;

import com.aspose.diagram.*;

public class CvssVisioGraph extends RawGraph {

	// something to store the list of nodes and edges in the graph
	private Map<String, Vertex> vertexList = new HashMap<String, Vertex>();
	private Set<Edge> edgeList = new LinkedHashSet<Edge>();

	static String version = "0.5a";

	private Page visioGraph = null;

	// takes in a visio file and the page identifier and starts importing it.
	public CvssVisioGraph(String visio_file, String page) throws Exception {
		InputStream inputStream = new FileInputStream(visio_file);
		Diagram vsdDiagram = new Diagram(inputStream);
		Map<Long, Shape> shapesMap = new HashMap<Long, Shape>();

		visioGraph = vsdDiagram.getPages().getPage(page);

		minusEdge = new CvssEdge();

		buildNodeList(shapesMap);
		buildEdgeList(shapesMap);
	}

	// code which builds up the node list in the graph.
	@SuppressWarnings("unchecked") //apose needs this
	private void buildNodeList(Map<Long, Shape> shapesMap) {
		for (Shape shape : (Iterable<Shape>) visioGraph.getShapes()) {
			// TODO -- we need to consider a CVSS metric being set here?
			//build shapesMap
			shapesMap.put(shape.getID(), shape);
			String shapeName = shape.getText().getValue().getText().replaceAll("\\<.*?>", "").trim().replace("\n", "")
					.replace("\r", " ");
			if (shapeName != "") {
				//System.out.println("Node: "+shape.getID()+" "+shapeName);

				if (!vertexList.keySet().contains(String.valueOf(shape.getID()))) {

					String cvssVectorValue = getShapeCVSSVector(shape);

					if (cvssVectorValue != null && !cvssVectorValue.isEmpty()) {
						if (!cvssVectorValue.equals("0")) {
							//create node
							Vertex vertex = new CvssVertex();
							//get node id
							vertex.setId(String.valueOf(shape.getID()));
							//get node name
							vertex.setName((String) shapeName);

							Cvss cvssVector = null;
							Double cvssValue = null;
							Double tweakingFactor = getShapeTweakFactor(shape);

							//System.out.println("TWEAKING FACTOR: "+tweakingFactor);

							try {
								cvssVector = CvssTransform.getCVSS(cvssVectorValue);
								//cvssValue = CvssTransform.getESCToProb(cvssVector.getVector())*tweakingFactor; //probabilistic
								cvssValue = CvssTransform.getESCToProb(cvssVector.getVector()); // override

								if (tweakingFactor != null) {
									cvssValue = tweakingFactor;
								}
							} catch (Exception e) {
								System.out.println(
										"Node <" + (String) shapeName + "> with id <" + String.valueOf(shape.getID())
												+ "> does not have a CVSS vector. Exception: " + e);
								/*
								for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
									System.out.println(ste);
								}
								*/
								System.exit(1);
							}

							vertex.setDataProfile("", cvssVector, cvssValue, tweakingFactor); // set the Vertex Data Profile

							setShapeDataTypes(vertex, shape);
							setShapeBridges(vertex, shape);

							vertexList.put(String.valueOf(shape.getID()), vertex);
							//System.out.println("CVSSVertex: "+vertex.getId());
						} else {
							System.out.println("Shape " + (String) shapeName
									+ " has an empty CVSS vector. Vector value: " + cvssVectorValue);
						}
					} else {
						//System.out.println("Shape "+(String)shapeName+" has an invalid CVSS vector. Vector value: "+cvssVectorValue);
					}

				} else {
					System.out.println("Duplicate node id " + String.valueOf(shape.getID()));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void buildEdgeList(Map<Long, Shape> shapesMap) {
		Map<String, Edge> connectors = new HashMap<String, Edge>();

		// edges have two endpoints.
		// use a map to keep track of connectors.
		// first encounter initialize edge with start point.
		// second encounter add connector data.
		// optionally, create reverse edge.

		for (Connect connector : (Iterable<Connect>) visioGraph.getConnects()) {
			Edge edge = connectors.get(String.valueOf(connector.getFromSheet()));
			if (edge == null) {
				//START connector
				String profileName = getConnectorString(shapesMap, connector, "profile");
				Edge newEdge = new CvssEdge();
				Vertex source = vertexList.get(String.valueOf(connector.getToSheet()));
				source.addOut(newEdge);
				newEdge.setSource(source);

				connectors.put(String.valueOf(connector.getFromSheet()), newEdge);
			} else {
				//END connector
				Vertex target = vertexList.get(String.valueOf(connector.getToSheet()));
				target.addIn(edge);
				edge.setTarget(target);

				String profileName = getConnectorString(shapesMap, connector, "profile");
				Cvss cvssVector = getConnectorCVSS(shapesMap, connector, "cvss");
				Double tweakingFactor = getConnectorDouble(shapesMap, connector, "tweak");
				Double cvssValue = null;

				if (cvssVector != null) {
					cvssValue = CvssTransform.getESCToProb(cvssVector.getVector());
					if (tweakingFactor != null && tweakingFactor!=0.0) {
						cvssValue = tweakingFactor;
					}
				}
				/* #TODO remove this in final version !!!!!!!!!! */
				else {
					cvssValue = 1.0;
				}
				// if (tweakingFactor!= null && tweakingFactor!=0.0){
				// 	// do something here
				// 	// cvssValue = cvssValue*tweakingFactor; //probabilistic
				// } else{ //TODO THIS NEEDS TO BE REMOVED IN FINAL VERSION
				// 	System.out.println("Overriding Tweak");
				// 	tweakingFactor=1.0;
				// }

				if (profileName == null) {
					System.out.println("Connector [" + edge.getSource().getName() + "]->[" + target.getName()
							+ "] has no data profile.");
					System.exit(1);
				}

				/*
				if (cvssVector==null){
					System.out.println("Connector ["+edge.getSource().getName()+"]->["+target.getName()+"] has no cvss vector." );
					System.exit(1);
				}
				*/

				edge.setDataProfile(profileName, cvssVector, cvssValue, tweakingFactor);

				//TODO Verify direction and define direction in VISIO (maybe by adding new set of arrows).
				//for now duplicates direction
				//System.out.println(getConnectorDirection(shapesMap, connector));
				//System.out.println(getConnectorColor(shapesMap, connector));
				//System.out.println(getConnectorPattern(shapesMap, connector));

				/* also setup reverse edge profile. currently duplicates the fwd edge. */
				Edge edgeRev = new CvssEdge();
				//updates nodes to link contain the edge
				edgeRev.setSource(edge.getTarget());
				edgeRev.setTarget(edge.getSource());
				edge.getSource().addIn(edgeRev);
				edge.getTarget().addOut(edgeRev);
				edgeRev.setDataProfile(profileName, cvssVector, cvssValue, tweakingFactor);
			}
		}
	}

	// set the data types of the node.
	@SuppressWarnings("unchecked")
	private void setShapeDataTypes(Vertex vertex, Shape shape) {
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("data_types")) {
				String[] dataTypesList = propertyItr.getValue().getVal().split(",");
				for (int i = 0; i < dataTypesList.length; i++) {
					vertex.addDataType(dataTypesList[i].trim());
				}
			}
		}
	}

	// looks at the properties of a given node and work out if it bridges to anotehr thing.
	@SuppressWarnings("unchecked")
	private void setShapeBridges(Vertex vertex, Shape shape) {
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("bridge")) {
				String[] bridges = propertyItr.getValue().getVal().split(":");
				String[] bridgesTo = bridges[1].split(",");
				Set<String> bridgesToSet = new LinkedHashSet<String>();
				for (int i = 0; i < bridgesTo.length; i++) {
					bridgesToSet.add(bridgesTo[i].trim());
				}
				vertex.addBridge(bridges[0].trim(), bridgesToSet);
			}
		}
	}

	// get some property in a connector as a double
	@SuppressWarnings("unchecked")
	private Double getConnectorDouble(Map<Long, Shape> shapesMap, Connect connector, String property) {
		Double ret = 0.0;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals(property)) {
				try {
					ret = Double.parseDouble(propertyItr.getValue().getVal());
				} catch (NumberFormatException e) {
					ret = 0.0;
				}
			}
		}
		return ret;
	}

	// get some property in a connector as a String
	@SuppressWarnings("unchecked")
	private String getConnectorString(Map<Long, Shape> shapesMap, Connect connector, String property) {
		String ret = null;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals(property)) {
				ret = String.valueOf(propertyItr.getValue().getVal());
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	// return the CVSS object for a given connector (looking at the cvss property)
	private Cvss getConnectorCVSS(Map<Long, Shape> shapesMap, Connect connector, String property) {
		Cvss ret = null;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals(property)) {
				ret = CvssTransform.getCVSS(String.valueOf(propertyItr.getValue().getVal()));
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private Double getShapeTweakFactor(Shape shape) {
		Double ret = null;
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("tweak")) {
				ret = Double.parseDouble(String.valueOf(propertyItr.getValue().getVal()));
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private String getShapeCVSSVector(Shape shape) {
		String ret = null;
		for (Prop propertyItr : (Iterable<Prop>) shape.getProps()) {
			if (String.valueOf(propertyItr.getLabel().getValue()).equals("cvss")) {
				ret = String.valueOf(propertyItr.getValue().getVal());
				;
			}
		}
		return ret;
	}

	// get the set of edges in the visio file
	public Set<Edge> getEdges() {
		return edgeList;
	}

	// return all the nodes in the graph
	public Map<String, Vertex> getVertices() {
		return vertexList;
	}

	// get the ID of a given node by its name
	public String getVertexIdByName(String value) {
		String ret = null;
		for (String vertex : vertexList.keySet()) {
			if (vertexList.get(vertex).getName().equals(value))
				ret = vertex;
		}
		return ret;
	}

	public Vertex getVertexByName(String value) {
		Vertex ret = null;
		for (String vertex : vertexList.keySet()) {
			if (vertexList.get(vertex).getName().equals(value))
				ret = vertexList.get(vertex);
		}
		return ret;
	}

	public Vertex getVertexById(String value) {
		Vertex ret = null;
		ret = vertexList.get(value);
		return ret;
	}

	// get the direction of a given connector in a map
	@SuppressWarnings("unchecked")
	private Direction getConnectorDirection(Map<Long, Shape> shapesMap, Connect connector) {
		Direction direction = Direction.NODIRECTION;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		direction = getDirection(shape.getLine());
		return direction;
	}

	// get the colour of the line/connector between two nodes
	@SuppressWarnings("unchecked")
	private String getConnectorColor(Map<Long, Shape> shapesMap, Connect connector) {
		String colour = "";
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		colour = shape.getLine().getLineColor().getValue();
		return colour;
	}

	// get the pattern of the connector between two shapes
	@SuppressWarnings("unchecked")
	private int getConnectorPattern(Map<Long, Shape> shapesMap, Connect connector) {
		int connectionPattern = 0;
		Shape shape = shapesMap.get(Long.valueOf(connector.getFromSheet()));
		connectionPattern = shape.getLine().getLinePattern().getValue();
		return connectionPattern;
	}

	// funciton which determines the direction of a given line
	private Direction getDirection(Line l) {
		boolean beginningArrow = l.getBeginArrow().getValue() == 4;
		boolean endArrow = l.getEndArrow().getValue() == 4;

		Direction ret = Direction.NODIRECTION;
		if (!beginningArrow && endArrow)
			ret = Direction.FORWARD;
		if (beginningArrow && !endArrow)
			ret = Direction.BACKWARD;
		if (beginningArrow && endArrow)
			ret = Direction.BIDIRECTIONAL;

		return ret;
	}
}