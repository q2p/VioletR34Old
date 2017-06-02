package q2p.violetr34.addons.tagger.collections.exceptions;

@SuppressWarnings("serial")
public class StringParsingException extends Exception {
	private String string;

	public StringParsingException(final String string) {
		super("Name contains unrecognizable symbols: \""+string+"\".", null, false, false);
		this.string = string;
	}
	
	public String toRussian() {
		return "Имя содержет не позволительные символы: \""+string+"\".";
	}
}