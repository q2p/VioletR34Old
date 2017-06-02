package q2p.violetr34.addons.resources;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

public class Resources {
	private static final String RESOURCES_PATH = Assist.DATA_FOLDER + "resources/resources.dat";
	private static final String RESOURCE_TYPES_DIR = Assist.DATA_FOLDER + "resources/types/";

	private static final ResourceType[] TYPES = loadTypes();
	private static final ArrayList<Resource> resources = loadResources();

	private static ArrayList<Resource> loadResources() {
		final ArrayList<Resource> resources = new ArrayList<Resource>();
		try {
			final DataInputStream dis = new DataInputStream(new FileInputStream(RESOURCES_PATH));
			while(dis.available() > 0) {
				final Resource res = new Resource(Assist.readString(dis), Assist.readString(dis), Assist.readString(dis), dis.readLong());
				if(res.resourceType != null)
					resources.add(res);
			}
			Assist.tryToCloseInput(dis);
		} catch (IOException e) {
			Assist.abort("Can't load resources");
		}
		return resources;
	}
	
	private static ResourceType[] loadTypes() {
		final File dir = new File(RESOURCE_TYPES_DIR);
		if(!dir.exists()) Assist.abort("Can't load resource types");
		final String[] list = dir.list();
		int amount = 0;
		for(int i = 0; i < list.length; amount += (list[i++].endsWith(".res")?1:0));
		final ResourceType[] types = new ResourceType[amount];
		amount = 0;
		for(int i = 0; i < list.length; i++) {
			if(!list[i].endsWith(".res")) continue;
			try {
				final FileInputStream fis = new FileInputStream(RESOURCE_TYPES_DIR + list[i]);
				final byte[] buff = new byte[fis.available()];
				fis.read(buff);
				fis.close();
				final String total = new String(buff, StandardCharsets.UTF_8);
				types[amount] = new ResourceType(
						list[i].substring(0, list[i].lastIndexOf(".res")),
						total.substring(0, total.indexOf("\n")).trim(),
						total.substring(total.indexOf("\n")+1, total.lastIndexOf("\n")).trim(),
						total.substring(total.lastIndexOf("\n")+1).trim()
				);
			} catch (IndexOutOfBoundsException | IOException e) {
				Assist.abort("Can't load resourc type " + list[i]);
			}
			amount++;
		}
		return types;
	}

	public static boolean sendList(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourcesList")) return false;
		
		String ret = "[";
		for(int i = 0; i < resources.size(); i++) {
			final Resource res = resources.get(i);
			final String timeStr = toTimeString(res);
			ret += "[\"" + res.resourceType.storageName + "\",\"" + res.address + "\",\"" + res.name + "\",\"" + timeStr + "\"]";
			if(i < resources.size() - 1) ret += ",";
		}
		ret += "]";
		
		response.setData(ResponseCode._200_OK, ContentType.application_json, ret);
		return true;
	}
	
	private static boolean sendTypes(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourceTypes")) return false;
		
		String ret = "[";
		for(int i = 0; i < TYPES.length; i++) {
			ret += "[\"" + TYPES[i].storageName + "\",\"" + TYPES[i].name + "\",\"" + Assist.JSONescape(TYPES[i].beg) + "\",\"" + Assist.JSONescape(TYPES[i].end) + "\"]";
			if(i < TYPES.length - 1) ret += ",";
		}
		
		response.setData(ResponseCode._200_OK, ContentType.application_json, ret+"]");
		return true;
	}
	
	private static String toTimeString(final Resource res) {
		if(res.lastUpdate == -1) return "Никогда";
		long diff = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis()-res.lastUpdate;

		int i = (int)(diff/(1000*60*60*24*7));
		if(i != 0) return i+Assist.decline(i, " неделю назад", " недели назад", " недель назад");
		i = (int)(diff/(1000*60*60*24) % 7);
		if(i != 0) return i+Assist.decline(i, " день назад", " дня назад", " дней назад");
		i = (int)((long)(diff/(1000*60*60)) % 24);
		if(i != 0) return i+Assist.decline(i, " час назад", " часа назад", " часов назад");
		i = (int)((long)(diff/(1000*60)) % 60);
		if(i != 0) return i+Assist.decline(i, " минуту назад", " минуты назад", " минут назад");
		
		return "Меньше минуты назад";
	}
	
