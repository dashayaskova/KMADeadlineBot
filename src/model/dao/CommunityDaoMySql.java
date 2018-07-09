package model.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.Community;

public class CommunityDaoMySql implements CommunityDao {
	
	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot?useSSL=true";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private Connection connection;
	private Statement statement;

	public CommunityDaoMySql() {
		try {
			Driver driver = new FabricMySQLDriver();
			DriverManager.registerDriver(driver);
		} catch (SQLException e) { e.printStackTrace(); }
	}

	private void openConnection() {
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			// connection.getClientInfo().setProperty("useSSL", "false");
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
	
	/** get all communities from database */
	@Override
	public Set<Community> select() {
		
		Set<String> communityNames = new HashSet<>();
		
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery("SELECT community_name_sn FROM community;");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(communityNames);	
	}

	/** wrap community name to set and call select(Set<String> communityNames) method */
	@Override
	public Community select(String communityName) {
		
		Set<String> set = new HashSet<>();
		set.add(communityName);
		return select(set).stream().findAny().get(); // get single community from set
	}

	/** get communities by communityNames */
	@Override
	public Set<Community> select(Set<String> communityNames) {
		
		Set<Community> communities = new HashSet<>();
		
		openConnection();
		communityNames.forEach(communityName -> {
			try {
				// select memberIds
				Set<Long> memberIds = new HashSet<>();
				ResultSet memberIdSet = statement.executeQuery(
						"SELECT user_id FROM user_community WHERE community_name_sn = '" + communityName + "';");
				while(memberIdSet.next()) {
					memberIds.add(memberIdSet.getLong("user_id"));
				}
				
				// select adminIds
				Set<Long> adminIds = new HashSet<>();
				ResultSet adminIdSet = statement.executeQuery(
						"SELECT user_id FROM admin_community WHERE community_name_sn = '" + communityName + "';");
				while(adminIdSet.next()) {
					adminIds.add(adminIdSet.getLong("user_id"));
				}
				
				// build community
				communities.add(new Community(communityName, memberIds, adminIds));
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
		
		return communities;
	}

	/** wrap community to set and call update(Set<Community> communities) method */
	@Override
	public void update(Community community) {
		
		Set<Community> set = new HashSet<>();
		set.add(community);
		update(set);
	}

	/** updates lists of members and admins in database */
	// add 'rename' option
	@Override
	public void update(Set<Community> communities) { 
		
		communities.forEach(community -> {
			
			Community oldCommunity = select(community.getName());

			openConnection();
			
			// delete old members
			oldCommunity.getMemberIds().stream()
				// find old members
				.filter(oldMemberId -> !community.getMemberIds().contains(oldMemberId))
				// delete old members
				.forEach(memberId -> {
					try {
						statement.execute("DELETE FROM user_community WHERE user_id = " + memberId + ";");
					} catch (SQLException e) { e.printStackTrace(); }
				});
			
			// delete old admins
			oldCommunity.getAdminIds().stream()
				// find old admins
				.filter(oldAdminId -> !community.getAdminIds().contains(oldAdminId))
				// delete old admins
				.forEach(adminId -> {
					try {
						statement.execute("DELETE FROM admin_community WHERE user_id = " + adminId + ";");
					} catch (SQLException e) { e.printStackTrace(); }
				});
			
			// add new members
			community.getMemberIds().stream()
				// find new members
				.filter(memberId -> !oldCommunity.getMemberIds().contains(memberId))
				// add new members
				.forEach(memberId -> {
					try {
						statement.execute(String.format(
							"INSERT INTO user_community (user_id, community_name_sn) VALUES (%d, '%s');",
							memberId, community.getName()));
					} catch (SQLException e) { e.printStackTrace(); }
				});
			
			// add new admins
			community.getAdminIds().stream()
				// find new admins
				.filter(adminId -> !oldCommunity.getAdminIds().contains(adminId))
				// add new admins
				.forEach(adminId -> {
					try {
						statement.execute(String.format(
							"INSERT INTO admin_community (user_id, community_name_sn) VALUES (%d, '%s');",
							adminId, community.getName()));
					} catch (SQLException e) { e.printStackTrace(); }
				});
			closeConnection();
		});
	}

	/** wrap community name to set and call delete(Set<String> communityNames) method */
	@Override
	public void delete(String communityName) {
		
		Set<String> set = new HashSet<>();
		set.add(communityName);
		delete(set);
	}
	
	/** delete all communityNames from community, admin_community and user_community */
	@Override
	public void delete(Set<String> communityNames) {
		openConnection();
		communityNames.forEach(communityName -> {
			try {
				statement.execute("DELETE FROM user_community WHERE community_name_sn = '" + communityName + "';");
				statement.execute("DELETE FROM admin_community WHERE community_name_sn = '" + communityName + "';");
				statement.execute("DELETE FROM deadline WHERE community_name_sn = '" + communityName + "';");	
				statement.execute("DELETE FROM community WHERE community_name_sn = '" + communityName + "';");	
				
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
	}

	/** return true if database contains community which name is 'communityName' */
	@Override
	public boolean contains(String communityName) {
		
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery(
				"SELECT * FROM community WHERE community_name_sn = '" + communityName + "';");
			return communityNameSet.next();
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return false;
	}

	/** get community names for admin */
	@Override
	public Set<Community> selectByAdminId(long adminId) {
		
		Set<String> communityNames = new HashSet<>();
		
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery(
				"SELECT community_name_sn FROM admin_community WHERE user_id = " + adminId + ";");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(communityNames);
	}
	
	/** get community names for member */
	@Override
	public Set<Community> selectByMemberId(long memberId) {
		
		Set<String> communityNames = new HashSet<>();
		
		openConnection();
		// get community names for member
		try {
			ResultSet communityNameSet = statement.executeQuery(
				"SELECT community_name_sn FROM user_community WHERE user_id = " + memberId + ";");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		// get communities by community names
		return select(communityNames);
	}
	
	@Override
	public Set<String> selectNamesByMemberId(long memberId) { // TODO change implementation
		return selectByMemberId(memberId).stream()
				.map(Community::getName)
				.collect(Collectors.toSet());
	}
	
	@Override
	public Set<String> selectNamesByAdminId(long adminId) { // TODO change implementation
		return selectByAdminId(adminId).stream()
				.map(Community::getName)
				.collect(Collectors.toSet());
	}

	/** create new community and add it to database */
	@Override
	public Community create(String communityName, Set<Long> memberIds, Set<Long> adminIds) {
		
		openConnection();
		try {
			// add community add date created to community table
			statement.execute(
				String.format("INSERT INTO community (community_name_sn, date_created_dt) VALUES ('%s', '%s');",
					communityName, java.sql.Date.valueOf(LocalDate.now())));

			// add member ids to user_community table
			for (long memberId : memberIds) {
				statement.execute(
					String.format("INSERT INTO user_community (user_id, community_name_sn) VALUES (%d, '%s');",
						memberId, communityName));
			}

			// add admin ids to admin_community table
			for (long adminId : adminIds) {
				statement.execute(
					String.format("INSERT INTO admin_community (user_id, community_name_sn) VALUES (%d, '%s');",
						adminId, communityName));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		// build community object
		return new Community(communityName, memberIds, adminIds);
	}
	
	/** get all community names from database */
	@Override
	public Set<String> selectNames() {
		
		Set<String> communityNames = new HashSet<>();
		
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery("SELECT community_name_sn FROM community;");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return communityNames;
	}
}
