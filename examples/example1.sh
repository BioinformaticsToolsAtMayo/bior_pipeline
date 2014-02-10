### Find all dbSNP variants within the BRCA1 gene
echo "BRCA1" \
| bior_lookup -d $BIOR_CATALOG/NCBIGene/GRCh37_p10/genes.tsv.bgz -p gene \
| bior_overlap -d $BIOR_CATALOG/dbSNP/137/00-All_GRCh37.tsv.bgz \
> out1.tsv
