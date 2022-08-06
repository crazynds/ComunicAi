package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;

import ufsm.comunicacao.t2.layer.lib.RepeatARQReciveChannel;
import ufsm.comunicacao.t2.layer.lib.RepeatARQSendChannel;
import ufsm.comunicacao.t2.network.HeaderPackage;

public class FlowControlLayer extends ConfirmationLayer{


	private RepeatARQSendChannel sendChannel;
	private RepeatARQReciveChannel reciveChannel;
	

	public FlowControlLayer() {
		sendChannel = new RepeatARQSendChannel(this, 8);
		reciveChannel = new RepeatARQReciveChannel(this, 8);
	}

	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent,Runnable onConfirmation) {
		sendChannel.addPackage(datagram, dataWithoutCurrent,onConfirmation);
	}
	
	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent) {
		sendChannel.addPackage(datagram, dataWithoutCurrent,new Runnable() {
			
			@Override
			public void run() {
			}
		});
	}

	@Override
	public void recivePackage(HeaderPackage datagram, ByteBuffer dataWithCurrent) {
		if(!sendChannel.processPackage(datagram, dataWithCurrent))
			reciveChannel.processPackage(datagram, dataWithCurrent);
	}
	
	
	

}
