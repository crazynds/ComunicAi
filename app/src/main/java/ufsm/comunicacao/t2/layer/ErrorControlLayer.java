package ufsm.comunicacao.t2.layer;

import java.nio.ByteBuffer;

import ufsm.comunicacao.t2.Variable;
import ufsm.comunicacao.t2.network.HeaderPackage;

public class ErrorControlLayer extends Layer {

	@Override
	public void sendPackage(HeaderPackage datagram, ByteBuffer buffer) {
		ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity()+4);
		newBuffer.putInt(0).put(buffer);
		int checksum = checksum(newBuffer);
		newBuffer.position(0);
		newBuffer.putInt(checksum);
		newBuffer.position(0);
		getNextLayer().sendPackage(datagram, newBuffer);
	}

	@Override
	public void recivePackage(HeaderPackage datagram, ByteBuffer buffer) {
		if(checksum(buffer)==0) {
			buffer.position(4);
			getPrevLayer().recivePackage(datagram, buffer.slice());
		}else {
			System.out.println("Dropou pacote inválido");
			Variable.errorFrames++;
		}
	}

	private static int checksum(ByteBuffer buff) {
		int checksum = 0;
		for(int x=0;x<=buff.capacity()-4;x+=4) {
			int s = buff.getInt(x);
			checksum+=s * (x+1);
		}
		return ~checksum;
	}
	
}
