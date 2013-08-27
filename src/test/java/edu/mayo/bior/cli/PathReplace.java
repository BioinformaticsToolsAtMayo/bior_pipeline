package edu.mayo.bior.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: m102417
 * Date: 8/22/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathReplace {

    public static List<String> replacePathDontCare(List<String> input){
        ArrayList output = new ArrayList();
        for(String s : input){
            output.add(s.replaceAll("Path=.*.tsv.bgz","Path=@@Path"));
        }
        return output;
    }
}
