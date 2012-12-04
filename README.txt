
= Building =

	You can build the project by running using the following different methods:
	
	1. COMMAND LINE:	mvn clean package
	
	2. M2ECLIPSE:		Right-click and select Run As --> Maven Build...
						Under Goals enter "clean package"
						Click Run
	
	The maven build produces the build artifacts under the ${project}/target folder.

	
= Setup =
	
	Prior to running any project executables, the following must be setup:
	
	1. Setup your UNIX environment:

		1.1		open terminal
		1.2		cd ${project}
		1.3		mvn clean package	# runs the build to create the distribution
		1.4		source setupEnv.sh	# inspects the target folder to setup env vars	
		
= Running =
	
	Execute any of the CLI scripts from your shell.  Since all scripts are prefixed
	with "bior" by convention, TAB'ing after typing "bior" will show you all of 
	the CLI commands.