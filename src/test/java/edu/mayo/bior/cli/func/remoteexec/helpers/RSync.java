package edu.mayo.bior.cli.func.remoteexec.helpers;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program will demonstrate the sftp protocol support.
 *   $ CLASSPATH=.:../build javac Sftp.java
 *   $ CLASSPATH=.:../build java Sftp
 * You will be asked username, host and passwd. 
 * If everything works fine, you will get a prompt 'sftp>'. 
 * 'help' command will show available command.
 * In current implementation, the destination path for 'get' and 'put'
 * commands must be a file, not a directory.
 *
 */
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.*;

public class RSync{
  
  public enum FileOperation { noChange, update, remove };
  class FileInfo implements Comparable<String> {
	  public String path;
	  public Date modDate;
	  public long size;
	  public boolean isRemote = false;
	  public boolean isDir = false;
	  public ArrayList<FileInfo> dirContents = new ArrayList<FileInfo>();
	  
	  /** Only applies to remote files */
	  public FileOperation operation;
	  
	  public int compareTo(String other) {
		  return path.compareTo(other);
	  } 
  }
  
  private boolean mIsPrintSyncdFiles = false;
  
  private int mNumFilesTotal = 0;
  private int mNumFileEqual = 0;
  private int mNumRemoteFilesRemoved = 0;
  private int mNumFilesUpdated = 0;
  private int mNumDirs = 0;
  
  private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
  
  // For:  "Thu Mar 14 10:15:09.0000000000 2013"
  private SimpleDateFormat mDateFormat2= new SimpleDateFormat("EEE MMM d HH:mm:ss.SSS yyyy");

  public static void main(String[] arg){
	  try {
		  Properties props = Ssh.getTempProperties();
		  Session session = new Ssh().openSession(props);
		  new RSync().syncSftp(session, props);
	      session.disconnect();

		  System.out.println("Done.");
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
  }

  
  public void syncSftp(Session session, Properties props) throws JSchException, IOException, ParseException, SftpException  {
	  double start = System.currentTimeMillis();
	  System.out.println("Establishing connection...");
 	  ChannelSftp sftpChannel = openSftpChannel(session);
 	  double endConn = System.currentTimeMillis();
 	  System.out.println("  Connection time: " + (endConn-start)/1000.0);

 	  String startPathRemote = props.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPath.toString());
 	  System.out.println("Starting path: " + startPathRemote);
 	  
	  System.out.println("Get local project files...");
      String startPathLocal  = new File(".").getCanonicalPath();
      ArrayList<FileInfo> localFiles  = getLocalFiles(startPathLocal);
      removeTargetDir(localFiles, startPathLocal);
	  double endLocal = System.currentTimeMillis();
	  System.out.println("  local fetch time: " + (endLocal-endConn)/1000.0);

	  System.out.println("Get remote project files (quickly)...");
	  ArrayList<FileInfo> remoteFiles = getRemoteFilesQuick(sftpChannel.getSession(), startPathRemote);
      removeTargetDir(remoteFiles, startPathRemote);
	  double endRemote2 = System.currentTimeMillis();
	  System.out.println("  remote fetch time: " + (endRemote2-endLocal)/1000.0);

	  System.out.println("Sync'ing files:");
      syncFiles(sftpChannel, localFiles, remoteFiles, startPathRemote);
	  double endSync = System.currentTimeMillis();
	  System.out.println("  sync time: " + (endSync-endRemote2)/1000.0);

	  uploadMavenSettingsIfNeeded(sftpChannel);
	  
	  sftpChannel.disconnect();
      sftpChannel.exit();
      
      System.out.println();
      System.out.println("Num remote files removed: " + mNumRemoteFilesRemoved);
      System.out.println("Num files uploaded:       " + mNumFilesUpdated);
      System.out.println("Num files equal:          " + mNumFileEqual);
      System.out.println("Total local files:        " + mNumFilesTotal);
      System.out.println("----");
      System.out.println("Num directories:          " + mNumDirs);
      System.out.println();

      double end = System.currentTimeMillis();
      System.out.println("Total Elapsed: " + (end-start)/1000.0);
  }



  public ChannelSftp openSftpChannel(Session session) throws JSchException {
	  ChannelSftp sftpChannel = (ChannelSftp)session.openChannel("sftp");
	  sftpChannel.connect();
      return sftpChannel;
  }


