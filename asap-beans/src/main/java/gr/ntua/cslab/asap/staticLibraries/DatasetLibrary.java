package gr.ntua.cslab.asap.staticLibraries;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDescription;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class DatasetLibrary {
	private static HashMap<String,Dataset> datasets;
	private static String datasetDirectory;
	private static Logger logger = Logger.getLogger(DatasetLibrary.class.getName());
	
	public static void initialize(String directory) throws IOException{
		datasetDirectory = directory;
		datasets = new HashMap<String,Dataset>();
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && !listOfFiles[i].isHidden()) {
		        Logger.getLogger(DatasetLibrary.class.getName()).info("Loading Dataset: " + listOfFiles[i].getName());
		        Dataset temp = new Dataset(listOfFiles[i].getName());
				temp.readPropertiesFromFile(listOfFiles[i]);
				datasets.put(temp.datasetName, temp);
		    }
		}
	}
	
	public static void refresh(){
		
	}
	
	public static List<String> getDatasets(){
		List<String> ret = new ArrayList<String>();
		for(Dataset op : datasets.values()){
			ret.add(op.datasetName);
		}
		return ret;
	}
	
	public static String getDatasetDescription(String id) {
		Dataset d = datasets.get(id);
		if(d==null)
			return "No description available";
		return d.toKeyValues("\n");
	}

	public static void add(Dataset d) {
		datasets.put(d.datasetName, d);
	}

	public static void addDataset(String name, String value) throws IOException {
		Dataset d = new Dataset(name);
    	InputStream is = new ByteArrayInputStream(value.getBytes());
    	d.readPropertiesFromFile(is);
    	d.writeToPropertiesFile(datasetDirectory+"/"+d.datasetName);
    	add(d);
    	is.close();
	}

	public static void deleteDataset(String opname) {
		Dataset d = datasets.remove(opname);
		File file = new File(datasetDirectory+"/"+d.datasetName);
		file.delete();
	}
	

	public static Dataset getDataset(String opname) {
		return datasets.get(opname);
	}

	public static OperatorDescription getOperatorDescriptionJSON(String id) {
		Dataset d = datasets.get(id);
		if(d==null)
			return new OperatorDescription("", "");
		return d.toOperatorDescription();
	}

}
