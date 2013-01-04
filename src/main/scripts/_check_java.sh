#!/bin/sh

# DESCRIPTION: This script will check an enforce java 1.6 or higher
REQUIRED_MINOR_VERSION=6

# grab full version string from running "java -version"
VERSION=`java -version 2>&1 | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`

# parse out the minor version number
MINOR_VERSION=`echo $VERSION | cut -d . -f 2`

# check minor version, anything less than the required version is invalid
if [ $MINOR_VERSION -lt $REQUIRED_MINOR_VERSION ]; then
	echo "Invalid Java version $VERSION.  Java 1.$REQUIRED_MINOR_VERSION or higher is required."
	echo "You can check your java version by running: java -version"
	exit 1
fi