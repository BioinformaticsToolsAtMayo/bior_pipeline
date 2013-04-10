package edu.mayo.bior.cli.func.remoteexec.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Ssh {

	
	
	public static void main(String[] args) {
		try {
			Ssh ssh = new Ssh();
			Session session = ssh.openSession(getTempProperties());
			ArrayList<String> output = ssh.runRemoteCommand(session, "ls -la", true);

			//System.out.println("Output from running remote command:  \"" + cmd + "\"");
			//printOutput(resultLines);
			
			session.disconnect();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> runRemoteCommand(Session session, String command, boolean isPrintOutput) throws JSchException, IOException {
		 ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
		 execChannel.setCommand(command);
		 execChannel.setInputStream(null);
		 execChannel.setErrStream(System.err);
		 InputStream inStream = execChannel.getInputStream();
		 execChannel.connect();
		 
		 ArrayList<String> output = streamToStrings(inStream, isPrintOutput);
		 inStream.close();
		 
		 execChannel.disconnect();
		 return output;
	}
	
	public static Properties getTempProperties() {
		Properties props = new Properties();
		props.setProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerName.toString(),     "biordev.mayo.edu");
		props.setProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerUsername.toString(), "mmeiners");
		props.setProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPassword.toString(), "");
		props.setProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPath.toString(), 	   "/home/mmeiners/biorLite/bior_pipeline");
		props.setProperty(RemoteFunctionalTest.DevServerUserPropKeys.isFirstSync.toString(), 	   "true");
		return props;
	}
	  
	public static ArrayList<String> streamToStrings(InputStream inStream, boolean isPrintLines) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
		ArrayList<String> lines = new ArrayList<String>();
		String line = null;
		while( (line = in.readLine()) != null ) {
			if(isPrintLines)
				System.out.println(line);
			lines.add(line);
		}
		return lines;
	}
	  

	public Session openSession(Properties props) throws JSchException {
		JSch jsch=new JSch();
	      
		String host = props.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerName.toString());
		String user = props.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerUsername.toString());
		String pass = props.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPassword.toString());
		int port=22;

		Session session=jsch.getSession(user, host, port);
		session.setPassword(pass);
		// Ignore host key errors:
		JSch.setConfig("StrictHostKeyChecking", "no");
		session.connect();

		return session;
	}
	  
	
	private static void printOutput(ArrayList<String> output) {
		System.out.println("-------------------------------------------------------------");
		for(String s : output) {
			System.out.println(s);
		}
		System.out.println("-------------------------------------------------------------");
	}


}
