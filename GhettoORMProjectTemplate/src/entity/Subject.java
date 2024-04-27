package entity;

import annotations.Column;
import annotations.Entity;

@Entity(table="subject")
public class Subject {
	
	@Column(name="id", 			sqlType="INTEGER not NULL AUTO_INCREMENT", id=true)
	private Integer id;

	@Column(name="name", 	sqlType="VARCHAR(255)")
	private String name;

	@Column(name="num_students", 		 sqlType="INTEGER")
	private Integer numStudents;

	public Integer getID() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getNumStudents() {
		return numStudents;
	}
	public void setNumStudents(Integer numStudents) {
		this.numStudents = numStudents;
	}
	
	@Override
	public String toString() {
		return String.format("Subject [id=%s, name=%s, num_students=%s]", id, name, numStudents);
	}
}
