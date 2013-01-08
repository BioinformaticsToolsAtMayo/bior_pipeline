#!/bin/sh

# exit if any statement returns a non-zero exit code
set -e

$BIOR_LITE_HOME/bin/_check_java.sh

#export BIOR_VEP_PERL_HOME=/usr/local/biotools/perl/5.14.2/bin
#export BIOR_VEP_HOME=/data2/bsi/RandD/test/vep/variant_effect_predictor

### MAC
export BIOR_VEP_PERL_HOME=/usr/bin
export BIOR_VEP_HOME=/Applications/variant_effect_predictor

#$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf -polyphen b -sift b --offline | java -cp $BIOR_LITE_HOME/conf:$BIOR_LITE_HOME/lib/* edu.mayo.bior.pipeline.VEPPostProcessingPipeline $0 $@

### MAC
# from https://github.com/arq5x/gemini/blob/master/docs/content/functional_annotation.rst
#To use the cache, the gzip and zcat utilities are required. VEP uses zcat to decompress cached files. For systems where zcat may not be installed or may not work, the following option needs to be added along with the --cache option:
#--compress "gunzip -c"
$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf -polyphen b -sift b --offline --compress "gunzip -c" | java -cp $BIOR_LITE_HOME/conf:$BIOR_LITE_HOME/lib/* edu.mayo.bior.pipeline.VEPPostProcessingPipeline $0 $@
