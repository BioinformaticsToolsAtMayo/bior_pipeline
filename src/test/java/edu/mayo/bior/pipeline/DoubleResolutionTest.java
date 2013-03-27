package edu.mayo.bior.pipeline;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

public class DoubleResolutionTest {
    
	public static void main(String[] args) {
		DoubleResolutionTest test = new DoubleResolutionTest();
		test.printJavaVersion();
		test.testJsonResolution();
		test.testDouble();
	}
	
	class JsonVal {
		Double val;

		public void setVal(Double val) {this.val = val;}
		public Double getVal() {return this.val;}
	}

	@Test
	public void printJavaVersion() {
		System.out.println("Java runtime: " + System.getProperty("java.runtime.version"));
		System.out.println("JAVA_HOME:    " + System.getProperty("JAVA_HOME"));
	}
	
    
	@Test
	/** Test if json puts 3 or 4 numbers to the right of the decimal point 
	 *  It was doing 3 when run on laptop or directly on biordev.
	 *  However, it was doing 4 when running remotely on biordev from laptop thru functional tests. */
	public void testJsonResolution() {
		String json = "{val:0.001}";
		//String json = "{val:0.0010}";
		Gson gson = new Gson();
		
		JsonVal jsonRes = gson.fromJson(json, JsonVal.class);
		String jsonResStr = "" + jsonRes.val;
		System.out.println("Evaluate inappropriate handling of doubles such as 0.001 in different Java JDKs");
		System.out.println("Json string: " + json);
		System.out.println("Expected value: = 0.001");
		System.out.println("Actual value:   = " + jsonResStr);
		Assert.assertEquals("0.001", jsonResStr);
	}
	
	@Test
	public void testDouble() {
		String[] inputStrs = new String[] {
				"0.0",
				"1.0",
				"0.1",
				"0.10",
				"0.01",
				"0.010",
				"0.001",
				"0.0010",
				"0.0001",
				"0.00010",
				"0.00001",
				"0.000010",
				"0.000001",
				"0.0000001",
				"0.00000001",
				"0.000000001",
				"0.0000000001",
				"0.00000000001",
				"0.000000000001",
				"0.0000000000001",
				"0.0123456789",
				"0.0123456789012345678901234567890123456789"
		};

		String[] expected = new String[] {
				"0.0",
				"1.0",
				"0.1",
				"0.1",
				"0.01",
				"0.01",
				"0.001",
				"0.001",
				"1.0E-4",
				"1.0E-4",
				"1.0E-5",
				"1.0E-5",
				"1.0E-6",
				"1.0E-7",
				"1.0E-8",
				"1.0E-9",
				"1.0E-10",
				"1.0E-11",
				"1.0E-12",
				"1.0E-13",
				"0.0123456789",
				"0.012345678901234568"
		};

		System.out.println("=============================================");
		System.out.println("Testing many decimal points...");
		System.out.println("Input                        Expected (String)   Actual (Double.toString()) ");
		System.out.println("---------------------------- ------------------- ---------------------------");
		for(int i=0; i < inputStrs.length; i++) {
			System.out.println(pad(inputStrs[i], 27) + "  " + pad(expected[i], 18) + "  " + new Double(inputStrs[i]));
			Assert.assertEquals(expected[i], new Double(inputStrs[i]).toString());
		}
	}
	
	private String pad(String s, int len) {
		while(s.length() < len) {
			s = s + " ";
		}
		return s;
	}
    
}
