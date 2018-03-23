package objects;

import java.util.HashSet;
import java.util.Set;

public class Community {

	// data fields
	private long communityID;
	private String communityName;
	/** RECONSIDER SETS OF USERS INSTEAD OF LONGS **/
	private Set<User> communityMembers;
	private Set<User> communityAdmins;
	private User communityCreator;
	
	// default constructor
	// test comment
	public Community() {
		this.communityID = 0L;
		this.communityName = "";
		this.communityMembers = new HashSet<User>();
		this.communityAdmins = new HashSet<User>();
		this.communityCreator = new User();
	}
	
	// constructor with parameters
	
	/** PLEASE, WRITE THEM **/
	
	// getters and setters
	public long getID() {
		return this.communityID;
	}
	
	public String getCommunityName() {
		return this.communityName;
	}
	
	/** REWRITE **/
	public User getCommunityCreator() {
		return this.communityCreator;
	}
	
	/** REWRITE **/
	public Set<Deadline> getDeadlines() {
		return new HashSet<Deadline>();
	}
	
	/** REWRITE **/
	public Set<Long> getDeadlinesID() {
		return new HashSet<Long>();
	}
	
	/** REWRITE **/
	public Set<User> getCommunityMembers() {
		return this.communityMembers;
	}
	
	/** REWRITE **/
	public Set<User> getAdmins() {
		return this.communityAdmins;
	}
	
	/** REWRITE **/
	public void setCommunityMembers(Set<User> communityMembers) {
		this.communityMembers = communityMembers;
	}
	
	/** REWRITE **/
	public void setCommunityAdmins(Set<User> communityAdmins) {
		this.communityAdmins = communityAdmins;
	}

	// methods
	/** REWRITE **/
	public void addCommunityUser(User user) {}
	
	/** REWRITE **/
	public void removeCommunityUser(User user) {}
	
	/** REWRITE **/
	public void addCommunityAdmin(User admin) {}
	
	/** REWRITE **/
	public void removeCommunityAdmin(User admin) {}
}
