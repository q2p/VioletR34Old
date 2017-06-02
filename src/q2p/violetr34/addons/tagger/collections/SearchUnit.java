package q2p.violetr34.addons.tagger.collections;

import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;

class SearchUnit implements Comparable {
	final Tag tag;
	final boolean exclusion;

	SearchUnit(String text) throws NotExistingTagException {
		if(exclusion = text.startsWith(Tagger.EXCLUSION)) text = text.substring(1);
				
		tag = Collector.findTag(text);
	}
	
	public String toString() {
		return (exclusion?Tagger.EXCLUSION:"")+tag;
	}
	
	public boolean same(Comparable as) {
		if(!(as instanceof SearchUnit)) return false;
		SearchUnit sa = (SearchUnit) as;
		return sa.tag == tag;
	}
}