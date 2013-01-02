# exit if any statement returns a non-zero exit code
set -e
$BIOR_LITE_HOME/bin/_check_java.sh
# It needs some additional things, such as help.
export BIOR_VEP_PERL_HOME=/usr/local/biotools/perl/5.14.2/bin
export BIOR_VEP_HOME=/data2/bsi/RandD/test/vep/variant_effect_predictor
$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf -polyphen b -sift b --offline | java -cp $BIOR_LITE_HOME/conf:$BIOR_LITE_HOME/lib/* edu.mayo.bior.pipeline.VEPPostProcessingPipeline $0 $@