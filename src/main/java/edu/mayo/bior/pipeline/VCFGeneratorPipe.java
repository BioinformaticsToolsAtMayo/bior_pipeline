package edu.mayo.bior.pipeline;


import java.text.ParseException;
import java.util.*;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.util.metadata.AddMetadataLines;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryMetaData;

public class VCFGeneratorPipe extends AbstractPipe<History, History> {

    public final String DEFAULT_DESCRIPTION = "BioR property file missing description";
    public final String DEFAULT_TYPE = "String";
    public final String DEFAULT_NUMBER = ".";
    private static final Logger sLogger = Logger.getLogger(VCFGeneratorPipe.class);
    Map<Integer, String> biorindexes = new HashMap<Integer, String>();
    boolean modifyMetadata = false;

    @Override
    protected History processNextStart() throws NoSuchElementException {
        History history = this.starts.next();
        // Modify Metadata only once
        if (!modifyMetadata) {
            history = changeHeader(history);
            modifyMetadata = true;
        }
        history = modifyhistory(history, biorindexes);
        history = (History) removeAnnotationColumns(history, biorindexes);

        return history;
    }

//    public History changeHeader(History history){
//        List<String> biorcolumnsFromMetadata = getBIORColumnsFromMetadata(History.getMetaData().getOriginalHeader());
//        List<String> colsFromHeader = getBIORColumnsFromHeader(History.getMetaData().getColumns(), biorcolumnsFromMetadata);
//        return history;
//    }

