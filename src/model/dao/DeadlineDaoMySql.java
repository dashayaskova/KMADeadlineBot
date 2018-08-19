package model.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mysql.fabric.jdbc.FabricMySQLDriver;

import model.Community;
import model.Deadline;

public class DeadlineDaoMySql implements DeadlineDao {

	private static final String URL = "jdbc:mysql://localhost:3306/kmadeadlinebot?useSSL=true";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private Connection connection;
	private Statement statement;

	public DeadlineDaoMySql() {
		
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

	//	public static void main(String[] args) throws SQLException {
	//		DeadlineDaoMySql dao = new DeadlineDaoMySql();
	//		dao.openConnection();
	//		ResultSet set = dao.statement.executeQuery("SELECT date FROM deadline_date;");
	//		set.next();
	//		System.out.println(new Time(set.getDate("date").getTime()));
	//		dao.closeConnection();
	//	}
	//	
	//	private static void printAll() { // TODO remove
	//		Set<Deadline> deadlines = DaoContainer.deadlineDao.select();
	//		System.out.println(deadlines);
	//	}
	
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

	//	@Override
	private void insert(Deadline deadline) {
		Set<Deadline> deadlineSet = Stream.of(deadline).collect(Collectors.toSet());
		insert(deadlineSet);
	}

	//	@Override
	private void insert(Set<Deadline> deadlines) {
		openConnection();
		deadlines.forEach(deadline -> {
			try {
				statement.execute(String.format(
					"INSERT INTO deadline (deadline_id, date_of_deadline, description_sn, community_name_sn) "
					+ "VALUES (%d, '%s', '%s', '%s');", deadline.getId(),
					new java.sql.Date(deadline.getDate().getTime()) + " " + new Time(deadline.getDate().getTime()),
						deadline.getDescription(), deadline.getCommunityName()));

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
						"INSERT INTO deadline_date (user_id, deadline_id, date) VALUES (%d, %d, '%s');", userId,
						deadline.getId(), new java.sql.Date(cal.getTimeInMillis()) + " " + new Time(cal.getTimeInMillis())));
				}
			} catch(SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
	}

	@Override
	public void delete(long deadlineId) {
		
		Set<Long> set = new HashSet<>();
		set.add(deadlineId);
		delete(set);
	}
	
	@Override
	public void delete(Set<Long> deadlineIds) {
		openConnection();
		deadlineIds.forEach(deadlineId -> {
			try {
				statement.execute("DELETE FROM deadline_message WHERE deadline_id = " + deadlineId + ";");
				statement.execute("DELETE FROM deadline_date WHERE deadline_id = " + deadlineId + ";");
				statement.execute("DELETE FROM deadline WHERE deadline_id = " + deadlineId + ";");
			} catch (SQLException e) { e.printStackTrace(); }
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
					new java.sql.Date(deadline.getDate().getTime()) + " " + new Time(deadline.getDate().getTime()),
					deadline.getDescription(), deadline.getCommunityName(), deadline.getId()));
			} catch (SQLException e) { e.printStackTrace(); }
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
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(deadlineIds);
	}

	@Override
	public Deadline select(long deadlineId) {
		
		Set<Long> set = new HashSet<>();
		set.add(deadlineId);
		
		Set<Deadline> deadlines = select(set);
		return deadlines.stream().findAny().get(); // get single element from deadlines set
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
				
				Time time = deadlineSet.getTime("date_of_deadline");
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
				deadlines.add(new Deadline(deadlineId, new Date(time.getTime() + date.getTime()), description, communityName, chatId, messageIds));
			} catch (SQLException e) { e.printStackTrace(); }
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
		} catch (SQLException e) { e.printStackTrace(); }
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
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(deadlineIds);
	}

	@Override
	public Set<Deadline> selectForCommunity(String communityName) {
		
		Set<Long> deadlineIds = new HashSet<>();
		
		openConnection();
		try {
			ResultSet deadlineIdSet = statement.executeQuery(
				"SELECT deadline_id FROM deadline WHERE community_name_sn = '" + communityName + "';");
			while(deadlineIdSet.next()) {
				deadlineIds.add(deadlineIdSet.getLong("deadline_id"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return select(deadlineIds);
	}
	
	@Override
	public boolean contains(long deadlineId) {
		
		openConnection();
		try {
			ResultSet deadlineSet = statement.executeQuery("SELECT * FROM deadline WHERE deadline_id = " + deadlineId + ";");
			return deadlineSet.next();
		} catch (SQLException e) { e.printStackTrace(); }
		closeConnection();
		
		return false;
	}
	
	public void insertDeadlineDates(long userId, long deadlineId, Set<Date> dates) {
		
		openConnection();
		dates.forEach(date -> {
			try {
				
				// if database doesn't contain deadline date
				ResultSet deadlineDateSet = statement.executeQuery(String.format(
					"SELECT * FROM deadline_date WHERE user_id = %d AND deadline_id = %d AND date = '%s';",
					userId, deadlineId, new java.sql.Date(date.getTime()) + " " + new Time(date.getTime())));
				if(!deadlineDateSet.next()) {
					
					// add deadline date
					statement.execute(String.format(
						"INSERT INTO deadline_date (user_id, deadline_id, date) VALUES (%d, %d, '%s')", userId,
						deadlineId, new java.sql.Date(date.getTime()) + " " + new Time(date.getTime())));
				}
			} catch (SQLException e) { e.printStackTrace(); }
		});
		closeConnection();
	}
	
	public void insertDeadlineDates(String communityName, long deadlineId, Set<Date> dates) {
		
		Community community = DaoContainer.communityDao.select(communityName);
		community.getMemberIds().forEach(memberId -> insertDeadlineDates(memberId, deadlineId, dates));
	}
	
}
