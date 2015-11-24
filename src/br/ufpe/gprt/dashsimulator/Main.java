package br.ufpe.gprt.dashsimulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

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
		
//		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
//		ExecutorService executor = Executors.newWorkStealingPool();
		ExecutorService executor = new ForkJoinPool();

		for (int i = 0; i < numberOfClients; i++) {
			DASHPlayer player = new DASHPlayer(i, repetitions);
			
//			
//			Thread t = new Thread(player);
//			t.start();
			
			executor.execute(player);
		}
		
		executor.shutdown();
		
		while (!executor.isTerminated()) {
			 
		}
	}

}
