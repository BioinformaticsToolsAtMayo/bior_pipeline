/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.PipeFunction;

import edu.mayo.bior.util.BiorProperties;
import edu.mayo.bior.util.BiorProperties.Key;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.exec.UnixStreamCommand;

/**
 * @author m102417
 */
public class VEPEXE implements PipeFunction<String,String>{

	private static final Logger sLogger = Logger.getLogger(UnixStreamCommand.class);
	private UnixStreamCommand mVep;
	// NOTE: A buffer size of "1" appears to be required when using streaming thru Java classes
	//       else the call will hang.
	//       (though when used separately on just the command line, 20-50 is most efficient)
	private static final String VEP_BUFFER_SIZE = "1";

	public VEPEXE(String[] vepCmd) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
		mVep = new UnixStreamCommand(getVEPCommand(vepCmd), NO_CUSTOM_ENV, true, true); 
		mVep.launch();
		mVep.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		//send some fake data to get the ball rolling...
		mVep.send("chr1\t1588717\trs009\tG\tA\t0.0\t.\t.");
		//and get out all of the header lines... dump them to /dev/null
		mVep.receive();//#fileformat=VCFv4.0
		mVep.receive();//##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|HGNC|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
		mVep.receive();//#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
	}

	public VEPEXE() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		this(getVEPCommand(null));
	}

	public static String[] getVEPCommand(String[] userOptions) throws IOException {
		// See src/main/resources/bior.properties for example file to put into your user home directory
		BiorProperties biorProps = new BiorProperties();

		//VEP_COMMAND="$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf --hgnc -polyphen b -sift b --offline --buffer_size $VEP_BUFFER_SIZE"
		final String[] command = {
				// On Dan's Mac, first part of cmd: "/usr/bin/perl"
				biorProps.get(Key.BiorVepPerl),
				biorProps.get(Key.BiorVep),
				"-i",
				"/dev/stdin",
				"-o",
				"STDOUT",
				"-dir",
				biorProps.get(Key.BiorVepCache),
				"-vcf",
				"--hgnc",
				"-polyphen",
				"b",
				"-sift",
				"b",
				"--offline",
				"--buffer_size",
				VEP_BUFFER_SIZE,
				// Add these to run on a Mac!
				//"--compress",
				//"gunzip -c"
		};
		String[] allCommands = (String[]) ArrayUtils.addAll(command, userOptions);
		return allCommands;
	}


	public String compute(String a) {
		try {
			mVep.send(a);
			String result =  mVep.receive();
			return result;
		} catch( RuntimeException runtimeExc) {
			terminate();
			// Rethrow any runtime exceptions
			throw runtimeExc;
		} catch (Exception ex) {
			terminate();
			sLogger.error(ex);
		}

		// If we make it hear, then throw a NoSuchElementException
		// However, since this is not a normal pipe, it may not reach this point
		throw new NoSuchElementException();
	}

	public void terminate() {
		try {
			this.mVep.terminate();
		} catch(Exception e) { 
			sLogger.error("Error terminating VEPEXE pipe" + e);
		}
	}
}
