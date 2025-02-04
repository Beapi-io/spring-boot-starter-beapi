package io.beapi.api.domain

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.persistence.*
import javax.validation.constraints.NotBlank

//import org.hibernate.validator.constraints.NotEmpty;
//import lombok.Getter;
//import lombok.Setter;


@Transactional(propagation=Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
@Entity
@Table(name = "stat")
//@Getter
//@Setter
public class Stat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(nullable = false, name = "statuscode")
	@NotBlank
	private Integer statusCode;

	@Column(nullable = false, name = "url")
	@NotBlank
	private String url

	@Column(nullable = false, name = "count")
	@NotBlank
	private Integer count

	@Column(nullable = false, name = "date")
	@NotBlank
	private Long date



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String setCount(Integer count) {
		this.count = count;
	}

	public String getCount() {
		return count;
	}

	public String setDate(Integer date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}
}

