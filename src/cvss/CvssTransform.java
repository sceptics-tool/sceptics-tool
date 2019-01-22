package com.cvss;


/**
Parts of this code are from the Java Implementation of the CVSS v3 Calculator,
available at https://github.com/stevespringett/cvss-calculator, licensed under
Apache License 2.0.
*/
public class CvssTransform{

/**
Function which takes in a CVSS Vector and outputs a CVSS Object which we can
reason with.
@param cvssMetric the CVSS Vector generated through the online tool
@return either a null value (if it failed to parse the vector), or an Object
of type Cvss that contains the values represented by the vector.
*/
public static Cvss getCVSS(String cvssMetric){
		try{
			return Cvss.fromVector(cvssMetric);
		} catch (Exception e){
			System.out.println("CVSStransform.getCVSS() Exception: "+e);
			return null;
		}
	}

	/**
	Function which outputs the Exploitability Score for a given CVSS Vector
	@param cvssMetric the CVSS Vector generated through the online tool
	@return either -1 (an error occurred) or the Exploitability score represented
	as a number greater than 0.
	*/
	public static double getESC(String cvssMetric){
		try{
			Cvss vector = Cvss.fromVector(cvssMetric);
			Score sc = vector.calculateScore();
			return sc.getExploitabilitySubScore();
		} catch (Exception e){
			return -1;
		}
	}

	/**
	Function which transforms an Exploitability score into a probabilistic measure
	which scales the value from a range of [0.121,3.887] to [0,10].
	@param cvssMetric the CVSS Vector generated through the online tool
	@return the probability for a given node (negative if an error occured.)
	*/

	//TODO probably worth actually throwing exceptions if it's a negative value?
	public static double getESCToProb (String cvssMetric){
		double d = getESC(cvssMetric);
		d = d-0.121; // remove the minumum interval of the ESC Score
		return d/3.887; // divide by the maximum of the ESC
	}

}
