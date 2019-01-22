package com.meehien.graph;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;

public class XmlGraph extends RawGraph{

	private Map<String,Vertex> vertexList = new HashMap<String,Vertex>();
	private Set<Edge> edgeList = new LinkedHashSet<Edge>();

	public XmlGraph(String xml_file) throws Exception{
		File inputFile = new File(xml_file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();

		minusEdge = new ClsEdge();

		buildNodeList(doc);
		buildEdgeList(doc);
	}

	private void buildNodeList(Document doc){
		NodeList docVertices = doc.getElementsByTagName("node");
		for (int docVerticesItr = 0; docVerticesItr < docVertices.getLength(); docVerticesItr++){
			Node docNode = docVertices.item(docVerticesItr);
			if (docNode.getNodeType() == Node.ELEMENT_NODE) {
				Element docNodeElement = (Element) docNode;
				if (!vertexList.keySet().contains((String)docNodeElement.getAttribute("id"))){
					//create node
					Vertex vertex = new ClsVertex();
					//get node id
					vertex.setId((String)docNodeElement.getAttribute("id"));
					//get node name
					vertex.setName((String)docNodeElement.getAttribute("name"));
					
					//get node data_types
					NodeList docNodeDataTypes = docNodeElement.getElementsByTagName("data_types");
					for (int docNodeDataTypesItr = 0; docNodeDataTypesItr < docNodeDataTypes.getLength(); docNodeDataTypesItr++){
						Node docNodeDataType = docNodeDataTypes.item(docNodeDataTypesItr);
						if (docNodeDataType.getNodeType() == Node.ELEMENT_NODE) {
							Element docNodeDataTypeElement = (Element) docNodeDataType;
							//get all the type values in data_types
							NodeList docNodeTypes = docNodeDataTypeElement.getElementsByTagName("type");
							for (int docNodeTypesItr = 0; docNodeTypesItr < docNodeTypes.getLength(); docNodeTypesItr++){
								Node docNodeType = docNodeTypes.item(docNodeTypesItr);
								if (docNodeType.getNodeType() == Node.ELEMENT_NODE) {
									Element docNodeTypeElement = (Element) docNodeType;
									vertex.addDataType((String)docNodeTypeElement.getTextContent());
								}
							}
						}
					}

					//get bridges
					NodeList docNodeBridges = docNodeElement.getElementsByTagName("bridge");
					for (int docNodeBridgesItr = 0; docNodeBridgesItr < docNodeBridges.getLength(); docNodeBridgesItr++){
						Node docNodeBridge = docNodeBridges.item(docNodeBridgesItr);
						if (docNodeBridge.getNodeType() == Node.ELEMENT_NODE) {
							Element docNodeBridgeElement = (Element) docNodeBridge;

							String from = (String)docNodeBridgeElement.getAttribute("from");
							Set<String> to = new LinkedHashSet<String>();

							//get all the destinations
							NodeList docNodeTos = docNodeBridgeElement.getElementsByTagName("to");
							for (int docNodeTosItr = 0; docNodeTosItr < docNodeTos.getLength(); docNodeTosItr++){
								Node docNodeTo = docNodeTos.item(docNodeTosItr);
								if (docNodeTo.getNodeType() == Node.ELEMENT_NODE) {
									Element docNodeToElement = (Element) docNodeTo;
									to.add((String)docNodeToElement.getTextContent());
								}
							}

							vertex.addBridge(from,to);
							//System.out.println(vertex.getBridge());
						}
					}
					vertexList.put((String)docNodeElement.getAttribute("id"),vertex);
					//System.out.println("Vertex: "+vertex.getId());
				} else {
					System.out.println("Duplicate node id "+(String)docNodeElement.getAttribute("id"));
				}
			}
		}
	}

	private void buildEdgeList(Document doc){
		NodeList docEdges = doc.getElementsByTagName("edge");
		for (int docEdgesItr = 0; docEdgesItr < docEdges.getLength(); docEdgesItr++){
			Node docNode = docEdges.item(docEdgesItr);
			if (docNode.getNodeType() == Node.ELEMENT_NODE) {
				Element docNodeElement = (Element) docNode;

				//new edge
				Edge edge = new ClsEdge();
				//get start vertex
				Vertex source = vertexList.get((String)docNodeElement.getAttribute("source"));
				//get end vertex
				Vertex target = vertexList.get((String)docNodeElement.getAttribute("target"));

				//updates nodes to link contain the edge
				edge.setSource(source);
				edge.setTarget(target);
				source.addOut(edge);
				target.addIn(edge);
				
				//get edge type
				//edge.setLinkType((String)docNodeElement.getAttribute("linkType"));

				//get data profiles
				NodeList docEdgeDataProfiles = docNodeElement.getElementsByTagName("data_profile");
				for (int docEdgeDataProfilesItr = 0; docEdgeDataProfilesItr < docEdgeDataProfiles.getLength(); docEdgeDataProfilesItr++){
					Node docEdgeDataProfile = docEdgeDataProfiles.item(docEdgeDataProfilesItr);
					if (docEdgeDataProfile.getNodeType() == Node.ELEMENT_NODE) {
						Element docEdgeDataProfileElement = (Element) docEdgeDataProfile;

						String dataType = (String)docEdgeDataProfileElement.getAttribute("dataType");
						String linkType = (String)docEdgeDataProfileElement.getAttribute("linkType");

						Node vNode = docEdgeDataProfileElement.getElementsByTagName("vulnerability").item(0);
						if (vNode.getNodeType() == Node.ELEMENT_NODE) {
								Element vNodeElement = (Element) vNode;
								edge.setDataProfile(dataType,ClsEdge.VULNERABILITY, Double.parseDouble((String)vNode.getTextContent()));
						}

						Node cNode = docEdgeDataProfileElement.getElementsByTagName("confidentiality").item(0);
						if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								Element cNodeElement = (Element) cNode;
								edge.setDataProfile(dataType,ClsEdge.CONFIDENTIALITY, Double.parseDouble((String)cNode.getTextContent()));
						}

						Node aNode = docEdgeDataProfileElement.getElementsByTagName("availability").item(0);
						if (aNode.getNodeType() == Node.ELEMENT_NODE) {
								Element aNodeElement = (Element) aNode;
								edge.setDataProfile(dataType,ClsEdge.AVALABILITY, Double.parseDouble((String)aNode.getTextContent()));
						}

						Node iNode = docEdgeDataProfileElement.getElementsByTagName("integrity").item(0);
						if (iNode.getNodeType() == Node.ELEMENT_NODE) {
								Element iNodeElement = (Element) iNode;
								edge.setDataProfile(dataType,ClsEdge.INTEGRITY, Double.parseDouble((String)iNode.getTextContent()));
						}
					}
				}
			}
		}
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
			if (vertexList.get(vertex).getName()==value)
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
}
