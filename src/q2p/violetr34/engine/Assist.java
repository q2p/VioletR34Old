package q2p.violetr34.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

public class Assist {
	public static final String MAIN_FOLDER; // .../p/
	public static final String DATA_FOLDER; // .../p/VioletR34/data/
	
	static {
		// TODO: при компиляции ставить нормальный путь
		// final String path = normalizeURL(new File("test.file").getAbsolutePath());
		// MAIN_FOLDER = path.substring(0, path.indexOf("/p/"+3));
		MAIN_FOLDER = "E:/@MyFolder/p/";
		DATA_FOLDER = MAIN_FOLDER + "VioletR34/data/";
	}
	
	public static String readString(final DataInputStream dis) throws IOException {
		final byte[] buff = new byte[dis.readInt()];
		dis.read(buff);
		return new String(buff, StandardCharsets.UTF_8);
	}
	public static void readStrings(final DataInputStream dis, final ArrayList<String> container) throws IOException {
		for(int i = dis.readInt(); i != 0; i--)
			container.add(readString(dis));
	}
	public static void writeString(final DataOutputStream dos, final String string) throws IOException {
		final byte[] buff = string.getBytes(StandardCharsets.UTF_8);
		dos.writeInt(buff.length);
		dos.write(buff);
	}
	public static void writeStrings(final DataOutputStream dos, final ArrayList<String> strings) throws IOException {
		dos.writeInt(strings.size());
		for(final String s : strings) writeString(dos, s);
	}
	public static void copyStrings(final DataInputStream dis, final DataOutputStream dos) throws IOException {
		int i = dis.readInt();
		dos.writeInt(i);
		for(;i != 0; i--)
			writeString(dos, readString(dis));
	}
	public static int sizeString(final String string) {
		return Integer.BYTES + string.getBytes(StandardCharsets.UTF_8).length;
	}
	public static int sizeStrings(final ArrayList<String> strings) {
		int ret = Integer.BYTES;
		for(final String s : strings) ret += sizeString(s);
		return ret;
	}
	public static void skipString(final DataInputStream dis) throws IOException {
		dis.skipBytes(dis.readInt());
	}
	public static void skipStrings(DataInputStream dis) throws IOException {
		for(int i = dis.readInt(); i != 0; i--)
			skipString(dis);
	}
	
	public static String JSONescape(final String string) {
		String str = "";
		int last = 0;
		
		for(int i = 0; i < string.length(); i++) {
			if(string.charAt(i) == '\n') {
				str += string.substring(last, i) + "\\n";
				last = i+1;
			} else if(string.charAt(i) == '\"') {
				str += string.substring(last, i) + "\\\"";
				last = i+1;
			} else if(string.charAt(i) == '\\'){
				str += string.substring(last, i) + "\\\\";
				last = i+1;
			}
		}
		str += string.substring(last, string.length());
		return str;
	}

	public static String normalizeURL(final String path) {
		return path.replace("\\", "/");
	}
	
	public static String JSescape(final String string) {
		String str = "";
		int last = 0;
		for(int i = 0; i < string.length(); i++) {
			if(string.charAt(i) == '\'') {
				str += string.substring(last, i) + "\\\'";
				last = i+1;
			} else if(string.charAt(i) == '\"') {
				str += string.substring(last, i) + "\\\"";
				last = i+1;
			} else if(string.charAt(i) == '\\'){
				str += string.substring(last, i) + "\\\\";
				last = i+1;
			} else if(string.charAt(i) == '\t') {
				str += string.substring(last, i) + "\\t";
				last = i+1;
			}
		}
		str += string.substring(last, string.length());
		return str;
	}
	
	public static Integer getIntegerFromArgument(final RequestInfo request, String name) {
		if((name = request.getArgument(name)) == null) return null;
		try { return Integer.parseInt(name); }
		catch(NumberFormatException e) { return null; }
	}
	
	public static final String getPathExtention(final String path) {
		final int idx = path.lastIndexOf('.')+1;
		return (idx==0 || path.indexOf('/', idx) != -1)?"":path.substring(idx);
	}
	
	public static void abort(final String reason) {
		System.out.println("Server was shuted down.\nDetails:\n" + reason);
		System.exit(1);
	}

