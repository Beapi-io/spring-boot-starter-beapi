package io.beapi.api.domain;

//import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.io.Serializable;
import javax.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
import java.util.*;
import java.time.Clock;
import org.hibernate.annotations.BatchSize
//@Cacheable
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "user")
//@Getter
//@Setter
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Version
	@Column(nullable = false, name = "version")
	private Long version;

	@Column(nullable = false, name = "username", unique = true)
	String username;

	@Column(nullable = false, name = "password")
	String password;

	@Column(nullable = true, name = "uid")
	String uid;

	@Column(nullable = true, name = "first_name")
	String firstName = "null";

	@Column(nullable = true, name = "last_name")
	String lastName = "null";

	//@NotEmpty(message = "Email is required")
	@Column(nullable = false, name = "email", unique = true)
	String email;

	@Column(nullable = true, name = "verification_code")
	String verificationCode;

	@Column(nullable = true, name = "verification_expiry")
	Long verificationExpiry

	@Column(nullable = false, name = "credentials_expired")
	boolean credentialsExpired=false;

	@Column(nullable = true, name = "email_verified")
	Boolean emailVerified=false;

	@Column(nullable = true, name = "oauth_id")
	String oauthId;

	@Column(nullable = true, name = "oauth_provider")
	String oauthProvider;

	@Column(nullable = true, name = "avatar_url")
	String avatarUrl;

	@Column(nullable = false, name = "enabled")
	Boolean enabled=true;

	@Column(nullable = false, name = "password_expired")
	Boolean passwordExpired=false;

	@Column(nullable = false, name = "account_expired")
	Boolean accountExpired=false;

	@Column(nullable = false, name = "account_locked")
	Boolean accountLocked=false;

	@Column(nullable = false, name = "throttle_lock")
	private boolean throttleLock=false;

	@Column(nullable = true, name = "current_limit")
	private Long currentLimit;

	@Column(nullable = true, name = "current_content_length")
	private Long currentContentLength;


	//@OneToMany(targetEntity=UserAuthority.class, fetch=FetchType.EAGER, orphanRemoval = true)
	//@JoinColumn(name = "user_id")
	//@OrderColumn(name="authority_id")
	@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinTable(name = "user_authority",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName="id")
	)
	private List<Authority> roles;
	//private UserAuthority[] roles

	//@OneToMany(mappedBy="user",fetch = FetchType.EAGER)
	//private Set<Hook> hooks;

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
	public String getUsername(){
		return username;
	}

	public void setUsername(String name){
		this.username = name;
	}

	public String getEmail(){
		return email;
	}

	public String getFirstName(){
		return firstName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public boolean isEnabled() {
		return this.enabled
	}

	public Boolean getEnabled(){
		return this.enabled;
	}

	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}

	public boolean getAccountNonExpired(){
		return (this.accountExpired)?false:true
	}

	public Boolean getAccountExpired(){
		return this.accountExpired;
	}

	public void setAccountExpired(Boolean accountExpired){
		this.accountExpired=accountExpired;
	}

	public boolean getAccountNonLocked(){
		return (this.accountLocked)?false:true
	}

	public Boolean getAccountLocked(){
		return this.accountLocked;
	}

	public void setAccountLocked(Boolean accountLocked){
		this.accountLocked = accountLocked;
	}

	public Boolean getPasswordExpired(){
		return this.passwordExpired;
	}

	public void setPasswordExpired(Boolean passwordExpired){
		this.passwordExpired = passwordExpired;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public List<Authority> getAuthorities(){
		return roles;
	}

	public Long getCurrentLimit() {
		return currentLimit;
	}

	public void setCurrentLimit(Long currentLimit) { this.currentLimit = currentLimit; }

	public Long getCurrentContentLength() {
		return currentContentLength;
	}

	public void setCurrentContentLength(Long currentContentLength) { this.currentContentLength = currentContentLength; }

	public boolean getThrottleLock() {
		return throttleLock;
	}

	public void setThrottleLock(boolean throttleLock) { this.throttleLock = throttleLock; }

	//public Set<Hook> getHooks() {
		//return this.hooks;
	//}

	public String getVerificationCode(){
		return this.verificationCode
	}

	public void setVerificationCode(String verificationCode){
		this.verificationCode = verificationCode
	}

	public Long getVerificationExpiry(){
		return this.verificationExpiry
	}


	public void setVerificationExpiry(){
		// current time plus 10 minutes
		Clock clock = Clock.systemUTC();
		long unixTime = Instant.now(clock).getEpochSecond();
		this.verificationCode = (unixTime+600000)
	}

	public boolean getCredentialsNonExpired(){
		return (this.credentialsExpired)?false:true
	}

	public Boolean getEmailVerified(){
		return this.emailVerified
	}

	public void getEmailVerified(Boolean emailVerified){
		this.emailVerified = emailVerified
	}
}
