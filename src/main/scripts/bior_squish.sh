# exit if any statement returns a non-zero exit code
set -e

$BIOR_LITE_HOME/bin/_check_java.sh

java -cp $BIOR_LITE_HOME/conf:$BIOR_LITE_HOME/lib/* edu.mayo.bior.cli.CommandLineApp edu.mayo.bior.cli.cmd.SquishCommand $0 $@