package br.ufpe.gprt.dashsimulator.util;

import java.util.HashMap;
import java.util.Map;

public class BitrateCalculations {
	
	private Map<Integer, Long> initialTimes;
	private Map<Integer, Long> downloadTimes;

	public BitrateCalculations(){
		this.initialTimes = new HashMap<Integer, Long>();
		this.downloadTimes = new HashMap<Integer, Long>();
	}
	
	public long startTrackingSegment(int id){
		if(this.initialTimes.containsKey(id)){
			return this.initialTimes.get(id);
		}
		
		long currentTime = System.currentTimeMillis();
		this.initialTimes.put(id, currentTime);
		return currentTime;
	}
	
	public int stopTrackingAndCalculateBitrate(int id, long dataLengthInBytes){
		long currentTime = System.currentTimeMillis();
		if(!this.initialTimes.containsKey(id)){
			System.out.println("Trying to calculate bitrate of a segment not tracked.");
			return -1;
		}
		
		if(this.downloadTimes.containsKey(id)){
			System.out.println("Already calculated bitrate for this segment.");
			return -2;
		}
		
		long downloadTime = (currentTime - this.initialTimes.get(id));

		int bitrate = ((int)((dataLengthInBytes*8)/ downloadTime )) *1000;
		
		//Marking the segment as already tracked
		this.downloadTimes.put(id, downloadTime);
		return  bitrate;
	}

	public long getTotalTimeMilis(int id){
		return this.downloadTimes.containsKey(id) ? this.downloadTimes.get(id) : -1;
	}

	public long getStartDownloadTime(int id) {
		return this.initialTimes.get(id);
	}
}
