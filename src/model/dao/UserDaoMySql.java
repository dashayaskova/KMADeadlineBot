package model.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.User;

public class UserDaoMySql implements UserDao {
	
	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot?useSSL=true";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "1999";

	private Connection connection;
	private Statement statement;

	public UserDaoMySql() {
		
		try {
			Driver driver = new FabricMySQLDriver();
			DriverManager.registerDriver(driver);
		} catch (SQLException e) { e.printStackTrace(); }
	}

	private void openConnection() {
		
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			//connection.getClientInfo().setProperty("useSSL", "false");
			statement = connection.createStatement();
		} catch (SQLException e) { e.printStackTrace(); }
	}

	private void closeConnection() {
		
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			if (statement != null && !statement.isClosed()) {
				statement.close();
			}
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	/** get all users from database */
	@Override
	public Set<User> select() {
		
		Set<Long> userIds = new HashSet<>();
		
		openConnection();
		try {
			ResultSet userSet = statement.executeQuery("SELECT user_id FROM user;");
			while(userSet.next()) {
				userIds.add(userSet.getLong("user_id"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(userIds);
	}
	
	
	//	public static void main(String[] args) {
	//		Set<User> users = DaoContainer.userDao.select();
	//		System.out.println(users);
	//		
	//		System.out.println("insert #228 #229");
	//		User user1 = new User(228l);
	//		User user2 = new User(229l);
	//		Set<User> newUsers = Stream.of(user1, user2).collect(Collectors.toSet());
	//		DaoContainer.userDao.insert(newUsers);
	//		
	//		users = DaoContainer.userDao.select();
	//		System.out.println(users);
	//		
	//		boolean contains = DaoContainer.userDao.contains(312);
	//		System.out.println(contains);
	//		
	//	}
	
	/** wrap user to set and call insert(Set<User> users) method */
	@Override
	public void insert(User user) {
		
		Set<User> set = new HashSet<>();
		set.add(user);
		insert(set);	
	}
	
	/** add users to database */
	@Override
	public void insert(Set<User> users) {
		
		openConnection();
		users.forEach(user -> {
			try {
				
				// if contains user
				ResultSet userSet = statement.executeQuery("SELECT * FROM user WHERE user_id = " + user.getId() + ";");
				if (userSet.next()) {
					
					// delete user
					closeConnection();
					delete(user.getId());
					openConnection();
				}

				// add user's id to 'user' table
				statement.execute("INSERT INTO user (user_id) VALUES ('" + user.getId() + "');");

				// add deadlines and dates to remind to 'deadline_date' table
				for (long deadlineId : user.getDeadlineIdDates().keySet()) {
					for (Date date : user.getDeadlineIdDates().get(deadlineId)) {
						statement.execute(String.format(
							"INSERT INTO deadline_date (user_id, deadline_id, date) VALUES (%d, %d, '%s');",
							user.getId(), deadlineId, 
							new java.sql.Date(date.getTime()) + " " + new Time(date.getTime())));
					}
				}
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
	}
	
	/** wrap userId to set and call select(Set<Long> userIds) mehtod */
	@Override
	public User select(long userId) {
		
		Set<Long> set = new HashSet<>();
		set.add(userId);
		return select(set).stream().findAny().get(); // get single element of set
	}
	
	/** get users by ids */
	@Override
	public Set<User> select(Set<Long> userIds) {
		
		Set<User> users = new HashSet<>();
		
		openConnection();
		userIds.forEach(userId -> {
			try {
				
				// if database contains userId
				ResultSet userSet = statement.executeQuery("SELECT * FROM user WHERE user_id = " + userId + ";");
				if(userSet.next()) {
					
					// get user's deadline ids
					Set<Long> deadlineIds = new HashSet<>();
					ResultSet deadlineIdsSet = statement.executeQuery(
							"SELECT deadline_id FROM deadline_date WHERE user_id = " + userId + ";");
					while(deadlineIdsSet.next()) {
						deadlineIds.add(deadlineIdsSet.getLong("deadline_id"));
					}
					
					// build deadlineIdDates from deadlineIds
					Map<Long, Set<Date>> deadlineIdDates = deadlineIds.stream()
						// key - deadline id, value - list of dates to remind
						.collect(Collectors.toMap(deadlineId -> deadlineId, deadlineId -> {
							Set<Date> dates = new HashSet<>();
							try {
								// get dates to remind
								ResultSet datesSet = statement.executeQuery(String.format(
									"SELECT date FROM deadline_date WHERE user_id = %d AND deadline_id = %d;",
									userId, deadlineId));
								while (datesSet.next()) {
									dates.add(new Date(datesSet.getTime("date").getTime()));
								}
							} catch (SQLException e) { e.printStackTrace(); }
							return dates;
						}));
					
					users.add(new User(userId, deadlineIdDates));
				
				// if database does't contain userId
				//how we can add user to our database in this place, if the bot cannot write to users first
				} else {
					/*
					User user = new User(userId, "");
					insert(user);
					users.add(user);
					*/
				}
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
		
		return users;
	}
	
	/** wrap user to set and call update(Set<User> user) method */
	@Override
	public void update(User user) {
		
		Set<User> set = new HashSet<>();
		set.add(user);
		update(set);
	}
	
	/** update user deadline dates */
	@Override
	public void update(Set<User> users) { // TODO change implementation
		
		delete(users.stream().map(User::getId).collect(Collectors.toSet()));
		insert(users);
	}
	
	/** wrap userId to set and call delete(Set<Long> userIds) method */
	@Override
	public void delete(long userId) {
		
		Set<Long> set = new HashSet<>();
		set.add(userId);
		delete(set);
	}
	
	/** delete user ids from 'user' and 'deadline_date' tables */
	@Override
	public void delete(Set<Long> userIds) {
		openConnection();
		userIds.forEach(userId -> {
			try {
				statement.execute("DELETE FROM admin_community WHERE user_id = " + userId + ";");
				statement.execute("DELETE FROM deadline_date WHERE user_id = " + userId + ";");
				statement.execute("DELETE FROM user_community WHERE user_id = " + userId + ";");
				statement.execute("DELETE FROM user WHERE user_id = " + userId + ";");
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
	}
	
	/** return true if 'global_admin' table contains userId */
	@Override
	public boolean isGlobalAdmin(long userId) {
		
		openConnection();
		try {
			ResultSet globalAdminSet = statement.executeQuery(
				"SELECT user_id FROM global_admin WHERE user_id = " + userId + ";");
			return globalAdminSet.next();
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return false;
	}

	/** return true if 'user' table contains userId */
	@Override
	public boolean contains(long userId) {
		
		openConnection();
		try {
			ResultSet userSet = statement.executeQuery("SELECT user_id FROM user WHERE user_id = " + userId + ";");
			return userSet.next();
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return false;
	}
	
	//	public Set<String> selectCommunityNames(long userId, boolean member /* member=true admin=false */) {
	//		Set<String> communityNames = new HashSet<>();
	//		openConnection();
	//		try {
	//			ResultSet communityNamesSet = statement.executeQuery("SELECT community_name_sn FROM "
	//					+ (member ? "user_community" : "admin_community") + " WHERE user_id = " + userId + ";");
	//			while (communityNamesSet.next()) {
	//				communityNames.add(communityNamesSet.getString("community_name_sn"));
	//			}
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		closeConnection();
	//		return communityNames;
	//	}
}
