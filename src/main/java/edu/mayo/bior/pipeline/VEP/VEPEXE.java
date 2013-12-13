/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
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

	private static final Logger sLogger = Logger.getLogger(VEPEXE.class);
	private UnixStreamCommand mVep;
	// NOTE: A buffer size of "1" appears to be required when using streaming thru Java classes
	//       else the call will hang.
	//       (though when used separately on just the command line, 20-50 is most efficient)
	private static final String VEP_BUFFER_SIZE = "1";
	
	private static final int VCF_REF_COL = 3; 
	private static final int VCF_ALT_COL = 4; 

	// 10 second timeout for VEP writing response to STDOUT
	private static final long RECEIVE_TIMEOUT = 10;

	private int mLinesSent = 0;
	
	public VEPEXE(String[] vepCmd) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
		mVep = new UnixStreamCommand(getVEPCommand(vepCmd), NO_CUSTOM_ENV, true, true); 
		//-------------------------------------------------------------------------------
		// NOTE: VEP will only work on 64-bit systems, when the "forcelinebuffering" 
		// flag at the end of this line is set to "true"
		// To get it to work on 32-bit systems, change this flag to "false"
		// HOWEVER, this should be tested thoroughly before putting it into production!
		//-------------------------------------------------------------------------------
		//mVep = new UnixStreamCommand(getVEPCommand(vepCmd), NO_CUSTOM_ENV, true, false); 
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
		// NOTE: Need to type-cast the Arrays.asList() to an ArrayList, otherwise the list will NOT be modifiable!!!
		List<String> command = new ArrayList<String>(Arrays.asList(
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
				VEP_BUFFER_SIZE
		));
		if( userOptions != null )
			command.addAll(Arrays.asList(userOptions));
		command.addAll(getMacFlagsIfNecessary());
		
		return command.toArray(new String[command.size()]);
	}


	private static List<String> getMacFlagsIfNecessary() {
		String os = System.getProperty("os.name"); 
		List<String> macFlags = new ArrayList<String>();
		if (os.equals("Mac OS X"))
		{
			// MAC ONLY
			// @see https://github.com/arq5x/gemini/blob/master/docs/content/functional_annotation.rst
			// 
			// To use the cache, the gzip and zcat utilities are required. VEP uses zcat to 
			// decompress cached files. For systems where zcat may not be installed or may
			// not work, the following option needs to be added along with the --cache option:
			// 
			// "--compress gunzip -c"
			// 
			macFlags.add("--compress");
			macFlags.add("gunzip -c");

			sLogger.info(String.format("Running on %s.  Adding Mac-specific options.", os));
		}
		return macFlags;
	}

	public String compute(String vcfLine)
	{
		//sLogger.info("VEP in: " + vcfLine);
		
		// For cases where VEP will not send a response to STDOUT,
		// VEP will be bypassed
		if (bypass(vcfLine))
		{
			sLogger.warn(String.format("WARNING!!!!!  Line cannot be processed by VEP!!!!!!   bypassing VCF line: %s", vcfLine));
			return getFakeResponse(vcfLine);
		}
		
		try
		{
			
			sLogger.info("VEP - sending line #" + ++mLinesSent + ":  " + firstXchars(vcfLine,100));
			mVep.send(vcfLine);
			sLogger.info("VEP - Line sent.");

			try
			{
				sLogger.info("VEP - Receiving line...");
				String result =  mVep.receive(RECEIVE_TIMEOUT, TimeUnit.SECONDS);
				String[] vepParts = result.split("\t");
				sLogger.info("VEP - received line: " + firstXchars(result, 100) + "\t....\t" + vepParts[vepParts.length-1]);
				return result;
			}
			catch (TimeoutException te)
			{
				sLogger.warn(String.format("Timeout of %s seconds reached for VCF line: %s",RECEIVE_TIMEOUT, vcfLine));
				return getFakeResponse(vcfLine);
			}
		}
		catch( RuntimeException runtimeExc)
		{
			terminate();
			// Rethrow any runtime exceptions
			throw runtimeExc;
		}
		catch (Exception ex)
		{
			terminate();
			sLogger.error(ex);
		}

		// If we make it hear, then throw a NoSuchElementException
		// However, since this is not a normal pipe, it may not reach this point
		throw new NoSuchElementException();
	}

	private String firstXchars(String str, int charLimit) {
		if(str == null || str.length() < charLimit )
			return str;
		return str.substring(0,charLimit);
	}

	
	/**
	 * Gets a "fake" response from VEP that is useful for when VEP is
	 * either bypassed entirely or VEP failed to send a response.
	 * 
	 * @param vcfLine
	 * @return
	 */
	private String getFakeResponse(String vcfLine)
	{
		// return with blank CSQ field in INFO column
		return vcfLine + "\tCSQ=VEPERR";		
	}
	
	public void terminate() {
		try {
			this.mVep.terminate();
		} catch(Exception e) { 
			sLogger.error("Error terminating VEPEXE pipe" + e);
		}
	}
	
	/**
	 * Checks for cases where VEP will <b>not</b> send a response to STDOUT.
	 * These cases should be avoided as the {@link UnixStreamCommand#receive()}
	 * will hang indefinitely.
	 * <p/>
	 * The following cases cause VEP to not return a response and are checked:
	 * <ul>
	 * <li>absent (e.g. NULL) value for ALT column</li>
	 * <li>1 or more whitespace characters for ALT column</li>
	 * <li>"." character for ALT column</li>
	 * <li>ALT and REF columns match</li>
	 * </ul>
	 *  
	 * @param line
	 * 		A single data line from a VCF file.
	 * @return
	 * 		True if VEP should be bypassed.  False otherwise.
	 */
	private boolean bypass(String line)
	{
		String[] cols = line.split("\t");
		
		// make sure we can access the ALT column
		if (cols.length < (VCF_ALT_COL + 1))
		{
			return true;
		}
		
		final String ref = cols[VCF_REF_COL].trim();
		final String alt = cols[VCF_ALT_COL].trim();
				
		if (
				// NULL or whitespace
				(alt.length() == 0) ||
				
				// dot
				alt.equals(".") ||
				
				// REF and ALT are same
				alt.equals(ref)
		   )
		{
			return true;
		}
		else
		{
			return false;
		}		
	}
}