  private ArrayList<FileInfo> getLocalFiles(String startPath) throws IOException {
	  ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
	  File startFile = new File(startPath).getCanonicalFile();
	  if(! startFile.isDirectory())
		  return fileList;
	  // Else, get all files within the directory
	  for(File file : startFile.listFiles()) {
		  FileInfo fileInfo = new FileInfo();
		  fileInfo.isDir = file.isDirectory();
		  fileInfo.isRemote = false;
		  fileInfo.modDate = new Date(file.lastModified());
		  fileInfo.operation = FileOperation.noChange;
		  fileInfo.path = file.getCanonicalPath();
		  fileInfo.size = file.length();
		  fileList.add(fileInfo);
		  fileInfo.dirContents = getLocalFiles(file.getCanonicalPath());
	  }
	  return fileList;
  }

  /** A slow way of getting the remote files (each file is a separate call to get its stats)
   *  This takes about 10 seconds per 1000 files.
   *  This uses recursion to get all of the files, starting with the main project path
   * @param sftpChannel
   * @param fullPathToDirectory
   * @return
   * @throws SftpException
   * @throws ParseException
   */
  private ArrayList<FileInfo> getRemoteFiles(ChannelSftp sftpChannel, String fullPathToDirectory) throws SftpException, ParseException {
	  ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
	  sftpChannel.cd(fullPathToDirectory);
	  Vector<LsEntry> files = sftpChannel.ls(".");
	  for(LsEntry file : files) {
		  // Don't add the "." and ".." directories
		  String filename = file.getFilename();
		  if( ".".equals(filename) || "..".equals(filename))
			  continue;
		  FileInfo fileInfo = new FileInfo();
		  fileInfo.path = fullPathToDirectory + "/" + filename;
		  SftpATTRS attrs = file.getAttrs();
		  fileInfo.isDir = attrs.isDir();
		  fileInfo.isRemote = true;
		  fileInfo.size = attrs.getSize();
		  fileInfo.modDate = (Date) mDateFormat.parse(attrs.getMtimeString());
		  fileInfo.operation = FileOperation.noChange;
		  fileList.add(fileInfo);
		  if( fileInfo.isDir )
			  fileInfo.dirContents = getRemoteFiles(sftpChannel, fileInfo.path);
	  }
	  return fileList;
  }
  
  /** Uses the find and printf commands to get a directory listing quickly. 
   *  This takes only a fraction of a second per 1000 files. 
   *  This uses recursion to get all of the files, starting with the main project path
   */
  private ArrayList<FileInfo> getRemoteFilesQuick(Session sshSession, String fullPathToDirectory) throws JSchException, IOException, ParseException {
	  // Prints: 
	  //	%p = relative path  (ex: "./src/assemble/distribution.xml")
	  //	%s = size
	  //	%t = time (ex: "Wed Feb 20 16:02:58.0458767789 2013")
	  //	%f = file (ex: "distribution.xml")
	  //	%h = parent dir (ex: "./src/assemble)
	  //	%y = type (f = file, d = dir)
	  String command = "find " + fullPathToDirectory + " -printf '%p\t%s\t%t\t%f\t%h\t%y\n'";
	  ChannelExec execChannel = (ChannelExec)(sshSession.openChannel("exec"));
	  execChannel.setCommand(command);
	  execChannel.setInputStream(null);
	  InputStream inStream = execChannel.getInputStream();
	  execChannel.connect();

	  ArrayList<String> lines = Ssh.streamToStrings(inStream, false);
	  inStream.close();
	  execChannel.disconnect();

	  // Start off the main directory, which will recurse and add all 
	  // subdirectories as children to each directory's FileInfo.dirContents
	  ArrayList<FileInfo> fileList = getAllFilesInSameDir(fullPathToDirectory, lines);
	  return fileList;
  }
  
  /** Remove the target directory and all subdirectories and files from the FileInfo list so they won't get sync'd
   *  (they get wiped out by a "mvn clean" command anyway, and would have to be updated again, so no use sync'ing them)
   *  NOTE: This will modify the original fileInfoList that is passed in and return it */
  private ArrayList<FileInfo> removeTargetDir(ArrayList<FileInfo> fileInfoList, String fullPathToDirectory) {
	  for(int i=0; i < fileInfoList.size(); i++) {
		  if( fileInfoList.get(i).path.equals(fullPathToDirectory + "/target") )
			  fileInfoList.remove(i);
	  }
	  return fileInfoList;
  }

