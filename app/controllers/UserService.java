package controllers;

import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import models.ModelManager;
import models.User;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserServiceDelegate;

public class UserService extends Controller implements UserServiceDelegate {

	@Override
	public SocialUser find(UserId id) {
		Logger.info("UserService.find() - type : " + id.provider);
		User uById = null;
		if (id.provider == ProviderType.facebook) {
			Logger.info("byFacebookId");
			uById = User.find("byFacebookId", id.id).first();
		} else if (id.provider == ProviderType.twitter) {
			Logger.info("byTwitterId");
			uById = User.find("byTwitterId", id.id).first();
		} else if (id.provider == ProviderType.google) {
			Logger.info("byGoogleId");
			uById = User.find("byGoogleId", id.id).first();
		}
		SocialUser su = null;
		if (uById != null) {
			su = new SocialUser();
			su.id = id;
			su.email = uById.email;
			su.displayName = uById.name;
			su.avatarUrl = uById.avatarUrl;
		}
		Logger.info("Social user : " + su);
		return su;
	}

	@Override
	public void save(SocialUser socialUser) {
		Logger.info("UserService.save()");
		// find SocialUser in database by ID
		User uById = null;
		User u = null;
		if (socialUser.id.provider == ProviderType.facebook) {
			uById = User.find("byFacebookId", socialUser.id.id).first();
		} else if (socialUser.id.provider == ProviderType.twitter) {
			uById = User.find("byTwitterId", socialUser.id.id).first();
		} else if (socialUser.id.provider == ProviderType.google) {
			uById = User.find("byGoogleId", socialUser.id.id).first();
		}

		// if SocialUser exists and registered (has password)
		if (uById != null) {
			if (!uById.password.equals("")) { // user is registered
				u = ModelManager.get().connect(uById.email, uById.password);
				if (u != null) {
					session.put("userid", u.id);
				}
			} else { // only SocialUser exists in database, not fully registered
				Cache.set("su" + session.getId(), socialUser, "15mn");
				session.put("socialauth", "0");
			}
			uById.avatarUrl = socialUser.avatarUrl;
			uById.name = socialUser.displayName;
			uById.save();

		} else { // not linked with social media
			User uByEmail = User.find("byEmail", socialUser.email).first();
			if (uByEmail != null) {
				if (!uByEmail.password.equals("")) { // user already registered
														// (has password), merge
														// social information
					u = ModelManager.get().connect(uByEmail.email, uByEmail.password);
					if (u != null) {
						if (socialUser.id.provider == ProviderType.facebook) {
							u.facebookId = socialUser.id.id;
						} else if (socialUser.id.provider == ProviderType.twitter) {
							u.twitterId = socialUser.id.id;
						} else if (socialUser.id.provider == ProviderType.google) {
							u.googleId = socialUser.id.id;
						}
						u.avatarUrl = socialUser.avatarUrl;
						u.save();
						session.put("userid", u.id);
					}
				} else { // only SocialUser exists in database, not registered
					uByEmail.avatarUrl = socialUser.avatarUrl;
					uByEmail.name = socialUser.displayName;
					uByEmail.save();
					Cache.set("su", socialUser, "15mn");
					session.put("socialauth", "0");
				}
			} else {
				u = new User(socialUser.email, "", socialUser.displayName, null, "N");
				if (socialUser.id.provider == ProviderType.facebook) {
					u.facebookId = socialUser.id.id;
				} else if (socialUser.id.provider == ProviderType.twitter) {
					u.twitterId = socialUser.id.id;
				} else if (socialUser.id.provider == ProviderType.google) {
					u.googleId = socialUser.id.id;
				}
				u.avatarUrl = socialUser.avatarUrl;
				u.create();

				Cache.set("su", socialUser, "15mn");
				session.put("socialauth", "0");
			}
		}
	}

	@Override
	public String createActivation(SocialUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean activate(String uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deletePendingActivations() {
		// TODO Auto-generated method stub

	}

	@Override
	public SocialUser find(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPasswordReset(SocialUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SocialUser fetchForPasswordReset(String username, String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableResetCode(String username, String uuid) {
		// TODO Auto-generated method stub
		
	}

}
