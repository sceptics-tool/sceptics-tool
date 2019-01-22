import com.meehien.graph.*;

import java.util.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

class modelMain {
	public static void main(String[] args) {
		String GRAPH_FILE = null;
		String start = "KMC";
		String end = "Car BUS";
		String profile = null;

		DecimalFormat df = new DecimalFormat("###.##");
		df.setRoundingMode(RoundingMode.DOWN);

		if (args.length > 0) {
			GRAPH_FILE = args[0];

			try {
				if (args[1] != null)
					start = args[1];
				if (args[2] != null)
					end = args[2];
				if (args[3] != null)
					profile = args[3];
			} catch (Exception e) {
			}

		} else {
			System.out.println("java -jar modelTool.jar <GRAPHML_FILE>");
			System.exit(0);
		}

		try {
			//XmlGraph rawGraph = new XmlGraph(GRAPH_FILE);
			//VisioGraph rawGraph = new VisioGraph(GRAPH_FILE, "Page-1");
			//CvssVisioGraph rawGraph = new CvssVisioGraph("./res/ERTMSDetail - CVSS.vsdx", "Page-1");
			//CvssVisioGraph rawGraph = new CvssVisioGraph("./res/ertmsCVSSExample.vsdx", "Page-1");
			//CvssVisioGraph rawGraph = new CvssVisioGraph("./res/smallERTMSExample_new.vsdx", "Page-1");
			CvssVisioGraph rawGraph = new CvssVisioGraph("./res/testModel.vsdx", "Page-1");

			//TODO: this is where we should carry out the matching/setup of the adversary/asset files.
			ArrayList<Adversary> adversaries = AdversaryParser.readXML("./res/adversary.xml", rawGraph);
			ArrayList<Asset> assets = AssetParser.readXML("./res/assets.xml", rawGraph);

			/* calling the tool with assets/adv object */
			// RoutedGraph.REV defines an end node (an asset) and returns all start nodes (attackers). 
			// RoutedGraph.FWD defines a start node (adversary) and looks up multiple end nodes (assets).

			//ProbORGraphAprx graph = new ProbORGraphAprx(rawGraph, RoutedGraph.REV, assets.get(0).getId(), assets.get(0).getDataTypes(), ClsEdge.VULNERABILITY);
			ProbORGraphAprx graph = new ProbORGraphAprx(rawGraph, RoutedGraph.REV, assets.get(0).getId(),
					assets.get(0).getDataTypes(), CvssEdge.CVSSVALUE);

			//datatypes come from assets.xml.
			//start comes from adversary.xml
			//end comes from assets.xml for RoutedGraph.REV
			//profile comes from CVSS

			System.out.println("                        ");
			System.out.println("     SCEPTICS TOOL      ");
			System.out.println("------------------------\n");
			System.out.println("INPUT:");
			System.out.println("Model File: " + "<filename var>"); // we need to get the filename variable sorted when we release this
			System.out.println("Attack Threshold: " + "<threshold>"); // again, needs to be a param - currently anything >5% will get printed
			System.out.println("Printing the Top " + "<count>" + " possible attack paths per adversary"); // currently set to 20, but you get the gist
			System.out.println("Key Assets: ");

			String htmlAssets = "";
			for (Asset a : assets) {
				htmlAssets+="- "+a.getName()+"<br/>";
				System.out.println("\t- " + a.getName());
			}
			System.out.println("Adversaries:");
			String htmlAdversaries = "";
			for (Adversary a : adversaries) {
				for (String s : a.getEntryNodes()) {
					System.out.println("\t- " + a.getID() + ", Entry Point: " + rawGraph.getVertexById(s).getName());

				}
			}
			System.out.println("--------------------");
			System.out.println("ANALYSIS\n");

			/* Iterate adversaries */
			for (Adversary adversary : adversaries) {
				// System.out.println("--------------------\n");
				System.out.println("Adversary " + adversary.getID() + ":");
				Set<LinkedList<Vertex>> adversaryPaths = new LinkedHashSet<LinkedList<Vertex>>();

				/* group adversary entry nodes */
				Iterator<String> entryNodesItr = adversary.getEntryNodes().iterator();
				while (entryNodesItr.hasNext()) {

					Set<LinkedList<Vertex>> advPaths = graph.getPaths(rawGraph.getVertexById(entryNodesItr.next()));
					Iterator<LinkedList<Vertex>> advPathsItr = advPaths.iterator();
					while (advPathsItr.hasNext()) {
						adversaryPaths.add(advPathsItr.next());
					}
				}

				/* print paths */

				// System.out.println("Paths: ");
				Iterator<LinkedList<Vertex>> adversaryPathsItr = adversaryPaths.iterator();

				/* This is a minimally viable sorting implementation - we definitely could improve it,
					but for demo purposes this will suffice
				 */
				ArrayList<String> path_strings = new ArrayList<String>();
				int index = 0;
				int count = 0;

				while (adversaryPathsItr.hasNext()) {
					LinkedList<Vertex> advPaths = adversaryPathsItr.next();
					// graph.printVertexPath(advPaths);
					if (rawGraph instanceof CvssVisioGraph) {
						// we instead need to override the printVertexGraph method to use my own calc.
						// graph.printPathScores(adversaryPathsItr.next());
						try {

							Object[] graphVals = graph.getOutputVals(advPaths);
							path_strings.add((Double) graphVals[1] + "," + (String) graphVals[0]);
						} catch (Exception e) {
							System.out.println(advPaths);
						}
					} else {
						graph.printVertexPath(adversaryPathsItr.next());

					}
				}
				// for(int indexes:indices){
				// 	System.out.println(path_strings.get(indexes));
				// }

				// print out the sorted list by prob of exploitation
				Collections.sort(path_strings, Collections.reverseOrder());
				for (String str : path_strings) {
					// System.out.println("OUTER LOOP");
					String[] path_prob = str.split(",");
					if (!path_prob[0].contains("E")) { // remove all the exponent ones
						Double d = Double.parseDouble(path_prob[0]);
						DecimalFormat df2 = new DecimalFormat("#.#####");
						//if (d > 0.05 && count < 20)
							System.out.println("\tPath " + path_prob[1] + " has a probability of " + df2.format(d));
						count++;

					}
					// System.out.println(path_strings);
				}
				System.out.println("------------");

				/* do stuff */

				//System.out.println("\nVulnerability: " + df.format(graph.computeProbability(adversaryPaths)*100) + "%");
			}

			// All Roads Lead to Me Analysis
			// go and create an adversary who has full access.
			ArrayList<Adversary> anyAdversary = AdversaryParser.readXML("./res/anyEntryAdversary.xml", rawGraph);
			for (Asset a : assets) {
				System.out.println("All Roads Point To Analysis for Asset " + a.getName());
				/* Iterate adversaries */
				for (Adversary adversary : anyAdversary) {
					Set<LinkedList<Vertex>> adversaryPaths = new LinkedHashSet<LinkedList<Vertex>>();

					/* group adversary entry nodes */
					Iterator<String> entryNodesItr = adversary.getEntryNodes().iterator();
					while (entryNodesItr.hasNext()) {

						Set<LinkedList<Vertex>> advPaths = graph.getPaths(rawGraph.getVertexById(entryNodesItr.next()));
						Iterator<LinkedList<Vertex>> advPathsItr = advPaths.iterator();
						while (advPathsItr.hasNext()) {
							adversaryPaths.add(advPathsItr.next());
						}
					}

					/* print paths */

					// System.out.println("Paths: ");
					Iterator<LinkedList<Vertex>> adversaryPathsItr = adversaryPaths.iterator();

					/* This is a minimally viable sorting implementation - we definitely could improve it,
						but for demo purposes this will suffice
					 */
					ArrayList<String> path_strings = new ArrayList<String>();
					int index = 0;
					int count = 0;

					while (adversaryPathsItr.hasNext()) {
						LinkedList<Vertex> advPaths = adversaryPathsItr.next();
						// graph.printVertexPath(advPaths);
						if (rawGraph instanceof CvssVisioGraph) {
							// we instead need to override the printVertexGraph method to use my own calc.
							// graph.printPathScores(adversaryPathsItr.next());
							try {

								Object[] graphVals = graph.getOutputVals(advPaths);
								path_strings.add((Double) graphVals[1] + "," + (String) graphVals[0]);
							} catch (Exception e) {
								System.out.println(advPaths);
							}
						} else {
							graph.printVertexPath(adversaryPathsItr.next());

						}
					}
					// for(int indexes:indices){
					// 	System.out.println(path_strings.get(indexes));
					// }

					// print out the sorted list by prob of exploitation
					Collections.sort(path_strings, Collections.reverseOrder());
					for (String str : path_strings) {
						String[] path_prob = str.split(",");
						if (path_prob[1].endsWith("[" + a.getName() + "]")) {
							if (!path_prob[0].contains("E")) { // remove all the exponent ones
								Double d = Double.parseDouble(path_prob[0]);
								DecimalFormat df2 = new DecimalFormat("#.#####");
								if (d > 0.05 && count < 20)
									System.out.println(
											"\tPath " + path_prob[1] + " has a probability of " + df2.format(d));
								count++;

							}
						}
						// System.out.println(path_strings);
					}
					System.out.println("------------");

					/* do stuff */

					//System.out.println("\nVulnerability: " + df.format(graph.computeProbability(adversaryPaths)*100) + "%");
				}
			}

			// Patient Zero analysis is using Graph.FWD (we specify the start node as compromised)
			CvssVisioGraph rawGraphFWD = new CvssVisioGraph("./res/testModel.vsdx", "Page-1");
			ProbORGraphAprx graphFWD = new ProbORGraphAprx(rawGraphFWD, RoutedGraph.FWD, assets.get(0).getId(),
					assets.get(0).getDataTypes(), CvssEdge.CVSSVALUE);
			for (Asset a : assets) {
				System.out.println("Patient-Zero Analysis for Asset " + a.getName());
				/* Iterate adversaries */
				for (Adversary adversary : anyAdversary) {
					Set<LinkedList<Vertex>> adversaryPaths = new LinkedHashSet<LinkedList<Vertex>>();

					/* group adversary entry nodes */
					Iterator<String> entryNodesItr = adversary.getEntryNodes().iterator();
					while (entryNodesItr.hasNext()) {

						Set<LinkedList<Vertex>> advPaths = graphFWD
								.getPaths(rawGraphFWD.getVertexById(entryNodesItr.next()));
						Iterator<LinkedList<Vertex>> advPathsItr = advPaths.iterator();
						while (advPathsItr.hasNext()) {
							adversaryPaths.add(advPathsItr.next());
						}
					}

					/* print paths */

					// System.out.println("Paths: ");
					Iterator<LinkedList<Vertex>> adversaryPathsItr = adversaryPaths.iterator();

					/* This is a minimally viable sorting implementation - we definitely could improve it,
						but for demo purposes this will suffice
					 */
					ArrayList<String> path_strings = new ArrayList<String>();
					int index = 0;
					int count = 0;

					while (adversaryPathsItr.hasNext()) {
						LinkedList<Vertex> advPaths = adversaryPathsItr.next();
						// graph.printVertexPath(advPaths);
						if (rawGraphFWD instanceof CvssVisioGraph) {
							// we instead need to override the printVertexGraph method to use my own calc.
							// graph.printPathScores(adversaryPathsItr.next());
							try {
								Object[] graphVals = graph.getOutputVals(advPaths);
								path_strings.add((Double) graphVals[1] + "," + (String) graphVals[0]);
							} catch (Exception e) {
								System.out.println(advPaths);
							}
						} else {
							graphFWD.printVertexPath(adversaryPathsItr.next());

						}
					}
					// for(int indexes:indices){
					// 	System.out.println(path_strings.get(indexes));
					// }

					// print out the sorted list by prob of exploitation
					Collections.sort(path_strings, Collections.reverseOrder());
					for (String str : path_strings) {
						String[] path_prob = str.split(",");
						if (!path_prob[0].contains("E")) { // remove all the exponent ones
							Double d = Double.parseDouble(path_prob[0]);
							DecimalFormat df2 = new DecimalFormat("#.#####");
							if (d > 0.05 && count < 20)
								System.out.println("\tPath " + path_prob[1] + " has a probability of " + df2.format(d));
							count++;

						}
						// System.out.println(path_strings);
					}
					System.out.println("------------");

					/* do stuff */

					//System.out.println("\nVulnerability: " + df.format(graph.computeProbability(adversaryPaths)*100) + "%");
				}
			}

		} catch (Exception e) {
			System.out.println("I'm throwing an exception");
			e.printStackTrace();
		}
	}
}
