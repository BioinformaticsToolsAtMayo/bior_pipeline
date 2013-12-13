#!/bin/bash

destDir=$1
if [[ -z "$1" ]];  then
  echo "usage: Download all catalog files to the destination directory <catalogDir>"
  echo "  downloadFullCatalogs.sh  <catalogDir>"
  echo "  (recommended catalogDir: /data)"
  exit 1
fi

if [ ! -d "$destDir" ]; then
  echo "Directory does not exist: $destDir";
  exit 1;
fi

currDir=$PWD
cd $destDir

urls[0]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/dbSNP.137.GRCh37.tar.gz
urls[1]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/1000_genomes.phase1_release_v3_20101123.GRCh37.tar.gz
urls[2]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/hapmap.2010_08_phaseII_and_III.GRCh37.tar.gz
urls[3]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/ucsc.hg19.GRCh37.tar.gz
urls[4]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/ESP.build37.GRCh37.tar.gz
urls[5]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/cosmic.v63.GRCh37.tar.gz
urls[6]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/hgnc.2012_08_12.GRCh37.tar.gz
urls[7]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/BGI.hg19.GRCh37.tar.gz
urls[8]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/NCBIGene.2013_04_08.GRCh37_p10.tar.gz
urls[9]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/omim.2013_02_27.GRCh37.tar.gz
urls[10]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/mirbase.release19.GRCh37_p5.tar.gz
urls[11]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/drugbank3.tar.gz
urls[12]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/pharmgkb_2013_07_03.tar.gz
urls[13]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/ttd_4.3.02.tar.gz
urls[14]=https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/uniprot_2013_07_10.tar.gz

echo "Downloading FULL catalogs..."
for file in "${urls[@]}"; do
  echo "Getting file: $file"
  wget --no-check-certificate  $file
done

echo ""
echo "Extracting all of the catalogs into directory $destDir..."
for file in "${urls[@]}"; do
  filename=`basename $file`
  echo "Extracting: $filename";
  tar -zxf $filename;
done

echo ""
read -p "Would you like to remove the downloaded tar.gz files? (y/n): " RESP
if [ "$RESP" = "y" ]; then
  echo "Removing the downloaded files..." 
  for file in "${urls[@]}"; do
    filename=`basename $file`
    echo "Removing: $filename";
    rm $filename;
  done
fi

### Go back to the dir the user was in
cd $currDir
