package com.meehien.graph;
import java.util.*;

//TODO map to xml
public class Asset{
	String assetID;
	String assetName;
	Vertex assetVertex;
	Set<String> assetDataTypes;

	public Asset(){
		assetID = null;
		assetName = null;
		assetDataTypes = new LinkedHashSet<String>();
	}

	public Asset(String val){
		assetID = null;
		assetName = val;
		assetDataTypes = new LinkedHashSet<String>();
	}

	public String getId(){
		return assetID;
	}
	
	public void setId(String val){
		assetID = val;
	}

	public String getName(){
		return assetName;
	}
	
	public void setName(String val){
		assetName = val;
	}

	public Set<String> getDataTypes(){
		return assetDataTypes;
	}

	public void setDataTypes(Set<String> val){
		assetDataTypes = val;
	}
}