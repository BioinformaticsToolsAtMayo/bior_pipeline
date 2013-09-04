package edu.mayo.bior.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;

import com.tinkerpop.pipes.Pipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.UNIX.GrepPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.*;
import edu.mayo.pipes.util.metadata.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/*
 * Tests if VCFGeneratorPipe is converting a tab BIOR generated data into VCF and headers are added
 * 
 */
public class VCFGeneratorPipeTest {

    @Before
    public void clearStaticHistoryBefore(){
        History.clearMetaData(); // clean up after ourselves
    }

    @After
    public void clearStaticHistoryAfter(){
        History.clearMetaData(); // clean up after ourselves
    }
	
	@Test
	public void testVCFGeneratorPipeWithoutMetadata() {
		
		List<String> input = Arrays.asList(
	    		"##Header start",
	    		"#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tBIOR.SNPeff.Effect",
	    		"1\t10144\trs144773400\tTA\tT\t.\t.\t.\tDeleterious"
	    	);
		
		Pipeline pipe = new Pipeline(
	    		new HistoryInPipe(),
	    		new VCFGeneratorPipe(),  		
	    		new HistoryOutPipe()
	    		//new PrintPipe()
	    		);
		
		pipe.setStarts(input);
    	//pipe.setStarts(Arrays.asList("src/test/resources/testData/metadata/validvcf.vcf"));
		List<String> expected = Arrays.asList(
	    		"##Header start",
	    		"##INFO=<ID=BIOR.SNPeff.Effect,Number=.,Type=String,Description=\"BioR property file missing description\">",
	    		"#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
	    		"1\t10144\trs144773400\tTA\tT\t.\t.\tBIOR.SNPeff.Effect=Deleterious"
	    	);   	    	
    	List<String> actual = PipeTestUtils.getResults(pipe);

		for(int i=0;i<expected.size();i++){
            assertEquals(expected.get(i), actual.get(i));
        }
		
	}

        
    public final List<String> header = Arrays.asList(
        "##fileformat=VCFv4.0",
        "##BIOR=<ID=\"bior.ToTJson\",Operation=\"bior_vcf_to_tjson\",DataType=\"JSON\",ShortUniqueName=\"ToTJson\">",
        "##BIOR=<ID=\"bior.brca1.dbsnp.tsv.gz\",Operation=\"bior_same_variant\",DataType=\"JSON\",ShortUniqueName=\"brca1.dbsnp.tsv.gz\",Path=\"REPLACEMEbrca1.dbsnp.tsv.gz\">",
        "##BIOR=<ID=\"bior.genes\",Operation=\"bior_overlap\",DataType=\"JSON\",ShortUniqueName=\"genes\",Path=\"REPLACEMEgenes.tsv.bgz\">",
        "##BIOR=<ID=\"bior.genes.HGNC\",Operation=\"bior_drill\",DataType=\"String\",Field=\"HGNC\",FieldDescription=\"\",ShortUniqueName=\"genes\",Path=\"REPLACEMEgenes.tsv.bgz\">",
        "##BIOR=<ID=\"bior.genes.2\",Operation=\"bior_lookup\",DataType=\"JSON\",ShortUniqueName=\"genes\",Path=\"REPLACEMEgenes.tsv.bgz\">",
        "##BIOR=<ID=\"bior.vep\",Operation=\"bior_vep\",DataType=\"JSON\",ShortUniqueName=\"vep\",Description=\"Tool from Ensembl that predicts the functional consequences of known and unknown variants.\",Version=\"2.7\",Build=\"Ensembl Release 69\",DataSourceProperties=\"REPLACEMEvep.datasource.properties\",ColumnProperties=\"REPLACEMEvep.column.properties\">",
        "##BIOR=<ID=\"bior.snpeff\",Operation=\"bior_snpeff\",DataType=\"JSON\",ShortUniqueName=\"snpeff\",Description=\"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes).\",Version=\"2.0.5d\",Build=\"GRCh37.64 (default)\",DataSourceProperties=\"REPLACEMEsnpeff.datasource.properties\",ColumnProperties=\"REPLACEMEsnpeff.column.properties\">",
        "##BIOR=<ID=\"bior.snpeff.Effect_impact\",Operation=\"bior_drill\",DataType=\"String\",Field=\"Effect_impact\",FieldDescription=\"\",ShortUniqueName=\"snpeff\",Description=\"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes).\",Version=\"2.0.5d\",Build=\"GRCh37.64 (default)\",DataSourceProperties=\"REPLACEMEsnpeff.datasource.properties\",ColumnProperties=\"REPLACEMEsnpeff.column.properties\">",
        "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tbior.ToTJson\tbior.brca1.dbsnp.tsv.gz\tbior.genes.HGNC\tbior.genes.2\tbior.vep\tbior.snpeff.Effect_impact"
    );

