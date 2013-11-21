package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.PathReplace;
import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipelineSingleThread;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 * Test exit codes for bior_annotate (both single-thread and multi-process)
 * exit = 0 : No errors or warnings
 * exit = 0 : May have warnings, some lines may not have been output, but ran to completion
 * exit = 1 : Contained errors that killed the pipeline
 * exit = 1 : Errors and warnings occurred
 * @author mmeiners
 *
 */
public class TreatITCaseExitCodes extends RemoteFunctionalTest
{

    @Before
    public void cleanupBefore(){
        System.out.println("Make sure you have the required catalogs installed and in your path (or mounted over SMB) before you attempt to run the TREAT/ANNOTATE TESTS");
        History.clearMetaData();
    }

    @After
    public void cleanupAfter(){
        History.clearMetaData();
    }

	@Test
	public void testExitCode_singleThread_exit0_noWarnings() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testExitCode_singleThread_exit0_noWarnings():");
		String input = 
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n" +
				"1	216424275	rs696723	C	G	.	.	.\n" +
				"20	14370	rs6054257	G	A	29	PASS	NS=3;DP=14;AF=0.5;DB;H2";

		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", input, "-l", "-c", "src/test/resources/treat/configtest/smallSubset.config");
		assertEquals(0, out.exit);
		assertEquals("", out.stderr);
        List<String> actual = TreatITCase.splitLines(out.stdout);
		List<String> expected = Arrays.asList(
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	rsID	dbSNP.SuspectRegion	dbSNP.ClinicalSig	dbSNP.DiseaseVariant	1000Genomes.EUR_AF",
				"1	216424275	rs696723	C	G	.	.	.	rs696723	0	1	.	0.01",
				"20	14370	rs6054257	G	A	29	PASS	NS=3;DP=14;AF=0.5;DB;H2	.	.	.	.	."
				);
        TreatITCase.compareListsNoHeader(expected, actual, true);
		System.out.println("<<<<<----------- Test passed -----");
	}
	
	@Test
	public void testExitCode_singleThread_exit0_1warning() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testExitCode_singleThread_exit0_warning():");
		String input = 
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n" +
				"1	216424275	rs696723	C	G	.	.	.\n" +
				"20	14370	rs6054257	G	A	29	PASS\n" +   // NOTE: Missing INFO column - this line should not be in output
				"21	21438265	21438265	A	G	.	.	.";

		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", input, "-l", "-c", "src/test/resources/treat/configtest/smallSubset.config");
		assertEquals(0, out.exit);
		assertEquals("WARNING: Found 1 data error(s).  Not all input data rows could be successfully processed.\n", out.stderr);
        List<String> actual = TreatITCase.splitLines(out.stdout);

        List<String> expected = Arrays.asList(
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	rsID	dbSNP.SuspectRegion	dbSNP.ClinicalSig	dbSNP.DiseaseVariant	1000Genomes.EUR_AF",
				"1	216424275	rs696723	C	G	.	.	.	rs696723	0	1	.	0.01",
				"21	21438265	21438265	A	G	.	.	.	rs138077500	0	.	.	0.0026"
				);
        TreatITCase.compareListsNoHeader(expected, actual, true);
		System.out.println("<<<<<----------- Test passed -----");
	}

	@Test
	public void testExitCode_singleThread_exit1_0warning() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testExitCode_singleThread_exit1_0warning():");
		String input = 
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n" +
				"1	216424275	rs696723	C	G	.	.	.\n" +
				"20	14370	rs6054257	G	A	29	PASS\n" +   // NOTE: Missing INFO column - this line should not be in output
				"21	21438265	21438265	A	G	.	.	.";

		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", input, "-l", "-c", "src/test/resources/treat/configtest/badConfigFile");
		assertEquals(1, out.exit);
		assertTrue(out.stderr.contains("ERROR: Invalid value specified for option: --configfile")
				&& out.stderr.contains("The Config file path")
				&& out.stderr.contains("does not exist (or is empty)") );
        List<String> actual = TreatITCase.splitLines(out.stdout);
		List<String> expected = Arrays.asList("");
        TreatITCase.compareListsNoHeader(expected, actual, true);
		System.out.println("<<<<<----------- Test passed -----");
	}

}
