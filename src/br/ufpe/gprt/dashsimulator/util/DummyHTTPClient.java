package br.ufpe.gprt.dashsimulator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DummyHTTPClient {

	private Socket socks;
	private String host;
	private int port;
	private int destinationPort;
	private BitrateCalculations bitrateCalculator;
	private long downloadedSizeInBytes;
	
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
		this.bitrateCalculator.startTrackingSegment(id);

		URL website = new URL(siteAddress);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(data);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
//		this.downloadedSizeInBytes = data.getTotalSpace();
		this.downloadedSizeInBytes = data.length();
		
//        String request = "GET "+url+"?tag=java HTTP/1.1 \n"
//        		+ "host: " + host +"\n"
//        		+ "User-Agent: dummyclient/0.1\n\n";
//		this.connect();
//		
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.socks.getOutputStream()));
//		out.write(request);
//		out.flush();
//		this.bitrateCalculator.startTrackingSegment(id);
////		BufferedReader in = new BufferedReader(new InputStreamReader(socks.getInputStream()));
//		InputStreamReader in = new InputStreamReader(socks.getInputStream());
//		
//		byte[] buffer = new byte[4096 * 2];
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		
//        int numBytesJustRead;
//		while((numBytesJustRead = socks.getInputStream().read(buffer)) != -1) {
//            baos.write(buffer, 0, numBytesJustRead);
//        }
//		
//		downloadedSizeInBytes = baos.size();
//		this.disconnect();
		
		
		int bitrate = this.bitrateCalculator.stopTrackingAndCalculateBitrate(id, this.getDownloadedSizeInBytes());
		
		return bitrate;
	}
	
	public long getDownloadedSizeInBytes() {
		return downloadedSizeInBytes;
	}

	private int connect() throws IOException {
		if(this.socks != null && this.socks.isConnected()){
			this.socks.close();
		}
		
		this.socks = new Socket();
		socks.connect(new InetSocketAddress(host, port));
		
		return this.socks.getLocalPort();
	}
	
	public void disconnect() throws IOException{
		if(this.socks != null){
			this.socks.close();
		}
	}
	
	public int getLocalPort() {
		return socks != null? socks.getLocalPort() : -1;
	}

	public InetAddress getLocalAddress() {
		return this.socks.getLocalAddress();
	}

	public String toString(){
		if(this.socks == null){
			return "DummyClient: Not connected yet...";
		}
		return "DummyClient: localPort="+this.getLocalPort()+ " localAddr="+this.socks.getLocalAddress()+" dstPort="+this.port+ " dstAddr="+this.host;
	}

	public int getDestinationPort() {
		return destinationPort;
	}
	
}