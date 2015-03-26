package gr.ntua.cslab.asap.staticLibraries;

import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;

public class MaterializedWorkflowLibrary {
	private static HashMap<String,MaterializedWorkflow1> materializedWorkflows;
	private static String workflowDirectory;

	public static String getWorkflowDirectory() {
		return workflowDirectory;
	}

	public static void setWorkflowDirectory(String workflowDirectory) {
		MaterializedWorkflowLibrary.workflowDirectory = workflowDirectory;
	}

	public static void initialize(String directory) throws Exception{

		workflowDirectory = directory;
		materializedWorkflows = new HashMap<String, MaterializedWorkflow1>();
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
		        Logger.getLogger(OperatorLibrary.class.getName()).info("Loading workflow: " + listOfFiles[i].getName());
		        MaterializedWorkflow1 w = new MaterializedWorkflow1(listOfFiles[i].getName(),listOfFiles[i].getPath());
		        w.readFromDir();
		        materializedWorkflows.put(listOfFiles[i].getName(), w);
		    }
		}
	}
	public static WorkflowDictionary getWorkflow(String name, String delimiter) throws NumberFormatException, EvaluationException{
		return materializedWorkflows.get(name).toWorkflowDictionary(delimiter);
	}
	
	public static List<String> getWorkflows() {
		return new ArrayList<String>(materializedWorkflows.keySet());
	}

	public static void add(MaterializedWorkflow1 workflow) throws Exception {
		
		materializedWorkflows.put(workflow.name, workflow);
		workflow.writeToDir();
	}

	public static MaterializedWorkflow1 get(String mw) {
		return materializedWorkflows.get(mw);
	}

}
