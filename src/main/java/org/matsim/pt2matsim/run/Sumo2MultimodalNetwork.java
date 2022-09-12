package org.matsim.pt2matsim.run;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.matsim.contrib.sumo.SumoNetworkConverter;
import org.xml.sax.SAXException;

public class Sumo2MultimodalNetwork {

	public static void main(String[] args) {
		
		if(args.length == 2) {
			run(args[0], args[1]);
		}
		else {
			throw new IllegalArgumentException("Input and output files are required");
		}

	}
	
	public static void run(String sumoFile, String outputFile) {
		
		Path input = FileSystems.getDefault().getPath(sumoFile);
		Path output = FileSystems.getDefault().getPath(outputFile);
		
		ArrayList<Path> inputs = new ArrayList<Path>();
		inputs.add(input);
		
		SumoNetworkConverter converter = SumoNetworkConverter.newInstance(inputs, output, "EPSG:4326", "EPSG:2154");
		
		try { converter.call();	} 
		catch (ParserConfigurationException | SAXException | IOException e) { e.printStackTrace(); } 
		catch (Exception e) { e.printStackTrace(); }
	}

}