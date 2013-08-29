package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.VEP.VEPPipeline;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class VEPPipelineITCase extends RemoteFunctionalTest {

	private final String VEPDIR = "src/test/resources/tools/vep/";

	private File dataSourceProps;
	private File columnProps;		
	private Metadata metadata;
	
	@Before
	public void setUp() throws IOException
	{
		File biorLiteHome = new File(sHomePath);
		dataSourceProps = new File(biorLiteHome, "conf/tools/vep.datasource.properties");
		columnProps     = new File(biorLiteHome, "conf/tools/vep.columns.properties");
		metadata = new Metadata(dataSourceProps.getCanonicalPath(), columnProps.getCanonicalPath(), "bior_vep");		
	}	

	@After
	public void tearDown()
	{
		History.clearMetaData();
	}

	/**
	 * Loads the file and does a search&replace on any dynamic stuff.
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private List<String> loadExpectedOutputFile(File f) throws IOException
	{
		String expectedContent = FileUtils.readFileToString(f);
		expectedContent = expectedContent.replace("@@SRC_PROP_PATH@@", dataSourceProps.getCanonicalPath());
		expectedContent = expectedContent.replace("@@COL_PROP_PATH@@", columnProps.getCanonicalPath());
		
		return Arrays.asList(expectedContent.split("\n"));
	}	
	/**
	 * Gets the ##BIOR metadata line for the header that corresponds to running bior_vep
	 * @return
	 * @throws IOException
	 */
	private String getMetadataLine() throws IOException
	{
		return String.format(
				"##BIOR=<ID=\"bior.vep\",Operation=\"bior_vep\",DataType=\"JSON\",ShortUniqueName=\"vep\",Description=\"Tool from Ensembl that predicts the functional consequences of known and unknown variants.\",Version=\"2.7\",Build=\"Ensembl Release 69\",DataSourceProperties=\"%s\",ColumnProperties=\"%s\">",
				dataSourceProps.getCanonicalPath(),
				columnProps.getCanonicalPath());		
	}
	
	@Test
	public void singleLine() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
        System.out.println("VEPPipelineITCase.singleline");
        VEPPipeline vepPipe = new VEPPipeline(null, false);
		Pipeline pipe = new Pipeline( new HistoryInPipe(metadata), vepPipe, new HistoryOutPipe() );
		pipe.setStarts(  Arrays.asList(
				"##fileformat=VCFv4.0",
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO",
				"21	26960070	rs116645811	G	A	.	.	." ));
		List<String> actual = PipeTestUtils.getResults(pipe);
		// Expected from: CSQ=A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||LINC00515|4432|||,A|ENSG00000154719|ENST00000352957|Transcript|intron_variant|||||||MRPL39||||,A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||tolerated(0.05)|benign(0.001)|
		List<String> expected = Arrays.asList(
				"##fileformat=VCFv4.0",
				getMetadataLine(),
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	bior.vep",
				"21	26960070	rs116645811	G	A	.	.	.	"
					+ "{\"CSQ\":"
					+  "[{\"Allele\":\"A\",\"Gene\":\"ENSG00000260583\",\"Feature\":\"ENST00000567517\",\"Feature_type\":\"Transcript\",\"Consequence\":\"upstream_gene_variant\",\"HGNC\":\"LINC00515\",\"DISTANCE\":\"4432\"},"
					+  "{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000352957\",\"Feature_type\":\"Transcript\",\"Consequence\":\"intron_variant\",\"HGNC\":\"MRPL39\"},"
					+  "{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000307301\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"1043\",\"CDS_position\":\"1001\",\"Protein_position\":\"334\",\"Amino_acids\":\"T/M\",\"Codons\":\"aCg/aTg\",\"HGNC\":\"MRPL39\",\"SIFT\":\"tolerated(0.05)\",\"PolyPhen\":\"benign(0.001)\",\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":0.05,\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.001}]"
					+  "}"
				);
		vepPipe.terminate();
		
		PipeTestUtils.assertListsEqual(expected, actual);

	}
	
	@Test
	public void pipelineFanout() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
        System.out.println("VEPPipelineITCase.pipelineFanout");
        VEPPipeline vepPipe = new VEPPipeline(null, false);
		Pipeline pipe = new Pipeline( new HistoryInPipe(metadata), vepPipe, new HistoryOutPipe() );
		List<String> input = FileCompareUtils.loadFile("src/test/resources/tools/vep/vepsample.vcf");
		pipe.setStarts(input);
		List<String> actual = PipeTestUtils.getResults(pipe);
		List<String> expected = loadExpectedOutputFile(new File(VEPDIR + "vepsample.expected.fanout2.vcf"));

		vepPipe.terminate();
		
		PipeTestUtils.assertListsEqual(expected, actual);
	}
		
	@Test
	public void pipelineWorst() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
        System.out.println("VEPPipelineITCase.pipelineWorst");
        List<String> input = FileCompareUtils.loadFile("src/test/resources/tools/vep/vepsample.vcf");
		VEPPipeline vepPipe = new VEPPipeline(null, true);
		Pipeline pipe = new Pipeline(
				new HistoryInPipe(metadata),
				vepPipe,
				new HistoryOutPipe()
				);
		pipe.setStarts(input);
		List<String> actual = PipeTestUtils.getResults(pipe);
		List<String> expected = loadExpectedOutputFile(new File(VEPDIR + "vepsample.expected.worstonly.vcf"));

		vepPipe.terminate();
		
		PipeTestUtils.assertListsEqual(expected, actual);
	}
	
	@Test
	public void test_VEPErrorMessage() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
        System.out.println("VEPPipelineITCase.test_VEPErrorMessage");
        VEPPipeline vepPipe = new VEPPipeline(null, false);
		Pipeline pipe = new Pipeline( new HistoryInPipe(metadata), vepPipe, new HistoryOutPipe() );
		pipe.setStarts(  Arrays.asList(
				"##fileformat=VCFv4.0",
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO",
				"2126960070	rs116645811	G	A	.	.	." )); //line has error so VEP will hang
		List<String> actual = PipeTestUtils.getResults(pipe);
		String[] aCSQ = actual.get(3).split("\t");
		
        String expected = "{\"CSQ\":[{\"VEPMessage\":\"VEPERRORMessage\",\"Status\":\"VEP failed to assign function to this variant\"}]}";
        				
		vepPipe.terminate();
		
		assertEquals(aCSQ[7], expected);		
	}

    public File getDataSourceProps() {
        return dataSourceProps;
    }

    public void setDataSourceProps(File dataSourceProps) {
        this.dataSourceProps = dataSourceProps;
    }

    public File getColumnProps() {
        return columnProps;
    }

    public void setColumnProps(File columnProps) {
        this.columnProps = columnProps;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
        
        
        
        
}
