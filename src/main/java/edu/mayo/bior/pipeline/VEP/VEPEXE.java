/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

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
	private static final String mVepBufferSize = "20";

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
		//mVep.receive();//fake data line
		//mVep.receive();
	}

	public VEPEXE() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		this(getVEPCommand(null));
	}

	public static String[] getVEPCommand(String[] userCmd) throws IOException {
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
				"20"
				// These added on Dan's Mac:
				//"--compress",
				// "gunzip -c"
				// "-i",
				// "/dev/stdin",
		};
		if (userCmd != null) {
			return concat(command,userCmd);
		} else {
			return command;
		}     
	}

	public static String[] concat(String[] A, String[] B) {
		List<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(A));
		list.addAll(Arrays.asList(B));
		return list.toArray(new String[list.size()]);
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
