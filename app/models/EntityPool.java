package models;

import java.util.ArrayList;

import javax.persistence.*;

import play.db.jpa.*;

public class EntityPool extends Model {

	static EntityPool instance = null;
	public ArrayList<User> users;

	public static EntityPool get() {
		if (instance == null) {
			instance = new EntityPool();
		}
		return instance;
	}

	public User connect(String login, String password) {
		User u = User.find("byLoginAndPassword", login, password).first();
		users.add(u);
		return u;
	}
	
	public boolean disconnect(User user){
		if(users.contains(user)){
			users.remove(user);
			return true;
		}
		return false;
	}
}
