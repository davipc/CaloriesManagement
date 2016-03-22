package com.toptal.calories.resources.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;


/**
 * The persistent class for the role database table.
 * 
 */
@Entity
@NamedQuery(name="Role.findAll", query="SELECT r FROM Role r")
public class Role extends BaseEntity {
	//private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="role_id_seq", sequenceName="role_id_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="role_id_seq")
	@Column(name = "id", updatable=false, nullable=false)	
	private Integer id;

	@Column(nullable=false)
	private String name;

	//bi-directional many-to-many association to User
	/** not really needed
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

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
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
}