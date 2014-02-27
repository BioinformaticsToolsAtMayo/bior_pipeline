#!/bin/bash

### Test if a vcf (or zip, bzip2, bgzip, or gz version of a vcf) is valid
vcfFile=$1
vcf1000LinesOut=$2
vcfValidateOutAll=$3
vcfValidateOutErrors=$4

if [ -z $vcfFile ] ; then
  echo "Validate a vcf file by looking at the top 1000 lines"
  echo "Usage:   ./testVcf.sh <vcfFile> [vcf1000LinesOut]  [vcfValidateOutAll]  [vcfValidateOutErrors]"
  echo "         <vcfFile> - REQUIRED - The VCF file to read (can be vcf, zip, bgz, bz2, or gz)"
  echo "         [vcf1000LinesOut] - Optional - Save the top 1000 lines of the vcf file to this output file.  NOTE: Must NOT contain '.gz' in the filename"
  echo "         [vcfValidateOutAll] - Optional - Save all output from vcf-validate to this file"
  echo "         [vcfValidateOutErrors] - Optional - Save all errors from vcf-validate" 
  echo "WARNING: Do NOT use .gz in the filename for vcf1000LinesOut!  If present, it will cause vcf-validator to fail!"
  echo "         Even a file like 'my.vcf.1.2.gz.3.4.5' will fail because it contains '.gz'"
  exit 1;
fi

if [ -z "$vcf1000LinesOut" ] ; then
  vcf1000LinesOut="$vcfFile.1000"
fi

if [ -z $vcfValidateOutAll ] ; then
  vcfValidateOutAll="$vcfFile.1000.validated.all"
fi
 
if [ -z $vcfValidateOutErrors ] ; then
  vcfValidateOutErrors="$vcfFile.1000.validated.errors"
fi

echo "vcfFile              = $vcfFile"
echo "vcf1000LinesOut      = $vcf1000LinesOut"
echo "vcfValidateOutAll    = $vcfValidateOutAll"
echo "vcfValidateOutErrors = $vcfValidateOutErrors"


java -Xmx256M -cp ReadAny.jar edu/mayo/bsi/files/ReadAny $vcfFile | head -1000 > $vcf1000LinesOut

### Run vcf-validator to check for errors
vcf-validator $vcf1000LinesOut &> $vcfValidateOutAll 


### Now, remove all known INFO and WARNING messages
grep -v "Not required" $vcfValidateOutAll  \
  | grep -v "assuming"  \
  | grep -v "Could not parse the fileformat version string"  \
  | grep -v "Empty fields in the header line,"  \
  > $vcfValidateOutErrors


exit 0;
