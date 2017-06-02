package q2p.violetr34.addons.tagger.collections.exceptions;

@SuppressWarnings("serial")
public class NotExistingTagException extends Exception {
	private String name;

	public NotExistingTagException(final String name) {
		super("Tag named \""+name+"\" not exists.", null, false, false);
		this.name = name;
	}
	
	public String toRussian() {
		return "Тэг с именем \""+name+"\" не существует.";
	}
}
