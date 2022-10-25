package fr.ina.dlweb.twitter.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.io.FileLineWriter;
import fr.ina.dlweb.utils.FileUtils;

public  class TwitterWriter {
	
	
	
	public String fileTweetsCurrentPath, fileInfoPath, fileTweetsDonePath;
	int tweetCounter = 0;
	int overallCounter = 0;
	
	ObjectNode infoFile ;
	ObjectMapper mapper = new ObjectMapper();
	
	
	SimpleDateFormat sdf ;
	
	String fileInitialName;
	String header;
	String currentFolder;
	String doneFolder;
	String fileNameTimeFormat;
	// With file writer to user with lock or without lock
	boolean mustLock = false;
	boolean moveToDone = false;
	FileLineWriter writer = null;
	public Date fileDate ; 
	int maxLineNumberPerFile;
	
	public TwitterWriter(	String fileInitialName, 
							String currentFolder,
							String doneFolder, 
							String fileNameTimeFormat, 
							String header, 
							ObjectNode infoFileContent, 
							boolean mustLock, 
							boolean mustMoveDone,
							int maxLineNumberPerFile) throws IOException 
	
	{
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			this.fileInitialName = fileInitialName;
			this.mustLock = mustLock;
			this.header = header;
			this.infoFile = infoFileContent;
			this.fileNameTimeFormat = fileNameTimeFormat;
			this.moveToDone = mustMoveDone;
			
			this.doneFolder = doneFolder;
			this.currentFolder = currentFolder;
			this.maxLineNumberPerFile = maxLineNumberPerFile;
			

			
			initializeWriter();
	}
	
	//Just for the logs to display count of tweets already archived
	public int getSizeofTweets()
	{
			return tweetCounter;
	}
	
	
	public void setSizeofTweets(int size)
	{
		tweetCounter = size;
	}
	
	protected void setFileNames()
	{

			if(!fileNameTimeFormat.isEmpty()) {
				sdf = new SimpleDateFormat(fileNameTimeFormat);
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				this.fileDate = new Date();
				this.fileTweetsCurrentPath = Paths.get(currentFolder,fileInitialName  + "_" + sdf.format(fileDate) + ".jsons.part" ).toString();
				
				   
			}
			else 
				this.fileTweetsCurrentPath =  Paths.get(currentFolder,  fileInitialName + ".part").toString();
			
			this.fileInfoPath = this.fileTweetsCurrentPath.replace(".jsons.part", "_info.json");
			
			
	}
	
	
	private void initializeWriter() throws IOException
	{
		
			tweetCounter = 0;
	
			if(writer != null)
				closeWriter();
			
			setFileNames();
			writer = new FileLineWriter(new File(this.fileTweetsCurrentPath), StandardCharsets.UTF_8, this.mustLock, true);
			System.out.print("<");
			if(header != null)
				this.writeTweet(header,"0");
	
			//created new file
			
			if(infoFile != null)
			{
				writeInfoFile(false, this.fileInfoPath, this.fileTweetsCurrentPath);
			}
		
		
	}
	

	/**
	 * Writes tweet to a file 
	 * @param tweet : tweet archived 
	 * @param id : id of archived tweet
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public void writeTweet(String tweet , String id) throws IOException 
	{
			// file names are not ok create a new file and go on
			// jth would like to change file name overy day even we have a small data in 
			if( fileDate.getDay() != new Date().getDay() ||  this.maxLineNumberPerFile < this.tweetCounter)
			{
				initializeWriter();
				
			}
			if(!tweet.trim().equals(""))
			{
				long date = new Date().getTime() / 1000;		
				writer.writeLine("[" + date +"," + id + "," +  tweet.trim() + "]");
				
				tweetCounter ++;
				overallCounter++;
			}
	}
	
	
	/**
	 * Before closing file adding last line which is the same as header for streaming and search 
	 * @throws IOException
	 * 
	 */
	public void closeWriter() throws IOException
	{
			 tweetCounter = 0;
			 overallCounter = 0; 
			 long date = new Date().getTime() / 1000;
			 if(this.header != null)//!isTrend && !isTimeline && !isIngest
				 writer.writeLine("[" + date  +",0," +  this.header + "]");
			 
			 writer.close();
			 System.out.print(">");
			 writer = null;
			 
			 
			 if(infoFile != null)
				 writeInfoFile(true, this.fileInfoPath, this.fileTweetsCurrentPath);
			 
	
			 if(this.moveToDone)
			 {
				 // from current file *.part => done file *.jsons
				 try {
					 Files.move(Paths.get(this.fileTweetsCurrentPath), Paths.get(this.doneFolder,Paths.get(this.fileTweetsCurrentPath).getFileName().toString().replace(".part","")) );
					 Files.move(Paths.get(this.fileInfoPath), Paths.get(this.doneFolder,Paths.get(this.fileInfoPath).getFileName().toString())); // no need seperated try catch same file name
				
				 } catch (FileAlreadyExistsException ex) {//update target file name with counter 
					 int random = (int) Math.ceil(Math.random() * 10000); // just to be sure while testing when it is too fast and maxline is too small overall remains same
					 String fileNameWithCounter = Paths.get(this.fileTweetsCurrentPath).getFileName().toString().replace(".jsons","_" +  random + "_"  + overallCounter + ".jsons").replace(".part","");
					 Files.move(Paths.get(this.fileTweetsCurrentPath), Paths.get(this.doneFolder, fileNameWithCounter));
					 //writing info file again with counter name info : we can just update the file name 
					 writeInfoFile(true, this.fileInfoPath, Paths.get(this.doneFolder, fileNameWithCounter).toString() );
					
					
					 Files.move(Paths.get(this.fileInfoPath), Paths.get(this.doneFolder,Paths.get(this.fileInfoPath).getFileName().toString().replace("_info.json","_" +  random + "_"  + overallCounter  + "_info.json") ));
				 }

			 }

	}
	
	
	// not using global fileTweets directly because this function can be called to create log files from existing jsons files in that case global fileTweets = null because setTwitter will  not be called
	private void writeInfoFile(boolean isOver, String fileInfoPath, String fileTweetsPath) throws JsonProcessingException, IOException
	{
			 File fTweets = new File(fileTweetsPath);
			 
			 FileLineWriter logWriter = new FileLineWriter(new File(fileInfoPath),  StandardCharsets.UTF_8, this.mustLock, false);
			 
			 
			 infoFile.put("timestamp",  new Date().getTime()); // 
			 
			 if(isOver){
				 infoFile.put("file_name", fTweets.getName().replace(".part", ""));
				 infoFile.put("file_state",  "closed");
				 infoFile.put("file_sha256",  FileUtils.getSha256(fTweets));
				 infoFile.put("file_length",  fTweets.length());
				
			 }
			 else
			 {
				 infoFile.put("file_name", fTweets.getName());
				 infoFile.put("file_state",  "active");
				 infoFile.put("file_sha256",  "");
				 infoFile.put("file_length",  0);
		
			 }
			 
			 logWriter.writeLine(mapper.writeValueAsString(infoFile));//writerWithDefaultPrettyPrinter().
			 logWriter.close();
	}
	
	public FileLineWriter getWriter() {
			return writer;
	}

}
