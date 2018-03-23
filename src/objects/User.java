package objects;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User {

	// data fields
	
	/** this value is recieve from telegram */
	private final long id; // is used as a primary key in the database
	private Map<Deadline, List<Date>> deadlineDates; // times to remind

	// constructors with parameters
	public User(long id) {
		this.id = id;
		this.userCommunities = new HashSet<Community>();
		this.userDeadlines = new HashSet<Deadline>();
	}

	public User(long id, Map<Deadline, List<Date>> deadlineDates) {
		this.id = id;
		this.deadlineDates = deadlineDates;
	}

	// getters and setters
	public long getId() {
		return this.userID;
	}

	public Set<Community> getCommunities() {
		return userCommunities;
	}

	/** REWRITE **/
	public Set<Long> getCommunitiesID() {
		return new HashSet<Long>();
	}

	/** REWRITE **/
	public Map<Deadline, List<Date>> getDeadlineStates() {
		return new HashMap<Deadline, List<Date>>();
	}

	/** REWRITE **/
	public Map<Long, List<Date>> getDeadlineIDStates() {
		return new HashMap<Long, List<Date>>();
	}

	public Set<Deadline> getDeadlines() {
		return userDeadlines;
	}

	/** REWRITE **/
	public Set<Long> getDeadlinesID() {
		return new HashSet<Long>();
	}

	/** REWRITE **/
	public Set<Community> getCommunitiesWhereAdmin() {
		return new HashSet<Community>();
	}

	/** REWRITE **/
	public Set<Long> getCommunitiesIDWhereAdmin() {
		return new HashSet<Long>();
	}

	// methods
	public boolean isGlobalAdmin() {
		// TODO implement
		return true;
	}

	public boolean isAdmin(Community community) {
		// TODO implement
		return true;
	}
	
	
	// methods from class Object
	
	/** compare by user's id */
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof User) {
			return id == ((User) object).id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return String.valueOf(id).hashCode();
	}
	
	@Override
	public String toString() {
		return Long.toString(id);
	}
}