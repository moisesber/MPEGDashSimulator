package br.ufpe.gprt.dashsimulator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DummyHTTPClient {
	
	private static int TIMEOUT_MILIS = 30000;
//	private static int TIMEOUT_MILIS = 5000;

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
			//	New IO java download
			newIOJavaDocumentDownload(data, siteAddress, startTime);
//			normalStreamDocumentDownload(data, siteAddress, startTime);

		} catch (SocketTimeoutException stoe){
			System.out.println("["+playerCount+"] Timeout downloading id "+id+" url "+url);
			return Integer.MIN_VALUE;
		}
		
		this.lastDownloadTimeMilis = System.currentTimeMillis() - startTime - this.lastConnectionTimeMilis;
		this.downloadedSizeInBytes = data.length();
		System.out.println("["+playerCount+"] Calculating md5sum of downloaded file ");
		String downloadedMd5 = getMd5Sum(data);
		
		System.out.println("["+playerCount+"] Calculating md5sum of local file ");
		String localMd5 = getMd5Sum(new File(url));
		
		System.out.println("["+playerCount+"] Checking sums... ");
		
		if(downloadedMd5.equals(localMd5)){
			System.out.println("["+playerCount+"] Md5 checksum OK for "+id+" url "+url);
		} else {
			System.out.println("["+playerCount+"] Md5 checksum FAIL for "+id+" url "+url);
		}
		
		int bitrate = this.bitrateCalculator.stopTrackingAndCalculateBitrate(id, this.getDownloadedSizeInBytes());
		
		return bitrate;
	}
	
	public String getMd5Sum(File file) {
		FileInputStream fis = null;
		try {
			System.out.println("1");
			fis = new FileInputStream(file);
			System.out.println("2");
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			System.out.println("3");
			fis.close();
			System.out.println("4");

			return md5;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("WTF!");
		}
		return "";
	}

	private void normalStreamDocumentDownload(File data, String siteAddress,
			long startTime) throws IOException, MalformedURLException,
			FileNotFoundException {
//		BufferedInputStream in = new BufferedInputStream(new URL(siteAddress).openStream());
		
		URL website = new URL(siteAddress);
		URLConnection con = website.openConnection();
		con.setConnectTimeout(TIMEOUT_MILIS);
		con.setReadTimeout(TIMEOUT_MILIS);
		con.setAllowUserInteraction(false);    
		InputStream in = con.getInputStream();
		

		this.lastConnectionTimeMilis = System.currentTimeMillis() - startTime;
		
		FileOutputStream fout = new FileOutputStream(data);

        final byte dowloadData[] = new byte[1024 * 8];
        int count;
        while ((count = in.read(dowloadData, 0, 1024 * 8)) != -1) {
//        	System.out.println("receiving data "+count);
            fout.write(dowloadData, 0, count);
        }
        
        
        fout.close();
        in.close();
	}

	private void newIOJavaDocumentDownload(File data, String siteAddress,
			long startTime) throws MalformedURLException, IOException,
			FileNotFoundException {
		URL website = new URL(siteAddress);
		URLConnection con = website.openConnection();
		con.setConnectTimeout(TIMEOUT_MILIS);
		con.setReadTimeout(TIMEOUT_MILIS);
		con.setAllowUserInteraction(false);    
		InputStream in = con.getInputStream();
		
		
		ReadableByteChannel rbc = Channels.newChannel(in);
		
		this.lastConnectionTimeMilis = System.currentTimeMillis() - startTime;
		
		FileOutputStream fos = new FileOutputStream(data);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
		//Force TCP session end
		HttpURLConnection httpCon = (HttpURLConnection)con;
		httpCon.disconnect();
		fos.close();
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