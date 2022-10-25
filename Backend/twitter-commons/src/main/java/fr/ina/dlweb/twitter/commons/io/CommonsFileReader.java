package fr.ina.dlweb.twitter.commons.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import fr.ina.dlweb.twitter.commons.utils.Utils;

public class CommonsFileReader implements Closeable{
	
	protected BufferedReader inputReader;
	protected long offset;
	protected String nextRecord;
	String charsetName=  "UTF-8";
	
	public CommonsFileReader(String inputFile, long offset) throws Exception
	{
		setOffsetJsonFile(offset );

		InputStream input = Utils.getInputStream(inputFile, offset);
				
		if(input != null)	
		{
				this.inputReader = new BufferedReader(new InputStreamReader(input,charsetName));
				nextRecord = readNextRecord();
		}
		
	}
	
	public boolean hasNext() {
		return (nextRecord != null);
	}

	
	protected String readNextRecord() throws IOException {

		
		String record =  inputReader.readLine();
		
		if(record != null) {
			offset += record.getBytes(Charset.forName(charsetName)).length + 1 ;
		}
		return record;

	}
	
	
	
	public String next() {
		String currentRecord = nextRecord;
		if(currentRecord == null) {
			throw new NoSuchElementException();
		}
		try {
			nextRecord = readNextRecord();	
		} catch(IOException ioe) {
			throw new RuntimeException("Can't read input ", ioe);
		}

		return currentRecord;
	}
	
	public long getOffsetJsonfile()
	{
		return offset;
	}
	
	public void setOffsetJsonFile(long offset)
	{
		this.offset = offset;
	}
	

	// need it when we read only one line without while loop we close manually
	public void close()
	{
		try {
			if(inputReader!=null)  inputReader.close();
		} catch (IOException e) {
			throw new RuntimeException("Can't close input reader for file " , e);
	
		}
		
	}
	
	
}
