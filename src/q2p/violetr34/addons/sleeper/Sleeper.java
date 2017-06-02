package q2p.violetr34.addons.sleeper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import q2p.violetr34.engine.Assist;
import q2p.violetr34.engine.ContentType;
import q2p.violetr34.engine.RequestInfo;
import q2p.violetr34.engine.ResponseCode;
import q2p.violetr34.engine.ResponseInfo;

public class Sleeper {
	private static final String SLEEPER_FILE_NAME = "sleeper/sleeper";
	
	public static boolean requestCheck(final RequestInfo request, final ResponseInfo response) {
		if(diaryList(request, response)) return true;
		if(dreamInstance(request, response)) return true;
		if(saveDream(request, response)) return true;
		if(deleteDream(request, response)) return true;
		
		return false;
	}

	private static boolean saveDream(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("sleeperSaveDream")) return false;
		
		LinkedList<String> data = Assist.split(request.getBody(), "\n");
		if(data.size() != 4)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");

		final short year;
		final byte month;
		final byte day;
		
		try {
			year = Short.parseShort(data.removeFirst());
			month = Byte.parseByte(data.removeFirst());
			day = Byte.parseByte(data.removeFirst());
		} catch(NumberFormatException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");
		}

		if(!isFineDate(year, month, day)) return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Не допустимый формат даты");
		
		String info = "";
		boolean needNewLine = false;
		while(!data.isEmpty()) {
			if(needNewLine) info += "\n";
			info += data.removeFirst();
			needNewLine = true;
		}

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(SLEEPER_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(SLEEPER_FILE_NAME));
		
		short cYear;
		byte cMonth;
		byte cDay;
		
		try {
			boolean found = false;
			while(dis.available() > 0) {
				cYear = dis.readShort();
				cMonth = dis.readByte();
				cDay = dis.readByte();


				if(!found && 
						(year > cYear) ||
						(year == cYear && month > cMonth) ||
						(year == cYear && month == cMonth && day > cDay))
				{
					dos.writeShort(year);
					dos.writeByte(month);
					dos.writeByte(day);
					Assist.writeString(dos, info);
					dos.writeShort(cYear);
					dos.writeByte(cMonth);
					dos.writeByte(cDay);
					Assist.writeString(dos, Assist.readString(dis));
					found = true;
				} else if(!found && year == cYear && month == cMonth && day == cDay) {
					dos.writeShort(year);
					dos.writeByte(month);
					dos.writeByte(day);
					Assist.writeString(dos, info);
					Assist.skipString(dis);
					found = true;
				} else {
					dos.writeShort(cYear);
					dos.writeByte(cMonth);
					dos.writeByte(cDay);
					Assist.writeString(dos, Assist.readString(dis));
				}
				
				dos.flush();
			}
			dis.close();
			
			if(!found) {
				dos.writeShort(year);
				dos.writeByte(month);
				dos.writeByte(day);
				Assist.writeString(dos, info);
				dos.flush();
			}
			
			dos.close();
		} catch (IOException e) {
			Assist.abort("Can't read or write to diary from \""+Assist.MAIN_FOLDER+SLEEPER_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(SLEEPER_FILE_NAME);
		
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
	}
	
	private static boolean deleteDream(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("sleeperDeleteDream")) return false;
		
		LinkedList<String> data = Assist.split(request.getBody(), "\n");
		if(data.size() != 3)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");

		final short year;
		final byte month;
		final byte day;
		
		try {
			year = Short.parseShort(data.removeFirst());
			month = Byte.parseByte(data.removeFirst());
			day = Byte.parseByte(data.removeFirst());
		} catch(NumberFormatException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Не допустимый формат даты");
		}

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(SLEEPER_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(SLEEPER_FILE_NAME));
		
		short cYear;
		byte cMonth;
		byte cDay;
		
		boolean found = false;
		
		try {
			while(dis.available() > 0) {
				cYear = dis.readShort();
				cMonth = dis.readByte();
				cDay = dis.readByte();
				
				if(year == cYear && month == cMonth && day == cDay) {
					Assist.skipString(dis);
					found = true;
				} else {
					dos.writeShort(cYear);
					dos.writeByte(cMonth);
					dos.writeByte(cDay);
					Assist.writeString(dos, Assist.readString(dis));
				}
				
				dos.flush();
			}
			dis.close();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Can't read or write to diary from \""+Assist.MAIN_FOLDER+SLEEPER_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(SLEEPER_FILE_NAME);
		
		if(found) return response.setData(ResponseCode._200_OK, ContentType.text_plain, "");
		return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, "Запись не найдена");
	}

	private static boolean diaryList(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("sleeperDiaryList")) return false;
				
		final DataInputStream dis = new DataInputStream(Assist.getDatStream(SLEEPER_FILE_NAME));
		String ret = "[";
		try {
			boolean needComma = false;
			while(dis.available() > 0) {
				if(needComma) ret += ",";
				ret += "[\""+dis.readShort()+"\",\""+dis.readByte()+"\",\""+dis.readByte()+"\"]";
				Assist.skipString(dis);
				needComma = true;
			}
			dis.close();
		} catch (IOException e) {
			Assist.abort("Can't read diary from \""+Assist.MAIN_FOLDER+SLEEPER_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		return response.setData(ResponseCode._200_OK, ContentType.text_plain, ret+"]");
	}

	private static boolean dreamInstance(final RequestInfo request, final ResponseInfo response) {
		if(!request.getPath().equals("sleeperDreamInstance")) return false;
		
		final LinkedList<String> date = Assist.split(request.getBody(), "\n");
		if(date.size() != 3)
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "");

		final short year;
		final byte month;
		final byte day;
		
		try {
			year = Short.parseShort(date.removeFirst());
			month = Byte.parseByte(date.removeFirst());
			day = Byte.parseByte(date.removeFirst());
		} catch(NumberFormatException e) {
			return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Не допустимый формат даты");
		}
		
		if(!isFineDate(year, month, day)) return response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Не допустимый формат даты");
		
		final DataInputStream dis = new DataInputStream(Assist.getDatStream(SLEEPER_FILE_NAME));
		try {
			while(dis.available() > 0) {
				if(year == dis.readShort() & month == dis.readByte() & day == dis.readByte()) {
					response.setData(ResponseCode._200_OK, ContentType.application_json, "[\""+year+"\",\""+month+"\",\""+day+"\",\""+Assist.JSONescape(Assist.readString(dis))+"\"]");
					dis.close();
					return true;
				}
				Assist.skipString(dis);
			}
			dis.close();
			return response.setData(ResponseCode._404_Not_Found, ContentType.text_plain, day+"."+month+"."+year+" сны не были записаны.");
		} catch (IOException e) {
			Assist.abort("Can't read diary from \""+Assist.MAIN_FOLDER+SLEEPER_FILE_NAME+".dat\".\n"+e.getMessage());
		}
		return true;
	}
	
	private static boolean isFineDate(final short year, final byte month, final byte day) {
		if(year < 2000 || month < 1 || month > 12 || day < 1) return false;
		
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DATE, 1);
		return day <= c.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
}
