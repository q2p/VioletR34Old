package q2p.violetr34.addons.tagger;

import java.util.ArrayList;
import java.util.LinkedList;
import q2p.violetr34.addons.tagger.collections.Collector;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;
import q2p.violetr34.addons.tagger.collections.exceptions.OverrideException;
import q2p.violetr34.addons.tagger.collections.exceptions.StringParsingException;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

public class Tagger {
	public static final byte MAX_UNIT_NAME_LENGTH = 40;
	public static final String ALLOWED_TAG_SYMS = "abcdefghijklmnopqrstuvwxyz_1234567890()/";
	public static final String SEPARATOR = " ";
	public static final String EXCLUSION = "-";
	
	public static void initilize() {
		Collector.initilize();
	}

	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(sendTagsList(request, response)) return true;
		
		if(deleteTag(request, response)) return true;

		if(renameTag(request, response)) return true;
		
		if(createTag(request, response)) return true;
		
		return false;
	}
	
	public static boolean allowedUnitNaming(final String name, final String filter) {
		if(name.length() > MAX_UNIT_NAME_LENGTH || name.length() < 1) return false;
		for(int i = 0; i < name.length(); i++) if(!filter.contains(""+name.charAt(i))) return false;
		return true;
	}

	private static boolean renameTag(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("taggerRenameTag")) return false;
		
		final LinkedList<String> lines = Assist.split(request.getBody(), "\n");
		if(lines.size() != 2)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		try {
			Collector.renameTag(lines.removeFirst(), lines.removeFirst());
		} catch (NotExistingTagException e) {
			return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, e.toRussian());
		} catch (OverrideException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, e.toRussian());
		} catch (StringParsingException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, e.toRussian());
		}
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}

	private static boolean createTag(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("taggerCreateTag")) return false;
		
		try { Collector.createTag(request.getBody()); }
		catch (OverrideException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, e.toRussian());
		} catch (StringParsingException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, e.toRussian());
		}
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}
	
	private static boolean deleteTag(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("taggerDeleteTag")) return false;
		
		try { Collector.removeTag(request.getBody()); }
		catch (NotExistingTagException e) {
			return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, e.toRussian());
		}
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}

	private static boolean sendTagsList(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("taggerTagsList")) return false;
		String ret = "[";
		for(int i = 0; i < Collector.tags.size(); i++) {
			if(i!=0) ret+=",";
			ret += "\""+Assist.JSONescape(Collector.tags.get(i).toString())+"\"";
		}
		return response.setData(ResponseCode._200_OK, ContentType.application_json, ret+"]");
	}
}