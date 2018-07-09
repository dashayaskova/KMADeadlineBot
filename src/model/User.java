package model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class User {

	/** telegram id */
	private final long id; // is used as a primary key in the database
	
	/** Long - deadline id, Set<Date> times to remind */
	private Map<Long, Set<Date>> deadlineIdDates;

	/** creates User instance with empty deadlineDates */
	public User(long id) {
		this(id, new HashMap<>());
	}
	
	public User(long id, Map<Long, Set<Date>> deadlineIdDates) {
		this.id = id;
		this.deadlineIdDates = deadlineIdDates;
	}

	// getters and setters

	public long getId() { return id; }

	/** return a copy of deadlineIdDates */
	public Map<Long, Set<Date>> getDeadlineIdDates() {
		
		// create copy of deadlineIdDates
		return deadlineIdDates.keySet().stream()
				.collect(Collectors.toMap(deadlineId -> deadlineId, deadlineIdDates::get));
	}
	
	/** add date to deadline date list or put new deadline and date*/
	public void addDeadlineIdDate(long deadlineId, Date date) {
		
		 if(deadlineIdDates.containsKey(deadlineId)) {
			 deadlineIdDates.get(deadlineId).add(date);
		 
		 } else { 	 
			 deadlineIdDates.put(deadlineId, Stream.of(date).collect(Collectors.toSet()));
		 }
	}
	
	/** remove old deadline dates if contains and add new */
	public void putDeadlineIdDate(long deadlineId, Set<Date> dates) {
		
		if(deadlineIdDates.containsKey(deadlineId)) {
			deadlineIdDates.remove(deadlineId);
		}
		
		deadlineIdDates.put(deadlineId, dates.stream().collect(Collectors.toSet()));
	}

	/** return a copy of user's deadline ids */
	public Set<Long> getDeadlineIds() {
		
		return deadlineIdDates.keySet().stream().collect(Collectors.toSet());
	}

	// methods from Object class

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
		return "user: " + Long.toString(id);
	}
}