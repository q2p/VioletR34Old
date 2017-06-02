package q2p.violetr34.addons.tagger.collections.exceptions;

@SuppressWarnings("serial")
public class OverrideException extends Exception {
	private String name;

	public OverrideException(final String name) {
		super("Tag named \""+name+"\" already exists.", null, false, false);
		this.name = name;
	}
	
	public String toRussian() {
		return "Тэг с именем \""+name+"\" уже существует.";
	}
}
