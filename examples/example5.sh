### Use Tabix to get all dbSNP variants on chr17 from positions 1-100
### with the first alt allele an "A", then count the # of lines, 
### disregarding headers
# Use Tabix to get all variants on chr17 between positions 1-100
# Drill out the first element in the _altAlleles array
# Use awk to swap the last two columns (the alt and JSON columns)
# Use grep to filter out headers
# Use grep to keep only those lines ending in the "A" allele (maintain non-display characters)
# Use wc -l to count the lines 
tabix $BIOR_CATALOG/dbSNP/137/00-All_GRCh37.tsv.bgz 17:1-100 \
| bior_drill -p _altAlleles[0] -k \
| awk -F"\t" '{print $1"\t"$2"\t"$3"\t"$5"\t"$4}' \
| grep -v "^#" \
| grep -P "\tA$" \
| wc -l \
> out5.tsv
