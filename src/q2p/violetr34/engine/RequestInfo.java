package q2p.violetr34.engine;

import java.net.URLDecoder;
import java.util.ArrayList;

public final class RequestInfo {
	private String path = null;
	private final ArrayList<URLArgument> arguments = new ArrayList<URLArgument>();
	private String body = "";
	
	public final String getBody() {
		return body;
	}
	
	public final String getPath() {
		return path;
	}
	
	public String getArgument(final String name) {
		for(URLArgument arg : arguments) if(arg.name.equals(name)) return arg.value;
		return null;
	}
	
	public final ArrayList<URLArgument> getArguments() {
		return arguments;
	}
	
	RequestInfo(final String httpHeader) {
		if(!parsePath(httpHeader))
			return;
		parseArguments();
		parseBody(httpHeader);
	}

	private final void parseBody(final String str) {
		int index = str.replace("\r", "").indexOf("\n\n");
		if(index != -1)
			body = str.substring(index+2, str.length());
	}

	private final boolean parsePath(final String str) {
		final int bidx = str.indexOf(' ')+1;
		
		if(bidx == 0)
			return false;
		
		final int ridx = str.indexOf('\r');
		final int nidx = str.indexOf('\n');
		
		int eidx;
		if(ridx == -1)
			eidx = nidx;
		else if(nidx == -1)
			eidx = nidx;
		else
			eidx = Math.min(ridx, nidx);

		if(eidx == -1)
			return false;
		
		final int sidx = str.substring(bidx, eidx).lastIndexOf(' ');
		
		if(sidx == -1)
			return false;
		
		try {
			path = URLDecoder.decode(str.substring(bidx, bidx+sidx).replaceAll("\\+", "%2B"), "UTF-8");
		} catch (final Exception e) {
			return false;
		}
		
		if(path.startsWith("/"))
			path = path.substring(1);
		
		return true;
	}
	
	private final void parseArguments() {
		// TODO: разобрать
		if(!path.contains("?"))
			return;
		String[] args = path.split("\\?");
		path = args[0];
		final String arg = args[1];
		
		args = arg.contains("&")?arg.split("&"):new String[]{arg};
		
		for(int i = 0; i < args.length; i++) {
			final URLArgument a = new URLArgument(args[i]);
			if(a.name != null) arguments.add(a);
		}
	}
}
