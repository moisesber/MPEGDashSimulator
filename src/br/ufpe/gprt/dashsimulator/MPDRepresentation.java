package br.ufpe.gprt.dashsimulator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class MPDRepresentation {
	
	private String baseURL;
	private int numberOfSegments;
	private int currentSegmentId;
	private String representation;
	private List<Integer> bitrates;
	
	private final static String NUMBER_OF_SEGMENTS = "NumberOfSegments";
	private final static String BITRATES_AVAILABLE = "Bitrates";
	private final static String SEGMENT_SIZE = "SegmentSize";

	public MPDRepresentation(String baseURL){
		this.baseURL = baseURL;
		this.currentSegmentId = 1;
		
		FileInputStream fileInput;
		try {
			fileInput = new FileInputStream(new File("dashplayer.properties"));
			Properties props = new Properties();
			props.load(fileInput);
			
			if(!props.containsKey(NUMBER_OF_SEGMENTS) || !props.containsKey(BITRATES_AVAILABLE) || !props.containsKey(SEGMENT_SIZE)){
				throw new Exception("Properties "+NUMBER_OF_SEGMENTS+", "+BITRATES_AVAILABLE+", and "+SEGMENT_SIZE+" have to be defined in properties file.");
			}
			
			this.numberOfSegments = Integer.parseInt(props.getProperty(NUMBER_OF_SEGMENTS));
			this.representation = props.getProperty(SEGMENT_SIZE);
			this.bitrates = this.getBitrateFromProperties(props.getProperty(BITRATES_AVAILABLE));
			
			fileInput.close();
		} catch (Exception e) {
			System.err.println("Problems reading properties file.");
			e.printStackTrace();
			System.exit(1);
		} 

	}
	
	private List<Integer> getBitrateFromProperties(String property) {
		StringTokenizer tokens = new StringTokenizer(property,",");
		
		List<Integer> bitratesList = new ArrayList<Integer>();

		while(tokens.hasMoreTokens()){
			bitratesList.add(Integer.parseInt(tokens.nextToken().trim()));
		}
		
		return bitratesList;
	}

	public List<Integer> getBitrates() {
		return bitrates;
	}

	public String getNextSegment(int bitrate){
		if(this.currentSegmentId > this.numberOfSegments){
			return null;
		}
		
		return baseURL +"/"+ representation +"/"+representation+"_"+bitrate+"kbit/"+representation+this.currentSegmentId+++".m4s";
	}
	
	public boolean hasMoreSegments(){
		return this.currentSegmentId <= this.numberOfSegments;
	}

	public int getCurrentSegmentId() {
		return currentSegmentId;
	}
	
}
