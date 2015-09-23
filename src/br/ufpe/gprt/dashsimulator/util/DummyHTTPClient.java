package br.ufpe.gprt.dashsimulator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DummyHTTPClient {

	private String host;
	private int port;
	private BitrateCalculations bitrateCalculator;
	private long downloadedSizeInBytes;
	
	private long lastConnectionTimeMilis;
	private long lastDownloadTimeMilis;
	
	
	public DummyHTTPClient(String host, int port){
		this.host = host;
		this.port = port;
		this.bitrateCalculator = new BitrateCalculations();
	}

	public int requestSegment(String url, int id) throws IOException {
		
		File data = new File("receivedData");
		
		if(data.exists()){
			data.delete();
		}
		
		data.createNewFile();
		String siteAddress = "http://"+host+":"+port+url;
		long startTime = this.bitrateCalculator.startTrackingSegment(id);

		URL website = new URL(siteAddress);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		
		this.lastConnectionTimeMilis = System.currentTimeMillis() - startTime;
		
		FileOutputStream fos = new FileOutputStream(data);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
		this.lastDownloadTimeMilis = System.currentTimeMillis() - startTime - this.lastConnectionTimeMilis;
		this.downloadedSizeInBytes = data.length();
		
		int bitrate = this.bitrateCalculator.stopTrackingAndCalculateBitrate(id, this.getDownloadedSizeInBytes());
		
		return bitrate;
	}
	
	public long getDownloadedSizeInBytes() {
		return downloadedSizeInBytes;
	}
	
	public long getSegmentTotalTimeMilis(int id){
		return this.bitrateCalculator.getTotalTimeMilis(id);
	}

	public long getLastConnectionTimeMilis() {
		return lastConnectionTimeMilis;
	}

	public long getLastDownloadTimeMilis() {
		return lastDownloadTimeMilis;
	}

	public long getStartDownloadTime(int id) {
		return this.bitrateCalculator.getStartDownloadTime(id);
	}

}