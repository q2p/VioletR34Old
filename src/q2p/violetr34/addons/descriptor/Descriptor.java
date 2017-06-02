package q2p.violetr34.addons.descriptor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.DescribersCollection;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

/*
Struct:
String name
boolean wasChecked?
String text
String todo
String tags
*/

public class Descriptor {
	private static final String NWS = Assist.MAIN_FOLDER + "nws/";
	private static final String ALL = Assist.MAIN_FOLDER + "all/";
	private static final Random r = new Random();
	public static final String DESCRIPTOR_FILE_NAME = "descriptor/descriptor";

	/* TODO:
	Кнопка "создать идею"?
	Показывать идеи в которых использован файл
	Если используются не правильные тэги, удалить их и уведомлять об их удалении
	*/
	
	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(descriptorRandom(request, response)) return true;
		if(descriptorByName(request, response)) return true;
		if(descriptorSave(request, response)) return true;
		return false;
	}
	
	private static boolean descriptorRandom(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("descriptorRandom")) return false;
		
		String path = getRandomFile();
		if(path == null) {
			updateWaves();
			path = getRandomFile();
		}
		
		if(path == null) return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "Нет файлов для поиска."); // TODO: смайл в коды: \\uXXXX
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, Assist.JSONescape(path));
	}
	
	private static boolean descriptorByName(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("descriptorGetInfo")) return false; 
		
		final String path = request.getBody();
		String name;
		
		if(!(new File(Assist.MAIN_FOLDER+path).exists())) return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "Файл \""+path+"\" не найден.");

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(DESCRIPTOR_FILE_NAME));
		try {
			while(dis.available() > 0) {
				name = Assist.readString(dis);
				dis.readByte();
				if(!name.equals(path)) {
					Assist.skipString(dis);
					Assist.skipString(dis);
					Assist.skipString(dis);
				} else {
					String ret = "[\""+Assist.JSONescape(path)+"\",\""+ContentType.getByExtention(Assist.getPathExtention(path)).html5+"\",\""+Assist.JSONescape(Assist.readString(dis))+"\",\""+Assist.JSONescape(Assist.readString(dis))+"\",[";
					final String tagsRaw = Assist.readString(dis);
					if(!tagsRaw.equals("")) {
						LinkedList<String> tags = Assist.split(tagsRaw, Tagger.SEPARATOR);
						dis.close();
						for(boolean i = false; !tags.isEmpty(); i = true) {
							if(i) ret += ",";
							ret += "\""+Assist.JSONescape(tags.removeFirst())+"\"";
						}
					}
					return response.setData(ResponseCode._200_OK, ContentType.application_json, ret+"]]");
				}
			}
			dis.close();
		} catch (IOException e) {
			Assist.abort("Can't read descriptions from \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+".dat\".\n"+e.getMessage());
		}

		return response.setData(ResponseCode._200_OK, ContentType.application_json, 
			"[\""+Assist.JSONescape(path)+"\",\""+ContentType.getByExtention(Assist.getPathExtention(path)).html5+"\",\"\",\"\",[]]"
		);
	}

	private static boolean descriptorSave(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("descriptorSave")) return false;

		final ArrayList<String> lines = Assist.splitLines(request.getBody());
		
		if(lines.size() != 4) return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		
		final String path = lines.remove(0);
		if(!(new File(Assist.MAIN_FOLDER+path).exists())) return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "Файл \""+path+"\" не найден.");

		final String text = lines.remove(0);
		final String todo = lines.remove(0);
		final String tags;
		try {
			tags = new DescribersCollection(lines.remove(0)).toString();
		} catch (NotExistingTagException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Не допустимые тэги.");
		}

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(DESCRIPTOR_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(DESCRIPTOR_FILE_NAME));
		
		String name;
		
		try {
			while(dis.available() > 0) {
				name = Assist.readString(dis);
				if(!name.equals(path)) {
					Assist.writeString(dos, name);
					dos.writeByte(dis.readByte());
					Assist.writeString(dos, Assist.readString(dis));
					Assist.writeString(dos, Assist.readString(dis));
					Assist.writeString(dos, Assist.readString(dis));
					dos.flush();
				} else {
					dis.readByte();
					Assist.skipString(dis);
					Assist.skipString(dis);
					Assist.skipString(dis);
					
					break;
				}
			}
			
			Assist.writeString(dos, path);
			dos.writeByte(1);
			Assist.writeString(dos, text);
			Assist.writeString(dos, todo);
			Assist.writeString(dos, tags);
			dos.flush();
			
			while(dis.available() > 0) {
				Assist.writeString(dos, Assist.readString(dis));
				dos.writeByte(dis.readByte());
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				dos.flush();
			}
			dis.close();
			dos.flush();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Can't read or write descriptions from \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+".twr\" to \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		Assist.swapTwrAndDatFiles(DESCRIPTOR_FILE_NAME);
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, ""); 
	}
	
	private static void updateWaves() {
		try {
			final DataInputStream dis = new DataInputStream(Assist.getDatStream(DESCRIPTOR_FILE_NAME));
			final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(DESCRIPTOR_FILE_NAME));
			
			while(dis.available() > 0) {
				Assist.writeString(dos, Assist.readString(dis));
				dis.readByte();
				dos.writeByte(0);
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				dos.flush();
			}

			dis.close();
			dos.flush();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Can't read or write descriptions from \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+".twr\" to \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		Assist.swapTwrAndDatFiles(DESCRIPTOR_FILE_NAME);
	}

	private static String getRandomFile() {
		final ArrayList<String> nwsList = new ArrayList<String>(Arrays.asList(new File(NWS).list()));
		final ArrayList<String> nwsListSorted = new ArrayList<String>();

		for(int i = 0; i != nwsList.size(); i++) {
			String nw = nwsList.get(i);
			if(!nw.startsWith("nw") || !(new File(NWS+nw).isDirectory())) {
				nwsList.remove(i--);
				continue;
			}
			try { Integer.parseInt(nw.substring(2)); }
			catch(NumberFormatException e) {
				nwsList.remove(i--);
				continue;
			}
		}

		int t;
		int max;
		int maxId;
		while(!nwsList.isEmpty()) {
			max = -1;
			maxId = -1;
			for(int i = 0; i != nwsList.size(); i++) {
				t = Integer.parseInt(nwsList.get(i).substring(2));
				if(t > max) {
					max = t;
					maxId = i;
				}
			}			
			nwsListSorted.add(nwsList.remove(maxId));
		}
		String path = null;
		while(!nwsListSorted.isEmpty() && (path = getFromDir(NWS+nwsListSorted.remove(0))) == null) {}
		
		if(path == null) path = getFromDir(ALL);
		if(path == null) path = checkUnlincked();
		
		return path;
	}

	private static String getFromDir(final String path) {
		final ArrayList<File> list = new ArrayList<File>(Arrays.asList(new File(path).listFiles()));
		
		for(int i = 0; i < list.size(); i++)
			if(list.get(i).getName().equalsIgnoreCase("thumbs.db")) list.remove(i);
		
		try {
			final DataInputStream dis = new DataInputStream(Assist.getDatStream(DESCRIPTOR_FILE_NAME));
			String name;
			int checked;
			while(dis.available() > 0) {
				name = Assist.readString(dis);
				checked = dis.readByte();
				Assist.skipString(dis);
				Assist.skipString(dis);
				Assist.skipString(dis);
								
				for(int i = list.size()-1; i != -1; i--) {
					try {
						if(Assist.trimPath(list.get(i).getAbsolutePath()).equals(name)) {
							if(checked == 1) throw new FileNotFoundException();
							break;
						}
					} catch (FileNotFoundException e) {
						list.remove(i);
						break;
					}
				}
			}
			dis.close();
		} catch (IOException e) {
			Assist.abort("Can't read descriptions from \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+"\".\n"+e.getMessage()); //TODO: сообщение об ошибке в константу
		}
		
		if(list.isEmpty()) return null;
		try { return Assist.trimPath(list.get(r.nextInt(list.size())).getAbsolutePath()); }
		catch (FileNotFoundException e) { return null; }
	}

	private static String checkUnlincked() {
		final ArrayList<String> list = new ArrayList<String>();
		try {
			final DataInputStream dis = new DataInputStream(Assist.getDatStream(DESCRIPTOR_FILE_NAME));
			String name;
			int checked;
			while(dis.available() > 0) {
				name = Assist.readString(dis);
				checked = dis.readByte();
				Assist.skipString(dis);
				Assist.skipString(dis);
				Assist.skipString(dis);
				if(checked == 0 && new File(Assist.MAIN_FOLDER+name).exists()) list.add(name);
			}
			dis.close();
		} catch (IOException e) {
			Assist.abort("Can't read descriptions from \""+Assist.MAIN_FOLDER+DESCRIPTOR_FILE_NAME+"\".\n"+e.getMessage());
			return null;
		}
		
		if(list.isEmpty()) return null;
		
		return list.get(r.nextInt(list.size()));
	}
}