package edu.mayo.bior.cli.cmd;

import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.JSON.InjectIntoJsonPipe;
import edu.mayo.pipes.JSON.inject.Injector;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * Created with IntelliJ IDEA.
 * User: m102417
 * Date: 7/24/13
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tab2JSONCommandTest {
    @Test
    public void testParseConfigFile(){
        Tab2JSONCommand t = new Tab2JSONCommand();
        Injector[] injectors = t.parseConfigFile("src/test/resources/Tab2JSONFiles/example.config");
        //System.out.println(injectors.length);
        assertEquals(2, injectors.length);
    }


    public final List<String> out = Arrays.asList(
            "##BIOR=<ID=\"bior.ToTJson\",Operation=\"bior_tab_to_json\",DataType=\"JSON\",ShortUniqueName=\"ToTJson\">",
            "#gene\trsID\tbior.ToTJson",
            "MRPL39\trs142513484\t{\"gene\":\"MRPL39\",\"rsID\":\"rs142513484\"}",
            "MRPL39\trs200187839\t{\"gene\":\"MRPL39\",\"rsID\":\"rs200187839\"}",
            "PANX2\trs191258266\t{\"gene\":\"PANX2\",\"rsID\":\"rs191258266\"}",
            "PANX2\trs145036669\t{\"gene\":\"PANX2\",\"rsID\":\"rs145036669\"}",
            "BRCA1\trs184237074\t{\"gene\":\"BRCA1\",\"rsID\":\"rs184237074\"}",
            "BRCA1\trs189382442\t{\"gene\":\"BRCA1\",\"rsID\":\"rs189382442\"}",
            "BRCA1\trs182218567\t{\"gene\":\"BRCA1\",\"rsID\":\"rs182218567\"}"
    );
    
    @Test
    public void testConvertTab2JSON(){
        Tab2JSONCommand t = new Tab2JSONCommand();
        Metadata md = new Metadata("bior_tab_to_json");
        Injector[] injectors = t.parseConfigFile("src/test/resources/Tab2JSONFiles/example.config");
        InjectIntoJsonPipe inject = new InjectIntoJsonPipe(true, injectors);
        Pipeline pipe = new Pipeline(
                new CatPipe(),
                new HistoryInPipe(md),
                inject,
                new HistoryOutPipe()
                //new PrintPipe()
        );
        pipe.setStarts(Arrays.asList("src/test/resources/Tab2JSONFiles/example.tsv"));
        for(int i = 0; out.size() > i; i++){
            String s = (String) pipe.next();
            assertEquals(out.get(i), s);
        }
    }

}
