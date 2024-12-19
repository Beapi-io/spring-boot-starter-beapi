package io.beapi.api.domain

import java.time.*;
import javax.persistence.*;

// formerly 'PasswordResetToken'
// this is ONLY used for resetting password and for a PUBLIC token associated with the user
//
// we may at some point want to also use this with email for simplicity and consolidation

@Entity
@Table(name = "authentication_token")
public class AuthenticationToken {

	//private static final int EXPIRATION = 60 * 24;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

    @Column(nullable = false, name = "token")
	private String token;

	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

    @Column(nullable = false, name = "expiry_date")
	private Long expiryDate;

/*
	public ResetToken() {
		super();
	}

	public ResetToken(final String token) {
		super();

		this.token = token;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}

	public ResetToken(final String token, final User user) {
		super();

		this.token = token;
		this.user = user;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}
*/

	//
	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	public Long getExpiryDate() {
		return expiryDate;
	}

    // unixTime
	public void setExpiryDate() {
		// current time plus 10 minutes
		Clock clock = Clock.systemUTC();
		long unixTime = Instant.now(clock).getEpochSecond();
		this.expiryDate = (unixTime+600000)
	}

	/*
	private Date calculateExpiryDate(final int expiryTimeInMinutes) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(new Date().getTime());
		cal.add(Calendar.MINUTE, expiryTimeInMinutes);
		return new Date(cal.getTime().getTime());
	}

	public void updateToken(final String token) {
		this.token = token;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}

	//

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getExpiryDate() == null) ? 0 : getExpiryDate().hashCode());
		result = prime * result + ((getToken() == null) ? 0 : getToken().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {return true;}
		if (obj == null) {return false;}
		if (getClass() != obj.getClass()) {return false;}

		final ResetToken other = (ResetToken) obj;
		if (getExpiryDate() == null) {
			if (other.getExpiryDate() != null) {
				return false;
			}
		} else if (!getExpiryDate().equals(other.getExpiryDate())) {
			return false;
		}

		if (getToken() == null) {
			if (other.getToken() != null) {
				return false;
			}
		} else if (!getToken().equals(other.getToken())) {
			return false;
		}

		if (getUser() == null) {
			if (other.getUser() != null) {
				return false;
			}
		} else if (!getUser().equals(other.getUser())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Token [String=").append(token).append("]").append("[Expires").append(expiryDate).append("]");
		return builder.toString();
	}

	 */

}
