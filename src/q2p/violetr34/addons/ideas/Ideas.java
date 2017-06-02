package q2p.violetr34.addons.ideas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

/*
int - кол-во постов(не заметок)
{
	int - длина названия
	StrBt - название
	int - длина текста
	StrBt - текст
	int - длина todo
	StrBt - todo
	int - кол-во якорей
	{
		int - якорь
	}
	int - кол-во картинок
	{
		int - длина картинки
		StrBt - картинка
	}
	int - кол-во аудио
	{
		int - длина аудио
		StrBt - аудио
	}
	int - кол-во видио
	{
		int - длина видио
		StrBt - видио
	}
	int - кол-во ссылок
	{
		int - длина ссылки
		StrBt - ссылка
	}
	int - кол-во файлов
	{
		int - длина файла
		StrBt - файл
	}
}
*/

// TODO: Файл не найден, найти замену
// TODO: Редизайн ВСЕГО!
// TODO: в идеях(32-ой пост), дескрипторе и соннике надо убрать отображение html кода
// a i a v l f
public class Ideas {
	private static final String IDEAS_FILE_NAME = "ideas/ideas";
	
	private static final String NWS_DIR = Assist.MAIN_FOLDER + "nws/";
	private static final String ALL_DIR = Assist.MAIN_FOLDER + "all/";

	private static final String CANT_READ_IDEAS = "Can't read from ideas file.\n";
	
	private static final byte IDEAS_PER_PAGE = 50;
	
	// TODO: оптимизировать код, использовать новые функции т.к. Assist.abort(), рефакториг с использованием final
	// TODO: вернуть создание бэкапов, но делать их после изменения 5 идей или через день если изменений не было
	
