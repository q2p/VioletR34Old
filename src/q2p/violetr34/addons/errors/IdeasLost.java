package q2p.violetr34.addons.errors;

import java.util.ArrayList;

public class IdeasLost {
	String path;
	ArrayList<String> replacements = new ArrayList<String>();
	ArrayList<Integer> meets = new ArrayList<Integer>();

	public IdeasLost(String path, int id) {
		this.path = path;
		meets.add(id);
	}
}
