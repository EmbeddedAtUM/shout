package org.whispercomm.shout.provider;

import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;

public class ShoutProviderContract {
	
	public static Shout retrieveShoutById(long id){
		return null;
	};
	
	public static long storeShout(Shout shout){
		return -1;
	}
	
	public static User retrieveUserById(long id){
		return null;
	}

	public static long storeUser(User user){
		return -1;
	}
	
	public static Tag retrieveTagById(long id){
		return null;
	}
	
	public static List<Tag> retrieveTagsByShoutId(long id){
		return null;
	}
	
	public static long storeTag(Tag tag){
		return -1;
	}
}