	private static boolean sendIdeas(final RequestInfo request, final ResponseInfo response) {
 		if(!request.getPath().equals("ideasData")) return false;
		
		final Integer page;
		if((page = Assist.getIntegerFromArgument(request, "page")) == null)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final int firstId = IDEAS_PER_PAGE*(page-1)+1;
		if(firstId < 1)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		
		try {
			final int postsAmount = dis.readInt();
			final int notesAmount = dis.readInt();
			
			if(firstId > (postsAmount + notesAmount)) {
				dis.close();
				return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			}
			
			final int lastId = Math.min(firstId+IDEAS_PER_PAGE, postsAmount+notesAmount+1); // Не включительно
			
			int id = 1;
			
			while(dis.available() > 0) {
				if(id == firstId)
					break;
				skipPost(dis);
				id++;
			}
			
			if(dis.available() <= 0) {
				dis.close();
				return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "");
			}
			
			String ret = "[["+firstId+","+(int)Math.ceil((float)(postsAmount+notesAmount)/(float)IDEAS_PER_PAGE)+"],[";
			if(firstId <= postsAmount) {
				final int tend = Math.min(postsAmount+1, lastId);
				for(int bid = firstId; bid != tend; bid++) {
					
					ret += "\""+Assist.JSONescape(getOnlyTitle(dis))+"\"";
					
					if(tend-1 != bid) ret += ",";
				}
			}
			ret += "],[";
			if(lastId > postsAmount) {
				final int tbeg = Math.max(postsAmount+1, firstId);
				for(int bid = tbeg; bid < lastId; bid++) {
					ret += "\""+Assist.JSONescape(getOnlyTitle(dis))+"\"";
					if(lastId-1 != bid) ret += ",";
				}
			}
			dis.close();
			return response.setData(ResponseCode._200_OK, ContentType.application_json, ret+"]]");
		} catch (IOException e) {
			e.printStackTrace();
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
			return true;
		}
	}
 	
	private static boolean sendIdea(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideaData")) return false;
		
		final Integer bid;
		if((bid = Assist.getIntegerFromArgument(request, "id")) == null) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}

		if(bid < 1)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		
		try {
			final int postsAmount = dis.readInt();
			final int notesAmount = dis.readInt();
			
			if(bid > (postsAmount + notesAmount)) {
				dis.close();
				return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			}
			
			int id = 1;
			
			while(dis.available() > 0) {
				if(id == bid) break;
				skipPost(dis);
				id++;
			}
			
			String ret = "["+bid+",\""+((bid <= postsAmount)?"post":"note")+"\",\"";
			ret += Assist.JSONescape(Assist.readString(dis))+"\",\"";
			ret += Assist.JSONescape(Assist.readString(dis))+"\",\"";
			ret += Assist.JSONescape(Assist.readString(dis))+"\",[";
			
			for(int i = dis.readInt(); i != 0; i--) {
				ret += dis.readInt();
				if(i != 1) ret += ",";
			}
			
			ret += "],[";
			
			ret += JSONStrings(dis)+"],["; // images
			ret += JSONStrings(dis)+"],["; // audios
			ret += JSONStrings(dis)+"],["; // videos
			ret += JSONStrings(dis)+"],["; // links
			ret += JSONStrings(dis)+"]]"; // files
			
			dis.close();
			return response.setData(ResponseCode._200_OK, ContentType.application_json, ret);
		} catch (IOException e) {
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
			return true;
		}
	}
	
	private static boolean saveIdea(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideaSave")) return false;
		final ArrayList<String> dataStrings = Assist.splitLines(request.getBody());
		if(dataStrings.size() != 10)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final int bid;
		try { bid = Integer.parseInt(dataStrings.remove(0)); }
		catch(NumberFormatException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		}
		
		if(bid < 1)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		
		try {
			final int postsAmount = dis.readInt();
			final int notesAmount = dis.readInt();
			
			if(bid > (postsAmount + notesAmount)) {
				dis.close();
				return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			}
			
			final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(IDEAS_FILE_NAME));
			
			dos.writeInt(postsAmount);
			dos.writeInt(notesAmount);
			
			for(int id = 1; id != bid; id++)
				copyPost(dis, dos);
			
			skipPost(dis);

			Assist.writeString(dos, dataStrings.remove(0)); // title
			Assist.writeString(dos, dataStrings.remove(0)); // text
			Assist.writeString(dos, dataStrings.remove(0)); // todo

			final LinkedList<String> anchors = Assist.split(dataStrings.remove(0), "\n");
			final ArrayList<Integer> anchorsIntegers = new ArrayList<Integer>();
			while(!anchors.isEmpty()) {
				try { anchorsIntegers.add(Integer.parseInt(anchors.remove(0))); }
				catch(NumberFormatException e) {}
			}
			dos.writeInt(anchorsIntegers.size());
			while(!anchorsIntegers.isEmpty())
				dos.writeInt(anchorsIntegers.remove(0));
			
			writeNormalizedUrls(dos, dataStrings.remove(0)); // images
			writeNormalizedUrls(dos, dataStrings.remove(0)); // audios
			writeNormalizedUrls(dos, dataStrings.remove(0)); // videos
			writeNormalizedUrls(dos, dataStrings.remove(0)); // links
			writeNormalizedUrls(dos, dataStrings.remove(0)); // files
			dos.flush();
			
			while(dis.available() > 0)
				copyPost(dis, dos);

			dos.flush();
			dis.close();
			dos.close();
		} catch (IOException e) {
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(IDEAS_FILE_NAME);
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}
	
	private static boolean createNote(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideaCreateNote")) return false;

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(IDEAS_FILE_NAME));
		
		try {
			dos.writeInt(dis.readInt()); // posts
			dos.writeInt(dis.readInt()+1); // notes

			while(dis.available() > 0)
				copyPost(dis, dos);

			Assist.writeString(dos, "Новая заметка"); // title
			Assist.writeString(dos, ""); // text
			Assist.writeString(dos, ""); // todo

			dos.writeInt(0); // anchors
			Assist.writeString(dos, ""); // images
			Assist.writeString(dos, ""); // audios
			Assist.writeString(dos, ""); // videos
			Assist.writeString(dos, ""); // links
			Assist.writeString(dos, ""); // files
			
			dos.flush();
			dos.close();
			dis.close();
		} catch (IOException e) {
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(IDEAS_FILE_NAME);
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}
	
	private static boolean removeIdea(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("ideaRemove")) return false;

		final Integer bid;
		if((bid = Assist.getIntegerFromArgument(request, "id")) == null) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true; 
		}
		
		if(bid < 1)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		
		try {
			int posts = dis.readInt();
			int notes = dis.readInt();
			
			if(bid > (posts + notes)) {
				dis.close();
				return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			}
			
			if(bid <= posts) posts--;
			else notes--;
			
			final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(IDEAS_FILE_NAME));
			
			dos.writeInt(posts);
			dos.writeInt(notes);
			
			for(int id = 1; id != bid; id++)
				copyPostAndFixAnchorsFromRemoving(dis, dos, bid);
			
			skipPost(dis);
			
			while(dis.available() > 0)
				copyPostAndFixAnchorsFromRemoving(dis, dos, bid);
			
			dis.close();
			dos.flush();
			dos.close();
		} catch(IOException e) {
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
			return true;
		}

		Assist.swapTwrAndDatFiles(IDEAS_FILE_NAME);
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}

	private static boolean moveNoteToPosts(final RequestInfo request, final ResponseInfo response) {
		if(request.getBody() != "ideaMoveToPosts") return false;
		
		final Integer bid;
		if((bid = Assist.getIntegerFromArgument(request, "id")) == null) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		if(bid < 1)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");

		DataInputStream dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
		
		try {
			int posts = dis.readInt();
			
			if(bid > posts) {
				dis.close();
				return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			}
			
			final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(IDEAS_FILE_NAME));
			
			dos.writeInt(++posts);
			dos.writeInt(dis.readInt()-1);
			
			for(int id = 1; id != posts-1; id++)
				copyPostAndFixAnchorsFromReplacing(dis, dos, bid, posts);
			
			dis.close();
			dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
			dis.readInt();
			dis.readInt();
			
			for(int id = 1; id != bid; id++)
				skipPost(dis);
			
			copyPostAndFixAnchorsFromReplacing(dis, dos, bid, posts);
			
			dis.close();
			dis = new DataInputStream(Assist.getDatStream(IDEAS_FILE_NAME));
			dis.readInt();
			dis.readInt();
			
			int id = 1;
			for(;id != posts-1; id++)
				skipPost(dis);
			
			for(;id != bid; id++)
				copyPostAndFixAnchorsFromReplacing(dis, dos, bid, posts);
			
			skipPost(dis);
			
			while(dis.available() > 0)
				copyPostAndFixAnchorsFromReplacing(dis, dos, bid, posts);
			
			dis.close();
			dos.flush();
			dos.close();

			Assist.swapTwrAndDatFiles(IDEAS_FILE_NAME);
			return response.setData(ResponseCode._200_OK, ContentType.text_plain, ""+posts);
		} catch(IOException e) {
			Assist.abort(CANT_READ_IDEAS+e.getMessage());
			return true;
		}
	}

	// Вспомогательные функции
 	private static String getOnlyTitle(final DataInputStream dis) throws IOException {
 		final String title = Assist.readString(dis);
 		
		Assist.skipString(dis); // text
		Assist.skipString(dis); // todo
		
		for(int i = dis.readInt(); i != 0; i--)
			dis.readInt();

		Assist.skipStrings(dis); // images
		Assist.skipStrings(dis); // audios
		Assist.skipStrings(dis); // videos
		Assist.skipStrings(dis); // links
		Assist.skipStrings(dis); // files
		
		return title;
 	}
 	private static void skipPost(final DataInputStream dis) throws IOException {
		Assist.skipString(dis); // title
		Assist.skipString(dis); // text
		Assist.skipString(dis); // todo
		
		for(int i = dis.readInt(); i != 0; i--)
			dis.readInt(); // anchors

		Assist.skipStrings(dis); // images
		Assist.skipStrings(dis); // audios
		Assist.skipStrings(dis); // videos
		Assist.skipStrings(dis); // links
		Assist.skipStrings(dis); // files
 	}
	private static void copyPost(final DataInputStream dis, final DataOutputStream dos) throws IOException {
		Assist.writeString(dos, Assist.readString(dis)); // title
		Assist.writeString(dos, Assist.readString(dis)); // text
		Assist.writeString(dos, Assist.readString(dis)); // todo
		
		int i = dis.readInt();
		dos.writeInt(i);
		for(;i != 0; i--)
			dos.writeInt(dis.readInt()); // anchors
		
		Assist.copyStrings(dis, dos); // images
		Assist.copyStrings(dis, dos); // audios
		Assist.copyStrings(dis, dos); // videos
		Assist.copyStrings(dis, dos); // links
		Assist.copyStrings(dis, dos); // files
		dos.flush();
	}
	private static void copyPostAndFixAnchorsFromRemoving(final DataInputStream dis, final DataOutputStream dos, final int removedId) throws IOException {
		Assist.writeString(dos, Assist.readString(dis)); // title
		Assist.writeString(dos, Assist.readString(dis)); // text
		Assist.writeString(dos, Assist.readString(dis)); // todo
		
		final ArrayList<Integer> anchors = new ArrayList<Integer>();
		for(int i = dis.readInt(); i != 0; i--) {
			int t = dis.readInt();
			
			if(t == removedId)
				continue;
			if(t > removedId)
				anchors.add(t - 1);
			else
				anchors.add(t);
		}
		
		dos.writeInt(anchors.size());
		while(!anchors.isEmpty())
			dos.writeInt(anchors.remove(0)); // anchors
		
		Assist.copyStrings(dis, dos); // images
		Assist.copyStrings(dis, dos); // audios
		Assist.copyStrings(dis, dos); // videos
		Assist.copyStrings(dis, dos); // links
		Assist.copyStrings(dis, dos); // files
		dos.flush();
	}
	private static void copyPostAndFixAnchorsFromReplacing(final DataInputStream dis, final DataOutputStream dos, final int oldId, final int newId) throws IOException {
		Assist.writeString(dos, Assist.readString(dis)); // title
		Assist.writeString(dos, Assist.readString(dis)); // text
		Assist.writeString(dos, Assist.readString(dis)); // todo
		
		int i = dis.readInt();
		dos.writeInt(i);
		for(; i != 0; i--) {
			int t = dis.readInt();
			
			if(t == oldId)
				dos.writeInt(newId);
			else if(t > newId-1 && t < oldId)
				dos.writeInt(t+1);
			else
				dos.writeInt(t);
		}
		
		Assist.copyStrings(dis, dos); // images
		Assist.copyStrings(dis, dos); // audios
		Assist.copyStrings(dis, dos); // videos
		Assist.copyStrings(dis, dos); // links
		Assist.copyStrings(dis, dos); // files
		dos.flush();
	}
	private static String JSONStrings(final DataInputStream dis) throws IOException {
		String json = "";
		for(int i = dis.readInt(); i != 0; i--) {
			json += "\""+Assist.JSONescape(Assist.readString(dis))+"\"";
			if(i != 1) json += ",";
		}
		return json;
	}
	private static void writeNormalizedUrls(final DataOutputStream dos, final String urls) throws IOException {
		if(urls.trim().equals("")) {
			dos.writeInt(0);
			return;
		}
		final LinkedList<String> list = Assist.split(urls, "\n");
		dos.writeInt(list.size());
		while(!list.isEmpty())
			Assist.writeString(dos, Assist.normalizeURL(list.remove(0)));
	}
	
	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(sendIdeas(request, response)) return true;
		if(sendIdea(request, response)) return true;
		if(saveIdea(request, response)) return true;
		if(createNote(request, response)) return true;
		if(removeIdea(request, response)) return true;
		if(moveNoteToPosts(request, response)) return true;
		return false;
	}
}