package model.dao;

import java.sql.Date;
import java.util.Set;

import model.Deadline;

public interface DeadlineDao {
	
	Deadline create(Date date, String description, String communityName, long chatId, Set<Long> messageIds);
	
	void insert(Deadline deadline);
	void insert(Set<Deadline> deadlines);

	void delete(long deadlineId);
	void delete(Set<Long> deadlineIds);
	
	void update(Deadline deadline);
	void update(Set<Deadline> deadline);

	Set<Deadline> select(); // select all
	Deadline select(long deadlineId);
	Set<Deadline> select(Set<Long> deadlineIds);
	Set<Deadline> select(Date from, Date to);
	Set<Deadline> selectForUser(long userId);
	Set<Deadline> selectForCommunity(String communityName);
	
	boolean contains(long deadlineId);
	
}
