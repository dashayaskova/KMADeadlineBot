package model.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.Community;

public class CommunityDaoMySql implements CommunityDao {
	
	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private Connection connection;
	private Statement statement;

	public CommunityDaoMySql() {
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
	public Set<Community> select() {
		Set<String> communityNames = new HashSet<>();
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery("SELECT community_name_sn FROM community;");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(communityNames);
	}

	@Override
	public Community select(String communityName) {
		Set<String> communityNames = Stream.of(communityName).collect(Collectors.toSet());
		return select(communityNames).stream().findAny().get();
	}

	@Override
	public Set<Community> select(Set<String> communityNames) {
		Set<Community> communities = new HashSet<>();
		openConnection();
		communityNames.forEach(communityName -> {
			try {
				Set<Long> memberIds = new HashSet<>();
				ResultSet memberIdSet = statement.executeQuery(
						"SELECT user_id FROM user_community WHERE community_name_sn = '" + communityName + "';");
				while(memberIdSet.next()) {
					memberIds.add(memberIdSet.getLong("user_id"));
				}
				
				Set<Long> adminIds = new HashSet<>();
				ResultSet adminIdSet = statement.executeQuery(
						"SELECT user_id FROM admin_community WHERE community_name_sn = '" + communityName + "';");
				while(adminIdSet.next()) {
					adminIds.add(adminIdSet.getLong("user_id"));
				}
				
				communities.add(new Community(communityName, memberIds, adminIds));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
		return communities;
	}

	@Override
	public void update(Community community) {
		Set<Community> communities = Stream.of(community).collect(Collectors.toSet());
		update(communities);
	}

	@Override
	public void update(Set<Community> communities) {
		communities.forEach(community -> {
			
			Community oldCommunity = select(community.getName());

			openConnection();
			oldCommunity.getMemberIds().stream().filter(oldMemberId -> !community.getMemberIds().contains(oldMemberId))
					.forEach(memberId -> {
						try {
							statement.execute("DELETE FROM user_community WHERE user_id = " + memberId + ";");
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});

			oldCommunity.getAdminIds().stream().filter(oldAdminId -> !community.getAdminIds().contains(oldAdminId))
					.forEach(adminId -> {
						try {
							statement.execute("DELETE FROM admin_community WHERE user_id = " + adminId + ";");
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});
		
			community.getMemberIds().stream().filter(memberId -> !oldCommunity.getMemberIds().contains(memberId))
					.forEach(memberId -> {
						try {
							statement.execute(String.format(
									"INSERT INTO user_community (user_id, community_name_sn) VALUES (%d, '%s');",
									memberId, community.getName()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});

			community.getAdminIds().stream().filter(adminId -> !oldCommunity.getAdminIds().contains(adminId))
					.forEach(adminId -> {
						try {
							statement.execute(String.format(
									"INSERT INTO admin_community (user_id, community_name_sn) VALUES (%d, '%s');",
									adminId, community.getName()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});
			closeConnection();
		});
	}

	@Override
	public void delete(String communityName) {
		Set<String> communityNames = Stream.of(communityName).collect(Collectors.toSet());
		delete(communityNames);
	}

	@Override
	public void delete(Set<String> communityNames) {
		openConnection();
		communityNames.forEach(communityName -> {
			try {
				statement.execute("DELETE FROM community WHERE community_name_sn = '" + communityName + "';");
				statement.execute("DELETE FROM user_community WHERE community_name_sn = '" + communityName + "';");
				statement.execute("DELETE FROM admin_community WHERE community_name_sn = '" + communityName + "';");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}

	@Override
	public boolean contains(String communityName) {
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery(
					"SELECT * FROM community WHERE community_name_sn = '" + communityName + "';");
			return communityNameSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return false;
	}

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
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(communityNames);
	}

	@Override
	public Set<Community> selectByMemberId(long memberId) {
		Set<String> communityNames = new HashSet<>();
		openConnection();
		try {
			ResultSet communityNameSet = statement.executeQuery(
					"SELECT community_name_sn FROM user_community WHERE user_id = " + memberId + ";");
			while(communityNameSet.next()) {
				communityNames.add(communityNameSet.getString("community_name_sn"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(communityNames);
	}

}
