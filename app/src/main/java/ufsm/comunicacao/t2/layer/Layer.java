package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;

import ufsm.comunicacao.t2.network.HeaderPackage;

public abstract class Layer {
	

	public abstract void sendPackage(HeaderPackage datagram,ByteBuffer dataWithoutCurrent);
	public abstract void recivePackage(HeaderPackage datagram,ByteBuffer dataWithCurrent);

	
	private Layer nextLayer;
	private Layer prevLayer;
	
	
	protected void setPrevLayer(Layer l) {
		prevLayer = l;
	}
	public Layer setNextLayer(Layer l) {
		l.setPrevLayer(this);
		nextLayer = l;
		return this;
	}
	public Layer addLayer(Layer l) {
		if(nextLayer==null)return this.setNextLayer(l);
		else return this.nextLayer.addLayer(l);
	}
	public Layer getPrevLayer() {
		return prevLayer;
	}
	public Layer getNextLayer() {
		return nextLayer;
	}

	public Layer getLastLayer() {
		if(nextLayer == null)return this;
		return nextLayer.getLastLayer();
	}
	public boolean isAlive(){
		if(prevLayer!=null)return prevLayer.isAlive();
		else return false;
	}

}