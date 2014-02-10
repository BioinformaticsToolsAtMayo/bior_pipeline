### Get Genes and Diseases that overlap SNP positions
# Drill to get gene name and hgnc id
# Get diseases from OMIM based on hgnc id
# Look into OMIM catalog - it has no coordinates, 
#   but it does have indexes created on a couple keys
cat example.vcf \
| bior_vcf_to_tjson \
| bior_overlap -d $BIOR_CATALOG/NCBIGene/GRCh37_p10/genes.tsv.bgz \
| bior_drill -p gene -p MIM \
| bior_lookup -d $BIOR_CATALOG/omim/2013_02_27/genemap_GRCh37.tsv.bgz -p MIM_Number \
| cut --complement -f 9-11 \
> out3.tsv