  private void uploadMavenSettingsIfNeeded(ChannelSftp sftpChannel) throws SftpException {
	  // Get the user's home directory
	  String settingsXmlPath = sftpChannel.getHome() + "/.m2/settings.xml";
	  System.out.println("Checking maven settings.xml at " + settingsXmlPath);
	  boolean isFileExist = true;
	  try {
		  SftpATTRS attrs = sftpChannel.lstat(settingsXmlPath);
	  }catch(SftpException e) {
		  isFileExist = ! e.getMessage().contains("No such file");
	  }
	  
	  // If the file doesn't exist, then copy it from resources
	  if( ! isFileExist)
		  sftpChannel.put("src/test/resources/maven/settings.xml", settingsXmlPath);
  }


  
  // Ex line in allLines: "/home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog/expectedOutput.txt 	 3 	 Wed Feb 20 16:02:51.0135188154 2013 	 expectedOutput.txt 	 /home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog 	 f"
  private ArrayList<FileInfo> getAllFilesInSameDir(String dir, ArrayList<String> allLines) throws ParseException {
	  // Col 4 (0-based) contains dir
	  ArrayList<FileInfo> filesInSameDir = new ArrayList<FileInfo>();
	  for(String line : allLines) {
		  // Skip if not in same directory, or if it is the "." directory
		  if( ! isInDir(dir, line)  ||  ".".equals(line.split("\t")[3]) )
			  continue;
		  FileInfo fileInfo = lineToFileInfo(line);
		  filesInSameDir.add(fileInfo);
		  if( fileInfo.isDir )
			  fileInfo.dirContents = getAllFilesInSameDir(fileInfo.path, allLines);
	  }
	  return filesInSameDir;
  }

  // Ex line in allLines: "/home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog/expectedOutput.txt 	 3 	 Wed Feb 20 16:02:51.0135188154 2013 	 expectedOutput.txt 	 /home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog 	 f"
  private boolean isInDir(String dir, String line) {
	  return dir.equals(line.split("\t")[4]);
  }
  
  // Ex line in allLines: "/home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog/expectedOutput.txt 	 3 	 Wed Feb 20 16:02:51.0135188154 2013 	 expectedOutput.txt 	 /home/mmeiners/biorLite/bior_pipeline/src/test/resources/miniCatalog 	 f"
  // NOTE: YOu should pass the full path to the find command to have it return the full path
  private FileInfo lineToFileInfo(String lineFromFind) throws ParseException {
	  String[] cols = lineFromFind.split("\t");

	  FileInfo fileInfo = new FileInfo();
	  fileInfo.path = cols[0];
	  fileInfo.size = Integer.parseInt(cols[1]);
	  fileInfo.modDate = (Date)mDateFormat2.parse(cols[2]);
	  fileInfo.isDir = "d".equals(cols[5]);
	  fileInfo.isRemote = true;
	  fileInfo.operation = FileOperation.noChange;
	  return fileInfo;
  }

  /** If this is the first sync, double-check with the user if the paths are 
   *  truly what they intend, because it will wipe out everything that 
   *  does NOT match on the biordev server side!!! 
   */
  private void confirmIfFirstSync() {
	  // TODO..............
  }
  
