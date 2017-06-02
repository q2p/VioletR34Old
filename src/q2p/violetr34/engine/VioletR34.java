package q2p.violetr34.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import q2p.violetr34.addons.Initnilizer;

public final class VioletR34 {
	// TODO: Пробежаться по проекту и попробовать оптимизировать забытые районы кода
	public static ServerSocket serverSocket;
	private static final ProcessRequest PROCESS_REQUEST = new ProcessRequest();
	private static Thread thread;
	public static final int MAX_BUFF = 256*1024*1024;
	
	public static final void main(final String[] args) {
		try { serverSocket = new ServerSocket(3434, 0, InetAddress.getLoopbackAddress()); }
		catch (Exception e) { Assist.abort("Can't open a socket.\n" +e.getMessage()); }
		
		Initnilizer.initilize();
		
		System.out.println("Server started. You can access it by \""+serverSocket.getInetAddress().getHostAddress()+":"+serverSocket.getLocalPort()+"\".");
		
		System.gc();
		
		while(true) {
			System.gc();
			try {
				ProcessRequest.socket = serverSocket.accept();
				ProcessRequest.socket.setSendBufferSize(2*MAX_BUFF);
			} catch (IOException e) { continue; }
			thread = new Thread(PROCESS_REQUEST);
			thread.start();
			try { thread.join(); }
			catch (final InterruptedException e) {}
		}
	}
}