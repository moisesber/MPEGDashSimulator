package br.ufpe.gprt.dashsimulator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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

	public int requestSegment(String url, int id, int playerCount) throws IOException {
		System.out.println("["+playerCount+"] Downloading chunk id "+id+" url "+url);
		
		File data = new File("receivedData-"+playerCount);
		
		if(data.exists()){
			data.delete();
		}
		
		data.createNewFile();
		String siteAddress = "http://"+host+":"+port+url;
		long startTime = this.bitrateCalculator.startTrackingSegment(id);

		try{
			URL website = new URL(siteAddress);
			URLConnection con = website.openConnection();
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			con.setAllowUserInteraction(false);    
			InputStream in = con.getInputStream();
//			InputStream in = website.openStream();
			
			
			ReadableByteChannel rbc = Channels.newChannel(in);
			
			this.lastConnectionTimeMilis = System.currentTimeMillis() - startTime;
			
			FileOutputStream fos = new FileOutputStream(data);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			
			//Force TCP session end
			HttpURLConnection httpCon = (HttpURLConnection)con;
			httpCon.disconnect();
		} catch (SocketTimeoutException stoe){
			System.out.println("["+playerCount+"] Timeout downloading id "+id+" url "+url);
			return Integer.MIN_VALUE;
		}

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