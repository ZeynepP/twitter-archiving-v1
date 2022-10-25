package fr.ina.dlweb.dowser.ws.twitter.model.request;

public class TwitterAnalyzeSearchRequest {

	
	private String hashtags;
	private String mentions;
	private String urls;
	private String users;
	private String texte;

	
	


	public String getUsers() {
		return users;
	}


	public void setUsers(String users) {
		this.users = users;
	}


	public String getUrls() {
		return urls;
	}


	public void setUrls(String urls) {
		this.urls = urls;
	}


	public String getMentions() {
		return mentions;
	}


	public void setMentions(String mentions) {
		this.mentions = mentions;
	}


	public String getHashtags() {
		return hashtags;
	}


	public void setHashtags(String hashtags) {
		this.hashtags = hashtags;
	}


	public String getTexte() {
		return texte;
	}


	public void setTexte(String texte) {
		this.texte = texte;
	}
	
	
}
