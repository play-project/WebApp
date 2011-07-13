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
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof StreamDesc)) return false;
		StreamDesc u = (StreamDesc) o;
		if(u.id.equals(id) && u.source.equals(source)){
			return true;
		}
		return false;
	}
}
