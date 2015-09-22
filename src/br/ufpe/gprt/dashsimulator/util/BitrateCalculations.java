package br.ufpe.gprt.dashsimulator.util;

import java.util.HashMap;
import java.util.Map;

public class BitrateCalculations {
	
	private Map<Integer, Long> initialTimes;

	public BitrateCalculations(){
		this.initialTimes = new HashMap<Integer, Long>();
		
	}
	
	public void startTrackingSegment(int id){
		this.initialTimes.put(id, System.currentTimeMillis());
	}
	
	public int stopTrackingAndCalculateBitrate(int id, long dataLengthInBytes){
		long currentTime = System.currentTimeMillis();
		if(!this.initialTimes.containsKey(id)){
			System.out.println("Trying to calculate bitrate of a segment not tracked.");
			return -1;
		}
		
		if(this.initialTimes.containsKey(id) && this.initialTimes.get(id) < 0){
			System.out.println("Already calculated bitrate for this segment.");
			return -2;
		}
		
		int bitrate = ((int)((dataLengthInBytes*8)/ (currentTime - this.initialTimes.get(id)) )) *1000;
		
		//Marking the segment as already tracked
		this.initialTimes.put(id, -1l);
		return  bitrate;
	}

}
