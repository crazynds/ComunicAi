package ufsm.comunicacao.t2.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ufsm.comunicacao.t2.layer.EndLayer;
import ufsm.comunicacao.t2.layer.Layer;


public class NetworkManager implements Runnable {
	
	public static final int MAX_PACKAGE_SIZE=1200;
	
	
	private TreeMap<String, Comunication> comMap;
	
	private ExecutorService executor;
	private int port;
	private NetworkListener listener;
	
	private DatagramSocket socket;
	
	
	public NetworkManager(NetworkListener listener,int port) throws SocketException {
		this.port = port;
		this.comMap = new TreeMap<String, Comunication>();
		this.listener = listener;
		socket = new DatagramSocket(port);
		socket.setSoTimeout(this.port);
		executor = Executors.newCachedThreadPool();
		executor.submit(this);
	}
	
	public Comunication openComunication(InetAddress address, int port) {
		Comunication c= new Comunication(new EndLayer() {
			
			@Override
			public void sendPackage(HeaderPackage datagram, ByteBuffer dataWithoutCurrent) {
				ByteBuffer newByteBuffer = ByteBuffer.allocate(MAX_PACKAGE_SIZE);

				newByteBuffer.position(0);
				if(dataWithoutCurrent.capacity() >= MAX_PACKAGE_SIZE-2) {;
					newByteBuffer.putShort((short) (MAX_PACKAGE_SIZE-2));
					newByteBuffer.put(dataWithoutCurrent);
				}else {
					newByteBuffer.putShort((short) dataWithoutCurrent.capacity());
					newByteBuffer.put(dataWithoutCurrent);
				}
				
				DatagramPacket pack = new DatagramPacket(newByteBuffer.array(), newByteBuffer.capacity(), datagram.getAddress(),datagram.getPort());
				executor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							socket.send(pack);
						} catch (IOException e) {}
					}
				});
			}
			
			@Override
			public void recivePackage(HeaderPackage datagram, ByteBuffer dataWithCurrent) {
				short size = dataWithCurrent.getShort();
				if(size<=MAX_PACKAGE_SIZE-2) {
					ByteBuffer newBuff = ByteBuffer.allocate(size);
					Layer prevLayer = getPrevLayer();
					dataWithCurrent.limit(size+2);
					newBuff.put(dataWithCurrent);
					executor.submit(new Runnable() {
						@Override
						public void run() {
							newBuff.position(0);
							prevLayer.recivePackage(datagram, newBuff.asReadOnlyBuffer());
						}
					});
				}
			}
		},address, port);
		comMap.put(address.getHostAddress(), c);
		return c;
	}
	
	private void processaPacote(Comunication com, DatagramPacket datagram,ByteBuffer receiveData) {
		Layer l = com.getLastLayer();
		
		HeaderPackage header = new HeaderPackage();
		header.setAddress(datagram.getAddress());
		header.setPort(datagram.getPort());
		
		l.recivePackage(header, receiveData.asReadOnlyBuffer());
	}
	
	private void recebePacotes(ByteBuffer receiveData) throws IOException,SocketTimeoutException {
		DatagramPacket receivePacket = new DatagramPacket(receiveData.array(),
				receiveData.capacity());
		socket.receive(receivePacket);
		
		InetAddress IPAddress = receivePacket.getAddress();
		int port = receivePacket.getPort();
		
		Comunication c = comMap.get(IPAddress.getHostAddress());
		if(c==null) {
			c = openComunication(IPAddress, port);
			listener.onOpenConnection(c);
			comMap.put(IPAddress.getHostAddress(), c);
		}
		processaPacote(c, receivePacket,receiveData);
	}	
	

	@Override
	public void run() {
		try {;
			ByteBuffer  data = ByteBuffer.allocate(MAX_PACKAGE_SIZE);
			while (true) {
				try {
					recebePacotes(data);
				}catch(SocketTimeoutException ex) {};
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void remove(Comunication c) {
		String key = null;
		for (Map.Entry<String, Comunication> entry : comMap.entrySet()) {
			if(c == entry.getValue()) {
				key = entry.getKey();
				break;
			}
		}
		if(key!=null) {
			comMap.remove(key);
		}
	}
}
