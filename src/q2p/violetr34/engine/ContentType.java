package q2p.violetr34.engine;

public enum ContentType {
	text_plain("text/plain", "unknown"),
	text_html("text/html", "unknown", "html", "htm"),
	text_css("text/css", "unknown", "css"),
	text_javascript("text/javascript", "unknown", "js"),
	audio_aac("audio/aac", "unknown", "aac"),
	audio_mpeg("audio/mpeg", "audio", "mp3"), //mp3
	audio_ogg("audio/ogg", "audio", "ogg"),
	audio_flac("audio/x-flac", "unknown", "flac"),
	audio_wave("audio/vnd.wave", "audio", "wav"),
	image_gif("image/gif", "image", "gif"),
	image_jpeg("image/jpeg", "image", "jpg","jpeg"),
	image_png("image/png", "image", "png"),
	image_svg_xml("image/svg+xml", "unknown", "svg"),
	image_ico("image/vnd.microsoft.icon", "image", "ico","icon"),
	video_mp4("video/mp4", "video", "mp4"),
	video_webm("video/webm", "video", "webm"),
	video_flv("video/x-flv", "unknown", "flv"),
	video_mpeg("video/mpeg", "video", "mpeg"),
	application_json("application/json", "unknown", "json");
	
	public final String print;
	public final String html5;
	public final String[] extention;
	
	private ContentType(final String print, final String html5, final String... extention) {
		this.print = print;
		this.html5 = html5;
		this.extention = extention;
	}
	
	public static ContentType getByExtention(final String extention) {
		final ContentType[] values = values();
		for(final ContentType type : values) for(final String ex : type.extention) if(ex.equalsIgnoreCase(extention)) return type;
		return text_plain;
	}
}
