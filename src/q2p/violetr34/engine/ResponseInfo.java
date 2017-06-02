package q2p.violetr34.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseInfo {
	private ResponseCode code = ResponseCode._200_OK;
	private ContentType contentType = ContentType.text_plain;
	private String body = "";
	private final OutputStream out;
	private boolean wasFlushing = false;
	
	public ResponseInfo(final OutputStream out) {
		this.out = out;
	}

	public void setCode(final ResponseCode code) {
		this.code = code;
	}
	
	public void setContentType(final ContentType contentType) {
		this.contentType = contentType;
	}
	
	public void setBody(final String body) {
		this.body = body;
	}
	
	public void pushBody(final byte buff[]) throws IOException {
		wasFlushing = true;
		out.write(buff);
		out.flush();
	}
	
	public void flushHeader() throws IOException {
		wasFlushing = true;
		out.write(("HTTP/1.1 "+code.print+"\n"
		+ (code == ResponseCode._301_Moved_Permanently?"Location: "+body+"\n":"")
		+ "Content-Type: "+contentType.print+"\n"
		+ "Connection: close\n"
		+ "Pragma: no-cache\n\n").getBytes(StandardCharsets.UTF_8));
		out.flush();
	}
	
	public String getPrint() {
		if(wasFlushing) return null;
		return	"HTTP/1.1 "+code.print+"\n"
				+ (code == ResponseCode._301_Moved_Permanently?"Location: "+body+"\n":"")
				+ "Content-Type: "+contentType.print+"\n"
				+ "Connection: close\n"
				+ "Pragma: no-cache\n\n"
				+ body;
	}
	
	public boolean setData(final ResponseCode code, final ContentType contentType, final String body) {
		setCode(code);
		setContentType(contentType);
		setBody(body);
		return true;
	}
}
