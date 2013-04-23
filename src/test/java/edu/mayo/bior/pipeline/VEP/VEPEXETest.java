package edu.mayo.bior.pipeline.VEP;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *  This unit test just tests getting the command, actually running VEP is in 
 * The VEPITCase functional test.
 * @author m102417
 */
public class VEPEXETest {
    
    /** Test of getVEPCommand method, of class VEPEXE. */
    @Test
    public void testGetVEPCommand() throws Exception {
        System.out.println("getVEPCommand");
        String bufferSize = "20";
        String[] actual = VEPEXE.getVEPCommand(null);
        
        String[] expected = { 
        	"", "", "-i", "/dev/stdin", "-o",
        	"STDOUT", "-dir", "", "-vcf", "--hgnc", 
        	"-polyphen", "b", "-sift", "b", "--offline",
//        	"--buffer_size", bufferSize
        };
        
        for(int i=0; i < expected.length; i++) {
            // Skip these as they are configurable depending on the system we are running on:
            //	0 (VEP's Perl path)
            //	1 (VEP's path)
            //  7 (VEP's cache path)
        	if( i==0 || i==1 || i==7 )
        		continue;
        	assertEquals("Command element [" + i + "] did not match.", expected[i], actual[i]);
        }
    }
}
