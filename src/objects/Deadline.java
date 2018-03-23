package objects;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Deadline {

	// data fields
	private final long id;
	private Date date;
	private String description;
	private long chatId;
	private Set<Long> messageIds;
	
	// default constructor

	
	// constructors with parameters
	/** REWRITE **/
	public Deadline(long deadlineID) {
		this.deadlineID = deadlineID;
		this.deadlineDate = new Date();
		this.deadlineDescription = "";
		this.deadlineChatID = 0L;
		this.deadlineMessagesID = new HashSet<Long>();
	}
	
	/** REWRITE **/
	public Deadline(long deadlineID, Date deadlineDate) {
		this.deadlineID = deadlineID;
		this.deadlineDate = deadlineDate;
		this.deadlineDescription = "";
		this.deadlineChatID = 0L;
		this.deadlineMessagesID = new HashSet<Long>();
	}
	
	/** REWRITE **/
	public Deadline(long deadlineID, Date deadlineDate, String description) {
		this.deadlineID = deadlineID;
		this.deadlineDate = deadlineDate;
		this.deadlineDescription = description;
		this.deadlineChatID = 0L;
		this.deadlineMessagesID = new HashSet<Long>();
	}
	
	/**		FINISH ALL CONSTRUCTORS		**/
	
	// getters and setters
	public long getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public long getChatId() {
		return chatId;
	}
	
	public Set<Long> getMessageIds() {
		return messageIds;
	}
	
	public Date getDate() {
		return date;
	}
	
	// methods
	/** REWRITE **/
	public Duration getTimeRemaining() {
		return Duration.ofHours(0);
	}

	/** REWRITE **/
	public Community getCommunity() {
		return new Community();
	}
}