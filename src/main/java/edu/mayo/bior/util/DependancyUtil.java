package edu.mayo.bior.util;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: m102417
 * Date: 8/21/13
 * Time: 1:41 PM
 * This class helps to check if a dependancy is installed correctly
 */
public class DependancyUtil {

    public static boolean isIndexInstalled(String indexPath){
        return isPathDepInstalled(indexPath,"BioR Index that is requested is not installed (either download it or run bior_index_catalog): ");
    }

    public static boolean isCatalogInstalled(String catalogPath){
        return isPathDepInstalled(catalogPath,"BioR Catalog that is requested is not installed: ");
    }

    public static boolean isPathDepInstalled(String path, String message){
        if(path == null){
            return false;
        }
        File f = new File(path);
        if(f.exists() == false){
            System.err.println(message + path);
            return false;
        }
        return true;
    }

    /**
     *
     * @param toolPath - this can be a full path, or a relative path
     * @return
     */
    public static boolean isToolInstalled(String toolPath){
        if(toolPath == null){
            return false;
        }
        File f = new File(toolPath);
        if(f == null){
            return false;
        }
        if(f.canExecute() == false){
            return false;
        }
        if(f.exists() == false){
            return false;
        }

        return true;
    }

    /**
     * check if snpEFF is installed correctly
     * @return
     * @throws IOException
     *
     * ###SNPEFF ============================================
        SnpEffJar=/projects/bsi/bictools/apps/annotation/snpeff/2.0.5d/snpEff.jar
        SnpEffConfig=/projects/bsi/bictools/apps/annotation/snpeff/2.0.5d/snpEff.config
     *
     */
    public static boolean isSNPEffInstalled() throws IOException{
        BiorProperties prop = new BiorProperties();
        String snpeffpath = prop.get("SnpEffJar");
        File snpefF = new File(snpeffpath);
        if(snpefF == null || !snpefF.exists()){
            System.err.println("SNPEffect is not correctly installed and on your path! you need to install it and modify the bior.properties file to use this feature!");
            System.err.println("Current Path: " + snpeffpath );
            System.err.println("bior.properties: " + BiorProperties.getFile());

            return false;
        }
        String snpconfpath = prop.get("SnpEffConfig");
        File snpconf = new File(snpconfpath);
        if(snpconf == null || !snpconf.exists() ){
            System.err.println("SNPEffect config is not correctly installed and in the bior.properties file! You need to create a valid config file, please see the SNPEff documentation");
            System.err.println("Current Path: " + snpconfpath );
            System.err.println("bior.properties: " + BiorProperties.getFile());
            return false;
        }

        return true;
    }

    /**
     *
     ###VEP ===============================================
     BiorVepPerl=/usr/local/biotools/perl/5.14.2/bin/perl
     BiorVep=/data2/bsi/RandD/test/vep/variant_effect_predictor/variant_effect_predictor.pl
     BiorVepCache=/data2/bsi/RandD/test/vep/variant_effect_predictor/cache/
     * @return
     */
    public static boolean isVEPInstalled() throws IOException {
        BiorProperties prop = new BiorProperties();
        String veperl = prop.get("BioRVepPerl");
        if(isToolInstalled(veperl)){
            System.err.println("VEP needs perl 5.14.2 or greater, make sure it is installed and in the location specified in the bior.properties file");
            System.err.println("Current Path: " + veperl );
            System.err.println("bior.properties: " + BiorProperties.getFile());
            return false;
        }
        String vepcache = prop.get("BiorVepCache");
        File vepc = new File(vepcache);
        if( vepc == null || !vepc.exists() || !vepc.isDirectory()){
            System.err.println("VEP needs the cache to be downloaded, please read the VEP documentation to download the cache and specify the cache directory correctly in the bior.properties file");
            System.err.println("Current Path: " + vepcache );
            System.err.println("bior.properties: " + BiorProperties.getFile());
            return false;
        }
        String vep = prop.get("BiorVep");
        File vepf = new File(vep);
        if(vepf == null || !vepf.exists() ){
            System.err.println("VEP needs to be installed and in the location specified in your bior.properties file");
            System.err.println("Current Path: " + vep );
            System.err.println("bior.properties: " + BiorProperties.getFile());
            return false;
        }
        return true;
    }

}
