### Get 1000 Genomes Minor Allele Frequencies (African total population), given a VCF
# NOTE: This is a BAD call, and will generate an error, but can show how to use the --log
#   flag to dump the error to a log for analysis.
#   The reason it fails is that "AF" and "AFR_AF" should be "INFO.AF" and "INFO.AFR_AF"
cat example.vcf \
| bior_vcf_to_tjson \
| bior_same_variant -d $BIOR_CATALOG/1000_genomes/20110521/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37.tsv.bgz \
| bior_drill -p INFO.AF -p INFO.AFR_AF \
| cut --complement -f 9 \
> out4.tsv
