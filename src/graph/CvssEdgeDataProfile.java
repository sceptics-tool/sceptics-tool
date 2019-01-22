package com.meehien.graph;
import java.util.*;
import com.cvss.*;

public class CvssEdgeDataProfile {
	Double cvssValue;
	Cvss cvssVector;
	Double tweakingFactor;

	public CvssEdgeDataProfile(){
		cvssValue=0.0;
		cvssVector=null;
		tweakingFactor = 1.0; // set to 1 as we'll just do a simple multiplication as a decimal
	}

	public void set(String prop, Object val){
		switch (prop){
			case "value":
				cvssValue = (Double)val;
				break;
			case "vector":
				cvssVector = (Cvss)val;
				break;
			case "tweak":
				tweakingFactor = (Double)val; // override
				//tweakingFactor = (Double)val; // probabilistic
			default:
				break;
		}
	}

	public void set(Double v, Cvss c, Double t){
		cvssValue=v;
		cvssVector=c;
		tweakingFactor=t;

	}

	public Object get(String prop){
		switch (prop){
			case "value":
				return cvssValue;
			case "vector":
				return cvssVector;
			case "tweak":
				return tweakingFactor; //override
				//return tweakingFactor; // probabilistic
			default:
				return null;
		}
	}
}