# This is a first shot at a script for getting VEP to work
# It needs some additional things, such as help.
export BIOR_VEP_PERL_HOME=/usr/local/biotools/perl/5.14.2/bin
export BIOR_VEP_HOME=/data2/bsi/RandD/test/vep/variant_effect_predictor
$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf -polyphen b -sift b --offline 