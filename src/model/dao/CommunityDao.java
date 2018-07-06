package model.dao;

import java.util.Set;

import model.Community;

public interface CommunityDao {
	
	Set<Community> select();
	Community select(String communityName);
	Set<Community> select(Set<String> communityName);
	
	void update(Community community);
	void update(Set<Community> communities);
	
	void delete(String communityName);
	void delete(Set<String> communityName);
	
	boolean contains(String communityName);
	
	Set<Community> selectByAdminId(long adminId);
	Set<Community> selectByMemberId(long memberId);
	
}
