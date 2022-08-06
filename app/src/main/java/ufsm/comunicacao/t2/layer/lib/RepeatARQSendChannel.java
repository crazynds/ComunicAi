package ufsm.comunicacao.t2.layer.lib;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ufsm.comunicacao.t2.Variable;
import ufsm.comunicacao.t2.layer.Layer;
import ufsm.comunicacao.t2.network.HeaderPackage;

public class RepeatARQSendChannel {

	private LinkedList<Map.Entry<HeaderPackage, Map.Entry<ByteBuffer,Runnable>>> stackOfWaiting = new LinkedList<>();


	private Map.Entry<HeaderPackage, Map.Entry<ByteBuffer,Runnable>> fila[];
	private boolean confirmacoesFila[];

	private Layer currentLayer;
	private int windowSize;
	private int firstFrameWaitingResponse;
	private int nextFrameToSend;

	private ScheduledExecutorService executor;


	@SuppressWarnings("unchecked")
	public RepeatARQSendChannel(Layer currentLayer, int windowSizeBits) {
		this.windowSize=1<<windowSizeBits;
		this.currentLayer=currentLayer;
		this.confirmacoesFila = new boolean[windowSize];
		this.fila = new Map.Entry[windowSize];

		firstFrameWaitingResponse= 0;
		nextFrameToSend = 0;


		executor = Executors.newScheduledThreadPool(1);
	}


	public synchronized void process() {
		int windowToSend=  windowSize/4;
		for(int x=firstFrameWaitingResponse;x<firstFrameWaitingResponse+windowToSend;x++) {
			int a = x%windowSize;
			if(confirmacoesFila[a] && a==firstFrameWaitingResponse) {
				confirmacoesFila[a] = false;
				fila[a] = null;

				firstFrameWaitingResponse++;
				firstFrameWaitingResponse%=windowSize;

				process();
			}
			if(fila[a]==null)break;
		}
		while(!stackOfWaiting.isEmpty() && ((firstFrameWaitingResponse+windowToSend)%windowSize) != nextFrameToSend ) {
			fila[nextFrameToSend] = stackOfWaiting.removeFirst();

			//Mount the frame with data of package number
			ByteBuffer oldBuff = fila[nextFrameToSend].getValue().getKey();
			ByteBuffer newBuff = ByteBuffer.allocate(oldBuff.capacity()+4);
			newBuff.putInt(nextFrameToSend).put(oldBuff);

			//Send the frame to queue
			fila[nextFrameToSend] = Map.entry(fila[nextFrameToSend].getKey(), Map.entry(newBuff,fila[nextFrameToSend].getValue().getValue()));
			confirmacoesFila[nextFrameToSend] = false;
			sendItemFila(nextFrameToSend);

			nextFrameToSend++;
			nextFrameToSend%=windowSize;
		}
	}


	public synchronized boolean processPackage(HeaderPackage header,ByteBuffer buffer) {
		int packageType = buffer.getInt(0);
		if(packageType>=0)return false;
		int packageNumber = buffer.getInt(4);
		int windowToSend = windowSize/4;
		if(confirmacoesFila.length<=packageNumber || packageNumber<0)return false;
		if(
				!(firstFrameWaitingResponse+windowToSend>packageNumber && firstFrameWaitingResponse<=packageNumber)
						&& !((firstFrameWaitingResponse+windowToSend)>packageNumber+windowToSend && firstFrameWaitingResponse>packageNumber)
		) return false;

		switch(packageType) {
			case -1: // Recive an ANK
				Variable.sucessFrames++;
				System.out.println("Recived ANK");
				confirmacoesFila[packageNumber] = true;
				if(fila[packageNumber]!=null && fila[packageNumber].getValue().getValue()!=null) {
					//Confirm transaction
					fila[packageNumber].getValue().getValue().run();
				}
				break;
			case -2: // Recive an NANK
				System.out.println("Recived NANK");
				packageNumber = buffer.getInt(4);
				sendItemFila(packageNumber);
				break;
			default: // Package type not recognize
				return true;
		}
		this.process();
		return true;
	}

	private synchronized void sendItemFila(int val) {
		final long TIME_WAIT = 500;
		
		if(!currentLayer.isAlive())return;
		fila[val].getValue().getKey().position(0);
		currentLayer.getNextLayer().sendPackage(fila[val].getKey(), fila[val].getValue().getKey());
		fila[val].getKey().setSendTime(System.currentTimeMillis());

		Runnable r =new Runnable() {

			Map.Entry<HeaderPackage,  Map.Entry<ByteBuffer,Runnable>> itemCheck = fila[val];
			int pos = val;

			@SuppressWarnings("static-access")
			@Override
			public void run() {
				
				long sendTime = itemCheck.getKey().getSendTime();
				
				if(System.currentTimeMillis() - sendTime < TIME_WAIT)
					try {
						Thread.currentThread().sleep(TIME_WAIT);
					} catch (InterruptedException e) {}
				
				if(fila[pos]!=null && fila[pos] == itemCheck && !confirmacoesFila[pos]) {
					System.out.println("Não recebi confirmação do pacote "+pos);
					sendItemFila(pos);
					Variable.repetidosFrames++;
				}
			}
		};
		executor.schedule(r, TIME_WAIT, TimeUnit.MILLISECONDS);
	}

	public void addPackage(HeaderPackage header,ByteBuffer data,Runnable onConfirmation) {
		stackOfWaiting.addLast(Map.entry(header, Map.entry(data,onConfirmation)));
		this.process();
	}

}
