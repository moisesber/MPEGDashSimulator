package br.ufpe.gprt.dashsimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DASHAdaptationLogic {
	
	private static final int BITPERSECOND_MULTIPLIER = 1000;
	
	private int lowestRepresentation;
	private List<Integer> bitrates;
	private int currentRepresentation;

	public DASHAdaptationLogic(List<Integer> bitrates){
		if(bitrates.size() <= 0){
			throw new RuntimeException("Adaptation logic must have at least one bitrate.");
		}
		
		Collections.sort(bitrates);
		this.bitrates = new ArrayList<Integer>();
		
		for (int bitrate : bitrates) {
			this.bitrates.add(bitrate *  BITPERSECOND_MULTIPLIER);
		}
		
		this.lowestRepresentation = this.bitrates.get(0);
		this.currentRepresentation = this.bitrates.get(0);
	}
	
	public boolean switchRepresentation(int calculatedBitrate){
//		if(parseInt(_rel.bandwidth) < _mybps && n <= parseInt(_rel.bandwidth))
		
		int chosenOne = this.lowestRepresentation;
		for (int availableBitrate : bitrates) {
			
//			System.out.println("Calc = "+calculatedBitrate);
//			System.out.println("Chosenone = "+chosenOne);
//			System.out.println("Available bitrate = "+availableBitrate);
			if(availableBitrate < calculatedBitrate && chosenOne <= availableBitrate){
				chosenOne = availableBitrate;
			}
		}

		if(chosenOne != this.currentRepresentation){
			this.currentRepresentation = chosenOne;
			return true;
		}
		
		return false;
	}

	public int getCurrentRepresentation() {
		return currentRepresentation / BITPERSECOND_MULTIPLIER;
	}
	
}
