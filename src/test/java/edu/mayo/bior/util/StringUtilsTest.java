package edu.mayo.bior.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.mayo.bior.util.StringUtils;

/**
 * @author Michael Meiners (m054457)
 * Date created: Aug 29, 2013
 */
public class StringUtilsTest {

	@Test
	public void testSplit() {
		List<String> delims = Arrays.asList(",", ";");
		
		assertEquals(Arrays.asList("A","B","C"), 	
				StringUtils.split( "A,B,C", delims));
		
		assertEquals(Arrays.asList("\"A,B,C\""),
				StringUtils.split( "\"A,B,C\"", delims));
		
		assertEquals(Arrays.asList("1","2","3"),
				StringUtils.split( "1,2;3", delims));
		
		assertEquals(Arrays.asList("1","2","3"),
				StringUtils.split( "1, 2; 3", delims));
		
		assertEquals(Arrays.asList("\"A quoted sentence, with separators; duh.\""),
				StringUtils.split( "\"A quoted sentence, with separators; duh.\"", delims));
		
		assertEquals(Arrays.asList("An unquoted sentence", "with separators", "duh."),
				StringUtils.split( "An unquoted sentence, with separators; duh.", delims));

		assertEquals(Arrays.asList("Say something! \"Something, \"", "he replied"),
				StringUtils.split( "Say something! \"Something, \", he replied; ", delims));

		assertEquals(Arrays.asList("An escaped \\\"quote\\\" in the middle"),
				StringUtils.split( "An escaped \\\"quote\\\" in the middle", delims));
		
		assertEquals(Arrays.asList("Quotes in \"mid\" but no delims"),
				StringUtils.split( "Quotes in \"mid\" but no delims", delims));
		
		assertEquals(Arrays.asList("\"some\"\"thing\""),
				StringUtils.split( "\"some\"\"thing\"", delims));
		
		assertEquals(Arrays.asList("MTHFR","BRCA1"),
				StringUtils.split( "MTHFR;BRCA1", delims));
	}
}
