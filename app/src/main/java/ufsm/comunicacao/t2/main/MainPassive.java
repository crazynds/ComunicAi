package ufsm.comunicacao.t2.main;

import java.net.SocketException;
import java.util.Scanner;

import ufsm.comunicacao.t2.network.Comunication;
import ufsm.comunicacao.t2.network.NetworkListener;
import ufsm.comunicacao.t2.network.NetworkManager;

public class MainPassive {
	
	
	private static Comunication comunication = null;

	@SuppressWarnings({ "static-access", "unused" })
	public static void main(String[] args) throws SocketException, InterruptedException {
		NetworkManager net = new NetworkManager(new NetworkListener() {
			@Override
			public void onOpenConnection(Comunication c) {
				MainPassive.comunication = c;
			}
		}, 9000);
		Scanner scan = new Scanner(System.in);
		
		while(true) {
			String line = scan.nextLine();
			
			if(MainPassive.comunication!=null) {
				MainPassive.comunication.sendText(line);
			}
			while((line = MainPassive.comunication.nextLine()) !=null) {
				System.out.println("RECIVED: "+line);
			}
		}
	}

}
