import java.util.*;
import java.io.*;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.input.sax.*;
import org.jdom2.output.*;
import com.aspose.diagram.*;

public class ModelOutput {
    public static void generateModelImage(String modelPath){

		try{
		// load diagram
		Diagram diagram = new Diagram(modelPath);

		// Save diagram as PNG
		ImageSaveOptions options = new ImageSaveOptions(SaveFileFormat.PNG);

		// Save one page only, by page index
		//options.setPageIndex(2);

		// generate a GUID, store that and we'll figure out the rest...
		String filename = ""+java.util.UUID.randomUUID()+".PNG";

		// Save resultant Image file
		diagram.save("<FILEPATH>"+filename, options);
		// ExEnd:ExportPageToImage
		} catch(Exception e){
			System.err.println("Woops: Something went wrong");
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args){
		generateModelImage("<FILEPATH>/visio_good.vsdx");
	}

}