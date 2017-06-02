package q2p.violetr34.addons.errors;

import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseInfo;

public class Errors {
	/* TODO
	Ошибки:
		В дескрипторе не валидный тэг
		В дескрипторе одинаковые файлы (скрестить их)
		В дескрипторе/идеях не найденный файл
	*/
	/*private static void replace(final ArrayList<String> original, final ArrayList<String> replace) {
		original.clear();
		String s;
		while(!replace.isEmpty())
			if(!(s = Assist.normalizeURL(replace.remove(0)).trim()).equals("")) original.add(s);
	}
	
	private static boolean sendErrors(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideasErrors")) return false;
		
		final ArrayList<IdeasLost> losts = new ArrayList<IdeasLost>();
		
		Idea idea;
		
		for(iterInit(); iterHave();) {
			idea = iterNext();
			checkLosts(idea.images, losts, iterBid);
			checkLosts(idea.audios, losts, iterBid);
			checkLosts(idea.videos, losts, iterBid);
			checkLosts(idea.files, losts, iterBid);
		}
		
		//TODO: сделать утилиту для лёгкого превращения текста в массив json
		
		String ret = "[";
		while(!losts.isEmpty()) {
			IdeasLost l = losts.remove(0);
			ret+="[\""+Assist.JSONescape(l.path)+"\",[";
			l.replacements = searchForInMediaDirectories(l.path);
			while(!l.replacements.isEmpty()) {
				ret += "\""+Assist.JSONescape(l.replacements.remove(0))+"\"";
				if(l.replacements.size() != 0) ret+=",";
			}
			ret+="],[";
			while(!l.meets.isEmpty()) {
				ret += l.meets.remove(0);
				if(l.meets.size() != 0) ret+=",";
			}
			ret+="]]";
			if(losts.size() != 0) ret += ",";
		}
		
		response.setData(ResponseCode._200_OK, ContentType.application_json, ret+"]");
		return true;
	}

	//TODO: превратить в одну функцию
	private static void checkLosts(ArrayList<String> paths, ArrayList<IdeasLost> losts, int bid) {
		for(String path : paths) checkLosts(path, losts, bid);
	}

	private static void checkLosts(String path, ArrayList<IdeasLost> losts, int bid) {
		if(new File(Assist.MAIN_FOLDER + path).exists()) return;
		if(path.endsWith("/")) path = path.substring(0, path.length()-1);
		if(path.contains("/")) path = path.substring(path.lastIndexOf("/")+1, path.length());
		boolean found = false;
		for(IdeasLost l : losts) {
			if(l.path.equals(path)) {
				l.meets.add(bid);
				found = true;
				break;
			}
		}
		if(!found) losts.add(new IdeasLost(path, bid));
	}

	private static ArrayList<String> searchForInMediaDirectories(String path) {
		if(path.contains("/")) path = path.substring(path.lastIndexOf("/")+1, path.length());
		final ArrayList<String> found = new ArrayList<String>();
		String ret = checkInDir(ALL_DIR, path);
		if(ret != null) found.add(ret);
		final ArrayList<String> nws = new ArrayList<String>(Arrays.asList(new File(NWS_DIR).list()));
		String nw;
		while (!nws.isEmpty()) {
			nw = nws.remove(0);
			if(!nw.startsWith("nw")) continue;
			try { Integer.parseInt(nw.substring(2)); }
			catch(NumberFormatException e) { continue; }

			if((ret = checkInDir(NWS_DIR + nw + "/", path)) != null) found.add(ret);
		}
		return found;
	}
	
	private static String checkInDir(final String path, final String need) {
		final File f = new File(path+need);
		if(f.exists()) return Assist.normalizeURL(f.getAbsolutePath().substring(Assist.MAIN_FOLDER.length()));
		return null;
	}

	private static boolean replaceFiles(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideasFixError")) return false;
		
		final String old;
		final String replace;
		
		try {
			old = request.getBody().substring(0, request.getBody().indexOf(":"));
			replace = Assist.normalizeURL(request.getBody().substring(request.getBody().indexOf(":")+1, request.getBody().length()));
		} catch(IndexOutOfBoundsException e) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, request.getBody());
			return true;
		}
		
		if (!(new File(Assist.MAIN_FOLDER + replace).exists())) {
			response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, request.getBody());
			return true;
		}

		for(iterInit(); iterHave();) {
			final Idea idea = iterNext();

			replaceInStrings(idea.images, old, replace);
			replaceInStrings(idea.audios, old, replace);
			replaceInStrings(idea.videos, old, replace);
			replaceInStrings(idea.links, old, replace);
			replaceInStrings(idea.files, old, replace);
		}

		saveIdeas();
		
		response.setData(ResponseCode._200_OK, ContentType.text_plain, old);
		return true;
	}

	private static void replaceInStrings(final ArrayList<String> strings, final String old, final String replace) {
		for(int i = 0; i < strings.size(); i++) if(strings.get(i).equals(old)) strings.set(i, replace);
	}
	*/

	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		/*if(sendErrors(request, response)) return true;
		if(replaceFiles(request, response)) return true;*/
		return false;
	}
}