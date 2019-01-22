
//package com.meehien.graph;

import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.input.sax.*;
import org.jdom2.output.*;
import org.jdom2.filter.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aspose.diagram.*;
import javax.imageio.*;
import javax.xml.bind.DatatypeConverter;

public class ModelOutput {

	// generate a base64 string representation of the Visio Model
	public static String generateModelImageBase64(String modelPath) {
		try {
			// load diagram
			Diagram diagram = new Diagram(modelPath);

			// Save diagram as PNG
			ImageSaveOptions options = new ImageSaveOptions(SaveFileFormat.PNG);

			// Save one page only, by page index
			// options.setPageIndex(2);

			// generate a GUID, store that and we'll figure out the rest...
			String filename = "" + java.util.UUID.randomUUID() + ".PNG";

			// Save resultant Image file
			diagram.save("/Users/thomasrj/Documents/git/ics-modelling-tool/res/" + filename, options);
			// now, we're being slightly dirty here. We need to output to disk before converting
			// to base64 and then remove it from the file directory. Yes, this is hardcoded
			// that's a TODO
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			File f = new File("/Users/thomasrj/Documents/git/ics-modelling-tool/res/" + filename);
			BufferedImage image = ImageIO.read(f);
			ImageIO.write(image, "png", output);

			String s = DatatypeConverter.printBase64Binary(output.toByteArray());
			f.delete();
			return s;
		} catch (Exception e) {
			System.err.println("Woops: Something went wrong");
			e.printStackTrace();
			return "";
		}
	}

	// helper method as JDOM doesn't have a getElementById method natively, so this
	// allows us to get the physical elements that will allow us to manipulate them
	public static Element getElementById(String id, String type, Document d) {
		Element docRoot = d.getRootElement();
		ElementFilter filter = new ElementFilter(type);
		Element toReturn = null;
		for (Element c : docRoot.getDescendants(filter)) {
			if (id.equals(c.getAttributeValue("id")))
				toReturn = c;
		}
		return toReturn;
	}

	// method which creates a HTML output from the model.
	public static void generateReport(String base64Model) {
		// work out what arguments we use in the text output
		SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
		builder.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		});
		Document xmlDoc = null;
		File xmlFile = new File("/Users/thomasrj/Documents/git/ics-modelling-tool/res/sceptics-output.html");
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

		Element modelImage = getElementById("modelImage", "img", xmlDoc);
		modelImage.setAttribute("src", "data:image/png;base64," + base64Model);
		Element attackerDefn = getElementById("attackerDefinition","p",xmlDoc);
		attackerDefn.setText("blah");






		// output the entire thing as a nice HTML file
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOut.output(xmlDoc, new FileOutputStream("/Users/thomasrj/Documents/git/ics-modelling-tool/res/ertmsCVSSExample.html"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// first we need to get the Base64 representation of the Visio Model
		String s = generateModelImageBase64(
				"/Users/thomasrj/Documents/git/ics-modelling-tool/res/ertmsCVSSExample.vsdx");
		// then generate the report.
		generateReport(s);
		// System.out.println(s);
	}

}