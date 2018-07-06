package model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.Deadline;

public class DeadlineDaoMySql implements DeadlineDao {

	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private Connection connection;
	private Statement statement;

	public DeadlineDaoMySql() {
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

	public static void main(String[] args) {
		printAll();
		
		
		Set<Long> messageIds = Stream.of(100l, 200l, 300l).collect(Collectors.toSet());

		Deadline deadline = DaoContainer.deadlineDao.create(new Date(1234384348L),
				"this deadline was created in DeadlieDaoMySql class", "firstcommunity", 424L, messageIds);
		
		System.out.println("new deadline: " + deadline);
		
		printAll();
	}
	
	private static void printAll() { // TODO remove
		Set<Deadline> deadlines = DaoContainer.deadlineDao.select();
		System.out.println(deadlines);
	}
	
	@Override
	public Deadline create(Date date, String description, String communityName, long chatId, Set<Long> messageIds) {
		
		Random random = new Random();
		long id;
		
		do {
			id = Math.abs(random.nextLong());
		} while(contains(id));
		
		Deadline deadline = new Deadline(id, date, description, communityName, chatId, messageIds);
		insert(deadline);
		return deadline;
	}

	@Override
	public void insert(Deadline deadline) {
		Set<Deadline> deadlineSet = Stream.of(deadline).collect(Collectors.toSet());
		insert(deadlineSet);
	}

	@Override
	public void insert(Set<Deadline> deadlines) {
		openConnection();
		deadlines.forEach(deadline -> {
			try {
				statement.execute(String.format(
						"INSERT INTO deadline (deadline_id, date_of_deadline, description_sn, community_name_sn) "
								+ "VALUES (%d, '%s', '%s', '%s');",
						deadline.getId(), deadline.getDate(), deadline.getDescription(), deadline.getCommunityName()));

				for (long messageId : deadline.getMessageIds()) {
					statement.execute(String.format(
							"INSERT INTO deadline_message (message_id, chat_id, deadline_id) VALUES (%d, %d, %d);",
							messageId, deadline.getChatId(), deadline.getId()));
				}
				
				for (long userId : DaoContainer.communityDao.select(deadline.getCommunityName()).getMemberIds()) {

					Calendar cal = new GregorianCalendar();
					cal.setTime(deadline.getDate());
					cal.add(Calendar.DATE, -1);
					
					statement.execute(String.format(
							"INSERT INTO deadline_date (user_id, deadline_id, date) VALUES (%d, %d, '%s');",
							userId, deadline.getId(), new Date(cal.getTimeInMillis())));
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}

	@Override
	public void delete(long deadlineId) {
		Set<Long> deadlineIds = Stream.of(deadlineId).collect(Collectors.toSet());
		delete(deadlineIds);
	}
	
	@Override
	public void delete(Set<Long> deadlineIds) {
		openConnection();
		deadlineIds.forEach(deadlineId -> {
			try {
				statement.execute("DELETE FROM deadline WHERE deadline_id = " + deadlineId + ";");
				statement.execute("DELETE FROM deadline_message WHERE deadline_id = " + deadlineId + ";");
				statement.execute("DELETE FROM deadline_date WHERE deadline_id = " + deadlineId + ";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}
	
	@Override
	public void update(Deadline deadline) {
		Set<Deadline> deadlineSet = Stream.of(deadline).collect(Collectors.toSet());
		update(deadlineSet);
	}

	@Override
	public void update(Set<Deadline> deadlines) {
		openConnection();
		deadlines.forEach(deadline -> {
			try {
				statement.execute(String.format(
						"UPDATE deadline SET date_of_deadline = '%s', description_sn = '%s', community_name_sn = '%s'"
								+ " WHERE deadline_id = %d;",
						new Date(deadline.getDate().getTime()).toString(), deadline.getDescription(),
						deadline.getCommunityName(), deadline.getId()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
	}
	
	@Override
	public Set<Deadline> select() {
		Set<Long> deadlineIds = new HashSet<>();
		openConnection();
		try {
			ResultSet  deadlineIdSet = statement.executeQuery("SELECT deadline_id FROM deadline;");
			while(deadlineIdSet.next()) {
				deadlineIds.add(deadlineIdSet.getLong("deadline_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(deadlineIds);
	}

	@Override
	public Deadline select(long deadlineId) {
		Set<Deadline> deadlines = select(Stream.of(deadlineId).collect(Collectors.toSet()));
		return deadlines.stream().findAny().get();
	}
	
	@Override
	public Set<Deadline> select(Set<Long> deadlineIds) {
		Set<Deadline> deadlines = new HashSet<>();
		openConnection();
		deadlineIds.forEach(deadlineId -> {
			try {
				ResultSet deadlineSet = statement.executeQuery(
						"SELECT * FROM deadline WHERE deadline_id = " + deadlineId + ";");
				deadlineSet.next();
				
				Date date = deadlineSet.getDate("date_of_deadline");
				String description = deadlineSet.getString("description_sn");
				String communityName = deadlineSet.getString("community_name_sn");
				
				ResultSet messageIdSet = statement.executeQuery(
						"SELECT message_id, chat_id FROM deadline_message WHERE deadline_id = " + deadlineId + ";");
				Set<Long> messageIds = new HashSet<>();
				long chatId = -1;
				
				if(messageIdSet.next()) {
					chatId = messageIdSet.getLong("chat_id");
					
					do {
						messageIds.add(messageIdSet.getLong("message_id"));
					} while (messageIdSet.next());
					
				}
				deadlines.add(new Deadline(deadlineId, date, description, communityName, chatId, messageIds));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		closeConnection();
		return deadlines;
	}

	@Override
	public Set<Deadline> select(Date from, Date to) {
		Set<Long> deadlineIds = new HashSet<>();
		openConnection();
		try {
			ResultSet deadlineIdSet = statement.executeQuery(String.format(
					"SELECT deadline_id FROM deadline WHERE date_of_deadline >= '%s' AND date_of_deadline <= '%s';",
					from, to));
			while (deadlineIdSet.next()) {
				deadlineIds.add(deadlineIdSet.getLong("deadline_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(deadlineIds);
	}
	
	@Override
	public Set<Deadline> selectForUser(long userId) {
		Set<Long> deadlineIds = new HashSet<>();
		openConnection();
		try {
			ResultSet deadlineIdSet = statement.executeQuery(
					"SELECT deadline_id FROM deadline_date WHERE user_id = " + userId + ";");
			while(deadlineIdSet.next()) {
				deadlineIds.add(deadlineIdSet.getLong("deadline_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(deadlineIds);
	}

	@Override
	public Set<Deadline> selectForCommunity(String communityName) {
		Set<Long> deadlineIds = new HashSet<>();
		openConnection();
		try {
			ResultSet deadlineIdSet = statement.executeQuery(
					"SELECT deadline_id FROM deadline WHERE community_name_sn = " + communityName + ";");
			while(deadlineIdSet.next()) {
				deadlineIds.add(deadlineIdSet.getLong("deadline_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return select(deadlineIds);
	}
	
	@Override
	public boolean contains(long deadlineId) {
		openConnection();
		try {
			ResultSet deadlineSet = statement.executeQuery("SELECT * FROM deadline WHERE deadline_id = " + deadlineId + ";");
			return deadlineSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		return false;
	}
}
