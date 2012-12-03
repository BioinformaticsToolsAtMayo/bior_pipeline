
= Building =

	You can build the project by running using the following different methods:
	
	1. COMMAND LINE:	mvn clean package
	
	2. M2ECLIPSE:		Right-click and select Run As --> Maven Build...
						Under Goals enter "clean package"
						Click Run
	
	The maven build produces the build artifacts under the ${project}/target folder.

	
= Setup =
	
	Prior to running any project executables, the following must be setup:
	
	1. Source in the setupEnv.sh script to setup your environment.
		
= Running =
	
	Execute any of the CLI scripts from your shell.  All scripts are prefixed with
	"bior" by convention, so TAB'ing after typing "bior" will show you all of the 
	CLI commands.