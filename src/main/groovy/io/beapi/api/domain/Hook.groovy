package io.beapi.api.domain;


//import lombok.Getter;
//import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "hook")
//@Getter
//@Setter
public class Hook implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(nullable = false, name = "version")
	private Long version;

	@ManyToOne
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

	// endpoint (set via 'hook' in IOState)
	@Column(nullable = false, name = "service")
	private String service;

	@Column(nullable = false, name = "format")
	private String format = "JSON";

	// uri to be called when webhook is triggered
	@Column(nullable = false, name = "callback")
	private String callback;

	// bearer token (not optional)
	@Column(nullable = false, name = "authorization")
	private String authorization;

	// attempts until disabled
	@Column(nullable = false, name = "attempts")
	private int attempts = 0;

	@Column(nullable = false, name = "enabled")
	private Boolean enabled = true;

	@Column(nullable = false, name = "date_created")
	private Long dateCreated;

	@Column(nullable = false, name = "last_modified")
	private Long lastModified;


	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public Long getVersion(){
		return version;
	}

	public void setVersion(Long version){
		this.version = version;
	}

	public User getUser(){
		return this.user;
	}

	public void setUser(User user){
		this.user = user;
	}

	public String getService(){
		return service;
	}

	public void setService(String service){
		this.service = service;
	}

	public String getFormat(){
		return format;
	}

	public void setFormat(String format){
		this.format = format;
	}

	public String getCallback(){
		return callback;
	}

	public void setCallback(String callback){
		this.callback = callback;
	}

	public String getAuthorization(){
		return authorization;
	}

	public void setAuthorization(String authorization){
		this.authorization = authorization;
	}

	public int getAttempts(){
		return attempts;
	}

	public void setAttempts(int attempts){
		this.attempts = attempts;
	}

	public Boolean getEnabled(){
		return enabled;
	}

	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}

	public Long getDateCreated(){
		return dateCreated;
	}

	public void setDateCreated(Long dateCreated){
		this.dateCreated = dateCreated;
	}

	public Long getLastModified(){
		return lastModified;
	}

	public void setLastModified(Long lastModified){
		this.lastModified = lastModified;
	}

}