  /**
   * Local			Remote			DateCompare			Operation
   * f1.txt			f1.txt			same				no-op
   * f1.txt (*)		f1.txt			local newer			upload to remote
   * f1.txt			f1.txt (*)		remote newer		upload to remote (overwrite)
   * f1.txt							(no remote match)	upload to remote
   * 				f1.txt			(no local match)	remove from remote
   * dir/			dir/			(check unnecessary)	Compare all children (if remote children is empty, then upload all locals)
   * dir/							(no remote)			upload dir and all sub files and dirs to remote
   * 				dir/			(no local)			remove remote dir with all sub-files and sub-dirs
   * @param sftpChannel
   * @param localFiles
   * @param remoteFiles
 * @throws SftpException 
   */
  private void syncFiles(ChannelSftp sftpChannel, ArrayList<FileInfo> localFiles, ArrayList<FileInfo> remoteFiles, String remoteParentDir) throws SftpException {
	  // Go thru remote files - if no match, then delete
	  for(FileInfo remoteFile : remoteFiles) {
		  FileInfo localFileMatch = getMatching(remoteFile, localFiles);
		  if(localFileMatch == null) {
			  print("    XXX " + remoteFile.path);
			  mNumRemoteFilesRemoved++;
			  removeRemoteFileOrDir(remoteFile, sftpChannel);
		  }
	  }

	  // Go thru localFiles:
	  for(FileInfo localFile : localFiles) {
		  mNumFilesTotal++;
		  FileInfo remoteFileMatch = getMatching(localFile, remoteFiles);
		  // Is file  AND  (File does not exist on remote, or was different) ==> Upload
		  if( ! localFile.isDir ) {
			  // If doesn't exist on remote server, or the file has a different timestamp, then upload
			  if( remoteFileMatch == null  ||  ! isSameTimestampAndSize(localFile, remoteFileMatch) ) {
				  String remotePath = remoteParentDir + "/" + new File(localFile.path).getName();
				  print("    +++ " + remotePath);
				  mNumFilesUpdated++;
				  sftpChannel.put(localFile.path, remoteParentDir);
				  // Set the last mod time so it matches the local file (otherwise all uploaded files will have a date of now)
				  SftpATTRS attrs = sftpChannel.lstat(remotePath);
				  int lastModLocal = (int)(new File(localFile.path).lastModified() / 1000);
				  attrs.setACMODTIME(lastModLocal, lastModLocal);
				  sftpChannel.setStat(remotePath, attrs);
			  } else {
				  print("    =   " + remoteFileMatch.path);
				  mNumFileEqual++;
				  // Do nothing
			  }
		  }
		  else if( localFile.isDir ) {
			  mNumDirs++;
			  String remoteDir = remoteParentDir + "/" + new File(localFile.path).getName();
			  ArrayList<FileInfo> remoteDirListing = new ArrayList<FileInfo>();
			  // Create remote dir if it didn't already exist
			  if( remoteFileMatch == null ) {
				  print("    +++ " + remoteDir + "/");
				  mNumFilesUpdated++;
				  sftpChannel.mkdir(remoteDir);
			  }
			  else {
				  remoteDirListing = remoteFileMatch.dirContents;
				  mNumFileEqual++;
			  }
			  // Recurse into the directory
			  syncFiles(sftpChannel, localFile.dirContents, remoteDirListing, remoteDir);
		  }
	  }
  }
  
  private boolean isSameTimestampAndSize(FileInfo localFile, FileInfo remoteFile) {
	  return (0 == localFile.modDate.compareTo(remoteFile.modDate))
		  && (localFile.size == remoteFile.size);
  }


  private void removeRemoteFileOrDir(FileInfo remoteFile, ChannelSftp sftpChannel) throws SftpException {
	  if( remoteFile.isDir ) {
		  // Remove all child files
		  for(FileInfo childFile : remoteFile.dirContents)
			  removeRemoteFileOrDir(childFile, sftpChannel);
		  sftpChannel.rmdir(remoteFile.path);
	  }
	  else
		  sftpChannel.rm(remoteFile.path);
  }

  private FileInfo getMatching(FileInfo fileToCheck, ArrayList<FileInfo> fileList) {
	  for(FileInfo fileFromList : fileList) {
		  if( new File(fileFromList.path).getName().equals(new File(fileToCheck.path).getName()) )
			  return fileFromList;
	  }
	  // Not found - return null
	  return null;
  }

  private void print(String str) {
	  if(mIsPrintSyncdFiles)
		  System.out.println(str);
  }

//======================================================================================================
  
  //======================================================================================================
  //======================================================================================================
  //======================================================================================================
  //======================================================================================================
  //======================================================================================================
  


