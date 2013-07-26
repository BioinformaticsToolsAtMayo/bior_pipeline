/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.PrintPipe;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 *
 * @author m102417
 * Given a simple tab-delimited header line, this command will construct a
 * default config file for use in Tab2JSON
 */
public class CreateTab2JSONConfig implements CommandPlugin {
	
    
        private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
        
	public void init(Properties props) throws Exception {
	}
        
        
        /**

         * This function creates the config file based on column headers

         * @param filename 
         */
        public class CreateConfig implements PipeFunction{

        @Override
        public Object compute(Object a) {
            StringBuilder sb = new StringBuilder();
            String s = (String) a;
            String[] split = s.split("\t");
            //System.out.println(s);
            for(int i=0; i<split.length; i++){
                //0) Column# (not specified) is the column number that we wish to get the data from.
                sb.append(i+1);
                sb.append("\t");
                //1) Key is the name of the identifier used to describe the value in the column
                sb.append(split[i].trim());
                sb.append("\t");
                //2) JsonType is 	BOOLEAN, NUMBER, or STRING -- default here is string
                sb.append("STRING\t");
                //3) InjectorType is LITERAL, ARRAY, or COLUMN     -- here is Column
                sb.append("COLUMN\t");
                //4) Delimiter/Literal_Value (.)
                sb.append(".\t");
                //5) Golden Identifier (.)
                sb.append(".\n");
            }
            return sb.toString();
        }
            
        }


	public void execute(CommandLine line, Options opts) throws Exception {
                TransformFunctionPipe t = new TransformFunctionPipe(new CreateConfig());
                this.mPipeline.execute(new IdentityPipe(), t, new IdentityPipe());				
	}
    
    
}
