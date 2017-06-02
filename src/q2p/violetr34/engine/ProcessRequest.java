package q2p.violetr34.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import q2p.violetr34.addons.RequestType;

public class ProcessRequest implements Runnable{
	private static InputStream in;
	private static OutputStream out;
	public static Socket socket;
	private static RequestInfo request;
	private static ResponseInfo response;
	
	public void run() {
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
			response = new ResponseInfo(out);

			String httpHeader = "";
			// чтение заголовка HTTP
			byte[] buffer = new byte[8*1024*1024];
			int pointer = 0;
			int left = buffer.length;
			while(in.available() > 0 && left > 0) {
				int o = in.read(buffer, pointer, Math.min(left, in.available()));
				pointer += o;
				left -= o;
			}
			
			if(pointer <= 0) {
				out.close();
				socket.close();
				return;
			}
			
			// преобразование полученных байт в строку
			httpHeader = new String(buffer, 0, pointer, StandardCharsets.UTF_8);
			buffer = null;
			
			request = new RequestInfo(httpHeader);

			if(request.getPath() == null) {
				response.setData(ResponseCode._400_Bad_Request, ContentType.text_plain, "");
				
				out.write(response.getPrint().getBytes(StandardCharsets.UTF_8));
				out.flush();
				out.close();
				socket.close();
				return;
			}
			
			RequestType.decide(request, response);
			
			httpHeader = response.getPrint();
			if(httpHeader != null) out.write(httpHeader.getBytes(StandardCharsets.UTF_8));

			if(socket != null && !socket.isClosed()) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			try { out.flush(); } catch (IOException e1) {}
			Assist.tryToClose(in, out);
			try { socket.close(); } catch(IOException e1) {}
		}
	}
}