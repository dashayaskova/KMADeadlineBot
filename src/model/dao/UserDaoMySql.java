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
import java.util.stream.Stream;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.User;

public class UserDaoMySql implements UserDao {
	
	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private Connection connection;
	private Statement statement;

	public UserDaoMySql() {
		try {
			Driver driver = new FabricMySQLDriver();
			DriverManager.registerDriver(driver);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void openConnection() {
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			//connection.getClientInfo().setProperty("useSSL", "false");
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			if (statement != null && !statement.isClosed()) {
				statement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<User> select() {
		Set<Long> userIds = new HashSet<>();
		openConnection();
		try {
			ResultSet userSet = statement.executeQuery("SELECT user_id FROM user;");
			while(userSet.next()) {
				userIds.add(userSet.getLong("user_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public void insert(User user) {
		Set<User> users = Stream.of(user).collect(Collectors.toSet());
		insert(users);	
	}
	
	@Override
	public void insert(Set<User> users) {
		openConnection();
		users.forEach(user -> {
			try {
				ResultSet userSet = statement.executeQuery("SELECT * FROM user WHERE user_id = " + user.getId() + ";");
				if (userSet.next()) {
					closeConnection();
					delete(user.getId());
					openConnection();
				}

				statement.execute("INSERT INTO user (user_id) VALUES (" + user.getId() + ");");

				for (long deadlineId : user.getDeadlineIdDates().keySet()) {
					for (Date date : user.getDeadlineIdDates().get(deadlineId)) {
						statement.execute(String.format(
								"INSERT INTO deadline_date (user_id, deadline_id, date) VALUES (%d, %d, '%s');",
								user.getId(), deadlineId,
								new java.sql.Date(date.getTime()) + " " + new Time(date.getTime())));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}
	
	@Override
	public User select(long userId) {
		Set<Long> userIds = Stream.of(userId).collect(Collectors.toSet());
		return select(userIds).stream().findAny().get();
	}

	
	@Override
	public Set<User> select(Set<Long> userIds) {
		Set<User> users = new HashSet<>();
		openConnection();
		userIds.forEach(userId -> {
			try {
				ResultSet userTable = statement.executeQuery("SELECT * FROM user WHERE user_id = " + userId + ";");
				if(userTable.next()) {
					ResultSet deadlineIdsSet = statement.executeQuery(
							"SELECT deadline_id FROM deadline_date WHERE user_id = " + userId + ";");
					Set<Long> deadlineIds = new HashSet<>();
					while(deadlineIdsSet.next()) {
						deadlineIds.add(deadlineIdsSet.getLong("deadline_id"));
					}
					
					Map<Long, Set<Date>> deadlineIdDates = deadlineIds.stream()
							.collect(Collectors.toMap(deadlineId -> deadlineId, deadlineId -> {
								Set<Date> dates = new HashSet<>();
								try {
									ResultSet datesSet = statement.executeQuery(String.format(
											"SELECT date FROM deadline_date WHERE user_id = %d AND deadline_id = %d;",
											userId, deadlineId));
									while (datesSet.next()) {
										dates.add(new Date(datesSet.getTime("date").getTime()));
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
								return dates;
							}));
					users.add(new User(userId, deadlineIdDates));
				} else {
					User user = new User(userId);
					insert(user);
					users.add(user);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
		return users;
	}
	
	@Override
	public void update(User user) {
		Set<User> users = Stream.of(user).collect(Collectors.toSet());
		update(users);
	}
	
	@Override
	public void update(Set<User> users) { // TODO change implementation
		delete(users.stream().map(User::getId).collect(Collectors.toSet()));
		insert(users);
	}
	
	@Override
	public void delete(long userId) {
		Set<Long> userIds = Stream.of(userId).collect(Collectors.toSet());
		delete(userIds);
	}
	
	@Override
	public void delete(Set<Long> userIds) {
		openConnection();
		userIds.forEach(userId -> {
			try {
				statement.execute("DELETE FROM user WHERE user_id = " + userId + ";");
				statement.execute("DELETE FROM deadline_date WHERE user_id = " + userId + ";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}
	
	@Override
	public boolean isGlobalAdmin(long userId) {
		openConnection();
		try {
			ResultSet globalAdminSet = statement.executeQuery(
					"SELECT user_id FROM global_admin WHERE user_id = " + userId + ";");
			return globalAdminSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return false;
	}

	@Override
	public boolean contains(long userId) {
		openConnection();
		try {
			ResultSet userSet = statement.executeQuery("SELECT user_id FROM user WHERE user_id = " + userId + ";");
			return userSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
