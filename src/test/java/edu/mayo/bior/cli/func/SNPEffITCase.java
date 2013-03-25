/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.SNPEff.SNPEffPreProcessPipe;
import edu.mayo.bior.pipeline.SNPEff.VCFProgram2HistoryPipe;
import edu.mayo.exec.Command;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.ExecPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;

/**
 *
 * @author m102417
 */
public class SNPEffITCase {
        private static final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
        private static final String SEP = System.getProperty("line.separator");

        public final String treatvcf = "src/test/resources/tools/treat/treatInput.vcf";
        public final String snpeff = "/data/snpEff/snpEff_2_0_5/snpEff.jar";
        public final String snpeffconfig = "/data/snpEff/snpEff_2_0_5/snpEff.config";
        
           //java -Xmx4g -jar /data/snpEff/snpEff_3_1/snpEff.jar eff -c /data/snpEff/snpEff_3_1/snpEff.config -v GRCh37.68 example.vcf > output.vcf
           final String[] command = {"java", 
               "-Xmx4g", 
                "-jar", 
                snpeff,
                "eff",
                "-c",
                snpeffconfig,
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


        //@Test
        public void testExecSNPEff() throws IOException, InterruptedException{
            System.out.println("Test the raw output of a run on SNPEff");
            
           //first test to see if SNPEff is in the path and can be run..
                String[] cmdarray = {"cat", "-n"}; //number all output lines
		boolean useParentEnv = true;
		Command c = new Command(command, NO_CUSTOM_ENV, useParentEnv);
		
		List<String> inLines = Arrays.asList("foobar", "test"); 
		c.execute(inLines);
		assertEquals(1159175, c.getStdout().length());

                //System.out.println(c.getStderr());
                //System.out.println(c.getStdout());
        }
        
        /**
         * note: if you want to dig deep and debug this code, you probably want to set your log4j properties to:
         * ##active
           ## log4j configuration used during build and unit tests
           #log4j.rootLogger=DEBUG, console
           #log4j.threshhold=ALL

           ## console appender logs to STDOUT
           #log4j.appender.console=org.apache.log4j.ConsoleAppender
           ##log4j.appender.console.layout=org.apache.log4j.PatternLayout
           #log4j.appender.console.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
         * 
         * 
         * @throws IOException
         * @throws InterruptedException 
         */
        @Test
        public void testExecSNPEffPipe() throws IOException, InterruptedException{
            System.out.println("Test the raw output of a run on SNPEff");
        
            ExecPipe exe = new ExecPipe(command, true);
            //BridgeOverPipe b = new BridgeOverPipe();
            Pipeline q = new Pipeline(new CatPipe(), new VCFProgram2HistoryPipe(), new MergePipe("\t"));
            Pipeline p = new Pipeline(
                    new CatPipe(),
                    new HistoryInPipe(),
                    new SNPEffPreProcessPipe(),
                    //new PrintPipe(),
                    exe,
                    new VCFProgram2HistoryPipe(),
                    new MergePipe("\t"),
                    new PrintPipe()
                    );
            p.setStarts(Arrays.asList(treatvcf));
            q.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
            //q.next();q.next();q.next();q.next();q.next();//advance past the headers
            //q.next();
            //p.next();
            for(int i=1;p.hasNext();i++){
                System.out.println(i);
                String o = (String) p.next();
                String res = (String) q.next();
                assertEquals(res,o);                           
            }
            exe.shutdown();
        }
        
        
        //@Test
//        public void testExecSNPEffWOPipe() throws IOException, InterruptedException{
//            System.out.println("Test the raw output of a run on SNPEff");
//            
//            UnixStreamCommand cmd = new UnixStreamCommand(command, NO_CUSTOM_ENV, true,  UnixStreamCommand.StdoutBufferingMode.LINE_BUFFERED, 0);
//            cmd.launch();
//            
//            BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
//            BufferedReader br2 = new BufferedReader(new FileReader("src/test/resources/tools/treat/treatInput.vcf"));
//            String line;
//            List<String> output;
//            int count = 0;
//            br.readLine();
//            br.readLine();
//            br.readLine();
//            br2.readLine();
//            br2.readLine();
//            while((line = br.readLine()) != null){
//                count++;
//                System.out.println(line);
//                String input = br2.readLine();
//                if(!input.startsWith("#")){
//                    if(count==1){
//                        cmd.send(Arrays.asList("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n" + input));
//                    }else{
//                        cmd.send(Arrays.asList(input));
//                        output = cmd.receive();
//                        System.out.println(output.get(0));
//                        if(count >2){
//                            assertEquals(line,output.get(0));
//                        }
//                    }
//                }
//            }
//
//        }
        
        //this sucker takes a long time to run, so we don't run it as common integration tests...
        //@Test
        public void testSNPEFF() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{

            final String[] snpeffCmdArray =
            	{
            	    "java", 
                    "-Xmx4g", 
                     "-jar", 
                     snpeff,
                     "eff",
                     "-c",
                     snpeffconfig,
                     "-v",
                     "GRCh37.64",
                     "-o",
                     "vcf",
                     "-noLog",
                     "-noStats"
                };
            UnixStreamCommand snpeff = new UnixStreamCommand(snpeffCmdArray, NO_CUSTOM_ENV, true, true);        	
        	
        	File inputFile    = new File("src/test/resources/tools/treat/treatInput.vcf");            
            File outputFile   = new File("/tmp/snpEffOutput.vcf");
            File expectedFile = new File("src/test/resources/tools/snpeff/snpEffOutput205.vcf");

            // make sure we start with a clean output file
            if (outputFile.exists()) {
            	outputFile.delete();
            	outputFile.createNewFile();
            }            

            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile));            
            
