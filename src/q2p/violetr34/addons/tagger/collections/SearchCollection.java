package q2p.violetr34.addons.tagger.collections;

import java.util.ArrayList;
import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;
import q2p.violetr34.engine.Assist;

public class SearchCollection {
	final ArrayList<SearchUnit> units = new ArrayList<SearchUnit>();
	
	public SearchCollection(final String text) {
		final ArrayList<String> parts = Assist.split(text, Tagger.SEPARATOR);
		
		while(!parts.isEmpty()) {
			try { units.add(new SearchUnit(parts.remove(0))); }
			catch (NotExistingTagException e) {}
		}
		
		Comparable.removeSame(units);
	}
	
	public boolean appliesTo(DescribersCollection to) {
		for(SearchUnit unit : units)
			if(unit.exclusion == to.contains(unit)) return false;
		return true;
	}
	
	public String toString() {
		String ret = "";
		for(int i = 0; i < units.size(); i++) {
			if(i != 0) ret += Tagger.SEPARATOR;
			ret += units.get(i);
		}
		return ret;
	}
}
