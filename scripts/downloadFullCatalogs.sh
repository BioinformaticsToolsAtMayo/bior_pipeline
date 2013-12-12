#!/bin/bash

destDir=$1
if [[ -z "$1" ]];  then
  echo "usage: Download all catalog files to the destination directory <catalogDir>"
  echo "  downloadSmallCatalogs.sh  <catalogDir>"
  echo "  (recommended catalogDir: /data)"
  exit 1
fi


currDir=$PWD
cd $destDir

### Get all of the small catalogs 
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/dbSNP.137.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/1000_genomes.phase1_release_v3_20101123.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/hapmap.2010_08_phaseII%2BIII.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/ucsc.hg19.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/ESP.build37.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/cosmic.v63.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/hgnc.2012_08_12.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/BGI.hg19.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/NCBIGene.2013_04_08.GRCh37_p10.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/omim.2013_02_27.GRCh37.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/Bior/mirbase.release19.GRCh37_p5.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/drugbank3.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/pharmgkb_2013_07_03.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/ttd_4.3.02.tar.gz
wget https://s3-us-west-2.amazonaws.com/mayo-bic-tools/bior/Catalogs/User/uniprot_2013_07_10.tar.gz

### Extract all of the catalogs into the current directory
echo "Extracting all files"
for file in *.tar.gz; do
  tar -zxvf $file;
done

### Clean up the tar.gz files 
rm *.tar.gz

### Go back to the dir the user was in
cd $currDir
