package com.meehien.graph;

import java.util.*;
import java.io.*;

public class XMLTest {

    public static void main(String[] args) {
        try {
            VisioGraph rawGraph = new VisioGraph(
                    "/Users/thomasrj/Documents/git/ics-modelling-tool/res/ERTMSDetail - CVSS.vsdx", "Page-1");
             AdversaryParser.readXML("/Users/thomasrj/Documents/git/ics-modelling-tool/res/adversary.xml",rawGraph); 
        } catch (Exception e) {
            System.out.println("Woops: "+e.getMessage());
        }
    }

}