  private void syncViaJazsync() {
	  //jazsync.jazsync.jazsync jaz = new jazsync.jazsync.jazsync();
  }
  

  
  
  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
      Object[] options={ "yes", "no" };
      int foo=JOptionPane.showOptionDialog(null, 
             str,
             "Warning", 
             JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE,
             null, options, options[0]);
       return foo==0;
    }
  
    String passwd;
    JTextField passwordField=(JTextField)new JPasswordField(20);

    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
      Object[] ob={passwordField}; 
      int result=
	  JOptionPane.showConfirmDialog(null, ob, message,
					JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
	passwd=passwordField.getText();
	return true;
      }
      else{ return false; }
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
    final GridBagConstraints gbc = 
      new GridBagConstraints(0,0,1,1,1,1,
                             GridBagConstraints.NORTHWEST,
                             GridBagConstraints.NONE,
                             new Insets(0,0,0,0),0,0);
    private Container panel;
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx = 1.0;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridx = 0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts=new JTextField[prompt.length];
      for(int i=0; i<prompt.length; i++){
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(prompt[i]),gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        if(echo[i]){
          texts[i]=new JTextField(20);
        }
        else{
          texts[i]=new JPasswordField(20);
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }

      if(JOptionPane.showConfirmDialog(null, panel, 
                                       destination+": "+name,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE)
         ==JOptionPane.OK_OPTION){
        String[] response=new String[prompt.length];
        for(int i=0; i<prompt.length; i++){
          response[i]=texts[i].getText();
        }
	return response;
      }
      else{
        return null;  // cancel
      }
    }
  }

