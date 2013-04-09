/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import edu.mayo.bior.pipeline.SNPEff.*;
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
public class VEPEXE implements PipeFunction<String,String>{

	private static final Logger log = Logger.getLogger(UnixStreamCommand.class);
	private UnixStreamCommand vep;
        private static final String bufferSize = "20";

	public VEPEXE(String[] vepCmd) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
		vep = new UnixStreamCommand(vepCmd, NO_CUSTOM_ENV, true, true); 
		vep.launch();
		vep.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		//send some fake data to get the ball rolling...
		vep.send("chr1\t1588717\trs009\tG\tA\t0.0\t.\t.");
		//and get out all of the header lines... dump them to /dev/null
		vep.receive();//#fileformat=VCFv4.0
		vep.receive();//##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|HGNC|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
		vep.receive();//#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
		//vep.receive();//fake data line
		//vep.receive();
	}
	
	public VEPEXE() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		this(getVEPCommand(bufferSize, "LINUX"));
	}
        
        public static String[] getVEPCommand(String bufferSize, String platform) throws IOException{
            if(platform.equalsIgnoreCase("MAC")){
                return getVEPMac(bufferSize);
            }else {
                return getVEPCommand(bufferSize);
            }
        }
	
        /**
         * e.g. 
         *  /usr/bin/perl \
         * variant_effect_predictor.pl \
         * -i /dev/stdin  \
         * -o STDOUT \
         * -dir cache \
         * -vcf --hgnc -polyphen b -sift b \
         * --offline \
         * --compress \
         * "gunzip -c" 
         * @param bufferSize
         * @return 
         */
        public static String[] getVEPMac(String bufferSize) throws IOException{
                // See src/main/resources/bior.properties for example file to put into your user home directory
		BiorProperties biorProps = new BiorProperties();
		
		//VEP_COMMAND="$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf --hgnc -polyphen b -sift b --offline --buffer_size $VEP_BUFFER_SIZE"
                final String[] command = {
			"/usr/bin/perl",
			biorProps.get("BIOR_VEP"),
			"-i",
			"/dev/stdin",
                        "-o",
			"STDOUT",
			"-dir",
			biorProps.get("BIOR_VEP_CACHE"),
			"-vcf",
			"--hgnc",
                        "-polyphen",
                        "b",
                        "-sift",
                        "b",
                        "--offline",
                        "--buffer_size",
                        bufferSize,
                        "--compress",
                        "gunzip -c"
		};
		return command;
            
        }        
                
	public static String[] getVEPCommand(String bufferSize) throws IOException {
		// See src/main/resources/bior.properties for example file to put into your user home directory
		BiorProperties biorProps = new BiorProperties();
		
		//VEP_COMMAND="$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf --hgnc -polyphen b -sift b --offline --buffer_size $VEP_BUFFER_SIZE"
                final String[] command = {
			biorProps.get("BIOR_VEP_PERL"),
			biorProps.get("BIOR_VEP"),
			"-i",
			"/dev/stdin",
                        "-o",
			"STDOUT",
			"-dir",
			biorProps.get("BIOR_VEP_CACHE"),
			"-vcf",
			"--hgnc",
                        "-polyphen",
                        "b",
                        "-sift",
                        "b",
                        "--offline",
                        "--buffer_size",
                        bufferSize
		};
		return command;
	}

	
	public String compute(String a) {
		try {
			vep.send(a);
			String result =  vep.receive();
			return result;
		} catch( RuntimeException runtimeExc) {
			terminate();
			// Rethrow any runtime exceptions
			throw runtimeExc;
		} catch (Exception ex) {
			terminate();
			log.error(ex);
		}

		// If we make it hear, then throw a NoSuchElementException
		// However, since this is not a normal pipe, it may not reach this point
		throw new NoSuchElementException();
	}
	
	public void terminate() {
		try {
			this.vep.terminate();
		} catch(Exception e) { 
			log.error("Error terminating SNPEFFEXE pipe" + e);
		}
	}
}
