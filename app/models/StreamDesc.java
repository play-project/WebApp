package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class StreamDesc extends Model {
	public String source;
	public String title;
	public String content;

	public StreamDesc(String source, String title, String content) {
		this.source = source;
		this.title = title;
		this.content = content;
	}
}