    public History changeHeader(History history) {
        int totalcolumns = History.getMetaData().getColumns().size();
        //System.err.println(History.getMetaData().getOriginalHeader().get(History.getMetaData().getOriginalHeader().size()-1));
        List<String> biorcolumnsFromMetadata = getBIORColumnsFromMetadata(History.getMetaData().getOriginalHeader());
        List<String> colsFromHeader = getBIORColumnsFromHeader(History.getMetaData().getColumns(), biorcolumnsFromMetadata);
        if (totalcolumns > 7 && History.getMetaData().getColumns().get(7).getColumnName().equalsIgnoreCase("INFO")) {
            biorindexes = getBiorColumnsIndexes(history, biorcolumnsFromMetadata);
        }
        //checks if biorcolumns is not null
        if (biorcolumnsFromMetadata != null) {

            if (biorcolumnsFromMetadata.containsAll(colsFromHeader) && biorcolumnsFromMetadata.size() == colsFromHeader.size()) {
                HistoryMetaData hmd = removeColumnHeader(History.getMetaData(), biorindexes);
                History.getMetaData().setOriginalHeader(
                        addColumnheaders(
                                hmd.getOriginalHeader(), null, null)
                );

            } else if (biorcolumnsFromMetadata.containsAll(colsFromHeader) && biorcolumnsFromMetadata.size() > colsFromHeader.size()) {

                List<String> biorcolumn = biorcolumnsFromMetadata;
                biorcolumn.removeAll(colsFromHeader);
                HistoryMetaData hmd = (removeColumnHeader(History.getMetaData(), biorindexes));
                History.getMetaData().setOriginalHeader(
                        addColumnheaders(hmd.getOriginalHeader(), null, biorcolumn)
                );

            } else if (colsFromHeader.containsAll(biorcolumnsFromMetadata) && colsFromHeader.size() > biorcolumnsFromMetadata.size()) {

                List<String> addDefaultColumn = colsFromHeader;
                addDefaultColumn.removeAll(biorcolumnsFromMetadata);
                History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(), biorindexes)).getOriginalHeader(), addDefaultColumn, null));

            } else if (!colsFromHeader.containsAll(biorcolumnsFromMetadata) || !biorcolumnsFromMetadata.containsAll(colsFromHeader)) {

                List<String> biorcolumn = biorcolumnsFromMetadata;
                biorcolumn.removeAll(colsFromHeader);
                List<String> addDefaultColumn = colsFromHeader;
                addDefaultColumn.removeAll(biorcolumnsFromMetadata);
                History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(), biorindexes)).getOriginalHeader(), addDefaultColumn, biorcolumn));

            }
        } else {

            //No ##BIOR available since bior
            History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(), biorindexes)).getOriginalHeader(), colsFromHeader, null));

        }
        return history;
    }


    /**
     * Returns a Map of BioR column index and name
     *
     * @param h              - History object containing all metadata (currently static, but we may refactor)
     * @param biorcolumn     - these are the IDs in the ##BIOR columns, most will be bior.X but some may be any arbitrary string (e.g. output from bior_annotate)
     *                       get these by calling getBIORColumnsFromMetadata();
     * @return
     */
    public Map<Integer, String> getBiorColumnsIndexes(History h,  List<String> biorcolumn) {
        int totalcolumn = History.getMetaData().getColumns().size();
        List<ColumnMetaData> columns = History.getMetaData().getColumns();
        List<String> originalheader = History.getMetaData().getOriginalHeader();
        Map<Integer, String> biorindex = new HashMap<Integer, String>();
        int indexsize = originalheader.size();
        String columnheader = originalheader.get(indexsize - 1);
        if (columnheader.startsWith("#CHROM")) {
            for (int i = 0; i < columns.size(); i++) {
                String colname = columns.get(i).getColumnName();
                if (colname.contains("bior") || colname.contains("BIOR")) {
                    biorindex.put(i, colname);
                } else if (biorcolumn.contains(colname)) {
                    biorindex.put(i, colname);
                }
            }
        }
        return biorindex;
    }


    /* Add ##INFO columns to the Metadata lines
     * @param colmeta (List<String> of current metadata) - the current header as a list
     * @param retain (List of BioRColumns that has no ##BIOR...Constructs default ##INFO) --for these build a default info string
     * @param remove (List of BioRColumns that are only mentioned in ##BIOR but not in ColumnHeader(no data available)) -- in the metadata but there is not in column header
     * @returns List of modified metadata after adding ##INFO Columns
     */
    public List<String> addColumnheaders(List<String> colmeta, List<String> retain, List<String> remove) {
        List<String> infoMeta = new ArrayList<String>();
        List<String> biorList = new ArrayList<String>();

        for (String info : colmeta) {
            //Places all BIOR Headers in a list
            if (info.startsWith("##BIOR=<ID")) {
                biorList.add(info);
            }

            if (info.startsWith("##BIOR=<ID") && info.contains("drill")) {
                LinkedHashMap<String,String> attr = amdl.parseHeaderLine(info);
                //If there is nothing to remove, then add a new ##INFO
                if (remove == null ) {
                    String newInfoRow =buildInfoFromBioRAttr(attr);
                    //System.err.println(newInfoRow);
                    infoMeta.add(newInfoRow);
                //If remove does not contain the current line, then add new ##INFO
                }else if(!remove.contains(attr.get("ID"))){
                    String newInfoRow =buildInfoFromBioRAttr(attr);
                    //System.err.println(newInfoRow);
                    infoMeta.add(newInfoRow);
                }

            }
        }

        // Builds default Info Metadata line when ##BIOR is not available
        if (retain != null && !retain.isEmpty()) {
            for (String key : retain) {
                String newInfoRow = buildDefaultINFO(key);
                //System.err.println(newInfoRow);
                infoMeta.add(newInfoRow);
            }
        }

        int index = lastInfoLineNumber(colmeta);

        //add the info columns before the #CHROM column header
        colmeta.addAll(index, infoMeta);
        colmeta.removeAll(biorList);
        return colmeta;
    }


    /**
     * for a line without a ##BIOR line and returns a ##INFO line
     * @return
     */
    public String buildDefaultINFO(String id){
        return buildINFO(id,".","String",DEFAULT_DESCRIPTION);
    }

    AddMetadataLines amdl = new AddMetadataLines();
    /**
     * given a ##BIOR line, return a ##INFO line
     * @param biorLine- the bior line that we will turn into an info line
     * @return
     */
    public String buildINFOFromBioR(String biorLine){
        LinkedHashMap<String,String> attr = amdl.parseHeaderLine(biorLine);
        return buildInfoFromBioRAttr(attr);
    }

    public String buildInfoFromBioRAttr(LinkedHashMap<String,String> attr){
        String fielddesc = attr.get(AddMetadataLines.BiorMetaControlledVocabulary.FIELDDESCRIPTION.toString());
        if(fielddesc == null || fielddesc.length() < 1){ //if the description is empty
            //try to give it the description
            fielddesc = attr.get(AddMetadataLines.BiorMetaControlledVocabulary.DESCRIPTION.toString());
            if(fielddesc == null || fielddesc.length() < 1){
                attr.put(AddMetadataLines.BiorMetaControlledVocabulary.FIELDDESCRIPTION.toString(), DEFAULT_DESCRIPTION);
            }else { //give it the default
                attr.put(AddMetadataLines.BiorMetaControlledVocabulary.FIELDDESCRIPTION.toString(), fielddesc);
            }

        }
        String datatype = attr.get(AddMetadataLines.BiorMetaControlledVocabulary.DATATYPE.toString());
        if(datatype == null || datatype.length() < 1){
            attr.put(AddMetadataLines.BiorMetaControlledVocabulary.DATATYPE.toString(), DEFAULT_TYPE);//String
        }else if(datatype.equalsIgnoreCase(ColumnMetaData.Type.Boolean.toString())){
            attr.put(AddMetadataLines.BiorMetaControlledVocabulary.DATATYPE.toString(), "Flag");  //booleans are represented as flags in VCF
        }
        String number = attr.get(AddMetadataLines.BiorMetaControlledVocabulary.NUMBER.toString());
        if(number == null || number.length() < 1){
            attr.put(AddMetadataLines.BiorMetaControlledVocabulary.NUMBER.toString(), ".");
        }
        return buildINFO(
                attr.get("ID"),
                attr.get(AddMetadataLines.BiorMetaControlledVocabulary.NUMBER.toString()),
                attr.get(AddMetadataLines.BiorMetaControlledVocabulary.DATATYPE.toString()),
                attr.get(AddMetadataLines.BiorMetaControlledVocabulary.FIELDDESCRIPTION.toString()));
    }

    /**
     *  Construct a ##INFO line:
     *  e.g.
     *  ##INFO=<ID=BIOR.genes.GeneID,Number=.,Type=String,Description="something">
     */
    public String buildINFO(String id, String number, String type, String description ){
        if(description == null){
            description = DEFAULT_DESCRIPTION;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("##INFO=<ID=");
        sb.append(id);
        sb.append(",Number=");
        sb.append(number);
        sb.append(",Type=");
        sb.append(type);
        sb.append(",Description=\"");
        if(description.length() > 1){
            sb.append(description);
        }else {
            sb.append(DEFAULT_DESCRIPTION);
        }
        sb.append("\">");
        return sb.toString();
    }

    /**
     * for a header line e.g. #CHROM, remove all bior/annotation columns.
     *
     * @param metaData
     * @param biorindexes2
     * @return
     */
    public HistoryMetaData removeColumnHeader(HistoryMetaData metaData, Map<Integer, String> biorindexes2) {
        List<ColumnMetaData> columns = metaData.getColumns();
        List<Integer> indexes = new ArrayList<Integer>(biorindexes2.keySet());
        Collections.sort(indexes);    // 8 9 10 ...
        Collections.reverse(indexes); // 10 9 8 ...
        for (int j : indexes) {
            ColumnMetaData cmd = columns.get(j);
            //System.err.println(cmd.getColumnName());
            columns.remove(cmd);
        }
        return metaData;
    }

    /**
     * Removes columns from history after appending them into INFO Column
     *
     * @param h           -List<String> of history
     * @param biorindexes ---indexes of BioR columns that needs to be removed
     * @return Modified history String
     */
    public History removeAnnotationColumns(History h, Map<Integer, String> biorindexes) {
        List<Integer> indexes = new ArrayList<Integer>(biorindexes.keySet());
        Collections.sort(indexes);    //8 9 10 ...
        Collections.reverse(indexes); //10 9 8 ...
        for (int j : indexes) {
            h.remove(j);
        }
        return h;
    }


    /**
     * Extracts the List of Bior Columns looking at Metadata (##BIOR lines)
     *
     * @param metadata - the original header lines including ##BIOR lines
     * @return
     */
    public List<String> getBIORColumnsFromMetadata(List<String> metadata) {
        List<String> columns = new ArrayList<String>();
        for (String c : metadata) {
            if (c.startsWith("##BIOR=<ID")) {
                String[] ast = c.split("<")[1].replace(">", "").split(",")[0].split("=");
                columns.add(ast[1].replace("\"", ""));
            }
        }
        return columns;
    }


    /**
     * Extracts the List of BioR Columns looking at column header
     *
     * @param header - header passed to historyIn   e.g. #CHROM\tPOS\t...\tbior.foo\n
     * @param biorcolumn     - any overides that are bior annotations, but are not prefixed with bior. (there must be a ##BIOR line designating them)
     * @return the columns from the header that are bior columns.
     */
    public List<String> getBIORColumnsFromHeader(List<ColumnMetaData> header, List<String> biorcolumn) {
        List<String> columns = new ArrayList<String>();
        int indexsize = header.size();
        if (header.get(0).getColumnName().startsWith("CHROM")) {
            for (ColumnMetaData cmd : header) {
                String colname = cmd.getColumnName();
                //   String colname =History.getMetaData().getColumns().get(j).getColumnName();
                if (colname.contains("bior") || colname.contains("BIOR")) {
                    columns.add(colname);
                } else if (biorcolumn.contains(colname)) {
                    columns.add(colname);
                }
            }
        }
        return columns;
    }

    private String infoDataPair(String key, String value){
        StringBuilder sb = new StringBuilder();
        //It is actually a Flag NOT a string
        if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
            sb.append(";");
            sb.append(key);
        }else {
            sb.append(";");
            sb.append(key);
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * Modify the history string(VCF row) by appending the columns into INFO
     *
     * @param history      ---Takes History list<String>
     * @param biorindexes2 ---corresponding bior indexes (columns that need to be pushed into INFO Column)
     * @return modified history ---History String after appending BIOR Columns into INFO column
     */

    private History modifyhistory(History history, Map<Integer, String> biorindexes2) {
        //   history.
        Set<Integer> indexes = biorindexes2.keySet();
        Iterator<Integer> iterator = indexes.iterator();
        while (iterator.hasNext()) {
            int value = iterator.next();
            String val = null;
            //Checks sure the index of BioR is within the history
            if (value < history.size()) {

                val = history.get(value);
                if (val != null && !val.isEmpty() && !val.contentEquals(".") && !val.startsWith("{")) {
                    String newValue = history.get(7).concat(infoDataPair( biorindexes2.get(value), val ));  //TODO: potential performance issue!
                    if (newValue.startsWith(".;"))
                        history.set(7, newValue.replaceFirst(".;", ""));
                    else
                        history.set(7, newValue);
                }
            }

        }
        //	history = removeColumns(history,biorindexes2);
        return history;

    }

    /**
     * returns the position of the last ##INFO row in the header
     * @param originalHeader all of the lines int the original header
     * @return
     */
    public int lastInfoLineNumber(List<String> originalHeader){
        int last = 0;
        int count =0;
        for(String headerLine : originalHeader){
            if(headerLine.startsWith("##INFO")){
                last = count;
            }
            count++;
        }
        if(last == 0){
            return originalHeader.size() -1;
        }
        return last+1;
    }

}
