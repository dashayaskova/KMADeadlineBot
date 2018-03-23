package objects;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class User {

	// data fields

	/** this value is recieve from telegram */
	private final long id; // is used as a primary key in the database

	private Map<Deadline, Set<Date>> deadlineDates; // times to remind

	// constructors

	/** creates User instance with empty deadlineDates */
	public User(long id) {
		this(id, new HashMap<>());
	}

	public User(long id, Map<Deadline, Set<Date>> deadlineDates) {
		this.id = id;
		this.deadlineDates = deadlineDates;
	}

	// getters and setters

	public long getId() {
		return id;
	}

	public Set<Community> getCommunities() {
		return new HashSet<>(); // TODO implement
	}

	public Set<String> getCommunityNames() {
		return getCommunities().stream().map(Community::getName).collect(Collectors.toSet());
	}

	public Map<Deadline, Set<Date>> getDeadlineDate() {
		// TODO return a copy of the deadlineDates
		return deadlineDates;
	}

	public Map<Long, Set<Date>> getDeadlineIdStates() {
		Map<Long, Set<Date>> result = new HashMap<>();

		deadlineDates.forEach((deadline, dateSet) -> {
			long deadlineId = deadline.getId();
			result.put(deadlineId, dateSet);
		});
		return result;
	}
	
	public void addDeadlineState(Deadline deadline, Date date) {
		 
	}

	public Set<Deadline> getDeadlines() {
		return null; // TODO implement
	}

	public Set<Long> getDeadlinesID() {
		return getDeadlines().stream().map(Deadline::getId).collect(Collectors.toSet());
	}

	public Set<Community> getCommunitiesWhereAdmin() {
		return new HashSet<>(); // TODO implement
	}

	public Set<String> getCommunitieIdsWhereAdmin() {
		return getCommunitiesWhereAdmin().stream().map(Community::getName).collect(Collectors.toSet());
	}

	public boolean isGlobalAdmin() {
		return true; // TODO implement
	}

	public boolean isAdmin(Community community) {
		return community.getAdmins().contains(this);
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
		return "User: " + Long.toString(id);
	}
}