            // launch snpeff java process
            snpeff.launch();
            
            // send VCF header, this is required
            snpeff.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");

            boolean outputHeaderProcessed = false;
            
            String line = br.readLine();
            while( line != null ) {

            	// only send VCF data lines to snpeff
                if(!line.startsWith("#")){
                	
                	// send data line to snpeff
                    snpeff.send(line);

                    // receive data line from snpeff
                    String outputLine = snpeff.receive();
                    
                    // handle header outputted from SNPEFF
                    while (outputLine.startsWith("#")) {
                    	pw.println(outputLine);
                        pw.flush();
                        outputLine = snpeff.receive();
                    }
                    
                	pw.println(outputLine);
                    pw.flush();
                }
                
                line = br.readLine();
            }            
            // close stream to output file
            pw.close();
            
            // tell SNPEFF we're done
            snpeff.terminate();
            
            // perform diff between output file and expected file
            String[] diffArray = new String[] 
            	{
            		"diff",
            		expectedFile.getAbsolutePath(),
            		outputFile.getAbsolutePath()
            	};
            Command diff = new Command(diffArray, NO_CUSTOM_ENV, true);
            diff.execute();
            System.out.println(
            	String.format(
            		"Output from running: diff %s %s",
            		expectedFile.getAbsolutePath(),
            		outputFile.getAbsolutePath()            		
            	)
            );            
            // show diff output in console
            System.out.println(diff.getStdout());
        }
    
//        @Test
//        public void testTreat(){
//            System.out.println("Testing if SNPEff matches Treat");
//            String treatExample = "src/test/resources/tools/treat/treatExample.xls";
//            Pipe t = new TransformFunctionPipe(new Pad());
//            Pipeline p = new Pipeline(
//                    new CatPipe(),
//                    new SplitPipe("\t"),
//                    t,
//                    //new MergePipe("\t"),
//                    //new WritePipe("/tmp/foo.vcf", false, true)
//                    new PrintPipe()
//                    );
//            p.setStarts(Arrays.asList(treatExample));
//            while(p.hasNext()){
//                p.next();
//            }
//        }
        
//        public class Pad implements PipeFunction<ArrayList<String>,ArrayList<String>> {
//
//            @Override
//            public ArrayList<String> compute(ArrayList<String> a) {
//                ArrayList<String> ret = new ArrayList<String>();
//                ArrayList<Integer> keep = new ArrayList<Integer>();  
//                keep.add(1);
//                keep.add(2);
//                keep.add(3);
//                keep.add(18);
//                keep.add(19);
////                keep.add(58);
////                keep.add(59);
////                keep.add(60);
////                keep.add(61);
////                keep.add(62);
////                keep.add(63);
////                keep.add(64);
////                keep.add(65);
////                keep.add(66);
////                keep.add(67);
////                keep.add(68);
//                for(int i=0; i<a.size(); i++){
//                    if(keep.contains(new Integer(i))){
//                        ret.add(a.get(i));
//                    }
//                }
//                ret.add(5, ".");
//                ret.add(6, ".");
//                ret.add(7, ".");
//                //System.out.println(a.size());
//                return ret;
//            }
//        }
}
