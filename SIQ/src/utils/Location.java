package utils;

public class Location {
	public int query, subject;
	public Location(int query, int subject) {
		this.query = query;
		this.subject = subject;
	}
	public int getQuery() {
		return query;
	}
	public void setQuery(int query) {
		this.query = query;
	}
	public int getSubject() {
		return subject;
	}
	public void setSubject(int subject) {
		this.subject = subject;
	}
	public String toString() {
		return query+" "+subject;
	}
	
	
}
