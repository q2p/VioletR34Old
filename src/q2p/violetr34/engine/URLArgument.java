package q2p.violetr34.engine;

public class URLArgument {
	public String name = null;
	public String value = null;
	
	public URLArgument(final String argument) {
		if(!argument.contains("=")) return;
		String[] s = argument.split("=");
		
		if(s.length != 2) return;
		
		name = s[0];
		value = s[1];
	}
}