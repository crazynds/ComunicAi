package ufsm.comunicacao.t2.network;

import java.net.InetAddress;

public class HeaderPackage {

	private InetAddress address;
	private int port;
	private long sendTime;
	
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getSendTime() {
		return sendTime;
	}
	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}
	
	
	

}
