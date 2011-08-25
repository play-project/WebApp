package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class FbUser extends Model {

    public long uid;
    public String access_token;

    public FbUser(long uid) {
        this.uid = uid;
    }

    public static FbUser get(long id) {
        return find("uid", id).first();
    }

    public static FbUser createNew() {
        long uid = (long)Math.floor(Math.random() * 10000);
        FbUser user = new FbUser(uid);
        user.create();
        return user;
    }

}
