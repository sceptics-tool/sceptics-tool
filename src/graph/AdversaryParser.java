package com.meehien.graph;

import java.util.*;
import java.io.*;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.input.sax.*;
import org.jdom2.output.*;

import com.meehien.graph.Adversary;

public class AdversaryParser {

    private static String ANY_NODE = "any";

    /**
     * Generic readMethod, which allows us to tap into the XML file representing
     * an adversary. We will enforce validation of the XML files against their
     * respective DTDs.
     */
    public static ArrayList<Adversary> readXML(String inFile, RawGraph inGraph) {
        ArrayList<Adversary> adversaries = new ArrayList<Adversary>();
        // Set up all the pre-reqs and try and read in the file. Here, we need to
        // check if the file is valid, and catch any validation or read errors.
        SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
        Document xmlDoc = null;
        File xmlFile = new File(inFile);
        adversaries = new ArrayList<Adversary>();
        try {
            xmlDoc = builder.build(xmlFile);

        } catch (JDOMException e) {
            // This exception means that there was a "well-formedness" error 
            System.err.println(xmlFile.getAbsolutePath() + " is either not a well-formed XML document or is not valid: "
                    + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            System.exit(1);
        }

        // OK - it's parsed OK, let's try and play with it now.

        Element docRoot = xmlDoc.getRootElement();

        // this should be an 'Adversaries'

        List<Element> adversariesChildren = docRoot.getChildren();
        // should be a list of one or more adversary elements.

        // iterate over the list of adverary elements.
        for (Element e : adversariesChildren) {

            Adversary adversary = new Adversary();
            Set<String> edge_types = new LinkedHashSet<String>();
            Set<String> entry_nodes = new LinkedHashSet<String>();

            adversary.setID(e.getAttributeValue("id").trim());
            
            for (Element f : e.getChildren("edge_types")) {
                for (Element g : f.getChildren("type")) {
                    // do the data types
                    edge_types.add(g.getValue().trim());
                }
            }
            adversary.setEdgeTypes(edge_types);
            
            getentrynodes:
                for (Element f : e.getChildren("entry_nodes")) {
                    for (Element g : f.getChildren("node")) {
                        String nodeName = (g.getValue().trim());
                        if (nodeName.equals(ANY_NODE)){
                            System.out.println("Matching * for "+adversary.getID());
                            entry_nodes = new LinkedHashSet<String>(inGraph.getVertices().keySet());
                            break getentrynodes;
                        } else{
                            System.out.println("Matching non-* for "+adversary.getID());
                            entry_nodes.add(inGraph.getVertexIdByName(nodeName));
                        }
                    }
                }
            adversary.setEntryNodes(entry_nodes);

            //add adversaries to return list
            adversaries.add(adversary);
        }

        return adversaries;
    }
}