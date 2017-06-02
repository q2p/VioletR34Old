package q2p.violetr34.addons.tagger.collections;

import java.util.ArrayList;

public interface Comparable {
	public boolean same(Comparable as);
	
	public static void removeSame(ArrayList<? extends Comparable> list) {
		for(int i = 0; i < list.size()-1; i++)
			for(int j = i+1; j < list.size(); j++)
				if(list.get(i).same(list.get(j)))
					list.remove(j--);
	}
}