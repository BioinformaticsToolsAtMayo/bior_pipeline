/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import com.jayway.jsonpath.JsonPath;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.WritePipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import edu.mayo.cli.CommandPlugin;


/**
 *
 * @author m102417
 * 
 * Assumes that the user does not know the structure required for a catalog
 * e.g.
 * _landmark    _minBP  _maxBP  JSON
 * 
 * it will format a TJSON input file by cutting the JSON, 
 * drilling out the golden identifiers,
 * fixing any dots that result, to conform to the standard
 * removing any header
 * and prepping the file for bgzip followed by tabix
 * 
 */
public class CreateCatalogCommand implements CommandPlugin {
    
    private static final char OPTION_INPUT_FILE = 'i';
    private static final char OPTION_OUTPUT_FILE = 'o';
    private static final char OPTION_JSON_COLUMN = 'c';

    public void init(Properties props) throws Exception {

    }

    public void execute(CommandLine cl, Options optns) throws Exception {
        String infile = "";
        String outfile = "";
        if (cl.hasOption(OPTION_INPUT_FILE)) {
                infile = cl.getOptionValue(OPTION_INPUT_FILE);
        }
        if (cl.hasOption(OPTION_OUTPUT_FILE)) {
                outfile = cl.getOptionValue(OPTION_OUTPUT_FILE);
        }
        int jsoncol = -1;
        if (cl.hasOption(OPTION_JSON_COLUMN)) {
            jsoncol = new Integer(cl.getOptionValue(OPTION_JSON_COLUMN));
        }

        TransformFunctionPipe tjson2cat = new TransformFunctionPipe(new TJSON2Catalog(jsoncol));
        Pipeline p = new Pipeline(
                new CatPipe(), //input
                new HistoryInPipe(),
                tjson2cat,
                new WritePipe(outfile, false, true)//create a new file, append newlines to each line of input
                );
        p.setStarts(Arrays.asList(infile));
        while(p.hasNext()){
            p.next();
        }

    }

    public TJSON2Catalog newTJSON2Catalog(int c){
        return new TJSON2Catalog(c);
    }

    public class TJSON2Catalog implements PipeFunction<History,String> {

        private JsonPath landmarkPath = JsonPath.compile("_landmark");
        private JsonPath minBPPath = JsonPath.compile("_minBP");
        private JsonPath maxBPPath = JsonPath.compile("_maxBP");
        private int col = -1;
        private boolean firstRow = true;
        /**
         *
         * @param column  - the column to extract the json from.
         */
        public TJSON2Catalog(int column){
            col = column;
        }

        private void fixColumn(int historySize){
            if(firstRow == true){
                if(col > -1){
                    col = col -1;
                }
                if(col <= -1){
                    col = historySize + col;
                }
            }
            firstRow = false;
        }

        public String extractLandmark(String json){
            String landmark = "UNKNOWN";
            try{
                Object o = landmarkPath.read(json);
                if (o != null) {
                    String s = o.toString().trim();
                    if(s.equalsIgnoreCase(".")){
                        ;//do nothing
                    }else{
                        landmark = s;
                    }
                }
            }catch (Exception e){
                ;// do nothing it is UNKNOWN
            }
            return landmark;
        }

        public String extractMinBP(String json){
            String minbp = "0";
            try{
                Object o = minBPPath.read(json);
                if (o != null) {
                    String s = o.toString().trim();
                    if(s.equalsIgnoreCase(".")){
                        ;//do nothing
                    }else if(minbp.startsWith("-")){
                        ;//negative number, do nothing
                    }else{
                        Integer i = new Integer(minbp);  //this should work else an exception will be thrown
                        minbp = s;
                    }
                }
            }catch (Exception e){
                ;// do nothing it is 0
            }
            return minbp;
        }

        public String extractMaxBP(String json){
            String maxbp = "0";
            try{
                Object o = maxBPPath.read(json);
                if (o != null) {
                    String s = o.toString().trim();
                    if(s.equalsIgnoreCase(".")){
                        ;//do nothing
                    }else if(maxbp.startsWith("-")){
                        ;//negative number, do nothing
                    }else{
                        Integer i = new Integer(maxbp);  //this should work else an exception will be thrown
                        maxbp = s;
                    }
                }
            }catch (Exception e){
                ;// do nothing it is 0
            }
            return maxbp;
        }

        @Override
        public String compute(History h) {

            fixColumn(h.size());
            //first remove all of the columns but the json column
            StringBuilder sb = new StringBuilder();
            String json = h.get(col);
            String landmark = extractLandmark(json);
            String minbp = extractMinBP(json);
            String maxbp = extractMaxBP(json);

            sb.append(landmark);
            sb.append("\t");
            sb.append(minbp);
            sb.append("\t");
            sb.append(maxbp);
            sb.append("\t");
            sb.append(json);

            return sb.toString();  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
    
}
