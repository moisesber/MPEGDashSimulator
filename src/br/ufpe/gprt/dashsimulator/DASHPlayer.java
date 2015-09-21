package br.ufpe.gprt.dashsimulator;

import java.io.IOException;

import br.ufpe.gprt.dashsimulator.util.DummyHTTPClient;

public class DASHPlayer implements Runnable{
	
	private int port;
	private String host;
	private String baseURL;
	private MPDRepresentation mpd;
	private DASHAdaptationLogic logic;
	private DummyHTTPClient httpClient;

	public DASHPlayer(String baseURL, String host, int port){
		this.baseURL = baseURL;
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		this.mpd = new MPDRepresentation(baseURL);
		this.logic = new DASHAdaptationLogic(this.mpd.getBitrates());
		this.httpClient = new DummyHTTPClient(host, port);
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
				
				int downloadTime = calculatedBitrate == 0 ? -1 : ((this.httpClient.getDownloadedSizeInBytes()* 8) / calculatedBitrate);
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
		try {
			this.httpClient.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
