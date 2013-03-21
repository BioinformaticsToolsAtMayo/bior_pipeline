package edu.mayo.bior.cli.func.remoteexec.helpers;

/**
 * grep -R "<testsuite" ../surefire-reports/*.xml
../surefire-reports/TEST-edu.mayo.bior.cli.CommandLineAppTest.xml:<testsuite failures="0" time="0.242" errors="0" skipped="0" tests="3" name="edu.mayo.bior.cli.CommandLineAppTest">
../surefire-reports/TEST-edu.mayo.bior.cli.TestSameVariantOnESP.xml:<testsuite failures="0" time="0.288" errors="0" skipped="0" tests="1" name="edu.mayo.bior.cli.TestSameVariantOnESP">
../surefire-reports/TEST-edu.mayo.bior.pipeline.UnixStreamPipelineTest.xml:<testsuite failures="0" time="0" errors="0" skipped="0" tests="2" name="edu.mayo.bior.pipeline.UnixStreamPipelineTest">
../surefire-reports/TEST-edu.mayo.bior.pipeline.VEPPostProcessingPipelineTest.xml:<testsuite failures="2" time="3.413" errors="0" skipped="0" tests="7" name="edu.mayo.bior.pipeline.VEPPostProcessingPipelineTest">
 * @author m054457
 */
public class RemoteTestResult {
	public String path;
	public String testSuiteName;
	public int numFailures;
	public double runTime;
	public int numErrors;
	public int numSkipped;
	public int numTestsRun;
}
