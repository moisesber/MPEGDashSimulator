package br.ufpe.gprt.dashsimulator;

public class Main {

	public static void main(String[] args) {
		DASHPlayer player = new DASHPlayer("/dash/BigBuckBunny/", "192.168.1.3", 80);
		
		player.run();
	}

}
