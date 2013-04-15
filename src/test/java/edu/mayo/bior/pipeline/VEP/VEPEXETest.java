/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;

import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  This unit test just tests getting the command, actually running VEP is in 
 * The VEPITCase functional test.
 * @author m102417
 */
public class VEPEXETest {
    
    public VEPEXETest() {
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

    /**
     * Test of getVEPCommand method, of class VEPEXE.
     */
    //@Test
    // TODO: Disabled until Dan can refactor
    public void testGetVEPCommand() throws Exception {
        System.out.println("getVEPCommand");
        String bufferSize = "";
        String[] result = VEPEXE.getVEPCommand(bufferSize);
        //result[0] is vep's perl, don't test that, it is configurable
        //result[1] is vep's path, don't test that, it is also configurable
        assertEquals("-i", result[2]);
        //assertEquals("/dev/stdin", result[3]);

    }
}
