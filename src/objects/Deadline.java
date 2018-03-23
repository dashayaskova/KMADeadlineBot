package objects;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Deadline {

	// data fields
	private long deadlineID;
	private Date deadlineDate;
	private String deadlineDescription;
	private long deadlineChatID;
	private Set<Long> deadlineMessagesID;
	
	// default constructor
	/** REWRITE **/
	public Deadline() {
		this.deadlineID = 0L;
		this.deadlineDate = new Date();
		this.deadlineDescription = "";
		this.deadlineChatID = 0L;
		this.deadlineMessagesID = new HashSet<Long>();
	}
	
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
	public long getID() {
		return this.deadlineID;
	}
	
	public String getDescription() {
		return this.deadlineDescription;
	}
	
	public long getChatID() {
		return this.deadlineChatID;
	}
	
	public Set<Long> getMessagesID() {
		return this.deadlineMessagesID;
	}
	
	public Date getDate() {
		return this.deadlineDate;
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