	public static String decline(final int num, final String one, final String few, final String many) {
		if (num > 10 && ((num % 100) / 10) == 1) return many;
	
		switch (num % 10) {
			case 1:
				return one;
			case 2:
			case 3:
			case 4:
				return few;
			default: // case 0, 5-9
				return many;
		}
	}
	
	public static LinkedList<String> split(String original, final String pattern) {
		final LinkedList<String> ret = new LinkedList<String>();
		
		int i;
		while((i = original.indexOf(pattern)) != -1) {
			ret.add(original.substring(0, i));
			original = original.substring(i+pattern.length());
		}
		
		ret.add(original);
		
		return ret;
	}
	public static ArrayList<String> splitLines(String data) {
		final ArrayList<String> ret = new ArrayList<String>();
		
		final LinkedList<String> lines = Assist.split(data, "\n");
		int length;
		
		while(!lines.isEmpty()) {
			try { length = Integer.parseInt(lines.remove(0)); }
			catch(NumberFormatException e) { return ret; }
			
			data = "";
			
			boolean needNewLine = false;
			
			for(;length != 0 && !lines.isEmpty(); length--) {
				if(needNewLine) data += "\n";
				data += lines.remove(0);
				needNewLine = true;
			}
			ret.add(data);
		}
		
		return ret;
	}
	public static FileOutputStream getTwrStream(String name) {
		File twr = new File(DATA_FOLDER+name+".twr");
		try {
			if(!twr.exists()) twr.createNewFile();
			if(!twr.isFile()) throw new IOException();
			return new FileOutputStream(twr);
		} catch (Exception e) { Assist.abort("Can't write to file \""+twr.getAbsolutePath()+"\".\n"+e.getMessage()); }
		return null;
	}
	public static FileInputStream getDatStream(String name) {
		File dat = new File(DATA_FOLDER+name+".dat");
		try {
			if(!dat.exists() || !dat.isFile()) throw new IOException("File not exists or it's not a file.");
			return new FileInputStream(dat);
		} catch (Exception e) { Assist.abort("Can't read from file \""+dat.getAbsolutePath()+"\".\n"+e.getMessage()); }
		return null;
	}
	public static void swapTwrAndDatFiles(String name) {
		final File twr = new File(DATA_FOLDER+name+".twr");
		final File dat = new File(DATA_FOLDER+name+".dat");
		try {
			if(!twr.exists() || !twr.isFile()) throw new IOException();
			
			if(!dat.exists()) dat.createNewFile();
			if(!dat.isFile()) throw new IOException();
			
			final FileInputStream twrStream = new FileInputStream(twr);
			final FileOutputStream datStream = new FileOutputStream(dat);
			datStream.getChannel().transferFrom(twrStream.getChannel(), 0, twrStream.getChannel().size());
			datStream.flush();
			datStream.close();
			twrStream.close();

			if(twr.exists() && !twr.delete()) throw new IOException();
		} catch (Exception e) { Assist.abort("Can't swap \""+twr.getAbsolutePath()+"\" with \""+twr.getAbsolutePath()+"\".\n"+e.getMessage()); }
	}
	
	public static final void tryToClose(final InputStream is, final OutputStream os) {
		tryToCloseInput(is);
		tryToCloseOutput(os);
	}
	public static final void tryToCloseInput(final InputStream is) {
		try { if(is != null) is.close(); }
		catch (IOException e) {}
	}
	public static final void tryToCloseOutput(final OutputStream os) {
		try { if(os != null) os.close(); }
		catch (IOException e) {}
	}
	
	public static String trimPath(String absolutePath) throws FileNotFoundException {
		absolutePath = normalizeURL(absolutePath);
		int idx = absolutePath.indexOf("p");
		if(idx == -1) throw new FileNotFoundException();
		if(idx != 0) {
			if(absolutePath.charAt(idx-1) != '/') throw new FileNotFoundException();
			absolutePath = absolutePath.substring(idx);
		}
		if(absolutePath.length() == 1) return "";
		if(absolutePath.startsWith("p/")) return absolutePath.substring(2);
		throw new FileNotFoundException();
	}
	
	public static InputStream getResourceAsStream(final String path) throws FileNotFoundException {
		final InputStream is = Assist.class.getClassLoader().getResourceAsStream("res/"+path);
		if(is == null) throw new FileNotFoundException();
		return is;
	}
}