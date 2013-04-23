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
        System.out.println("VEPEXETest.testGetVEPCommand()");
        String[] actual = VEPEXE.getVEPCommand(null);
        
        String[] expected = { 
        	"", "", "-i", "/dev/stdin", "-o",
        	"STDOUT", "-dir", "", "-vcf", "--hgnc", 
        	"-polyphen", "b", "-sift", "b", "--offline",
        	"--buffer_size", "1"
        };
        
        assertVepCmd(expected, actual);
    }
    
    /** Test of getVEPCommand method, of class VEPEXE. */
    @Test
    public void testWithUserInputs() throws Exception {
        System.out.println("VEPEXETest.testWithUserInputs()");
        String[] actual = VEPEXE.getVEPCommand( new String[] { "--fork", "4", "-all"} );
        
        String[] expected = { 
        	"", "", "-i", "/dev/stdin", "-o",
        	"STDOUT", "-dir", "", "-vcf", "--hgnc", 
        	"-polyphen", "b", "-sift", "b", "--offline",
        	"--buffer_size", "1",
        	"--fork", "4",
        	"-all"
        };
        
        assertVepCmd(expected, actual);
    }

    
    private void assertVepCmd(String[] expected, String[] actual) {
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
