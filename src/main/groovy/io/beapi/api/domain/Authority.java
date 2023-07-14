package io.beapi.api.domain;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

//import org.hibernate.validator.constraints.NotEmpty;
//import lombok.Getter;
//import lombok.Setter;


@Transactional(propagation=Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
@Entity
@Table(name = "authority")
//@Getter
//@Setter
public class Authority {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(nullable = false, unique = true)
	@NotBlank
	private String authority;


	//@ManyToOne(targetEntity=UserAuthority.class, cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@OneToMany
	@JoinTable(name = "user_authority",joinColumns = @JoinColumn(name = "authority_id", referencedColumnName="id"),inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName="id"))
	private List <User> users;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String name) {
		this.authority = name;
	}

}
