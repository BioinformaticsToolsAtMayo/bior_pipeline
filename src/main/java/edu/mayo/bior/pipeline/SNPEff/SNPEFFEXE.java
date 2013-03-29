/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.PipeFunction;

import edu.mayo.bior.util.BiorProperties;
import edu.mayo.bior.util.BiorProperties.Key;
import edu.mayo.exec.UnixStreamCommand;

/**
 * @author m102417
 */
public class SNPEFFEXE implements PipeFunction<String,String>{

	private static final Logger log = Logger.getLogger(UnixStreamCommand.class);
	private UnixStreamCommand snpeff;

	public SNPEFFEXE(String[] snpEffCmd) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
		snpeff = new UnixStreamCommand(snpEffCmd, NO_CUSTOM_ENV, true, true); 
		snpeff.launch();
		snpeff.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		//send some fake data to get the ball rolling...
		snpeff.send("chr1\t1588717\trs009\tG\tA\t0.0\t.\t.");
		//and get out all of the header lines... dump them to /dev/null
		snpeff.receive();
		snpeff.receive();
		snpeff.receive();
		snpeff.receive();
		snpeff.receive();
	}
	
	public SNPEFFEXE() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		this(getSnpEffCommand());
	}
	
	public static String[] getSnpEffCommand() throws IOException {
		// See src/main/resources/bior.properties for example file to put into your user home directory
		BiorProperties biorProps = new BiorProperties();
		
		//java -Xmx4g -jar /data/snpEff/snpEff_3_1/snpEff.jar eff -c /data/snpEff/snpEff_3_1/snpEff.config -v GRCh37.68 example.vcf > output.vcf
		final String[] command = {"java", 
			"-Xmx4g", 
			"-jar", 
			biorProps.get(Key.SnpEffJar),
			"eff",
			"-c",
			biorProps.get(Key.SnpEffConfig),
			"-v",
			"GRCh37.64",
			"-o",
			"vcf",
			"-noLog",
			"-noStats"
			//">",
			//"/tmp/treatSNPEff.vcf"
			//"/dev/stdout"
		};
		return command;
	}

	
	@Override
	public String compute(String a) {
		try {
			snpeff.send(a);
			String result =  snpeff.receive();
			return result;
		} catch( RuntimeException runtimeExc) {
			// Rethrow any runtime exceptions
			try {
				terminate(); 
			} catch(Exception e) { 
				log.error("Error terminating SNPEFFEXE pipe" + e);
			}
			throw runtimeExc;
		} catch (Exception ex) {
			log.error(ex);
		}

		// If we make it hear, then throw a NoSuchElementException
		// However, since this is not a normal pipe, it may not reach this point
		throw new NoSuchElementException();
	}
	
	public void terminate() throws InterruptedException{
		this.snpeff.terminate();
	}
}
