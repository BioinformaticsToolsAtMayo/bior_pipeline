package edu.mayo.bior.cli.func.remoteexec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Test;

import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.VEP.VEPEXE;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class VEPEXEITCase extends RemoteFunctionalTest {

	private final String VEPDIR = "src/test/resources/tools/vep/";
	
	@After
	public void tearDown()
	{
		History.clearMetaData();
	}
	
	@Test
	/** Test only the output of VEP itself based on input (no JSON conversion, just the raw output) */
	public void vepExeOnlyPipe() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
        System.out.println("VEPEXEITCase.vepExeOnlyPipe");
        double start = System.currentTimeMillis();
		String[] vepCmd = VEPEXE.getVEPCommand(null);
		System.out.println("VEP Command: " + Arrays.asList(vepCmd));
		VEPEXE vepExe = new VEPEXE(vepCmd);
		Pipeline p = new Pipeline(
				new CatPipe(),               //raw file
				new HistoryInPipe(),         //get rid of the header
				new MergePipe("\t"),
				new TransformFunctionPipe(vepExe)
				);
		p.setStarts(Arrays.asList(VEPDIR + "example.vcf"));

		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "example.vcf.vep.correct");
		// Remove the header (first 3 lines) from the expected output
		while(expected.size() > 0 && expected.get(0).startsWith("#"))
			expected.remove(0);
		
		List<String> actual = PipeTestUtils.getResults(p);
		
		vepExe.terminate();

		//printComparison(p, expected, actual);

		PipeTestUtils.assertListsEqual(expected, actual);
		
		double end = System.currentTimeMillis();
		System.out.println("VEPITCase.vepExeOnlyPipe() - Total runtime: " + (end-start)/1000.0);
	}
}