/*
  public static class MyProgressMonitor implements com.jcraft.jsch.ProgressMonitor{
    JProgressBar progressBar;
    JFrame frame;
    long count=0;
    long max=0;

    public void init(String info, long max){
      this.max=max;
      if(frame==null){
        frame=new JFrame();
	frame.setSize(200, 20);
        progressBar = new JProgressBar();
      }
      count=0;

      frame.setTitle(info);
      progressBar.setMaximum((int)max);
      progressBar.setMinimum((int)0);
      progressBar.setValue((int)count);
      progressBar.setStringPainted(true);

      JPanel p=new JPanel();
      p.add(progressBar);
      frame.getContentPane().add(progressBar);
      frame.setVisible(true);
      System.out.println("!info:"+info+", max="+max+" "+progressBar);
    }
    public void count(long count){
      this.count+=count;
      System.out.println("count: "+count);
      progressBar.setValue((int)this.count);
    }
    public void end(){
      System.out.println("end");
      progressBar.setValue((int)this.max);
      frame.setVisible(false);
    }
  }
*/

  public static class MyProgressMonitor implements SftpProgressMonitor{
    ProgressMonitor monitor;
    long count=0;
    long max=0;
    public void init(int op, String src, String dest, long max){
      this.max=max;
      monitor=new ProgressMonitor(null, 
                                  ((op==SftpProgressMonitor.PUT)? 
                                   "put" : "get")+": "+src, 
                                  "",  0, (int)max);
      count=0;
      percent=-1;
      monitor.setProgress((int)this.count);
      monitor.setMillisToDecideToPopup(1000);
    }
    private long percent=-1;
    public boolean count(long count){
      this.count+=count;

      if(percent>=this.count*100/max){ return true; }
      percent=this.count*100/max;

      monitor.setNote("Completed "+this.count+"("+percent+"%) out of "+max+".");     
      monitor.setProgress((int)this.count);

      return !(monitor.isCanceled());
    }
    public void end(){
      monitor.close();
    }
  }
  
  private void runCommands(ChannelSftp sftpChannel)  throws IOException, Exception, SftpException {
	  java.io.InputStream in=System.in;
	  java.io.PrintStream out=System.out;

	  Session session = sftpChannel.getSession();

	  java.util.Vector cmds=new java.util.Vector();
	  byte[] buf=new byte[1024];
	  int i;
	  String str;
	  int level=0;

	  while(true){
		  out.print("sftp> ");
		  cmds.removeAllElements();
		  i=in.read(buf, 0, 1024);
		  if(i<=0)break;

		  i--;
		  if(i>0 && buf[i-1]==0x0d)i--;
		  //str=new String(buf, 0, i);
		  //System.out.println("|"+str+"|");
		  int s=0;
		  for(int ii=0; ii<i; ii++){
			  if(buf[ii]==' '){
				  if(ii-s>0){ cmds.addElement(new String(buf, s, ii-s)); }
				  while(ii<i){if(buf[ii]!=' ')break; ii++;}
				  s=ii;
			  }
		  }
		  if(s<i){ cmds.addElement(new String(buf, s, i-s)); }
		  if(cmds.size()==0)continue;

		  String cmd=(String)cmds.elementAt(0);
		  if(cmd.equals("quit")){
			  sftpChannel.quit();
			  break;
		  }
		  if(cmd.equals("exit")){
			  sftpChannel.exit();
			  break;
		  }
		  if(cmd.equals("rekey")){
			  session.rekey();
			  continue;
		  }
		  if(cmd.equals("compression")){
			  if(cmds.size()<2){
				  out.println("compression level: "+level);
				  continue;
			  }
			  try{
				  level=Integer.parseInt((String)cmds.elementAt(1));
				  if(level==0){
					  session.setConfig("compression.s2c", "none");
					  session.setConfig("compression.c2s", "none");
				  }
				  else{
					  session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
					  session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
				  }
			  }
			  catch(Exception e){}
			  session.rekey();
			  continue;
		  }
		  if(cmd.equals("cd") || cmd.equals("lcd")){
			  if(cmds.size()<2) continue;
			  String path=(String)cmds.elementAt(1);
			  try{
				  if(cmd.equals("cd")) sftpChannel.cd(path);
				  else sftpChannel.lcd(path);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("rm") || cmd.equals("rmdir") || cmd.equals("mkdir")){
			  if(cmds.size()<2) continue;
			  String path=(String)cmds.elementAt(1);
			  try{
				  if(cmd.equals("rm")) sftpChannel.rm(path);
				  else if(cmd.equals("rmdir")) sftpChannel.rmdir(path);
				  else sftpChannel.mkdir(path);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("chgrp") || cmd.equals("chown") || cmd.equals("chmod")){
			  if(cmds.size()!=3) continue;
			  String path=(String)cmds.elementAt(2);
			  int foo=0;
			  if(cmd.equals("chmod")){
				  byte[] bar=((String)cmds.elementAt(1)).getBytes();
				  int k;
				  for(int j=0; j<bar.length; j++){
					  k=bar[j];
					  if(k<'0'||k>'7'){foo=-1; break;}
					  foo<<=3;
					  foo|=(k-'0');
				  }
				  if(foo==-1)continue;
			  }
			  else{
				  try{foo=Integer.parseInt((String)cmds.elementAt(1));}
				  catch(Exception e){continue;}
			  }
			  try{
				  if(cmd.equals("chgrp")){ sftpChannel.chgrp(foo, path); }
				  else if(cmd.equals("chown")){ sftpChannel.chown(foo, path); }
				  else if(cmd.equals("chmod")){ sftpChannel.chmod(foo, path); }
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("pwd") || cmd.equals("lpwd")){
			  str=(cmd.equals("pwd")?"Remote":"Local");
			  str+=" working directory: ";
			  if(cmd.equals("pwd")) str+=sftpChannel.pwd();
			  else str+=sftpChannel.lpwd();
			  out.println(str);
			  continue;
		  }
		  if(cmd.equals("ls") || cmd.equals("dir")){
			  String path=".";
			  if(cmds.size()==2) path=(String)cmds.elementAt(1);
			  try{
				  java.util.Vector vv=sftpChannel.ls(path);
				  if(vv!=null){
					  for(int ii=0; ii<vv.size(); ii++){
						  //		out.println(vv.elementAt(ii).toString());

						  Object obj=vv.elementAt(ii);
						  if(obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry){
							  out.println(((com.jcraft.jsch.ChannelSftp.LsEntry)obj).getLongname());
						  }

					  }
				  }
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("lls") || cmd.equals("ldir")){
			  String path=".";
			  if(cmds.size()==2) path=(String)cmds.elementAt(1);
			  try{
				  java.io.File file=new java.io.File(path);
				  if(!file.exists()){
					  out.println(path+": No such file or directory");
					  continue; 
				  }
				  if(file.isDirectory()){
					  String[] list=file.list();
					  for(int ii=0; ii<list.length; ii++){
						  out.println(list[ii]);
					  }
					  continue;
				  }
				  out.println(path);
			  }
			  catch(Exception e){
				  System.out.println(e);
			  }
			  continue;
		  }
		  if(cmd.equals("get") || 
				  cmd.equals("get-resume") || cmd.equals("get-append") || 
				  cmd.equals("put") || 
				  cmd.equals("put-resume") || cmd.equals("put-append")
				  ){
			  if(cmds.size()!=2 && cmds.size()!=3) continue;
			  String p1=(String)cmds.elementAt(1);
			  //	  String p2=p1;
			  String p2=".";
			  if(cmds.size()==3)p2=(String)cmds.elementAt(2);
			  try{
				  SftpProgressMonitor monitor=new MyProgressMonitor();
				  if(cmd.startsWith("get")){
					  int mode=ChannelSftp.OVERWRITE;
					  if(cmd.equals("get-resume")){ mode=ChannelSftp.RESUME; }
					  else if(cmd.equals("get-append")){ mode=ChannelSftp.APPEND; } 
					  sftpChannel.get(p1, p2, monitor, mode);
				  }
				  else{ 
					  int mode=ChannelSftp.OVERWRITE;
					  if(cmd.equals("put-resume")){ mode=ChannelSftp.RESUME; }
					  else if(cmd.equals("put-append")){ mode=ChannelSftp.APPEND; } 
					  sftpChannel.put(p1, p2, monitor, mode); 
				  }
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("ln") || cmd.equals("symlink") || cmd.equals("rename")){
			  if(cmds.size()!=3) continue;
			  String p1=(String)cmds.elementAt(1);
			  String p2=(String)cmds.elementAt(2);
			  try{
				  if(cmd.equals("rename")) sftpChannel.rename(p1, p2);
				  else sftpChannel.symlink(p1, p2);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("stat") || cmd.equals("lstat")){
			  if(cmds.size()!=2) continue;
			  String p1=(String)cmds.elementAt(1);
			  SftpATTRS attrs=null;
			  try{
				  if(cmd.equals("stat")) attrs=sftpChannel.stat(p1);
				  else attrs=sftpChannel.lstat(p1);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  if(attrs!=null){
				  out.println(attrs);
			  }
			  else{
			  }
			  continue;
		  }
		  if(cmd.equals("readlink")){
			  if(cmds.size()!=2) continue;
			  String p1=(String)cmds.elementAt(1);
			  String filename=null;
			  try{
				  filename=sftpChannel.readlink(p1);
				  out.println(filename);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("realpath")){
			  if(cmds.size()!=2) continue;
			  String p1=(String)cmds.elementAt(1);
			  String filename=null;
			  try{
				  filename=sftpChannel.realpath(p1);
				  out.println(filename);
			  }
			  catch(SftpException e){
				  System.out.println(e.toString());
			  }
			  continue;
		  }
		  if(cmd.equals("version")){
			  out.println("SFTP protocol version "+sftpChannel.version());
			  continue;
		  }
		  if(cmd.equals("help") || cmd.equals("?")){
			  out.println(help);
			  continue;
		  }
		  out.println("unimplemented command: "+cmd);
	  } // end-while
	  session.disconnect();
  }


  private static String help =
		"      Available commands:\n"+
		"      * means unimplemented command.\n"+
		"cd path                       Change remote directory to 'path'\n"+
		"lcd path                      Change local directory to 'path'\n"+
		"chgrp grp path                Change group of file 'path' to 'grp'\n"+
		"chmod mode path               Change permissions of file 'path' to 'mode'\n"+
		"chown own path                Change owner of file 'path' to 'own'\n"+
		"help                          Display this help text\n"+
		"get remote-path [local-path]  Download file\n"+
		"get-resume remote-path [local-path]  Resume to download file.\n"+
		"get-append remote-path [local-path]  Append remote file to local file\n"+
		"*lls [ls-options [path]]      Display local directory listing\n"+
		"ln oldpath newpath            Symlink remote file\n"+
		"*lmkdir path                  Create local directory\n"+
		"lpwd                          Print local working directory\n"+
		"ls [path]                     Display remote directory listing\n"+
		"*lumask umask                 Set local umask to 'umask'\n"+
		"mkdir path                    Create remote directory\n"+
		"put local-path [remote-path]  Upload file\n"+
		"put-resume local-path [remote-path]  Resume to upload file\n"+
		"put-append local-path [remote-path]  Append local file to remote file.\n"+
		"pwd                           Display remote working directory\n"+
		"stat path                     Display info about path\n"+
		"exit                          Quit sftp\n"+
		"quit                          Quit sftp\n"+
		"rename oldpath newpath        Rename remote file\n"+
		"rmdir path                    Remove remote directory\n"+
		"rm path                       Delete remote file\n"+
		"symlink oldpath newpath       Symlink remote file\n"+
		"readlink path                 Check the target of a symbolic link\n"+
		"realpath path                 Canonicalize the path\n"+
		"rekey                         Key re-exchanging\n"+
		"compression level             Packet compression will be enabled\n"+
		"version                       Show SFTP version\n"+
		"?                             Synonym for help";
}
