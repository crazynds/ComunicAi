package ufsm.comunicacao.t2.network;

import android.util.Log;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.function.Consumer;

import ufsm.comunicacao.t2.layer.ErrorControlLayer;
import ufsm.comunicacao.t2.layer.EndLayer;
import ufsm.comunicacao.t2.layer.FlowControlLayer;
import ufsm.comunicacao.t2.layer.LimitLayer;
import ufsm.comunicacao.t2.layer.LossLayer;
import ufsm.comunicacao.t2.layer.StartLayer;

public class Comunication extends StartLayer{
	
	private InetAddress address;
	private int port;
	private LinkedList<String> bufferLines = new LinkedList<>();
	private ComunicationLinstener listener = null;
	
	private long timeFeedBack = -1;
	private Consumer<Integer> onFinishFeedBack = null;
	private boolean alive = true;
	

	public Comunication(EndLayer layer,InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.addLayer(new LimitLayer());
		this.addLayer(new FlowControlLayer());
		this.addLayer(new ErrorControlLayer());
		this.addLayer(new LossLayer(0.0f));
		this.addLayer(layer);
	}

	
	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent) {
		if(datagram==null)
			datagram = new HeaderPackage();
		datagram.setAddress(address);
		datagram.setPort(port);
		
		getNextLayer().sendPackage(datagram, dataWithoutCurrent);
	}
	
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent,Runnable onConfirmation) {
		if(datagram==null)
			datagram = new HeaderPackage();
		datagram.setAddress(address);
		datagram.setPort(port);
		
		if(getNextConfirmationLayer()!=null)
			getNextConfirmationLayer().sendPackage(datagram, dataWithoutCurrent,onConfirmation);
		else getNextLayer().sendPackage(datagram, dataWithoutCurrent);
	}

	@Override
	public void recivePackage(HeaderPackage datagram, ByteBuffer dataWithCurrent) {
		byte[] bytes = new byte[dataWithCurrent.remaining()];
		dataWithCurrent.get(bytes);
		String str = new String(bytes);
		if(str.compareTo("PONG")==0 && onFinishFeedBack!=null) {
			timeFeedBack= System.currentTimeMillis()-timeFeedBack;
			onFinishFeedBack.accept(Integer.valueOf((int)timeFeedBack));
			Log.i("PING","Recebi PONG");
		}else if(str.compareTo("PING")==0) {
			sendText("PONG");
		}else if(listener!=null)
			listener.onRecivePackage(str);
		else bufferLines.addLast(str);
	}
	
	public void setListener(ComunicationLinstener listener) {
		this.listener = listener;
	}
	

	public void sendText(String text) {
		sendPackage(null, ByteBuffer.wrap(text.getBytes()));
	}

	public void sendText(String text,Runnable onConfirmation) {
		sendPackage(null, ByteBuffer.wrap(text.getBytes()),onConfirmation);
	}
	
	public String nextLine() {
		if(bufferLines.isEmpty())return null;
		return bufferLines.removeFirst();
	}
	
	public void sendPing(Consumer<Integer> onFinish) {
		timeFeedBack = System.currentTimeMillis();
		onFinishFeedBack= onFinish;
		Log.i("PING","Enviei PING");
		sendText("PING");
	}


	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public void end(){
		alive=false;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}
}
