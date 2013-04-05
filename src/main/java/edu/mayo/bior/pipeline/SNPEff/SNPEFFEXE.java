/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import ca.mcgill.mcb.pcingola.fileIterator.NeedlemanWunsch;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;
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
import edu.mayo.exec.UnixStreamCommand;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author m102417
 */
public class SNPEFFEXE implements PipeFunction<String,String>{

	private static final Logger log = Logger.getLogger(UnixStreamCommand.class);
	private UnixStreamCommand snpeff;

	public SNPEFFEXE(String[] snpEffCmd) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
		snpeff = new UnixStreamCommand(getSnpEffCommand(snpEffCmd), NO_CUSTOM_ENV, true, true); 
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
		this(getSnpEffCommand(null));
	}
        
        //a back door constructor that lets us access methods for testing without starting SNPEFF
        public SNPEFFEXE(boolean silent){
            
        }
	
	public static String[] getSnpEffCommand(String[] userCmd) throws IOException {
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
                    String error = canCreateSeqChange(a);
                    if(error == null){
			snpeff.send(a);
			String result =  snpeff.receive();
			return result;
                    }else {
                        return a + "\t" + error;
                    }
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
			this.snpeff.terminate();
		} catch(Exception e) { 
			log.error("Error terminating SNPEFFEXE pipe" + e);
		}
	}
        
        
        ////
        //  BELOW THIS LINE WE ARE PULLING FORWARD THE INFORMATION FROM SNPEFF
        ////
        private static final long serialVersionUID = 4226374412681243433L;

	private String line; // Line from VCF file
	private int lineNum; // Line number
	private String chromosomeName; // Original chromosome name
	private String ref;
	private String[] alts;
	private Double quality;
	private String filterPass;
	private String infoStr = "";
	private HashMap<String, String> info;
	private String format;
	private ArrayList<VcfGenotype> vcfGenotypes = null;
	private ChangeType changeType;
	private String genotypeFields[]; // Raw fields from VCF file
	private String genotypeFieldsStr; // Raw fields from VCF file (one string, tab separated)
        private Marker parent;
        protected Genome genome = new Genome();
        /**
         * This method was 
         * Find chromosome 'chromoName'. If it does not exists and 'createChromos' is true, the chromosome is created
         * @param chromoName
         * @return
         */
        public Chromosome getChromosome(String chromoName) {
                //if (createChromos) return genome.getOrCreateChromosome(chromoName);
                return genome.getChromosome(chromoName);
        }
        
        /**
	 * Create a list of seqChanges frmo this VcfEntry
	 * @return
	 */
