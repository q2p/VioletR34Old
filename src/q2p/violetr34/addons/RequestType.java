package q2p.violetr34.addons;

import q2p.violetr34.addons.browser.Browser;
import q2p.violetr34.addons.descriptor.Descriptor;
import q2p.violetr34.addons.errors.Errors;
import q2p.violetr34.addons.ideas.Ideas;
import q2p.violetr34.addons.resources.Resources;
import q2p.violetr34.addons.sleeper.Sleeper;
import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.FileLoader;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

public class RequestType {
	private static final String[][] REDIRECTIONS = new String[][] {
		{"", "s/home/home.html"},
		{"updates", "s/updates/updates.html"},
		{"ideas", "s/ideas/ideas.html"},
		{"idea", "s/idea/idea.html"},
		{"tags", "s/tags/tags.html"},
		{"descriptor", "s/descriptor/descriptor.html"},
		{"tags", "s/tags/tags.html"},
		{"sleeper", "s/sleeper/sleeper.html"},
		{"errors", "s/errors/errors.html"}
	};
	
	public static void decide(RequestInfo request, ResponseInfo response) {
		if(Resources.requestCheck(request, response)) return;
		if(Ideas.requestCheck(request, response)) return;
		if(Tagger.requestCheck(request, response)) return;
		if(Descriptor.requestCheck(request, response)) return;
		if(Sleeper.requestCheck(request, response)) return;
		if(Errors.requestCheck(request, response)) return;
		if(Browser.requestCheck(request, response)) return;
		if(miscCheck(request, response)) return;
		sendFile(request, response);
	}

	private static boolean miscCheck(final RequestInfo request, final ResponseInfo response) {
		if(request.getPath().equals("localPath"))
			return response.setData(ResponseCode._200_OK, ContentType.text_plain, Assist.MAIN_FOLDER);
		return false;
	}
	
	private static void sendFile(final RequestInfo request, final ResponseInfo response) {
		String path = request.getPath();
		
		for(byte i = 0; i < REDIRECTIONS.length; i++) {
			if(REDIRECTIONS[i][0].equals(path)) {
				path = REDIRECTIONS[i][1];
				break;
			}
		}
		
		FileLoader.send(path, request, response);
	}
}