package q2p.violetr34.addons.tagger.collections;

import java.util.ArrayList;
import java.util.Collections;
import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;
import q2p.violetr34.engine.Assist;

public class DescribersCollection {
	private final ArrayList<Tag> tags = new ArrayList<Tag>();
	
	public DescribersCollection(final String text) throws NotExistingTagException {
		final ArrayList<String> parts = Assist.split(text, Tagger.SEPARATOR);
		
		Collections.sort(parts);
		
		final ArrayList<Tag> newTags = new ArrayList<Tag>();
		
		String next;
		
		while(!parts.isEmpty()){
			if(!(next = parts.remove(0)).equals("")) newTags.add(Collector.findTag(next)); 
		}
		
		Comparable.removeSame(newTags);
		
		tags.addAll(newTags);
	}
	
	public String toString() {
		String ret = "";
		for(int i = 0; i < tags.size(); i++) {
			if(i != 0) ret += Tagger.SEPARATOR;
			ret += tags.get(i).toString();
		}
		return ret;
	}

	public boolean contains(final SearchUnit unit) {
		for(final Tag tag : tags)
			if(unit.tag == tag) return true;
		return false;
	}
}