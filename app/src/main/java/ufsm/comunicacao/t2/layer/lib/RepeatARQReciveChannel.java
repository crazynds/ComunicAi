package ufsm.comunicacao.t2.layer.lib;

import java.nio.ByteBuffer;
import java.util.Map;

import ufsm.comunicacao.t2.layer.Layer;
import ufsm.comunicacao.t2.network.HeaderPackage;

public class RepeatARQReciveChannel {

	private Map.Entry<HeaderPackage, ByteBuffer> fila[];
	private boolean confirmacoesFila[];
	
	private Layer currentLayer;
	private int windowSize;
	private int firstFrameWaitingResponse;
	
	private HeaderPackage defaultHeaderPackage = null;
	
	
	@SuppressWarnings("unchecked")
	public RepeatARQReciveChannel(Layer currentLayer, int windowSizeBits) {
		this.windowSize=1<<windowSizeBits;
		this.currentLayer=currentLayer;
		this.confirmacoesFila = new boolean[windowSize];
		this.fila = new Map.Entry[windowSize];
		
		for(int x=0;x<windowSize;x++) {
			this.confirmacoesFila[x] = false;
			this.fila[x] = null;
		}
		
		firstFrameWaitingResponse= -1;
	}
	
	/**
	 * DATA	=> val>=0
	 * ANK	=> val==-1
	 * NANK	=> val==-2
	 */
	
	public synchronized void process() {
		if(firstFrameWaitingResponse==-1)return;
		int windowToSend = windowSize/4;
		int lastDataRecived = 0;
		for(int x=firstFrameWaitingResponse;x<firstFrameWaitingResponse+windowToSend;x++) {
			int a = x%windowSize;
			
			if(confirmacoesFila[a])lastDataRecived = x;
		} 
		for(int x=firstFrameWaitingResponse;x<=lastDataRecived;x++) {
			int a = x%windowSize;
			if(!confirmacoesFila[a]) {
				if(firstFrameWaitingResponse==a && lastDataRecived>x) {
					//envia aviso de NANK
					ByteBuffer packNANK = ByteBuffer.allocate(8);
					packNANK.putInt(0,-2);
					packNANK.putInt(4, a);
					currentLayer.getNextLayer().sendPackage(defaultHeaderPackage, packNANK);
				}
				continue;
			}
			if(firstFrameWaitingResponse==a) {
				confirmacoesFila[a] = false;
				currentLayer.getPrevLayer().recivePackage(fila[a].getKey(), fila[a].getValue());
				fila[a] = null;
				
				firstFrameWaitingResponse++;
				firstFrameWaitingResponse%=windowSize;
			}
		}
	}
	
	
	public synchronized boolean processPackage(HeaderPackage header,ByteBuffer buffer) {
		int aux = buffer.getInt();
		if(aux<0 || aux>=windowSize) return false;
		ByteBuffer data = ByteBuffer.allocate(buffer.capacity()-4);
		data.put(buffer.slice());
		data.position(0);
		int windowToSend=  windowSize/4;
		if(defaultHeaderPackage == null) {
			defaultHeaderPackage = new HeaderPackage();
			defaultHeaderPackage.setAddress(header.getAddress());
			defaultHeaderPackage.setPort(header.getPort());
		}
		
		if(
			firstFrameWaitingResponse==-1
			|| (firstFrameWaitingResponse+windowToSend>aux && firstFrameWaitingResponse<=aux)
			|| ((firstFrameWaitingResponse+windowToSend)>aux+windowSize && firstFrameWaitingResponse>aux)
			) {
			if(firstFrameWaitingResponse==-1)
				firstFrameWaitingResponse = aux;
			
			fila[aux] = Map.entry(header, data);
			confirmacoesFila[aux] = true;
			
			// Envia ANK
			ByteBuffer packANK = ByteBuffer.allocate(8);
			packANK.putInt(0,-1);
			packANK.putInt(4, aux);
			currentLayer.getNextLayer().sendPackage(defaultHeaderPackage, packANK);
		}else {
			ByteBuffer packANK = ByteBuffer.allocate(8);
			packANK.putInt(0,-1);
			packANK.putInt(4, aux);
			currentLayer.getNextLayer().sendPackage(defaultHeaderPackage, packANK);
		}
		process();
		return true;
	}
	
}
