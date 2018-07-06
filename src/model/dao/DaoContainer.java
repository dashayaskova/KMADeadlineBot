package model.dao;

public class DaoContainer {
	
	public static DeadlineDao deadlineDao = new DeadlineDaoMySql();
	public static UserDao userDao = new UserDaoMySql();
	public static CommunityDao communityDao = new CommunityDaoMySql();
	
}
