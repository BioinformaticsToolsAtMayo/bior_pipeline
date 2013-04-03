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
  
  private boolean mIsPrintSyncdFiles = true;
  private boolean mIsPrintEqualFiles = false;
  
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
      removeTargetDirFromList(localFiles, startPathLocal);
	  double endLocal = System.currentTimeMillis();
	  System.out.println("  local fetch time: " + (endLocal-endConn)/1000.0);

	  System.out.println("Get remote project files (quickly)...");
	  ArrayList<FileInfo> remoteFiles = getRemoteFilesQuick(sftpChannel.getSession(), startPathRemote);
      removeTargetDirFromList(remoteFiles, startPathRemote);
	  double endRemote2 = System.currentTimeMillis();
	  System.out.println("  remote fetch time: " + (endRemote2-endLocal)/1000.0);

	  System.out.println("Remove .svn folders...");
	  removeSvnFoldersFromList(localFiles);
	  removeSvnFoldersFromList(remoteFiles);
	  
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
  private ArrayList<FileInfo> removeTargetDirFromList(ArrayList<FileInfo> fileInfoList, String fullPathToDirectory) {
	  for(int i=0; i < fileInfoList.size(); i++) {
		  if( fileInfoList.get(i).path.equals(fullPathToDirectory + "/target") )
			  fileInfoList.remove(i);
	  }
	  return fileInfoList;
  }

  /** Remove all .svn folders from the list of files (since these are not necessary to upload,
   *  and may encounter errors because the permissions are set to "dr--r--r--")
   * @param fileInfoList
   * @return
   */
  private ArrayList<FileInfo> removeSvnFoldersFromList(ArrayList<FileInfo> fileInfoList) {
	  for(int i=0; i < fileInfoList.size(); i++) {
		  if( fileInfoList.get(i).path.endsWith("/.svn") )
			  fileInfoList.remove(i);
		  else if( fileInfoList.get(i).isDir )
			  removeSvnFoldersFromList(fileInfoList.get(i).dirContents);
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
				  if(mIsPrintEqualFiles)
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
} //END-CLASS
  
