/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.JSON.InjectIntoJsonPipe;
import edu.mayo.pipes.JSON.inject.ColumnArrayInjector;
import edu.mayo.pipes.JSON.inject.ColumnInjector;
import edu.mayo.pipes.JSON.inject.Injector;
import edu.mayo.pipes.JSON.inject.JsonType;
import edu.mayo.pipes.JSON.inject.LiteralInjector;
import edu.mayo.pipes.SplitPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.bioinformatics.vocab.CoreAttributes;
import edu.mayo.pipes.bioinformatics.vocab.Type;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 *
 * @author dquest
 */
public class Tab2JSONCommand implements CommandPlugin {
    private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	private String operation;
    
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}
        
        
        /**
         * A config file takes the following tab-delimited form:
         * Key JsonType InjectorType    Delimiter/Literal_Value
         * 
         * 
         * 0) Column# (not specified) is the column number that we wish to get the data from.  
         * The parser will insert the keys in the order that they are found in the file
         * It will assume there is one key for every column in the tab delimited file
         * Followed by any literals that should be injected for all values in the set
         * 
         * Here is what each of these values means:
         * 
         * 1) Key is the name of the identifier used to describe the value in the column
         * 
         * 2) JsonType is 	BOOLEAN, NUMBER, or STRING
         * This describes the types of values the column can take.
         * 
         * 3) An injector takes the value from the tab delimited file and puts it into the JSON column.
         * InjectorType is LITERAL, ARRAY, or COLUMN
         * LITERAL - every JSON will have the same value over the entire set
         * COLUMN - the data that appears in the COLUMN will be injected into the JSON (99% of the time this is what you want)
         * ARRAY - the data that appears in the column is actually a delimited array (e.g. values separated by a comma) and should be converted to a JSON array
         * 
         * 4) Delimiter/Literal_Value additional information to direct the injector
         * If the injector is a COLUMN this value is just a period (.)
         * If the injector is a LITERAL, this value is the value of the literal that should be injected.
         * If the injector is an ARRAY, this denotes the delimiter that should be used to parse the array.
         * 
         * 5) Golden Identifier (used to be called attribute)
         * If the column can be interpreted as a golden identifier (e.g. _landmark, _minBP) then place it here
         * else place a dot (.)
         * There can not be more than one golden attributed associated with a column, users will need to use tools
         * such as perl and awk to replicate the column before ingesting the data.
         * 
         * @param filename 
         */
        public Injector[] parseConfigFile(String filename){
            ArrayList<Injector> injectors = new ArrayList<Injector>();
            ArrayList<Injector> addQueue = new ArrayList<Injector>(); //for those injectors that we need to add out of order
            Pipeline<String,ArrayList<String>> parse = new Pipeline<String,ArrayList<String>>(
                    new CatPipe(),
                    new SplitPipe("\t")
                    );
            parse.setStarts(Arrays.asList(filename));
            int count = 0;
            for(count = 0; parse.hasNext(); count++){
                ArrayList<String> next = parse.next();
                if(next.size() < 5) break;
                Integer col = new Integer(next.get(0));
                Injector i = null;  Injector j = null;
                if(next.get(3).equalsIgnoreCase("LITERAL")){
                    //e.g. new LiteralInjector(CoreAttributes._type.toString(), Type.VARIANT.toString(), JsonType.STRING),
                    //String key, String value, JsonType type
                    i = new LiteralInjector(next.get(1), next.get(4), JsonType.valueOf(next.get(2)));
                    if(!next.get(5).equalsIgnoreCase(".")){
                        j = new LiteralInjector(next.get(5), next.get(4), JsonType.valueOf(next.get(2)));
                        addQueue.add(j);
                    }
                }else if(next.get(3).equalsIgnoreCase("COLUMN")){
                    //int column, String key, JsonType type
                    i = new ColumnInjector(col, next.get(1), JsonType.valueOf(next.get(2)));
                    if(!next.get(5).equalsIgnoreCase(".")){
                        j = new ColumnInjector(col, next.get(5), JsonType.valueOf(next.get(2)));
                        addQueue.add(j);
                    }
                }else if(next.get(3).equalsIgnoreCase("ARRAY")){
                    //int column, String key, JsonType type, String delimiterRegex, boolean stripWhitespace
                    i= new ColumnArrayInjector(col, next.get(1), JsonType.valueOf(next.get(2)), next.get(4), true);
                    if(!next.get(5).equalsIgnoreCase(".")){
                        j = new ColumnArrayInjector(col, next.get(1), JsonType.valueOf(next.get(2)), next.get(4), true);
                        addQueue.add(j);
                    }
                }
                
                injectors.add(i);
                
            }
            for(Injector i : addQueue){
                injectors.add(i);
            }
            Injector[] ret = new Injector[injectors.size()];
            ret = injectors.toArray(ret);

            return ret;
            
        }

	public void execute(CommandLine line, Options opts) throws Exception {

        String config = "";
        if(line.hasOption('c')){
            config = line.getOptionValue('c');    
        }
        Injector[] injectors = parseConfigFile(config);

		Metadata metadata = new Metadata(operation);
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
		Pipe<History, History>  logic     = new InjectIntoJsonPipe(true, injectors);
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		mPipeline.execute(preLogic, logic, postLogic);        
	}
    
}
