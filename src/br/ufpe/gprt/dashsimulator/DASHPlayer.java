package br.ufpe.gprt.dashsimulator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
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

	public DASHPlayer(int playerCount){
		this.playerCount = playerCount;
		this.mpd = new MPDRepresentation();
		this.host = this.mpd.getHost();
		this.port = this.mpd.getPort();
	}

	@Override
	public void run() {
		this.logic = new DASHAdaptationLogic(this.mpd.getBitrates());
		this.httpClient = new DummyHTTPClient(host, port);
		Charset charset = Charset.forName("US-ASCII");
		Path plotLogFile = FileSystems.getDefault().getPath("all.data.dash.player-"+this.playerCount+"-"+System.currentTimeMillis());
		NumberFormat formatter = new DecimalFormat("#0.000");		
				
		int numberOfSegmentsDownloaded = 1;
		int bitrateUp = 0;
		int bitrateDown = 0;
		int currentBitRate = this.logic.getCurrentRepresentation();
		
		System.out.println("Starting to download video data. Downloading from "+host+":"+port);
		
		while(this.mpd.hasMoreSegments()){
			String segmentURL = this.mpd.getNextSegment(currentBitRate);
			try {
				System.out.println("Requesting segment "+segmentURL+" currentBitrate = "+currentBitRate+" Up="+bitrateUp+" Down="+bitrateDown);
				int calculatedBitrate = this.httpClient.requestSegment(segmentURL, numberOfSegmentsDownloaded);
				
				long downloadTime = this.httpClient.getSegmentTotalTimeMilis(numberOfSegmentsDownloaded);
				
				String s = segmentURL + "\t"
						+ this.httpClient.getDownloadedSizeInBytes()+ "\t"
						+ (new Date(this.httpClient.getStartDownloadTime(numberOfSegmentsDownloaded))) + "\t"
						+ this.httpClient.getStartDownloadTime(numberOfSegmentsDownloaded) + "\t"
						+ this.httpClient.getLastConnectionTimeMilis() + "\t"
						+ this.httpClient.getLastDownloadTimeMilis() + "\t"
						+ this.httpClient.getSegmentTotalTimeMilis(numberOfSegmentsDownloaded)
						+ formatter.format((((double)this.httpClient.getDownloadedSizeInBytes()) / ((double)this.httpClient.getSegmentTotalTimeMilis(numberOfSegmentsDownloaded)) ))+ "\t"
						+ formatter.format((((double)this.httpClient.getDownloadedSizeInBytes()) / ((double)this.httpClient.getLastDownloadTimeMilis()) ))
						+ "\n";
				try (BufferedWriter writer = Files.newBufferedWriter(plotLogFile, StandardOpenOption.CREATE,StandardOpenOption.APPEND)) {
				    writer.write(s, 0, s.length());
				} catch (IOException x) {
				    System.err.format("IOException: %s%n", x);
				}
				
				
				System.out.println("Bitrate calculated was "+calculatedBitrate+" total bytes downloaded were "+this.httpClient.getDownloadedSizeInBytes()+ " time was "+downloadTime );
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	

}
