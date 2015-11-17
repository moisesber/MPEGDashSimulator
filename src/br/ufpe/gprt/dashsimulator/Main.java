package br.ufpe.gprt.dashsimulator;

public class Main {

	public static void main(String[] args) {
//		DASHPlayer player = new DASHPlayer("/dash/BigBuckBunny/", "192.168.1.3", 80);
		
		int numberOfClients = 1;
		int repetitions = 1;
		if(args.length > 0){
			
			try{
				numberOfClients = Integer.parseInt(args[0]);
			}catch (Exception e){
				System.err.println("Invalide argument.\n Argument must be a positive integer.");
				e.printStackTrace();
				System.exit(1);
			}
			
			if(args.length > 1){
				try{
					repetitions = Integer.parseInt(args[1]);
				}catch (Exception e){
				}
			}
			
		}

		for (int i = 0; i < numberOfClients; i++) {
			DASHPlayer player = new DASHPlayer(i, repetitions);
			Thread t = new Thread(player);
			t.start();
		}
		
	}

}