//        public List<SeqChange> seqChanges() {
//            LinkedList<SeqChange> list = new LinkedList<SeqChange>();
//
//            // Coverage
//            int coverage = Gpr.parseIntSafe(getInfo("DP"));
//
//            // Is it heterozygous, homozygous or undefined?
//            Boolean isHetero = calcHetero();
//
//            // Create one SeqChange for each alt
//            for (String alt : alts) {
//                Chromosome chr = (Chromosome) parent;
//                SeqChange seqChange = createSeqChange(chr, start, ref, alt, strand, id, getQuality(), coverage);
//                seqChange.setHeterozygous(isHetero);
//                list.add(seqChange);
//            }
//
//            return list;
//        }

        private int parsepos(String posStr){
            return Gpr.parseIntSafe(posStr) - 1;
        }
        //takes a line of VCF input and returns if the line can be processed by SNPEFF
        public String canCreateSeqChange(String line){
            // Parse line
		String fields[] = line.split("\t", 10); // Only pare the fist 9 fields (i.e. do not parse genotypes)
                // Is line OK?
		if (fields.length >= 4) {
                    // Chromosome and position. VCF files are one-base, so inOffset should be 1.
                    chromosomeName = fields[0].trim();
                    Chromosome chromo = getChromosome(chromosomeName);
                    parent = chromo;

                    int start = parsepos(fields[1]);
                    ref = fields[3].toUpperCase(); // Reference
                    String altsStr = fields[4].toUpperCase();
                    parseAlts(altsStr);
                    
                    // ID (e.g. might indicate dbSnp)
                    String id = fields[2];
                        
                    int strand = 1;
                    
                    // Quality
                    String qStr = fields[5];
                    if (!qStr.isEmpty()) quality = Gpr.parseDoubleSafe(qStr);
                    else quality = null;

                    filterPass = fields[6]; // Filter parameters

                    // INFO fields
                    if(fields.length>7){
                        infoStr = fields[7];
                        info = null;
                    }
                    
                    //coverage 
                    int coverage = Gpr.parseIntSafe(getInfo("DP"));

                    //Don't need this, we stripped it off in the step before
                    // Add genotype fields (lazy parse) 
                    //if (fields.length > 9) genotypeFieldsStr = fields[9];
                    
                    String error = null;
                    for (String altN : alts) {
                        error = canCreateSeqChange(chromo, start, ref, altN, strand, id, quality, coverage);
                        if(error != null){//if the alt is null, then it is ok, check the other ones, if it is not null, we have an error, return it!
                            return error;
                        }
                    }
                    
                    return error;
                }
                
            return null;
        }
        
        /**
	 * Create a seqChange 
	 * @return A string, null if true - SNPEff can process the variant
         *                 , message if false -SNPEff can not process the variant and the message contains the reason why
	 */
	private String canCreateSeqChange(Chromosome chromo, int start, String reference, String alt, int strand, String id, double quality, int coverage) {
		// No change?
		if (alt.isEmpty()) return null;  //new SeqChange(chromo, start, reference, reference, strand, id, quality, coverage);

		alt = alt.toUpperCase();

		// Case: Structural variant
		// 2 321682    .  T   <DEL>         6     PASS    IMPRECISE;SVTYPE=DEL;END=321887;SVLEN=-105;CIPOS=-56,20;CIEND=-10,62
		if (alt.startsWith("<DEL")) {
			int end = start + reference.length() - 1;

			// If there is an 'END' tag, we should use it
			if ((getInfo("END") != null)) {
				// Get 'END' field and do some sanity check
				end = (int) getInfoInt("END");
				if (end < start){//throw new RuntimeException("INFO field 'END' is before varaint's 'POS'\n\tEND : " + end + "\n\tPOS : " + start);
                                    return "INFO field 'END' is before varaint's 'POS'\n\tEND : " + end + "\n\tPOS : " + start;
                                }
                                
			}

			// Create deletion string
			// TODO: This should be changed. We should be using "imprecise" for these variants
			int size = end - start + 1;
			char change[] = new char[size];
			for (int i = 0; i < change.length; i++)
				change[i] = reference.length() > i ? reference.charAt(i) : 'N';
			String ch = "-" + new String(change);

			// Create SeqChange
			return null;//new SeqChange(chromo, start, reference, ch, strand, id, quality, coverage);
		}

		// Case: SNP, MNP
		// 20     3 .         C      G       .   PASS  DP=100
		// 20     3 .         TC     AT      .   PASS  DP=100
		if (reference.length() == alt.length()) {
			int startDiff = Integer.MAX_VALUE;
			String ch = "";
			String ref = "";
			for (int i = 0; i < reference.length(); i++) {
				if (reference.charAt(i) != alt.charAt(i)) {
					ref += reference.charAt(i);
					ch += alt.charAt(i);
					startDiff = Math.min(startDiff, i);
				}
			}

			return null;//new SeqChange(chromo, start + startDiff, ref, ch, strand, id, quality, coverage);
		}

		// Case: Deletion 
		// 20     2 .         TC      T      .   PASS  DP=100	
		// 20     2 .         AGAC    AAC    .   PASS  DP=100	
		if (reference.length() > alt.length()) {
			NeedlemanWunsch nw = new NeedlemanWunsch(alt, reference);
			nw.align();
			int startDiff = nw.getOffset();
			String ref = "*";
			String ch = nw.getAlignment();
			if (!ch.startsWith("-")){
                            //throw new RuntimeException("Deletion '" + ch + "' does not start with '-'. This should never happen!");
                            return "Deletion '" + ch + "' does not start with '-'. This should never happen!";
                        }

			return null;//new SeqChange(chromo, start + startDiff, ref, ch, strand, id, quality, coverage);
		}

		// Case: Insertion of A { tC ; tCA } tC is the reference allele
		// 20     2 .         TC      TCA    .   PASS  DP=100
		if (reference.length() < alt.length()) {
			NeedlemanWunsch nw = new NeedlemanWunsch(alt, reference);
			nw.align();
			int startDiff = nw.getOffset();
			String ch = nw.getAlignment();
			String ref = "*";
			if (!ch.startsWith("+")){
                            //throw new RuntimeException("Insertion '" + ch + "' does not start with '+'. This should never happen!");
                            return "Insertion '" + ch + "' does not start with '+'. This should never happen!";
                        }

			return null;//new SeqChange(chromo, start + startDiff, ref, ch, strand, id, quality, coverage);
		}

		// Other change type?
		//throw new RuntimeException("Unsupported VCF change type '" + reference + "' => '" + alt + "'\nVcfEntry: " + this);
                return "Unsupported VCF change type '" + reference + "' => '" + alt + "'\nVcfEntry: " + this.line;
	}
        
        public String getInfo(String key) {
		if (info == null) parseInfo();
		return info.get(key);
	}
        

        /**
	 * Parse INFO fields
	 */
	void parseInfo() {
		// Parse info entries
		info = new HashMap<String, String>();
		for (String inf : infoStr.split(";")) {
			String vp[] = inf.split("=");

			if (vp.length > 1) info.put(vp[0], vp[1]);
			else info.put(vp[0], "true"); // A property that is present, but has no value (e.g. "INDEL")
		}
	}
        
        /**
	 * Get info field as an long number
	 * The norm specifies data type as 'INT', that is why the name of this method might be not intuitive
	 * @param key
	 * @return
	 */
	public long getInfoInt(String key) {
		if (info == null) parseInfo();
		return Gpr.parseLongSafe(info.get(key));
	}
        
        /**
	 * Is this entry heterozygous?
	 * 
	 * 		Infer Hom/Her if there is only one sample in the file.
	 * 		Otherwise the field is null.
	 * 
	 * @return
	 */
