package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;

import ufsm.comunicacao.t2.network.HeaderPackage;

public abstract class ConfirmationLayer extends Layer {

	

	public abstract void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent,Runnable onConfirmation);
	
	public ConfirmationLayer getNextConfirmationLayer() {
		Layer l = getNextLayer();
		
		if(l instanceof ConfirmationLayer) {
			return (ConfirmationLayer)l;
		}
		return null;
	}

}
