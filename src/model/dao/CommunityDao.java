package model.dao;

import java.util.Set;

import model.Community;

public interface CommunityDao {
	
	Community create(String communityName, Set<Long> memberIds, Set<Long> adminIds);
	
	Set<Community> select();
	Set<String> selectNames();
	
	Community select(String communityName);
	Set<Community> select(Set<String> communityName);
	
	void update(Community community);
	void update(Set<Community> communities);
	
	void delete(String communityName);
	void delete(Set<String> communityName);
	
	boolean contains(String communityName);
	
	Set<Community> selectByAdminId(long adminId);
	Set<Community> selectByMemberId(long memberId);
	
	Set<String> selectNamesByMemberId(long memberId);
	Set<String> selectNamesByAdminId(long adminId);
	
}
