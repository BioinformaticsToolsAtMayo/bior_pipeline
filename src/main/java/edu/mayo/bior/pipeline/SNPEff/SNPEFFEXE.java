/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.PipeFunction;
import edu.mayo.exec.UnixStreamCommand;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author m102417
 */
        public class SNPEFFEXE implements PipeFunction<String,String>{
            /** 
             * This stuff needs to be configured!
             */ 
            public static final String snpeffpath = "/data/snpEff/snpEff_2_0_5/snpEff.jar";
            public static final String snpeffconfig = "/data/snpEff/snpEff_2_0_5/snpEff.config";
            private static final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();
        
            //java -Xmx4g -jar /data/snpEff/snpEff_3_1/snpEff.jar eff -c /data/snpEff/snpEff_3_1/snpEff.config -v GRCh37.68 example.vcf > output.vcf
            public static final String[] command = {"java", 
               "-Xmx4g", 
                "-jar", 
                snpeffpath,
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
            private static final Logger log = Logger.getLogger(UnixStreamCommand.class);
            private UnixStreamCommand snpeff;
            public SNPEFFEXE() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
                snpeff = new UnixStreamCommand(command, NO_CUSTOM_ENV, true, true); 
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
            int counter = 0;
            @Override
            public String compute(String a) {
                try {
                    snpeff.send(a);
                    return snpeff.receive();
                } catch (IOException ex) {
                    log.error(ex);
                } catch (InterruptedException ex) {
                    log.error(ex);
                } catch (BrokenBarrierException ex) {
                    log.error(ex);
                } catch (TimeoutException ex) {
                    log.error(ex);
                }
                throw new NoSuchElementException();
            }
            public void terminate() throws InterruptedException{
                this.snpeff.terminate();
            }
        }
