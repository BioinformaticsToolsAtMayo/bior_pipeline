package edu.mayo.bior.cli.func.remoteexec;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.Treat.TreatPipelineMultiCmd;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class Bior2VCFITCase extends RemoteFunctionalTest {
	
	@Test
	public void test() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
        System.out.println("Bior2VCFITCaseTest");
        Pipeline pipes = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(),
				new TreatPipelineMultiCmd("src/test/resources/treat/configtest/smallSubset.config"),
				new HistoryOutPipe(),
				new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/gold.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
		
		//System.out.println(Arrays.asList(actual));
		System.out.println("Test passed.");
	}

}
