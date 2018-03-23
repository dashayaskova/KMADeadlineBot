package objects;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User {
	
	// data fields
	
	private final long id;
	private Set<Community> communities;
	private Set<Deadline> deadlines;
	
	// default constructor
	public User() {
		this.userID = 0;
		this.userCommunities = new HashSet<Community>();
		this.userDeadlines = new HashSet<Deadline>();
	}
	
	// constructors with parameters
	public User(long userID) {
		this.userID = userID;
		this.userCommunities  = new HashSet<Community>();
		this.userDeadlines = new HashSet<Deadline>();
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
	/** REWRITE **/
	public boolean isGlobalAdmin() {
		return true;
	}
	
	/** REWRITE **/
	public boolean isAdmin(Community community) {
		return true;
	}
	
	/** REWRITE **/
	@Override
	public boolean equals(Object obj) {
		return true;
	}
	
	/** REWRITE **/
	@Override
	public int hashCode() {
		return 0;
	}
}
