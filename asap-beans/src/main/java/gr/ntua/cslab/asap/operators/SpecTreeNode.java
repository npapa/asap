package gr.ntua.cslab.asap.operators;

import gr.ntua.cslab.asap.rest.beans.OperatorDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecTreeNode implements Comparable<SpecTreeNode> {
	public TreeMap<String,SpecTreeNode> children;
	private String name, value;
	public boolean isRegex;
	
	public SpecTreeNode(String name) {
		children = new TreeMap<String,SpecTreeNode>();
		this.name = name;
	}


	public SpecTreeNode(String key, String value) {
		children = new TreeMap<String,SpecTreeNode>();
		isRegex =false;
		if (key.contains(".")){
			name = key.substring(0, key.indexOf("."));
			String nextKey = key.substring(key.indexOf(".")+1);
			
			SpecTreeNode temp = new SpecTreeNode(nextKey, value);
			children.put(temp.getName(),temp);
		}
		else{//leaf
			//System.out.println("adding1 leaf: "+key+ " , "+value);
			name = key;
			this.value = value;
		}
	}


	public SpecTreeNode(NodeName key, String value) {
		children = new TreeMap<String,SpecTreeNode>();
		isRegex =false;
		if(key.isRegex){
			isRegex = true;
			name = key.name;
			this.value = value;
		}
		else{
			children = new TreeMap<String,SpecTreeNode>();
			if (key.name.contains(".")){
				name = key.name.substring(0, key.name.indexOf("."));
				String nextKey = key.name.substring(key.name.indexOf(".")+1);
				
				SpecTreeNode temp = new SpecTreeNode(new NodeName(nextKey, key.nextName, false), value);
				children.put(temp.getName(),temp);
			}
			else{//leaf
				//System.out.println("adding1 leaf: "+key+ " , "+value);
				if(key.nextName!=null){
					name = key.name;
					SpecTreeNode temp = new SpecTreeNode(key.nextName, value);
					children.put(temp.getName(),temp);
				}
				else{
					name = key.name;
					this.value = value;
				}
			}
		}
	}


	public SpecTreeNode() {
		children = new TreeMap<String,SpecTreeNode>();
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void add(String key, String value) {
		//System.out.println("adding key: "+key);
		if (key.contains(".")){
			String curname = key.substring(0, key.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = children.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = key.substring(key.indexOf(".")+1);
				s.add(nextKey, value);
			}
			else{
				//System.out.println("not found!!");
				SpecTreeNode temp = new SpecTreeNode(key, value);
				children.put(temp.getName(),temp);
			}
		}
		else{//leaf
			//System.out.println("adding leaf: "+key+ " , "+value);
			SpecTreeNode temp = new SpecTreeNode(key, value);
			children.put(temp.getName(),temp);
		}
	}
	
	public void addRegex(NodeName key, String value) {
		if(key.isRegex){
			SpecTreeNode temp = new SpecTreeNode(key, value);
			children.put(temp.getName(),temp);
		}
		else{
			//System.out.println("adding key: "+key);
			if (key.name.contains(".")){
				String curname = key.name.substring(0, key.name.indexOf("."));
				//System.out.println("Checking name: "+curname);
				SpecTreeNode s = children.get(curname);
				
				if(s!=null){
					//System.out.println("Found!!");
					String nextKey = key.name.substring(key.name.indexOf(".")+1);
					NodeName nextNodeName = new NodeName(nextKey, key.nextName, false);
					s.addRegex(nextNodeName, value);
				}
				else{
					//System.out.println("not found!!");
					SpecTreeNode temp = new SpecTreeNode(key, value);
					children.put(temp.getName(),temp);
				}
			}
			else{//leaf
				//System.out.println("adding leaf: "+key+ " , "+value);
				SpecTreeNode temp = new SpecTreeNode(key, value);
				children.put(temp.getName(),temp);
			}
			
		}
	}
	
	@Override
	public String toString() {
		if(children.size()==0){
			return "("+name+", "+value+")";
		}
		else{
			String ret = name+"{";
			int i=0;
			for(SpecTreeNode n : children.values()){
				if(i>0){
					ret+=", ";
				}
				ret += n.toString();
				i++;
			}
			ret+="}";
			return ret;
		}
	}

	public void writeToPropertiesFile(String curentPath, Properties props) {
		if(children.size()==0){
			props.setProperty(curentPath+name, value);
		}
		else{
			curentPath+=name+".";
			for(SpecTreeNode n : children.values()){
				n.writeToPropertiesFile(curentPath, props);
			}
		}
	}

	public void toKeyValues(String curentPath, HashMap<String,String> curentValues) {
		if(children.size()==0){
			curentPath=curentPath.substring(0, curentPath.length()-1);
			curentValues.put(curentPath, value);
		}
		else{
			for(SpecTreeNode n : children.values()){
				n.toKeyValues(curentPath+n.name+".", curentValues);
			}
		}
	}
	
	public String toKeyValues(String curentPath, String ret, String separator) {
		if(children.size()==0){
			ret+= curentPath+name+" = "+value+separator;
		}
		else{
			curentPath+=name+".";
			for(SpecTreeNode n : children.values()){
				ret =n.toKeyValues(curentPath, ret, separator);
			}
		}
		return ret;
	}

	public void toOperatorDescription(OperatorDescription ret) {
		if(children.size()==0){
			OperatorDescription temp = new OperatorDescription(name, value);
			ret.addChild(temp);
		}
		else{
			OperatorDescription temp = new OperatorDescription(name, "");
			ret.addChild(temp);
			for(SpecTreeNode n : children.values()){
				n.toOperatorDescription(temp);
			}
		}
	}
	
	public boolean checkMatch(SpecTreeNode o1) {
		//materialized operator o1
		//System.out.println("checking: "+name);
		if(children.size()==0){
			if(o1.children.size()==0){
				Pattern p = Pattern.compile(value);
				Matcher m = p.matcher(o1.value);
				if(m.matches()){
					return true;
				}
				else{
					//System.out.println("notFound: "+name+" this.value="+value+" o.value="+o1.value);
					return false;
				}
			}
			else{
				return false;
			}
		}
		for(SpecTreeNode n : children.values()){
			//System.out.println("Checking: "+n.getName()+" isRegex: "+n.isRegex);
			if(n.isRegex){
				Pattern p = Pattern.compile(n.getName());
				boolean found =false;
				for(SpecTreeNode n1 : o1.children.values()){
					Matcher m = p.matcher(n1.getName());
					if(m.matches()){
						found =true;
						//System.out.println("found match: "+n.getName()+" "+n1.getName());
						return true;
					}
				}
				if(!found){
					//System.out.println("notFound: "+name);
					return false;
				}
			}
			else{
				SpecTreeNode n1 = o1.children.get(n.getName());
				if(n1!=null){
					if(!n.checkMatch(n1))
						return false;
				}
				else{
					return false;
				}
			}
		
		}
		return true;
	}


	@Override
	public int compareTo(SpecTreeNode o) {
		return this.name.compareTo(o.name);
	}
	
	

	
	@Override
	public SpecTreeNode clone() throws CloneNotSupportedException {
		SpecTreeNode ret = new SpecTreeNode(this.name);
		if(children.size()==0){
			//leaf
			ret.name = new String(this.name);
			ret.value = new String(this.value);
		}
		else{
			for(SpecTreeNode n : children.values()){
				SpecTreeNode temp = n.clone();
				ret.children.put(new String(temp.getName()), temp);
			}
		}
		return ret;
	}


	public SpecTreeNode copyInputToOpSubTree(String prefix, String inout) {
		SpecTreeNode ret = new SpecTreeNode(this.name);
		if(prefix==null){//copy all
			if(children.size()==0){
				//leaf
				ret.name = new String(this.name);
				ret.value = new String(this.value);
			}
			else{
				for(SpecTreeNode n : children.values()){
					SpecTreeNode temp = n.copyInputToOpSubTree(null, inout);
					ret.children.put(temp.getName(), temp);
				}
			}
		}
		else{
			if (prefix.contains(".")){
				String curname = prefix.substring(0, prefix.indexOf("."));
				//System.out.println("Checking name: "+curname);
				SpecTreeNode s = children.get(curname);
				
				if(s!=null){
					//System.out.println("Found!!");
					String nextKey = prefix.substring(prefix.indexOf(".")+1);
					SpecTreeNode temp = s.copyInputToOpSubTree(nextKey, inout);
					if(temp==null){
						//not found
						return null;
					}
					ret.children.put(temp.getName(), temp);
				}
				else{
					//not found
					return null;
				}
			}
			else{//leaf
				//add Input{i} node
				SpecTreeNode s = children.get(prefix);
				if(s!=null){

					SpecTreeNode temp = s.copyInputToOpSubTree(null,inout);
					
					SpecTreeNode ret1 = new SpecTreeNode(inout);
					for(SpecTreeNode n : temp.children.values()){
						ret1.children.put(n.getName(), n);
					}
					temp.children = new TreeMap<String, SpecTreeNode>();
					temp.children.put(ret1.getName(), ret1);
					
					ret.children.put(temp.getName(), temp);
					
				}
				else{
					//not found
					return null;
				}
			}
		}
		return ret;
	}
	
	
	public SpecTreeNode copyInputSubTree(String prefix) {
		SpecTreeNode ret = new SpecTreeNode(this.name);
		if(prefix==null){//copy all
			if(children.size()==0){
				//leaf
				ret.name = new String(this.name);
				ret.value = new String(this.value);
			}
			else{
				for(SpecTreeNode n : children.values()){
					SpecTreeNode temp = n.copyInputSubTree(null);
					ret.children.put(temp.getName(), temp);
				}
			}
		}
		else{
			if (prefix.contains(".")){
				String curname = prefix.substring(0, prefix.indexOf("."));
				//System.out.println("Checking name: "+curname);
				SpecTreeNode s = children.get(curname);
				
				if(s!=null){
					//System.out.println("Found!!");
					String nextKey = prefix.substring(prefix.indexOf(".")+1);
					SpecTreeNode temp = s.copyInputSubTree(nextKey);
					if(temp==null){
						//not found
						return null;
					}
					ret.children.put(temp.getName(), temp);
				}
				else{
					//not found
					return null;
				}
			}
			else{//leaf
				//remove Input{i} node
				SpecTreeNode s = children.get(prefix);
				if(s!=null){
					//SpecTreeNode temp = s.copySubTree(null);
					//ret.children.put(temp.getName(), temp);
					for(SpecTreeNode tn : s.children.values()){
						ret.children.put(tn.getName(), tn);
					}
				}
				else{
					//not found
					return null;
				}
			}
		}
		return ret;
	}
	
	public SpecTreeNode getNode(String key) {
		if (key.contains(".")){
			String curname = key.substring(0, key.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = children.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = key.substring(key.indexOf(".")+1);
				return s.getNode(nextKey);
			}
			else{
				return null;
			}
		}
		else{//leaf
			SpecTreeNode s = children.get(key);
			if(s!=null){
				return s;
			}
			else{
				//not found
				return null;
			}
		}
	}

	public String getParameter(String key) {
		if (key.contains(".")){
			String curname = key.substring(0, key.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = children.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = key.substring(key.indexOf(".")+1);
				return s.getParameter(nextKey);
			}
			else{
				return null;
			}
		}
		else{//leaf
			SpecTreeNode s = children.get(key);
			if(s!=null){
				return s.value;
			}
			else{
				//not found
				return null;
			}
		}
	}


	public void addAll(SpecTreeNode f) {

		for(Entry<String, SpecTreeNode> k : f.children.entrySet()){
			SpecTreeNode v = this.children.get(k.getKey());
			if(v!=null){
				v.addAll(k.getValue());
			}
			else{
				this.children.put(k.getKey(), k.getValue());
			}
		}
	}




}
