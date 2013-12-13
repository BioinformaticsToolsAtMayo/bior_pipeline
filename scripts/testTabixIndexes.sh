if [[ -z "$1" ]];  then
  echo "usage: Test all tabix indexes within a certain catalog directory"
  echo "  testTabixIndexes.sh  <catalogDir>"
  echo "  (recommended catalogDir: /data)"
  exit 1
fi

catalogDir=$1

for file in `find $catalogDir | grep tbi`; do
  echo "----------"
  # echo $file
  # The catalog file should be the one without the ".tbi" on end
  catalogFile=`echo $file | sed -e "s/.tbi//g"`
  echo $catalogFile
  # check if there is anything on chromosome 17
  tabix $catalogFile 17 | head -1
done

echo ""
echo "========================================="
echo "If there are any with errors, try first using the 'touch' command to update the .tbi file as this will set its LastModified date after the bgz catalog file's
echo "=========================================""
