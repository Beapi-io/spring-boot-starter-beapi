package io.beapi.api.domain;

//import lombok.Getter;
//import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;
import io.beapi.api.domain.Authority;

@Entity
@Table(name = "user_authority")
//@Getter
//@Setter
public class UserAuthority implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	public UserAuthority(User user, Authority auth) {
		this.user = user
		this.authority = auth
	}
	 */

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;


	//@ManyToOne(optional = false,targetEntity=User.class, fetch=FetchType.EAGER)
	@ManyToOne
	@JoinColumn(nullable = false, name = "user_id")
	private User user;


	//@ManyToOne(optional = false,targetEntity=Authority.class, fetch=FetchType.EAGER)
	@ManyToOne
	@JoinColumn(nullable = false, name = "authority_id")
	private Authority authority;

	public User getUser(){
		return this.user;
	}

	public void setUser(User user){
		this.user = user;
	}

	public Authority getAuthority(){
		return this.authority;
	}

	public void setAuthority(Authority auth){
		this.authority = auth;
	}
}
