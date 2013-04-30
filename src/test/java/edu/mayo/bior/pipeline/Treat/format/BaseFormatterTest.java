package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.util.List;

public abstract class BaseFormatterTest
{

	protected void validateHeader(Formatter f, String... expectedHeaders)
	{
		List<String> headers = f.getHeaders();
		assertEquals(expectedHeaders.length, headers.size());
		
		int idx = 0;
		for (String expectedHeader: expectedHeaders)
		{
			assertEquals(expectedHeader, headers.get(idx));			
			idx++;
		}		
	}
	
	protected void validateFormattedValues(Formatter f, String json, String... expectedValues)
	{			
		List<String> values = f.format(json);
		assertEquals(expectedValues.length, values.size());
		
		int idx = 0;
		for (String expectedValue: expectedValues)
		{
			assertEquals(expectedValue, values.get(idx));			
			idx++;
		}
	}

}