//	public Boolean calcHetero() {
//		// No genotyping information? => Use number of ALT fielsd
//		if (genotypeFieldsStr == null) return isHeterozygous();
//
//		Boolean isHetero = null;
//
//		// If there is only one genotype field => parse fields
//		if (genotypeFields == null) {
//
//			// Are there more than two tabs? (i.e. more than one format field + one genotype field)  
//			int countFields, fromIndex;
//			for (countFields = 0, fromIndex = 0; (fromIndex >= 0) && (countFields < 1); countFields++, fromIndex++)
//				fromIndex = genotypeFieldsStr.indexOf('\t', fromIndex);
//
//			// OK only one genotype field => Parse it in order to extract homo info.
//			if (countFields == 1) parseGenotypes();
//		}
//
//		// OK only one genotype field => calculate if it is heterozygous
//		if ((genotypeFields != null) && (genotypeFields.length == 1)) isHetero = getVcfGenotype(0).isHeterozygous();
//
//		return isHetero;
//	}
        
        	/**
	 * Parse ALT field
	 * @param altsStr
	 */
	private void parseAlts(String altsStr) {
		if (altsStr.length() == 1) {
			if (altsStr.equals("A") || altsStr.equals("C") || altsStr.equals("G") || altsStr.equals("T") || altsStr.equals(".")) {
				alts = new String[1];
				alts[0] = altsStr;
			} else if (altsStr.equals("N")) { // aNy base
				alts = new String[4];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "G";
				alts[3] = "T";
			} else if (altsStr.equals("B")) { // B: not A
				alts = new String[3];
				alts[0] = "C";
				alts[1] = "G";
				alts[2] = "T";
			} else if (altsStr.equals("D")) { // D: not C
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "G";
				alts[2] = "T";
			} else if (altsStr.equals("H")) { // H: not G
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "T";
			} else if (altsStr.equals("V")) { // V: not T
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "G";
			} else if (altsStr.equals("M")) {
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "C";
			} else if (altsStr.equals("R")) {
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "G";
			} else if (altsStr.equals("W")) { // Weak
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "T";
			} else if (altsStr.equals("S")) { // Strong
				alts = new String[2];
				alts[0] = "C";
				alts[1] = "G";
			} else if (altsStr.equals("Y")) {
				alts = new String[2];
				alts[0] = "C";
				alts[1] = "T";
			} else if (altsStr.equals("K")) {
				alts = new String[2];
				alts[0] = "G";
				alts[1] = "T";
			} else if (altsStr.equals(".")) { // No alternative (same as reference)
				alts = new String[1];
				alts[0] = ref;
			} else {
				throw new RuntimeException("WARNING: Unkown IUB code for SNP '" + altsStr + "'");
			}
		} else alts = altsStr.split(",");

		// What type of change do we have?
		int maxAltLen = Integer.MIN_VALUE, minAltLen = Integer.MAX_VALUE;
		for (int i = 0; i < alts.length; i++) {
			maxAltLen = Math.max(maxAltLen, alts[i].length());
			minAltLen = Math.min(minAltLen, alts[i].length());
		}

		// Infer change type
		if ((ref.length() == maxAltLen) && (ref.length() == minAltLen)) {
			if (ref.length() == 1) changeType = ChangeType.SNP;
			else changeType = ChangeType.MNP;
		} else if (ref.length() > minAltLen) changeType = ChangeType.DEL;
		else if (ref.length() < maxAltLen) changeType = ChangeType.INS;
		else changeType = ChangeType.MIXED;
	}



}
