package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;
import java.util.Random;

import ufsm.comunicacao.t2.network.HeaderPackage;

public class LossLayer extends Layer {
	
	private float percent;
	private Random random;
	
	public LossLayer(float percent) {
		this.percent=percent;
		random = new Random();
	}
			

	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent) {
		dataWithoutCurrent.position(0);

		while(random.nextFloat()<=percent) {
			byte error = 0;
			for(int x=0;x<random.nextInt(4);x++) {
				error |= 1<<random.nextInt(8);
			}
			int index = random.nextInt(dataWithoutCurrent.capacity());
			
			byte original = dataWithoutCurrent.get(index);
			dataWithoutCurrent.put(index,(byte) (original^error));
			getNextLayer().sendPackage(datagram, dataWithoutCurrent);
		}

		getNextLayer().sendPackage(datagram, dataWithoutCurrent);
		
	}

	@Override
	public void recivePackage(HeaderPackage datagram, ByteBuffer dataWithCurrent) {
		if(random.nextFloat()<=percent)return;
		getPrevLayer().recivePackage(datagram, dataWithCurrent);
	}

}
