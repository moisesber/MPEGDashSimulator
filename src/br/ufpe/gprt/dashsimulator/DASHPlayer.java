package br.ufpe.gprt.dashsimulator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import br.ufpe.gprt.dashsimulator.util.DummyHTTPClient;

public class DASHPlayer implements Runnable{
	
	private int port;
	private String host;
	private MPDRepresentation mpd;
	private DASHAdaptationLogic logic;
	private DummyHTTPClient httpClient;
	private int playerCount;
	private int repetitions;

	public DASHPlayer(int playerCount, int repetitions){
		this.playerCount = playerCount;
		this.repetitions = repetitions;
		this.mpd = new MPDRepresentation();
		this.host = this.mpd.getHost();
		this.port = this.mpd.getPort();
	}

	@Override
	public void run() {

		Charset charset = Charset.forName("US-ASCII");
		Path plotLogFile = FileSystems.getDefault().getPath("plotData/all.data.dash.player-"+this.playerCount+"-"+System.currentTimeMillis());
		NumberFormat formatter = new DecimalFormat("#0.000");		

		for (int i = 0; i < this.repetitions; i++) {
			requestAllChunks(formatter, plotLogFile);
		}
	}

	private void requestAllChunks(NumberFormat formatter, Path plotLogFile) {
		this.logic = new DASHAdaptationLogic(this.mpd.getBitrates());
		this.httpClient = new DummyHTTPClient(host, port);
		int numberOfSegmentsDownloaded = 1;
		int bitrateUp = 0;
		int bitrateDown = 0;
		int timeouts = 0;
		int currentBitRate = this.logic.getCurrentRepresentation();
		
		System.out.println("Starting to download video data. Downloading from "+host+":"+port);
		
		while(this.mpd.hasMoreSegments(numberOfSegmentsDownloaded)){
			String segmentURL = this.mpd.getSpecifSegment(currentBitRate, numberOfSegmentsDownloaded);
			try {
				System.out.println("["+this.playerCount+"] Requesting segment "+segmentURL+" currentBitrate = "+currentBitRate+" Up="+bitrateUp+" Down="+bitrateDown+ " timeouts="+timeouts);
				int calculatedBitrate = this.httpClient.requestSegment(segmentURL, numberOfSegmentsDownloaded,this.playerCount);
				
				if(calculatedBitrate == Integer.MIN_VALUE){
					timeouts++;
					
					synchronized(this){
						wait( 100 + (int)( Math.random() * 100));
					}
					
					continue;
				}
				
				long downloadeSizeInBytes = this.httpClient.getDownloadedSizeInBytes();
				long startTime = this.httpClient.getStartDownloadTime(numberOfSegmentsDownloaded);
				long totalTime = this.httpClient.getSegmentTotalTimeMilis(numberOfSegmentsDownloaded);
				long downloadTime = this.httpClient.getLastDownloadTimeMilis();
				long connectionTime = this.httpClient.getLastConnectionTimeMilis();
				
				String s = segmentURL + "\t"
						+ downloadeSizeInBytes+ "\t"
						+ (new Date(startTime)) + "\t"
						+ startTime + "\t"
						+ connectionTime + "\t"
						+ downloadTime + "\t"
						+ totalTime + "\t"
//						+ formatter.format((((double)this.httpClient.getDownloadedSizeInBytes()) / ((double)this.httpClient.getSegmentTotalTimeMilis(numberOfSegmentsDownloaded)) ))+ "\t"
						+ formatter.format(((double)downloadeSizeInBytes)/((double)totalTime))+ "\t"
//						+ formatter.format((((double)this.httpClient.getDownloadedSizeInBytes()) / ((double)this.httpClient.getLastDownloadTimeMilis()) ))
						+ formatter.format((((double)downloadeSizeInBytes) / ((double)downloadTime) ))
						+ "\n";
				
				try (BufferedWriter writer = Files.newBufferedWriter(plotLogFile, StandardOpenOption.CREATE,StandardOpenOption.APPEND)) {
				    writer.write(s, 0, s.length());
				} catch (IOException x) {
				    System.err.format("IOException: %s%n", x);
				}
				
				
				
				System.out.println("["+this.playerCount+"] Bitrate calculated was "+calculatedBitrate+" total bytes downloaded were "+this.httpClient.getDownloadedSizeInBytes()+ " total time was "+totalTime );

				if(this.logic.switchRepresentation(calculatedBitrate)){
					int newBitRate = this.logic.getCurrentRepresentation();
					
					if(newBitRate > currentBitRate ){
						bitrateUp++;
					} else if(newBitRate < currentBitRate ){
						bitrateDown++;
					}
				}
				
				
				numberOfSegmentsDownloaded++;
				currentBitRate = this.logic.getCurrentRepresentation();
				
				synchronized(this){
					wait(100);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	

}
