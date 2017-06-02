package q2p.violetr34.addons.tagger.collections;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import q2p.violetr34.addons.descriptor.Descriptor;
import q2p.violetr34.addons.tagger.Tagger;
import q2p.violetr34.addons.tagger.collections.exceptions.NotExistingTagException;
import q2p.violetr34.addons.tagger.collections.exceptions.OverrideException;
import q2p.violetr34.addons.tagger.collections.exceptions.StringParsingException;
import q2p.violetr34.engine.Assist;

public class Collector {
	public static final ArrayList<Tag> tags = new ArrayList<Tag>();

	private static final String TAG_LIST_FILE_NAME = "tags/tags";

	public static void initilize() {
		try {
			DataInputStream dis = new DataInputStream(Assist.getDatStream(TAG_LIST_FILE_NAME));
			while(dis.available() > 0) {
				tags.add(new Tag(Assist.readString(dis)));
			}
			dis.close();
		} catch (IOException | StringParsingException e) {
			Assist.abort("Can't read tags from file \""+Assist.DATA_FOLDER+TAG_LIST_FILE_NAME+".dat\".\n"+e.getMessage());
		}
	}
	
	public static void writeTags() {
		try {
			final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(TAG_LIST_FILE_NAME));
			for(final Tag tag : tags) {
				Assist.writeString(dos, tag.toString());
				dos.flush();
			}
			dos.close();
		} catch (IOException e) {
			Assist.abort("Can't save tags to file \""+Assist.DATA_FOLDER+TAG_LIST_FILE_NAME+".twr\".\n"+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(TAG_LIST_FILE_NAME);
	}

	public static Tag findTag(final String name) throws NotExistingTagException {
		for(final Tag tag : tags) if(tag.toString().equals(name)) return tag;
		throw new NotExistingTagException(name);
	}
	
	public static Tag createTag(final String name) throws StringParsingException, OverrideException {
		try {
			findTag(name);
			throw new OverrideException(name);
		} catch (NotExistingTagException e) {}
		final Tag tag = new Tag(name);
		tags.add(tag);
		sortLists();
		writeTags();
		return tag;
	}
	
	public static String asString() {
		String ret = "";
		for(int i = 0; i < tags.size(); i++) {
			if(i != 0) ret += Tagger.SEPARATOR;
			ret += tags.get(i);
		}
		return ret;
	}

	public static void removeTag(final String name) throws NotExistingTagException {
		final Tag tag = findTag(name);
		
		tags.remove(tag);
		
		writeTags();

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(Descriptor.DESCRIPTOR_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(Descriptor.DESCRIPTOR_FILE_NAME));
		
		String tags;
		ArrayList<String> tagsArray;
		
		try {
			while(dis.available() > 0) {
				Assist.writeString(dos, Assist.readString(dis));
				dos.writeByte(dis.readByte());
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				
				if((tags = Assist.readString(dis)).equals("")){
					Assist.writeString(dos, "");
					continue;
				}
				
				tagsArray = Assist.split(tags, Tagger.SEPARATOR);
				
				for(int i = 0; i != tagsArray.size(); i++)
					if(tagsArray.get(i).equals(name)) tagsArray.remove(i);
				
				tags = "";
				for(boolean i = false; !tagsArray.isEmpty(); i=true) {
					if(i) tags += " ";
					tags += tagsArray.remove(0);
				}
				Assist.writeString(dos, tags);
				
				dos.flush();
			}
			dis.close();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Error: Can't update tags in files descriptions.\n"+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(Descriptor.DESCRIPTOR_FILE_NAME);
	}

	public static void sortLists() {
		try {
			ArrayList<String> names = sortByName(tags);
			ArrayList<Tag> newTags = new ArrayList<Tag>();
			while(!names.isEmpty()) newTags.add(findTag(names.remove(0)));
			tags.clear();
			tags.addAll(newTags);
		} catch (NotExistingTagException e) {}
	}
	
	public static ArrayList<String> sortByName(final ArrayList<?> list) {
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
			names.add(list.get(i).toString());
		Collections.sort(names);
		return names;
	}

	public static void renameTag(final String from, final String to) throws NotExistingTagException, OverrideException, StringParsingException {
		final Tag tag = Collector.findTag(from);
		tag.name = to;

		try {
			Collector.findTag(to);
			throw new OverrideException(to);
		} catch (NotExistingTagException e) {}
		
		if(!Tagger.allowedUnitNaming(to, Tagger.ALLOWED_TAG_SYMS)) throw new StringParsingException(to);
		
		tag.name = to;
		
		Collector.sortLists();
		
		Collector.writeTags();

		final DataInputStream dis = new DataInputStream(Assist.getDatStream(Descriptor.DESCRIPTOR_FILE_NAME));
		final DataOutputStream dos = new DataOutputStream(Assist.getTwrStream(Descriptor.DESCRIPTOR_FILE_NAME));
		
		String tags;
		ArrayList<String> tagsArray;
		
		try {
			while(dis.available() > 0) {
				Assist.writeString(dos, Assist.readString(dis));
				dos.writeByte(dis.readByte());
				Assist.writeString(dos, Assist.readString(dis));
				Assist.writeString(dos, Assist.readString(dis));
				
				if((tags = Assist.readString(dis)).equals("")){
					Assist.writeString(dos, "");
					continue;
				}
				
				tagsArray = Assist.split(tags, Tagger.SEPARATOR);
				
				for(int i = 0; i != tagsArray.size(); i++)
					if(tagsArray.get(i).equals(from)) tagsArray.set(i, to);
				
				tags = "";
				for(boolean i = false; !tagsArray.isEmpty(); i=true) {
					if(i) tags += " ";
					tags += tagsArray.remove(0);
				}
				Assist.writeString(dos, tags);
				
				dos.flush();
			}
			dis.close();
			dos.close();
		} catch (IOException e) {
			Assist.abort("Error: Can't update tags in files descriptions.\n"+e.getMessage());
		}
		
		Assist.swapTwrAndDatFiles(Descriptor.DESCRIPTOR_FILE_NAME);
		
	}
}