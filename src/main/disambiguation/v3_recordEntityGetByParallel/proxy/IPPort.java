package main.disambiguation.v3_recordEntityGetByParallel.proxy;

public class IPPort {
	private String ip = null;
	private String port ;
	public IPPort(){
		
	}
	public IPPort(String ip,String port){
		this.setIp(ip);
		this.setPort(port);
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}


}
