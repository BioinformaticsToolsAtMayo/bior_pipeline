# DESC:	This script is designed to setup your UNIX shell for the maven
#		built distribution located as a subfolder under target.
#
# NOTE: From the root of the project folder, run this file via 2 methods:
#
#	1.	"source setupEnv.sh"
# 	2.	". setupEnv.sh"

BASEDIR=`pwd`

# dynamically grab the unzipped distribution folder name
FOLDER=`ls -F target | grep "bior.*/" | cut -d "/" -f 1`

# setup env vars
export BIOR_LITE_HOME=$BASEDIR/target/$FOLDER
export PATH=$BIOR_LITE_HOME/bin:$PATH