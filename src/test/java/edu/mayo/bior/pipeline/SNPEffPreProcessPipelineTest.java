package edu.mayo.bior.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgramPreProcessPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class SNPEffPreProcessPipelineTest {

	@Test
	public void verifyOutput() throws IOException {
		Pipeline pipe = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(),
				new VCFProgramPreProcessPipe()
				);
		pipe.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEff.preProcess.input.1.vcf"));
		ArrayList<String> actual = PipeTestUtils.pipeOutputToStrings2(pipe);
		ArrayList<String> expected = (ArrayList<String>) FileCompareUtils.loadFile(
				"src/test/resources/tools/snpeff/snpEff.preProcess.expected.1.vcf");
		PipeTestUtils.assertListsEqual(expected, actual);
	}
}
