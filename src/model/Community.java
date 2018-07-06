package model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Community implements Comparable<Community> {

	// data fields

	/** this value is unique and it is used as a primary key in the database */
	private String name;

	private Set<Long> memberIds;
	private Set<Long> adminIds;

	/** creates new community with empty set of members and admins */
	public Community(String name) {
		this(name, new HashSet<>(), new HashSet<>());
	}

	/** main constructor with all arguments */
	public Community(String name, Set<Long> memberIds, Set<Long> adminIds) {
		this.name = name;
		this.memberIds = memberIds;
		this.adminIds = adminIds;
	}

	// getters and setters

	public String getName() { return name; }

	public Set<Long> getMemberIds() {
		return memberIds.stream().collect(Collectors.toSet());
	}

	public Set<Long> getAdminIds() {
		return adminIds;
	}

	public void setMemberIds(Set<Long> memberIds) {
		this.memberIds = memberIds.stream().collect(Collectors.toSet());
	}
	
	public void setAdminIds(Set<Long> adminIds) {
		this.adminIds = adminIds.stream().collect(Collectors.toSet());
	}

	// methods

	public void addMemberId(long memberId) {
		memberIds.add(memberId);
	}

	public void removeMemberId(long memberId) {
		memberIds.remove(memberId);
	}

	public void addAdmin(long adminId) {
		adminIds.add(adminId);
	}
	
	public void addAdmins(Set<Long> adminIds) {
		adminIds.forEach(this::addAdmin);
	}
	
	public void removeAdminId(long adminId) {
		adminIds.remove(adminId);
	}
	
	public boolean isMember(long userId) {
		return memberIds.contains(userId);
	}
	
	public boolean isAdmin(long userId) {
		return adminIds.contains(userId);
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
		return "community '" + name + "'\nmemberIds: " + memberIds + "\nadminIds: " + adminIds;
	}
}