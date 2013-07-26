package edu.mayo.bior.cli.cmd;

import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.JSON.InjectIntoJsonPipe;
import edu.mayo.pipes.JSON.inject.Injector;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import org.junit.Test;

import java.util.Arrays;

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


    @Test
    public void testConvertTab2JSON(){
        Tab2JSONCommand t = new Tab2JSONCommand();
        Injector[] injectors = t.parseConfigFile("src/test/resources/Tab2JSONFiles/example.config");
        InjectIntoJsonPipe inject = new InjectIntoJsonPipe(true, injectors);
        Pipeline pipe = new Pipeline(
                new CatPipe(),
                new HistoryInPipe(),
                inject,
                new HistoryOutPipe(),
                new PrintPipe()
        );
        pipe.setStarts(Arrays.asList("src/test/resources/Tab2JSONFiles/example.tsv"));
        while(pipe.hasNext()){
            pipe.next();
        }
    }

}
