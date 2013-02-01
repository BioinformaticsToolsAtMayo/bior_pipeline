#!/bin/sh

# exit if any statement returns a non-zero exit code
set -e

# REQUIRED PARAMETERS
if [ $# -ne 3 ] ; then
      echo "USAGE: _bior_vep.sh <buffer_size> <num_forks> <-all/-worst>"   
      exit 1
fi
VEP_BUFFER_SIZE=$1
VEP_NUM_FORKS=$2
ALL=$3

$BIOR_LITE_HOME/bin/_check_java.sh

# RCF environment paths
export BIOR_VEP_PERL_HOME=/usr/local/biotools/perl/5.14.2/bin
export BIOR_VEP_HOME=/data2/bsi/RandD/test/vep/variant_effect_predictor

# MAC environment paths
#export BIOR_VEP_PERL_HOME=/usr/bin
#export BIOR_VEP_HOME=/Applications/variant_effect_predictor

export VEP_COMMAND="$BIOR_VEP_PERL_HOME/perl $BIOR_VEP_HOME/variant_effect_predictor.pl -i /dev/stdin -o STDOUT -dir $BIOR_VEP_HOME/cache/ -vcf --hgnc -polyphen b -sift b --offline --buffer_size $VEP_BUFFER_SIZE"

if test $VEP_NUM_FORKS -gt 1; then
	VEP_COMMAND="$VEP_COMMAND --fork $VEP_NUM_FORKS"
fi

# MAC ONLY
# @see https://github.com/arq5x/gemini/blob/master/docs/content/functional_annotation.rst
#
# To use the cache, the gzip and zcat utilities are required. VEP uses zcat to 
# decompress cached files. For systems where zcat may not be installed or may
# not work, the following option needs to be added along with the --cache option:
#
# "--compress gunzip -c"
#
if test `uname` = "Darwin"; then
	VEP_COMMAND="$VEP_COMMAND --compress \"gunzip -c\""
fi

eval $VEP_COMMAND | java -cp $BIOR_LITE_HOME/conf:$BIOR_LITE_HOME/lib/* edu.mayo.bior.pipeline.VEPPostProcessingPipeline $ALL 
