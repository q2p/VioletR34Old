package q2p.violetr34.addons.tagger.collections;

import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.exceptions.StringParsingException;

public class Tag implements Comparable {
	String name;
	
	Tag(String name) throws StringParsingException {
		if(!Tagger.allowedUnitNaming(name, Tagger.ALLOWED_TAG_SYMS)) throw new StringParsingException(name);
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public boolean same(Comparable as) {
		if(!(as instanceof Tag)) return false;
		return (Tag) as == this;
	}
}