    @Test
    public void testGetBIORColumnsFromMetadata(){
        String[] orig = header.get(9).split("\t");//split the header line
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        List<String> col = v.getBIORColumnsFromMetadata(header);
        for(int i=0; i<orig.length; i++){
            //System.out.println(orig[i]);
            if(orig[i].startsWith("bior.")){
                assertTrue(col.contains(orig[i]));
            }
        }
    }

    public final List<String> data = Arrays.asList(
            "21\t26960070\trs116645811\tG\tA\t.\t.\t.\t{\"CHROM\":\"21\",\"POS\":\"26960070\",\"ID\":\"rs116645811\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\".\":true},\"_id\":\"rs116645811\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":26960070,\"_maxBP\":26960070}\t{}\t.\t{}\t{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000307301\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"1043\",\"CDS_position\":\"1001\",\"Protein_position\":\"334\",\"Amino_acids\":\"T/M\",\"Codons\":\"aCg/aTg\",\"HGNC\":\"MRPL39\",\"SIFT\":\"tolerated(0.05)\",\"PolyPhen\":\"benign(0.001)\",\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":0.05,\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.001}\tMODERATE",
            "22\t29138293\trs17885497\tT\tC\t.\t.\tThisColumnShouldNotBeAltered\t{\"CHROM\":\"22\",\"POS\":\"29138293\",\"ID\":\"rs17885497\",\"REF\":\"T\",\"ALT\":\"C\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"ThisColumnShouldNotBeAltered\":true},\"_id\":\"rs17885497\",\"_type\":\"variant\",\"_landmark\":\"22\",\"_refAllele\":\"T\",\"_altAlleles\":[\"C\"],\"_minBP\":29138293,\"_maxBP\":29138293}\t{}\t.\t{}\t{}\tLOW",
            "21\t38439640\trs73901833\tT\tC\t.\t.\tA column with spaces in it (this should not be passed to VEP, otherwise it will cause problems). This line has multiple sift and polyphen scores\t{\"CHROM\":\"21\",\"POS\":\"38439640\",\"ID\":\"rs73901833\",\"REF\":\"T\",\"ALT\":\"C\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"A column with spaces in it (this should not be passed to VEP, otherwise it will cause problems). This line has multiple sift and polyphen scores\":true},\"_id\":\"rs73901833\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"T\",\"_altAlleles\":[\"C\"],\"_minBP\":38439640,\"_maxBP\":38439640}\t{}\t.\t{}\t{\"Allele\":\"C\",\"Gene\":\"ENSG00000185808\",\"Feature\":\"ENST00000399098\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"555\",\"CDS_position\":\"118\",\"Protein_position\":\"40\",\"Amino_acids\":\"I/V\",\"Codons\":\"Ata/Gta\",\"HGNC\":\"PIGP\",\"SIFT\":\"tolerated(0.79)\",\"PolyPhen\":\"benign(0.003)\",\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":0.79,\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.003}\tMODERATE",
            "17\t41209681\t41209681\tT\tA\t.\t.\tA variant within the BRCA1 gene \t{\"CHROM\":\"17\",\"POS\":\"41209681\",\"ID\":\"41209681\",\"REF\":\"T\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"A variant within the BRCA1 gene\":true},\"_id\":\"41209681\",\"_type\":\"variant\",\"_landmark\":\"17\",\"_refAllele\":\"T\",\"_altAlleles\":[\"A\"],\"_minBP\":41209681,\"_maxBP\":41209681}\t{\"CHROM\":\"17\",\"POS\":\"41209681\",\"ID\":\"rs181436152\",\"REF\":\"T\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"RSPOS\":41209681,\"GMAF\":5.0E-4,\"dbSNPBuildID\":135,\"SSR\":0,\"SAO\":0,\"VP\":\"050000080005000014000100\",\"GENEINFO\":\"BRCA1:672\",\"WGT\":1,\"VC\":\"SNV\",\"INT\":true,\"ASP\":true,\"KGPhase1\":true,\"KGPROD\":true},\"_id\":\"rs181436152\",\"_type\":\"variant\",\"_landmark\":\"17\",\"_refAllele\":\"T\",\"_altAlleles\":[\"A\"],\"_minBP\":41209681,\"_maxBP\":41209681}\t1100\t{\"_type\":\"gene\",\"_landmark\":\"17\",\"_strand\":\"-\",\"_minBP\":41196312,\"_maxBP\":41277500,\"gene\":\"BRCA1\",\"gene_synonym\":\"BRCAI; BRCC1; BROVCA1; IRIS; PNCA4; PPP1R53; PSCP; RNF53\",\"note\":\"breast cancer 1, early onset; Derived by automated computational analysis using gene prediction method: BestRefseq.\",\"GeneID\":\"672\",\"HGNC\":\"1100\",\"HPRD\":\"00218\",\"MIM\":\"113705\"}\t{}\tMODIFIER"
    );

    public final List<String> colmns = Arrays.asList(
        "bior.ToTJson",
        "bior.brca1.dbsnp.tsv.gz",
        "bior.genes.HGNC",
        "bior.genes.2",
        "bior.vep",
        "bior.snpeff.Effect_impact"
    );

    @Test
    public void testGetBIORColumnsFromHeader(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        List<String> col = v.getBIORColumnsFromHeader(History.getMetaData().getColumns(), v.getBIORColumnsFromMetadata(header));
        //ensure the sets are equal
        for(String c : col){
            assertTrue(colmns.contains(c));
        }
        for(String c1 : colmns){
            assertTrue(col.contains(c1));
        }
    }

    @Test
    public void testGetBiorColumnsIndexes(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        List<String> orig = v.getBIORColumnsFromMetadata(header);  //all the column headers
        Map<Integer,String> kv = v.getBiorColumnsIndexes(new History(), orig); //init static vars above
        Integer count = 8;
        for(Integer key : kv.keySet()){
            String value = kv.get(key);
            assertEquals(key, count);
            assertEquals(colmns.get(count-8), value);
            count++;
        }
    }

    @Test
    public void testInfoDataPair(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        v.populateHeaderLinesForHeaderKeys(); //need to call this, never a problem in a pipeline
        String inf1 = v.infoDataPair("foo", "bar");
        assertEquals(";foo=bar",inf1);
        String inf2 = v.infoDataPair("foo", "a,b,c");
        assertEquals(";foo=a|b|c",inf2);
        String inf3 = v.infoDataPair("foo", "a b  c");
        assertEquals(";foo=a_b__c",inf3);
        String inf4 = v.infoDataPair("foo", "a;b;c");
        assertEquals(";foo=a|b|c",inf4);
        String inf5 = v.infoDataPair("foo", "a:b=c");
        assertEquals(";foo=a:b:c",inf5);
        String inf6 = v.infoDataPair("foo", "a -_b=,c;d");
        assertEquals(";foo=a_-_b:|c|d",inf6);

    }


    @Test
    public void testRemoveColumnHeader(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        List<String> orig = v.getBIORColumnsFromMetadata(header);  //all the bior column headers
        orig.add("INFO"); //add another just to test functionality needed for bior_annotate
        Map<Integer,String> biorindexes = v.getBiorColumnsIndexes(new History(), orig); //init static vars above
        HistoryMetaData hmd = v.removeColumnHeader(History.getMetaData(), biorindexes);
        //System.out.println(hmd.getColumns().size());
        for(ColumnMetaData cmd : hmd.getColumns()){
            String col = cmd.getColumnName();
            assertTrue(!col.contains("bior."));
            assertTrue(!col.contains("INFO"));
        }
    }


    public final List<String> happypath = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##INFO=<ID=bior.genes.HGNC,Number=.,Type=String,Description=\"BioR property file missing description\">",
            "##INFO=<ID=bior.snpeff.Effect_impact,Number=.,Type=String,Description=\"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes).\">",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tbior.ToTJson\tbior.brca1.dbsnp.tsv.gz\tbior.genes.HGNC\tbior.genes.2\tbior.vep\tbior.snpeff.Effect_impact"
    );

    @Test
    public void testAddColumnheadersHappyPath(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        List<String> orig = v.getBIORColumnsFromMetadata(header);  //all the bior column headers
        Map<Integer,String> biorindexes = v.getBiorColumnsIndexes(new History(), orig); //init static vars above
        HistoryMetaData hmd = v.removeColumnHeader(History.getMetaData(), biorindexes);
        //first happy path - there exists a ##BIOR line for each column header
        v.addColumnheaders(hmd.getOriginalHeader(),null, null);
        int i = 0;
        for (String s : hmd.getOriginalHeader()) {
            assertEquals(happypath.get(i), s); //note that it is ok that #CHROM line is wrong, because HistoryOutPipe will clean that up for us.
            i++;
        }

        //not in the metadata, but need to build an ##INFO -  build default info string
        //in the metadata but there is not in column header
    }


    public final List<String> notmetaInInfo = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##INFO=<ID=bior.genes.HGNC,Number=.,Type=String,Description=\"BioR property file missing description\">",
            "##INFO=<ID=bior.snpeff.Effect_impact,Number=.,Type=String,Description=\"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes).\">",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tbior.ToTJson\tbior.brca1.dbsnp.tsv.gz\tbior.genes.HGNC\tbior.genes.2\tbior.vep\tbior.snpeff.Effect_impact"
    );

    @Test
    public void testAddColumnheadersNotInMetadata(){
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        createHistory();
        List<String> orig = v.getBIORColumnsFromMetadata(header);  //all the bior column headers
        Map<Integer,String> biorindexes = v.getBiorColumnsIndexes(new History(), orig); //init the static vars above
        HistoryMetaData hmd = v.removeColumnHeader(History.getMetaData(), biorindexes);
        //not in the metadata, but need to build an ##INFO -  build default info string
        v.addColumnheaders(hmd.getOriginalHeader(),null, null);
        int i = 0;
        for (String s : hmd.getOriginalHeader()) {
            assertEquals(notmetaInInfo.get(i), s); //note that it is ok that #CHROM line is wrong, because HistoryOutPipe will clean that up for us.
            i++;
        }


        //in the metadata but there is not in column header
    }

    public void createHistory(){
        Pipeline p = new Pipeline(
                new HistoryInPipe(),
                new HistoryOutPipe()
        );
        p.setStarts(createInput());
        while(p.hasNext()){
            p.next();
        }
    }

    public List<String> createInput(){
        ArrayList<String> input = new ArrayList<String>();
        input.addAll(header);
        input.addAll(data);
        return input;
    }

    public final List<String> output = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##INFO=<ID=bior.genes.HGNC,Number=.,Type=String,Description=\"BioR property file missing description\">",
            "##INFO=<ID=bior.snpeff.Effect_impact,Number=.,Type=String,Description=\"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes).\">",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
            "21\t26960070\trs116645811\tG\tA\t.\t.\tbior.snpeff.Effect_impact=MODERATE",
            "22\t29138293\trs17885497\tT\tC\t.\t.\tThisColumnShouldNotBeAltered;bior.snpeff.Effect_impact=LOW",
            "21\t38439640\trs73901833\tT\tC\t.\t.\tA column with spaces in it (this should not be passed to VEP, otherwise it will cause problems). This line has multiple sift and polyphen scores;bior.snpeff.Effect_impact=MODERATE",
            "17\t41209681\t41209681\tT\tA\t.\t.\tA variant within the BRCA1 gene ;bior.genes.HGNC=1100;bior.snpeff.Effect_impact=MODIFIER"
    );

    @Test
    public void testCurrentInput(){
        Pipeline p = new Pipeline(
                new HistoryInPipe(),
                new VCFGeneratorPipe(),
                new HistoryOutPipe()
                //new PrintPipe()
                );
        p.setStarts(createInput());
        for(int i=0; p.hasNext(); i++){
            String s = (String)p.next();
            assertEquals(output.get(i), s);
        }
    }

    @Test
    public void testRemoveAnnotation(){
        String result = data.get(1).replaceAll("\\{.*","");
        this.createHistory();
        History h = new History();
        VCFGeneratorPipe v = new VCFGeneratorPipe();
        List<String> orig = v.getBIORColumnsFromMetadata(header);  //all the bior column headers
        Map<Integer,String> biorindexes = v.getBiorColumnsIndexes(h, orig); //init the static vars above
        List<String> elements = Arrays.asList( data.get(1).split("\t") );
        for(String s: elements){
            h.add(s);
        }

        v.removeAnnotationColumns(h, biorindexes);
        StringBuilder sb = new StringBuilder();
        for(String s : h){
            sb.append(s);
            sb.append("\t");
        }
        assertEquals(result, sb.toString());
    }

    public final List<String> correctHeaderOut = Arrays.asList(
        "##INFO=<ID=bior.dbSNP137.INFO.RSPOS,Number=1,Type=Integer,Description=\"Chromosome position reported in dbSNP\">",
        "##INFO=<ID=bior.dbSNP137.INFO.RV,Number=1,Type=Flag,Description=\"RV Desc\">",
        "##INFO=<ID=bior.dbSNP137.INFO.GMAF,Number=1,Type=Float,Description=\"GMAF Desc\">"
    );

    public final List<String> correctDataOut = Arrays.asList(
        "AC=39;AF=0.342;AN=114;BaseQRankSum=-2.185;DP=22;Dels=0.00;FS=4.193;HaplotypeScore=0.0000;MLEAC=37;MLEAF=0.325;MQ=70.00;MQ0=0;MQRankSum=-0.282;QD=20.22;ReadPosRankSum=-1.128;bior.dbSNP137.INFO.GMAF=0.4734;bior.dbSNP137.INFO.RSPOS=28218100;bior.dbSNP137.INFO.RV"
    );
    //head -n 250 /data/VCFExamples/BATCH4.vcf | bior_vcf_to_tjson | bior_same_variant -d /data/catalogs/dbSNP/137/00-All.tsv.bgz | bior_drill -p INFO.RSPOS -p INFO.RV -p INFO.GMAF
    @Test
    public void testRealData() throws IOException {
        int start = 9;
        int end = 105;
        int[] cut = new int[end-start];
        int j = 0;
        for(int i=start;i<end;i++){ cut[j] = i; j++; }

        String catalog =  "src/test/resources/metadata/BATCH4/dbSNP.tsv.bgz";
        Metadata tojson = new Metadata("bior_vcf_to_json");
        Metadata md = new Metadata(catalog, "bior_same_variant");
        History.clearMetaData();
        String paths[] = new String[]{"INFO.RSPOS","INFO.RV","INFO.GMAF"};
        Metadata mddrill = new Metadata(-1, "bior_drill", false, paths);
        ArrayList<Metadata> mdlist = new ArrayList<Metadata>();
        mdlist.add(tojson); mdlist.add(md); mdlist.add(mddrill);
        //In this example, there are 3 drill paths, all need to be added to the info and one bior_same_variant - that gets dropped
        Pipeline p = new Pipeline(
                new CatPipe(),
                new HistoryInPipe(mdlist),
                new VCF2VariantPipe(),
                new SameVariantPipe(catalog),
                new DrillPipe(false, paths),
                new VCFGeneratorPipe(),
                //new HCutPipe(cut),
                new HistoryOutPipe()
                //new GrepEPipe("##"),
                //new GrepPipe("#"),
                //new PrintPipe()
        );
        p.setStarts(Arrays.asList("src/test/resources/metadata/BATCH4/BATCH4.vcf"));
        boolean chromReached = false;
        int dcount = 0;
        boolean[] checks = new boolean[4];
        for(int i=0; i<checks.length; i++){checks[i] = false;}
        String prev = "";
        for(int i=0; p.hasNext(); i++){
            String s = (String) p.next();
            if(s.startsWith("#CHROM")) chromReached = true;

            //check that the info lines exist, are in the correct location, and are correctly formatted
            if(s.startsWith("##INFO=<ID=bior.dbSNP137.INFO.RSPOS")){
                assertEquals(correctHeaderOut.get(0),s);
                assertTrue(prev.startsWith("##INFO=<ID=STR,Number=0,Type=Flag,Description=\"Variant is a short tandem repeat\">"));
                checks[0] = true;
            }
            if(s.startsWith("##INFO=<ID=bior.dbSNP137.INFO.RV")){
                assertEquals(correctHeaderOut.get(1),s);
                checks[1] = true;
            }
            if(s.startsWith("##INFO=<ID=bior.dbSNP137.INFO.GMAF")){
                assertEquals(correctHeaderOut.get(2),s);
                checks[2] = true;
            }

            //check the data lines
            if(dcount == 1){
                String[] split = s.split("\t");
                assertEquals(correctDataOut.get(0),split[7]);
                checks[3] = true;
            }
            //if(i>250) break;
            if(chromReached) dcount++;
            prev = s;
        }
        for(int i=0; i<checks.length;i++){
            assertTrue(checks[i]);//assert that all of the checks where actually done!
        }
    }

    public final List<String> arrayInput = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##BIOR=<ID=\"bior.JsonArray\",Operation=\"bior_foo\",Number=.,ShortUniqueName=JsonArray>",   //data type not defined to make sure test works for float, string and int
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tbior.JsonArray",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData\t[\"A\",\"B\",\"C\"]",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData\t[1,2,3]",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData\t[1.1,2.2,3.3]"
    );

    public final List<String> arrayOutput = Arrays.asList(
            "##fileformat=VCFv4.0",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData;bior.JsonArray=A,B,C",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData;bior.JsonArray=1,2,3",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData;bior.JsonArray=1.1,2.2,3.3"
    );

    @Test
    public void testSanatizeJSONArray(){
        //if the user somehow creates a json array and wants that injected into the info column, then this will test that that is sanitized
        System.out.println("Testing Sanatize JSON Array");
        Pipeline p = new Pipeline(
                new HistoryInPipe(),
                new VCFGeneratorPipe(),
                new HistoryOutPipe()
        );
        p.setStarts(arrayInput);
        List<String> actual = PipeTestUtils.getResults(p);
        PipeTestUtils.assertListsEqual(arrayOutput, actual);
    }

    @Test
    public void testVCFizeAnnotate() throws IOException{
        System.out.println("Testing to see if we can VCFize an annotate output");
        Pipeline p = new Pipeline(
            new CatPipe(),
            new HistoryInPipe(),
            new VCFGeneratorPipe(),
            new HistoryOutPipe()
            //new PrintPipe()
        );
        p.setStarts(Arrays.asList("src/test/resources/vcfizer/annotate.vcf"));
        
        List<String> expected = FileCompareUtils.loadFile("src/test/resources/vcfizer/annotateVcfized.vcf");
        List<String> actual = PipeTestUtils.getResults(p);
        PipeTestUtils.assertListsEqual(expected, actual);
    }


    public final List<String> vcfizeCompressInput = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##BIOR=<ID=\"bior.JsonArray\",Operation=\"bior_compress\",DataType=\"String\",Field=\"JsonArray\",FieldDescription=\"List of Strings\",ShortUniqueName=\"JsonArray\",Delimiter=\"|\",Path=\"REPLACEMEgenes.tsv.bgz\">",
            "##BIOR=<ID=\"bior.JsonArray2\",Operation=\"bior_compress\",DataType=\"String\",Field=\"JsonArray2\",FieldDescription=\"List of Numbers\",ShortUniqueName=\"JsonArray2\",Delimiter=\",\",Path=\"REPLACEMEgenes.tsv.bgz\">",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tbior.JsonArray\tbior.JsonArray2",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData\tA|B|C\t1,2,3"
    );

    public final List<String> vcfizeCompressOutput = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##INFO=<ID=bior.JsonArray,Number=.,Type=String,Description=\"List of Strings\">",
            "##INFO=<ID=bior.JsonArray2,Number=.,Type=String,Description=\"List of Numbers\">",
            "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
            "chr1\t10000\trs00020\tA\tC\t0\t.\tInfoData;bior.JsonArray=A,B,C;bior.JsonArray2=1,2,3"
    );

    @Test
    public void testVCFizeCompress(){
        System.out.println("Compress output converts some number fields into strings (usually with pipe) test to make sure that this works");
        Pipe p = new Pipeline(
                new HistoryInPipe(),
                new VCFGeneratorPipe(),
                new HistoryOutPipe()
                //new PrintPipe()
        );
        p.setStarts(vcfizeCompressInput);
        for(int i=0; p.hasNext();i++){
            String s = (String) p.next();
            assertEquals(vcfizeCompressOutput.get(i),s);
        }

    }

}
