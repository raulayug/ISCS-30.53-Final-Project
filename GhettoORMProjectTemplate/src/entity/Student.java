package entity;

import annotations.Column;
import annotations.Entity;

@Entity(table="student")
public class Student {
	
	@Column(name="pk", 			sqlType="INTEGER not NULL AUTO_INCREMENT", id=true)
	private Integer id;

	@Column(name="first_name", 	sqlType="VARCHAR(255)")
	private String first;

	@Column(name="last_name", 	sqlType="VARCHAR(255)")
	private String last;

	@Column(name="age", 		sqlType="INTEGER")
	private Integer age;
	
	
	public Integer getID() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	
	@Override
	public String toString() {
		return String.format("User [id=%s, first_name=%s, last_name=%s, age=%s]", id, first, last, age);
	}
}
