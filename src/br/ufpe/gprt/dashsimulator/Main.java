package br.ufpe.gprt.dashsimulator;

public class Main {

	public static void main(String[] args) {
//		DASHPlayer player = new DASHPlayer("/dash/BigBuckBunny/", "192.168.1.3", 80);
		
		DASHPlayer player = new DASHPlayer("/ftp/datasets/mmsys12/BigBuckBunny/", "www-itec.uni-klu.ac.at", 80);

		
		player.run();
	}

}
