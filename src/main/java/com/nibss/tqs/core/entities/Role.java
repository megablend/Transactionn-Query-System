package com.nibss.tqs.core.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name="roles")
@Data
@ToString(of = "name")
public class Role  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1209640081355399410L;

	public static final String USER = "ROLE_USER";

	public static final String ADMIN = "ROLE_ADMIN";

	public static final String NIBSS_ADMIN = "ROLE_NIBSS_ADMIN";

	public static final String NIBSS_USER = "ROLE_NIBSS_USER";
	public static final String BANK_USER = "ROLE_BANK_USER";
	public static final String BANK_ADMIN = "ROLE_BANK_ADMIN";
	public static final String CLOUNGE_USER = "ROLE_CL_USER";
	public static final String CLOUNGE_ADMIN = "ROLE_CL_ADMIN";
    public static final String NIBSS = "ROLE_NIBSS" ;
    public static final String BANK = "ROLE_BANK" ;

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(unique=true,nullable=false)
	@Getter
	private String name;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="user_roles")
	@Getter
	private List<User> users;
}
