package com.toptal.calories.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the role database table.
 * 
 */

@XmlRootElement(name = "user")
@XmlType(propOrder = {"id", "name"})
@XmlAccessorType(XmlAccessType.FIELD)

@Entity
public class Role extends BaseEntity {

	@Id
	@SequenceGenerator(name="role_id_seq", sequenceName="role_id_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="role_id_seq")
	@Column(name = "id", updatable=false, nullable=false)	
	private Integer id;

	// this field can never be modified by other entities updates
	@Column(nullable=false, length=20, updatable=false)
	@Enumerated(EnumType.STRING)
	private RoleType name;

	/** not really needed
	//bi-directional many-to-many association to User
	@ManyToMany
	@JoinTable(
		name="user_role"
		, joinColumns={
			@JoinColumn(name="role_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="user_id")
			}
		)
	private List<User> users;
	**/
	
	public Role() {
	}

	public Role(RoleType name) {
		super();
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public RoleType getName() {
		return this.name;
	}

	public void setName(RoleType name) {
		this.name = name;
	}

	/**
	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	**/
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj != null && obj instanceof Role) {
			Role other = (Role) obj;
			result = areEqual(other.getId(), getId()) &&
					areEqual(other.getName(), getName());
			
			/**
			if (result) {
				List<User> otherUsers = other.getUsers();
				
				// we will consider null list and empty list to be the same
				if (users == null || users.size() == 0) {
					result = (otherUsers == null || otherUsers.size() == 0);
				} else if (otherUsers == null || users.size() != otherUsers.size()) {
					result = false;
				} else {
					result = otherUsers.containsAll(users);
				}
			}
			**/
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("Role [Id: ").append(id)
			.append(", name: ").append(name);
		
		/**
			.append(", Users: { ");
			
		
		if (users == null) {
			sb.append("null");
		} else {
			for (User user: users) {
				sb.append(user).append(", ");
			}
		}
		**/
		
		sb.append("]");
		
		return sb.toString();
	}
	
	public String getJSON() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"id\":").append(id).append(",");
		builder.append("\"name\":\"").append(name).append("\"");
		builder.append("}");
		
		return builder.toString();
	}
}