	private static boolean saveRes(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourceSave")) return false;
		
		final String[] dta = request.getBody().split("\n");
		
		if(dta.length != 4) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		final int id;
		try { id = Integer.parseInt(dta[0]); }
		catch(NumberFormatException e) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		if(isClonning(dta[3].trim(), dta[1].trim(), id)) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		
		if(id < 0) resources.add(0, new Resource(dta[1].trim(), dta[2].trim(), dta[3].trim(), -1));
		else try { resources.set(id, new Resource(dta[1], dta[2], dta[3], resources.get(id).lastUpdate)); }
		catch(IndexOutOfBoundsException e) {
			response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "");
			return true;
		}
		
		writeResourcesToFile();
		
		response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
		return true;
	}
	
	private static void writeResourcesToFile() {
		try {
			final DataOutputStream dos = new DataOutputStream(new FileOutputStream(RESOURCES_PATH));
			for(final Resource res : resources) {
				Assist.writeString(dos, res.address);
				Assist.writeString(dos, res.name);
				Assist.writeString(dos, res.resourceType.storageName);
				dos.writeLong(res.lastUpdate);
			}
			dos.flush();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Unable to write to \""+RESOURCES_PATH+"\".");
		}
	}
	
	private static boolean deleteRes(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourceDelete")) return false;
		
		final Integer id = Assist.getIntegerFromArgument(request, "id");
		if(id == null) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		try { resources.remove(id.intValue()); }
		catch(IndexOutOfBoundsException e) {
			response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "");
			return true;
		}
		
		writeResourcesToFile();
		
		response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
		return true;
	}
	
	private static boolean updateRes(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourceUpdate")) return false;
		
		final Integer id = Assist.getIntegerFromArgument(request, "id");
		if(id == null) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		
		if(id.intValue() < 0 || id.intValue() >= resources.size()) {
			response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "");
			return true;
		}

		final Resource res = resources.remove(id.intValue());
		
		res.lastUpdate = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis();
		
		resources.add(res);
		
		writeResourcesToFile();
		response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
		return true;
	}

	private static boolean sendIsClonning(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("resourceClone")) return false;

		final String type;
		final String address;
		final int id;
		
		try {
			type = getTypeByStorageName(request.getBody().substring(0, request.getBody().indexOf("\n")).trim()).storageName;
			address = request.getBody().substring(request.getBody().indexOf("\n")+1, request.getBody().lastIndexOf("\n")).trim();
			id = Integer.parseInt(request.getBody().substring(request.getBody().lastIndexOf("\n")+1).trim());
		} catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
			return true;
		}
		if(isClonning(type, address, id)) response.setData(ResponseCode._200_OK, ContentType.text_plain, "cloning");
		else response.setData(ResponseCode._200_OK, ContentType.text_plain, "fine");
		
		return true;
	}
	
	private static boolean isClonning(final String type, final String address, final int id) {
		for(int i = 0; i < resources.size(); i++)
			if(i != id && resources.get(i).resourceType.storageName.equals(type) && resources.get(i).address.equals(address)) return true;
		return false;
	}

	public static ResourceType getTypeByStorageName(final String storageName) {
		for(final ResourceType type : TYPES)
			if(type.storageName.equals(storageName))
				return type;
		return null;
	}

	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(sendList(request, response)) return true;
		if(updateRes(request, response)) return true;
		if(saveRes(request, response)) return true;
		if(deleteRes(request, response)) return true;
		if(sendIsClonning(request, response)) return true;
		if(sendTypes(request, response)) return true;
		return false;
	}
}