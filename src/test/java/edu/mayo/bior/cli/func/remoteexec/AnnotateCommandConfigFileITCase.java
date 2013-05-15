package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.BaseFunctionalTest;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;

/**
*
* @author Surendra Konathala
*/
public class AnnotateCommandConfigFileITCase extends RemoteFunctionalTest {
	   
		public AnnotateCommandConfigFileITCase() {
	    }
	    
	    @BeforeClass
	    public static void setUpClass() {
	    }
	    
	    @AfterClass
	    public static void tearDownClass() {
	    }
	    
	    @Before
	    public void setUp() {
	    }
	    
	    @After
	    public void tearDown() {	    	
	    }
	    
	    @Rule
	    public TemporaryFolder tFolder = new TemporaryFolder();     
	    
	    private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	    
	    /** default config file is assumed to have all columns 
	     * @throws IOException 
	     * @throws AbnormalExitException 
	     * @throws TimeoutException 
	     * @throws BrokenBarrierException 
	     * @throws InvalidDataException */
	    //@Test
	    public void testDefaultFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
	    	System.out.println("Testing: AnnotateCommand ConfigFile - default");
	    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
			String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/default_output.tsv"));
			
			String configFilePath = "src/test/resources/treat/configtest/default.config";
			
			try{
			Pipe pipeline = new Pipeline(new CatPipe(), 
										new HistoryInPipe(),
										new TreatPipeline(configFilePath), 
										new PrintPipe());
			pipeline.setStarts(Arrays.asList("src/test/resources/treat/gold.vcf"));
						
			while(pipeline.hasNext()) {
				System.out.println("in..");
				pipeline.next();
			}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//mPipeline.execute(new TreatPipeline(configFilePath));
			
			// execute command with config file option - default
			//CommandOutput out = executeScript("bior_annotate", goldInput); //with 'config' option

			//if (out.exit != 0) {
			//	fail(out.stderr);
			//}
			
			//List<String> expectedOutputLines = splitLines(output);
			//List<String> actualOutputLines = splitLines(out.stdout); 
						
			//List<String> expectedOutputLines = splitLines(output);						
			//List<String> actualOutputLines = splitLines(output);			
			
			// compare line-by-line
			//assertEquals(expectedOutputLines.size(), actualOutputLines.size());
			//for (int i=0; i < expectedOutputLines.size(); i++) {
			//	assertEquals(expectedOutputLines.get(i), actualOutputLines.get(i));
			//}    	
	    }
	    

	    /** empty config file with no columns */
	    //@Test
	    public void testEmptyFile() throws IOException, InterruptedException {
	    	System.out.println("Testing: AnnotateCommand ConfigFile - empty file");

	    	// TODO what is the behaviour to test??
	    	    		    	
	    }

	    /** All columns defined */
	    //@Test
	    public void testAllColumns() throws IOException, InterruptedException {
	    	System.out.println("Testing: AnnotateCommand ConfigFile - all columns");
	    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
			String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/all_output.tsv"));
			
			// execute command with config file option - default
			//CommandOutput out = executeScript("bior_annotate", goldInput); //with 'config' option

			//if (out.exit != 0) {
			//	fail(out.stderr);
			//}
			
			//List<String> expectedOutputLines = splitLines(output);
			//List<String> actualOutputLines = splitLines(out.stdout); 
						
			List<String> expectedOutputLines = splitLines(output);						
			List<String> actualOutputLines = splitLines(output);			
			
			// compare line-by-line
			assertEquals(expectedOutputLines.size(), actualOutputLines.size());
			for (int i=0; i < expectedOutputLines.size(); i++) {
				assertEquals(expectedOutputLines.get(i), actualOutputLines.get(i));
			}    	
	    }

	    /** Column names defined, but are invalid */
	    //@Test
	    public void testInvalidColumns() throws IOException, InterruptedException {
	    	// TODO what is the behaviour to test??
	    }

	    /** file with only a subset of columns defined, but has all columns of a datasource. 
	     * In this case either all columns of a datasource exist or none */
	    //@Test
	    public void testSubsetOfColumns() throws IOException, InterruptedException {
	    	System.out.println("Testing: AnnotateCommand ConfigFile - Subset of columns");
	    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
			String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/subset_output.tsv"));
			
			// execute command with config file option - default
			//CommandOutput out = executeScript("bior_annotate", goldInput); //with 'config' option

			//if (out.exit != 0) {
			//	fail(out.stderr);
			//}
			
			//List<String> expectedOutputLines = splitLines(output);
			//List<String> actualOutputLines = splitLines(out.stdout); 
						
			List<String> expectedOutputLines = splitLines(output);						
			List<String> actualOutputLines = splitLines(output);			
			
			// compare line-by-line
			assertEquals(expectedOutputLines.size(), actualOutputLines.size());
			for (int i=0; i < expectedOutputLines.size(); i++) {
				assertEquals(expectedOutputLines.get(i), actualOutputLines.get(i));
			}    	
	    }

	    /** only a subset of columns of a datasource are defined */
	    //@Test
	    public void testSubsetColumnsOfADataSource() throws IOException, InterruptedException {
	    	System.out.println("Testing: AnnotateCommand ConfigFile - subset of columns of a datasource");
	    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
			String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/subset_datasource_output.tsv"));
			
			// execute command with config file option - default
			//CommandOutput out = executeScript("bior_annotate", goldInput); //with 'config' option

			//if (out.exit != 0) {
			//	fail(out.stderr);
			//}
			
			//List<String> expectedOutputLines = splitLines(output);
			//List<String> actualOutputLines = splitLines(out.stdout); 
						
			List<String> expectedOutputLines = splitLines(output);						
			List<String> actualOutputLines = splitLines(output);			
			
			// compare line-by-line
			assertEquals(expectedOutputLines.size(), actualOutputLines.size());
			for (int i=0; i < expectedOutputLines.size(); i++) {
				assertEquals(expectedOutputLines.get(i), actualOutputLines.get(i));
			}    	
	    }
	    
	    private List<String> splitLines(String s) throws IOException {
			List<String> lines = new ArrayList<String>();
			
			BufferedReader br = new BufferedReader(new StringReader(s));
			String line = br.readLine();
			while (line != null)
			{
				lines.add(line);
				line = br.readLine();
			}
			br.close();
			
			return lines;
		}	
}