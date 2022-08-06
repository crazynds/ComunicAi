package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;

import ufsm.comunicacao.t2.network.HeaderPackage;
import ufsm.comunicacao.t2.network.NetworkManager;

public class LimitLayer extends ConfirmationLayer{

	private int packageNumber = (new Random()).nextInt();
	
	private synchronized int getNextPackageNumber() {
		return packageNumber++;
	}
	
	private HashMap<Integer, TreeMap<Byte,ByteBuffer>> recivedPackages = new HashMap<>();
	

	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent, Runnable onConfirmation) {
		dataWithoutCurrent.position(0);
		int size = dataWithoutCurrent.capacity();
		
		
		final int L0 = 2;
		final int L1 = 4;
		final int L2 = 4;
		final int L3 = 6;
		final int MAXPAYLOAD = NetworkManager.MAX_PACKAGE_SIZE - (L0 + L1 + L2 + L3);
		
		final int PACKAGENUMBER = getNextPackageNumber();
		
		int x,maxSize = (size/MAXPAYLOAD);
		boolean confirmation = onConfirmation!=null && getNextConfirmationLayer()!=null;
		if(size%MAXPAYLOAD != 0)
			maxSize++;
		
		if(maxSize>=127)return;
		
		for(x=0;x<(size/MAXPAYLOAD);x++) {
			ByteBuffer slice = ByteBuffer.allocate(MAXPAYLOAD+L3).putInt(PACKAGENUMBER).put((byte)(x+1)).put((byte)maxSize);
			dataWithoutCurrent.limit((x+1)*MAXPAYLOAD).position(x*MAXPAYLOAD);
			slice.put(dataWithoutCurrent).position(0);
			if(confirmation)
				getNextConfirmationLayer().sendPackage(datagram, slice,onConfirmation);
			else getNextLayer().sendPackage(datagram, slice);
		}
		if(x<maxSize) {
			int capacity = size%MAXPAYLOAD;
			ByteBuffer slice = ByteBuffer.allocate(capacity+L3).putInt(PACKAGENUMBER).put((byte)maxSize).put((byte)maxSize);
			dataWithoutCurrent.position(x*MAXPAYLOAD).limit(size);
			slice.put(dataWithoutCurrent).position(0);
			if(confirmation)
				getNextConfirmationLayer().sendPackage(datagram, slice,onConfirmation);
			else getNextLayer().sendPackage(datagram, slice);
		}
		
	}
	
	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent) {
		sendPackage(datagram, dataWithoutCurrent, null);
	}

	@Override
	public synchronized void recivePackage(HeaderPackage datagram, ByteBuffer dataWithCurrent) {
		
		dataWithCurrent.position(0);
		int packNumber = dataWithCurrent.getInt();
		byte number = dataWithCurrent.get();
		byte max = dataWithCurrent.get();
		ByteBuffer newBuff = dataWithCurrent.slice();
		
		if(number == max && max==1)getPrevLayer().recivePackage(datagram, newBuff);
		
		
		if(recivedPackages.containsKey(packNumber)) {
			TreeMap<Byte,ByteBuffer> tree = recivedPackages.get(packNumber);
			tree.put(number, newBuff);
			if(tree.size()==max) {
				recivedPackages.remove(packNumber);
				int sizeReal = tree.entrySet().stream().map(new Function<Map.Entry<Byte,ByteBuffer>, Integer>() {

					@Override
					public Integer apply(Map.Entry<Byte,ByteBuffer> t) {
						t.getValue().position(0);
						return t.getValue().capacity();
					}
				}).reduce((t, u) -> t+u).get();
				ByteBuffer realValue = ByteBuffer.allocate(sizeReal);
				Map.Entry<Byte,ByteBuffer> aux;
				while((aux = tree.pollFirstEntry())!=null) {
					realValue.put(aux.getValue());
				}
				realValue.position(0);
				getPrevLayer().recivePackage(datagram,realValue) ;
			}
		}else {
			TreeMap<Byte,ByteBuffer> tree = new TreeMap<>();
			tree.put(number, newBuff);
			recivedPackages.put(packNumber, tree);
		}
		
	}


}
