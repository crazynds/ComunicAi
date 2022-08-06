package ufsm.comunicacao.t2.main;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ufsm.comunicacao.t2.network.Comunication;
import ufsm.comunicacao.t2.network.NetworkListener;
import ufsm.comunicacao.t2.network.NetworkManager;

public class Main {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws SocketException, InterruptedException, UnknownHostException {
		NetworkManager net = new NetworkManager(new NetworkListener() {
			@Override
			public void onOpenConnection(Comunication c) {
				//
			}
		}, 9001);
		
		Comunication c = net.openComunication(InetAddress.getLocalHost(), 9000);

		for(int x=0;x<256;x++) {
			c.sendText("Pacote: "+x);
		}
		
		
		while(true) {
			Thread.currentThread().sleep(1000);
		}
	}

}
