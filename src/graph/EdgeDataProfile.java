package com.meehien.graph;

class EdgeDataProfile {
	Double vulnerability;
	Double confidentiality;
	Double availability;
	Double integrity;

	public EdgeDataProfile(){
		vulnerability=null;
		confidentiality=null;
		availability=null;
		integrity=null;
	}

	public void set(String prop, Double val){
		switch (prop){
			case "vulnerability":
				vulnerability = val;
				break;
			case "confidentiality":
				confidentiality = val;
				break;
			case "availability":
				availability = val;
				break;
			case "integrity":
				integrity = val;
				break;
			default:
				break;
		}
	}

	public void set(Double v, Double c, Double a, Double i){
		vulnerability=v;
		confidentiality=c;
		availability=a;
		integrity=i;
	}

	public Double get(String prop){
		switch (prop){
			case "vulnerability":
				return vulnerability;
			case "confidentiality":
				return confidentiality;
			case "availability":
				return availability;
			case "integrity":
				return integrity;
			default:
				return null;
		}
	}
}