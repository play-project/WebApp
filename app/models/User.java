package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.*;
import play.libs.F.ArchivedEventStream;

@Entity
public class User extends Model {
	@Id public String userId;
	public String login;
	public String password;
	public String name;
	public String email;
	
	@Transient public List<ArchivedEventStream<Event>> eventStreamList;

	public User(String userId, String login, String password, String name, String email) {
		this.userId = userId;
		this.login = login;
		this.password = password;
		this.name = name;
		this.email = email;
		this.eventStreamList = null;
	}
}
