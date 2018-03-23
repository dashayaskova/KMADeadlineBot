package model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Community implements Comparable<Community> {

	// data fields

	/** this value is unique and it is used as a primary key in the database */
	private String name;

	private Set<User> members;
	private Set<User> admins;
	
	private final User creator;
	
	private Set<Deadline> deadlines;

	/** creates new community with empty set of members and admins */
	public Community(String name, User creator) {
		this(name, creator, new HashSet<>(), new HashSet<>());
	}

	/** main constructor with all arguments */
	public Community(String name, User creator, Set<User> members, Set<User> admins) {
		this.name = name;
		this.creator = creator;
		this.members = members;
		this.admins = admins;
		admins.add(creator); // creator is an admin too
	}

	// getters and setters

	public String getName() { return name; }
	
	public User getCreator() { return creator; }
	
	public Set<Deadline> getDeadlines() { return deadlines; }

	public Set<Long> getDeadlineIds() {
		// create Set<Long> from Set<Deadline> and change every
		// deadline to deadline.getId
		return deadlines.stream().map(Deadline::getId).collect(Collectors.toSet());
	}

	public Set<User> getMembers() {
		Set<User> result = new HashSet<>();
		result.addAll(members);
		return result;
	}

	public Set<User> getAdmins() {
		Set<User> result = new HashSet<>();
		result.addAll(admins);
		return result;
	}

	public Set<Long> getMemberIds() {
		return members.stream().map(User::getId).collect(Collectors.toSet());
	}

	public Set<Long> getAdminIds() {
		return admins.stream().map(User::getId).collect(Collectors.toSet());
	}

	public void setMembers(Set<User> members) {
		this.members = new HashSet<>();
		this.members.addAll(members);
	}

	public void setAdmins(Set<User> admins) {
		this.admins.clear();
		this.admins.addAll(admins);
	}

	// methods

	public void addMember(User member) {
		members.add(member);
	}

	public void removeMember(User member) {
		this.members.remove(member);
	}

	public void addAdmin(User admin) {
		admins.add(admin);
	}
	
	public void addAdmins(Set<User> admins) {
		this.admins.addAll(admins);
	}
	
	public void removeAdmin(User admin) {
		admins.remove(admin);
	}
	

	/** compare communities by name*/
	@Override
	public int compareTo(Community community) {
		return this.name.compareTo(community.name);
	}

	
	// methods from class Object
	
	// methods hashCode() and equals(Object object) are need 
	// to collect Community instances in the HashSet data structure
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		// communities are equals if their names are equals
		if (object != null && object instanceof Community) {
			return name.equals(((Community) object).name);
		}
		return false;
	}
	
	@Override
	public String toString() { // TODO rewrite
		return name + "\n" + members + "\n" + admins + "\n" + creator + "\n";
	}
}