package edu.mayo.bior.cli.func.remoteexec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFEXE;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class SNPEFFEXEITCase extends RemoteFunctionalTest
{
	/*
	  note: if you want to dig deep and debug this code, you probably want to set your log4j properties to:
	  ##active
           ## log4j configuration used during build and unit tests
           #log4j.rootLogger=DEBUG, console
           #log4j.threshhold=ALL

           ## console appender logs to STDOUT
           #log4j.appender.console=org.apache.log4j.ConsoleAppender
           ##log4j.appender.console.layout=org.apache.log4j.PatternLayout
           #log4j.appender.console.layout.ConversionPattern=%d [%t] %-5p %c - %m%n	  
	 */

	private static final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();  
	
	@Test
	/** This can be useful when trying to isolate what SnpEff is trying to do, without all the 
	 *  extra junk coming from the pre and post pipeline elements.
	 *  NOTE: We can only take clean variants ONLY (none with multiple insertions/deletions, else it will hang)!!	 */
	public void snpEffExeOnly() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.snpEffExeOnly(): run only the exec part of the pipeline to isolate and test it...");

		// NOTE: The genome build is now specified in the bior.properties file, not as a String argument here
		// For old SnpEff 2.0.5
		//UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(new String[] {"GRCh37.64"}), NO_CUSTOM_ENV, true, true);
		// For new SnpEff 3.4:
		//UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(new String[] {"GRCh37.69"}), NO_CUSTOM_ENV, true, true);        	
		UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(new String[0]), NO_CUSTOM_ENV, true, true);        	

		BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/snpeff/exeOnly_noMultiIndels.vcf"));

		// launch snpeff java process
		snpeff.launch();

		// send VCF header, this is required
		snpeff.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");

		ArrayList<String> actualOutput = new ArrayList<String>();
		
		String line = br.readLine();
		while( line != null ) {

			// only send VCF data lines to snpeff
			if( ! line.startsWith("#")){

				// send data line to snpeff
				snpeff.send(line);

				// receive data line from snpeff
				String outputLine = snpeff.receive();

				// handle header outputted from SNPEFF
				while (outputLine.startsWith("#"))
				{					
					// keep only  vcf related header lines
					// ignore SNPEFF specific lines (e.g. version and cmd)
					if ((outputLine.startsWith("##SnpEffVersion") == false) &&
						(outputLine.startsWith("##SnpEffCmd") == false))
					{
						actualOutput.add(outputLine);						
					}
					outputLine = snpeff.receive();
				}

				actualOutput.add(outputLine);
			}

			line = br.readLine();
		}            
		// tell SNPEFF we're done
		snpeff.terminate();

		// Compare the output
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/exeOnly_noMultiIndels.expected.vcf");
		
		System.out.println("========== Expected: =================");
		PipeTestUtils.printLines(expected);
		System.out.println("========== Actual:   =================");
		PipeTestUtils.printLines(actualOutput);

		PipeTestUtils.assertListsEqual(expected, actualOutput);
	}
}
