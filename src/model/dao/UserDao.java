package model.dao;

import java.util.Set;

import model.User;

public interface UserDao {
	
	void insert(User user);
	void insert(Set<User> users);
	
	User select(long userId);
	Set<User> select(Set<Long> userIds);
	Set<User> select();
	
	void update(User user);
	void update(Set<User> users);
	
	void delete(long userId);
	void delete(Set<Long> userIds);
	
	boolean contains(long userId);
	
	boolean isGlobalAdmin(long userId);
	
}
