package io.beapi.api.utils

import io.beapi.api.service.ExchangeService;

import java.io.Serializable
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired
//import org.json.JSONObject;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;

	// 24 hrs
	public static final long JWT_TOKEN_VALIDITY = 24 * (3600);

	@Autowired
	private SecretGenerator secretGenerator


	public Map<String,String> getHeaders(String token) {
		def headers = Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getHeader();
		return headers
	}

	//retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getBody();
			return claims.getSubject();
		}catch(Exception e){
			throw new Exception("[JwtTokenUtil :: getUsernameFromToken] : Exception - full stack trace follows:", e)
		} catch (io.jsonwebtoken.SignatureException e){
			//println("ExpiredJwtException found " + e);
			// old token / no token
		}
		return
	}

	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

    //for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getBody();
	}

	//check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	//generate token for user
	public String generateToken(UserDetails userDetails, String os, String browser, String ip) {
		Map<String, Object> claims = new HashMap<>();
		String temp = doGenerateToken(claims, userDetails.getUsername(), os, browser, ip);
		return temp;
	}

	//while creating the token -
	//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string 
	private String doGenerateToken(Map<String, Object> claims, String subject, String os, String browser, String ip) {
		//.setHeaderParam("typ","JWT")

		return Jwts.builder()
				.setHeaderParam("origin",ip)
				.setHeaderParam("browser",browser)
				.setHeaderParam("os",os)
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secretGenerator.getSecret()).compact();
	}

	//validate token
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public String getOs(String userAgent){
		switch(userAgent.toLowerCase()) {
			case {it.indexOf("windows" ) >= 0}:
				return "Windows";
				break;
			case {it.indexOf("mac") >= 0}:
				return "Mac";
				break
			case {it.indexOf("x11") >= 0}:
				return "Unix";
				break;
			case {it.indexOf("android") >= 0}:
				return "Android";
				break;
			case {it.indexOf("iphone") >= 0}:
				return "IPhone";
				break;
			case {it.indexOf("apache-httpclient") >=0}:
				return "ApacheHttpClient";
				break;
		}
		return "Unknown";
	}

	public String getBrowser(String user, String userAgent){
		switch(user) {
			case {it.contains("msie")}:
				String substring=userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
				return substring.split(" ")[0].replace("MSIE", "IE")+"-"+substring.split(" ")[1];
				break;
			case {it.contains("safari") && it.contains("version")}:
				return (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]+"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
				break;
			case {it.contains("opr")}:
				return ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
				break;
			case {it.contains("opera")}:
				return (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]+"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
				break;
			case {it.contains("chrome")}:
				return (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
				break;
			case {(it.indexOf("mozilla/7.0") > -1) || (it.indexOf("netscape6") != -1)  || (it.indexOf("mozilla/4.7") != -1) || (it.indexOf("mozilla/4.78") != -1) || (it.indexOf("mozilla/4.08") != -1) || (it.indexOf("mozilla/3") != -1)}:
				return "Netscape-?";
				break;
			case {it.contains("firefox")}:
				return (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
				break;
			case {user.contains("rv")}:
				return "IE-" + user.substring(user.indexOf("rv") + 3, user.indexOf(")"));
				break;
			case {user.contains("apache-httpclient")}:
				return "Apache-HttpClient" + user.substring(user.indexOf("rv") + 18, user.indexOf(" ("));
				break;
		}
		return "Unknown";
	}

	public String getIp(HttpServletRequest request){
		String ip = request.getHeader("X-FORWARDED-FOR");
		if (ip == null) { ip = request.getRemoteAddr(); }
		return ip
	}
}
