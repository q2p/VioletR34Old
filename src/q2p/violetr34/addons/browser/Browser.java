package q2p.violetr34.addons.browser;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;
import q2p.violetr34.engine.URLArgument;

public final class Browser {
	public static final boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(request.getPath().equals("/browser"))
			return response.setData(ResponseCode._301_Moved_Permanently, ContentType.text_plain, "/browser/");
		
		if(!request.getPath().startsWith("/browser/"))
			return false;
		
		try {
			String url = request.getPath().substring("/browser".length());
			ArrayList<URLArgument> args = request.getArguments();
			if(!args.isEmpty()) {
				url+="?";
				boolean needAnd = false;
				for(final URLArgument arg : args) {
					if(needAnd)
						url += "&";
					else
						needAnd = true;
					url += arg.name+"="+arg.value;
				}
			}
			return response.setData(ResponseCode._200_OK, ContentType.text_html, getLastPonies(url));
		} catch(Exception e) {
			return response.setData(ResponseCode._500_Internal_Server_Error, ContentType.text_plain, "500 Something went wrong :(");
		}
	}

	private static String getLastPonies(final String url) throws Exception {
		System.out.println("url"+url);
		try {
			return requestData(url);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static String requestData(final String url) throws Exception {
		final Socket s = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9150))); // TOR
		s.connect(new InetSocketAddress("rule34.xxx" , 80));
		final OutputStream out = s.getOutputStream();
		final InputStream in = s.getInputStream();
		
		out.write(("GET "+url+" HTTP/1.1\r\n" +
				"Host: rule34.xxx\r\n" +
				"User-Agent: Mozilla/5.0 (Windows NT 6.1; rv:45.0) Gecko/20100101 Firefox/45.0\r\n" +
				"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
				"Accept-Language: en-US,en;q=0.5\r\n" +
				"DNT: 1\r\n" +
				"Connection: keep-alive\r\n" + 
				"Cache-Control: max-age=0\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		out.flush();
		
		String response = "";
		
		final byte[] buff = new byte[1024];
		int idx;
		while((idx = response.toLowerCase().indexOf("</html>")) == -1) {
			final int r = in.read(buff);
			if(r > 0)
				response += new String(buff, 0, r);
		}
						
		s.shutdownInput();
		s.shutdownOutput();
		s.close();
				
		return response.substring(response.toLowerCase().indexOf("<html"), idx+"</html>".length());
	}
}