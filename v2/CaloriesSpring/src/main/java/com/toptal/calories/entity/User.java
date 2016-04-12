package com.toptal.calories.entity;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * The persistent class for the app_user database table.
 * 
 */

@XmlRootElement(name = "user")
@XmlType(propOrder = {"id", "login", "password", "name", "gender", "dailyCalories", "roles", "creationDt"})
@XmlAccessorType(XmlAccessType.FIELD)

@Entity
@Table(name="app_user",
		uniqueConstraints={@UniqueConstraint(name = "User_UNIQ", columnNames = {"login"})},
		indexes = {@Index(name="User_IDX", columnList="login")
})
@NamedQueries ({
	@NamedQuery(name="User.findMealsInDateAndTimeRange", 
			query="SELECT m FROM User u JOIN u.meals m where m.user.id = :userId and m.mealDate between :fromDate and :toDate and m.mealTime between :fromTime and :toTime"),
	@NamedQuery(name="User.findByLogin", query="SELECT u FROM User u where u.login = :login")

})

public class User extends BaseEntity {

	@Id
	@SequenceGenerator(name="app_user_id_seq", sequenceName="app_user_id_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="app_user_id_seq")
	@Column(name = "id", updatable=false, nullable=false)	
	private Integer id;

	@Column(nullable=false, unique=true, length=12)
	private String login;

	@Column(nullable=false, length=64)
	private String password;

	@Column(nullable=false, length=80)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable=false, length=1)
	private Gender gender;

	@Column(name="daily_calories", nullable=false)
	private Integer dailyCalories;

	//one-directional many-to-many association to Role
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="user_role" , 
		foreignKey=@ForeignKey(name="user_id_FK"),
		joinColumns={@JoinColumn(name="user_id", referencedColumnName = "id")}, 
		inverseForeignKey=@ForeignKey(name="role_id_FK"),
		inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName = "id")}
	)
	private List<Role> roles;

	// makes sure this is not present in the generated JSON
	@JsonIgnore
	@XmlTransient
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="user")
	private List<Meal> meals;
	
	@Column(name="creation_dt", nullable=false)
	private Timestamp creationDt;

	
	public User() {
	}

	public User(String login, String password, String name, Gender gender, Integer dailyCalories, List<Role> roles,
			List<Meal> meals, Timestamp creationDt) {
		super();
		this.login = login;
		this.password = password;
		this.name = name;
		this.gender = gender;
		this.dailyCalories = dailyCalories;
		this.roles = roles;
		this.meals = meals;
		this.creationDt = creationDt;
	}

	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreationDt() {
		return this.creationDt;
	}

	public void setCreationDt(Timestamp creationDt) {
		this.creationDt = creationDt;
	}

	public Integer getDailyCalories() {
		return this.dailyCalories;
	}

	public void setDailyCalories(Integer dailyCalories) {
		this.dailyCalories = dailyCalories;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Gender getGender() {
		return this.gender;
	}

	public void setGender(Gender gender) {
		this.gender= gender;
	}

	public List<Role> getRoles() {
		return this.roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	public List<Meal> getMeals() {
		return meals;
	}

	public void setMeals(List<Meal> meals) {
		this.meals = meals;
	}

	public boolean hasRole(RoleType t) {
		boolean result = false;
		for (Role r: roles) {
			if (r.getName().equals(t))
				result = true;
		}
			
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj != null && obj instanceof User) {
			User other = (User) obj;
			result = areEqual(other.getId(), getId()) &&
					areEqual(other.getLogin(), getLogin()) &&
					areEqual(other.getPassword(), getPassword()) &&
					areEqual(other.getName(), getName()) &&
					areEqual(other.getGender(), getGender()) &&
					areEqual(other.getDailyCalories(), getDailyCalories()) &&
					areEqual(other.getCreationDt(), getCreationDt());
			
			if (result) {
				List<Role> otherRoles = other.getRoles();
				
				// we will consider null list and empty list to be the same
				if (roles == null || roles.size() == 0) {
					result = (otherRoles == null || otherRoles.size() == 0);
				} else if (otherRoles == null || roles.size() != otherRoles.size()) {
					result = false;
				} else {
					result = otherRoles.containsAll(roles);
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("User [Id: ").append(id)
			.append(", login: ").append(login)
			.append(", password: ").append("**********")
			.append(", name: ").append(name)
			.append(", gender: ").append(gender)
			.append(", dailyCalories: ").append(dailyCalories)
			.append(", creationDt: ").append(creationDt)
			.append(", Roles: { ");
			
		
		if (roles == null) {
			sb.append("null");
		} else {
			for (Role role: roles) {
				sb.append(role).append(", ");
			}
		}
		
		sb.append("} ]");
		
		return sb.toString();
	}
	
	@JsonIgnore
	public String getJSON() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"id\":").append(id).append(",");
		builder.append("\"login\":\"").append(login).append("\",");
		builder.append("\"password\":\"").append(password).append("\",");
		builder.append("\"name\":\"").append(name).append("\",");
		builder.append("\"gender\":\"").append(gender).append("\",");
		builder.append("\"dailyCalories\":").append(dailyCalories).append(",");
		builder.append("\"roles\":[");
		
		if (roles != null && roles.size() > 0) {
			builder.append(roles.get(0).getJSON());
			for (int i = 1; i < roles.size(); i++) {
				builder.append(", ").append(roles.get(i).getJSON());
			}
		}
		builder.append("], ");
		builder.append("\"creationDt\":").append(creationDt != null ? creationDt.getTime() : 0).append("}");
		
		return builder.toString();
	}
	
	
	/**
	 * Validates the object, returning a list of errors if any exist, or null otherwise
	 * @return
	 */
	public String validate() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(validateString(login, true, 12, "login"));
		// no need to worry about password field size - hash function will always return 64 chars
		sb.append(validateString(password, false, Integer.MAX_VALUE, "password"));
		sb.append(validateString(name, true, 80, "name"));
		sb.append(validateForNull(gender, true, "gender"));
		sb.append(validateForNull(dailyCalories, true, "daily calories"));
		sb.append(validateForNull(creationDt, true, "creation date"));
		
		String result = sb.toString();
		if (result.trim().equals(""))
			result = null;
		
		return result;
	}
}