package edu.mayo.bior.pipeline;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.SplitPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;

/**
 * Pipeline that takes a history containing VCF columns and appends a JSON payload.
 */
public class VCF2VariantPipeline {

	public static void main(String[] args) {

		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		SplitPipe 		split 	= new SplitPipe("\t");
		VCF2VariantPipe	vcf2Var	= new VCF2VariantPipe();
		MergePipe		merge 	= new MergePipe("\t");
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,		// each STDIN line	--> String 
					split,	// each String		-->	history created
					vcf2Var,// history			--> history + JSON column appended
					merge,	// history			--> String
					print	// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
        pipeline.setStarts(Arrays.asList(System.in));

        // run pipeline
        while (pipeline.hasNext()) {
        	pipeline.next();
        }
	}
}
