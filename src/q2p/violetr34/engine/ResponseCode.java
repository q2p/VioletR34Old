package q2p.violetr34.engine;

public enum ResponseCode {
	_200_OK("200 OK"),
	_301_Moved_Permanently("301 Moved Permanently"),
	_400_Bad_Request("400 Bad Request"),
	_404_Not_Found("404 Not Found"),
	_406_Not_Acceptable("406 Not Acceptable"),
	_500_Internal_Server_Error("500 Internal Server Error");
	
	public final String print;
	
	private ResponseCode(String print) {
		this.print = print;
	}
}