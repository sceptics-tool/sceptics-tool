package com.meehien.graph;

import java.util.*;
import java.io.*;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.input.sax.*;
import org.jdom2.output.*;

import com.meehien.graph.Asset;

public class AssetParser {

    /**
     * Generic readMethod, which allows us to tap into the XML file representing
     * a list of assets. We will enforce validation of the XML files against their
     * respective DTDs.
     */
    public static ArrayList<Asset> readXML(String inFile, RawGraph inGraph) {
        ArrayList<Asset> assets = new ArrayList<Asset>();
        // Set up all the pre-reqs and try and read in the file. Here, we need to
        // check if the file is valid, and catch any validation or read errors.
        SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
        Document xmlDoc = null;
        File xmlFile = new File(inFile);
        assets = new ArrayList<Asset>();
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

        // this should be an 'Assets' element
        List<Element> assetsChildren = docRoot.getChildren();

        // now we need to drill down into each of the assets that exist.

        for (Element e : assetsChildren) {

            Asset a = new Asset();
            Set<String> dataProfiles = new LinkedHashSet<String>();
            Set<String> dataTypes = new LinkedHashSet<String>();

            //System.out.println("--DATATYPES--");
            for (Element f : e.getChildren("data_types")) {
                for (Element g : f.getChildren()) {
                    dataTypes.add(g.getValue().trim());
                }
            }

           // System.out.println("--DATAPROFILES--");            
            for (Element f : e.getChildren("data_profiles")) {
                for (Element g : f.getChildren()) {
                    dataProfiles.add(g.getValue().trim());
                }
            }
            a.setDataTypes(dataTypes);


            //System.out.println("--NODES--");
            for (Element f : e.getChildren("nodes")) {
                for (Element g : f.getChildren()) {
                    // get the name of the nodes
                    a.setName(g.getValue().trim());

                    // we'll need to carry out a lookup here.
                    //System.out.println(inGraph.getVertexIdByName(a.getName()));
                    a.setId(inGraph.getVertexIdByName(a.getName()));
                    // and we'll apply the getting and setting of data profiles and types too
                }
            }
            assets.add(a);
        }
        return assets;

    }
}