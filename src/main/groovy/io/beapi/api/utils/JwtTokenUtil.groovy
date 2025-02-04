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

/**
 * Class containing commonly used methods for authorization and manipulation of the JWT token
 *
 * @author Owen Rubel
 * @see JwtRequestFilter
 * @see JwtAuthenticationController
 */
@Component
public class JwtTokenUtil implements Serializable {


	/**
	 * 24 hrs
	 */
	public static final long JWT_TOKEN_VALIDITY = 24 * (3600);

	/**
	 * hash for setting signing key
	 */
	@Autowired private SecretGenerator secretGenerator


	/**
	 * Method for retrieving headers from jwt token
	 *
	 * @param  token String representing the JWT token
	 * @return Mao representing the headers in the JWT token
	 */
	public Map<String,String> getHeaders(String token) {
		def headers = Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getHeader();
		return headers
	}


	/**
	 * Method for retrieving expiration date from jwt token
	 *
	 * @param  token String representing the JWT token
	 * @return expiration date for the token
	 */
	public String getUsernameFromToken(String token) {
		try {
			Claims claims = getAllClaimsFromToken(token)
			return claims.getSubject();
		}catch(Exception e){
			throw new Exception("[JwtTokenUtil :: getUsernameFromToken] : Exception - full stack trace follows:", e)
		} catch (io.jsonwebtoken.SignatureException e){
			println("[JwtTokenUtil :: getUsernameFromToken] ExpiredJwtException found " + e);
			// old token / no token
		}
		return
	}


	/**
	 * Method for retrieving expiration date from jwt token
	 *
	 * @param  token String representing the JWT token
	 * @return expiration date for the token
	 */
	public Date getExpirationDateFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}

	/**
	 * Method for retrieving ALL claims from token
	 * @deprecated
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Secondary Method for retrieving ALL claims from token
	 * @deprecated
	 */
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secretGenerator.getSecret()).parseClaimsJws(token).getBody();
	}

	/**
	 * Method for checking if the token has expired
	 *
	 * @param  token String representing the JWT token
	 * @return Boolean representing whether the JWT token is expired or not
	 */
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	/**
	 * Method for generating the token
	 *
	 * @param  userDetails Object representing information about PRINCIPLE
	 * @param  os String representing the OS used for the JWT token
	 * @param  browser String representing the browser used for the JWT token
	 * @param  ip String representing the IP address used for the JWT token
	 * @return String representing the JWT token
	 * @see doGenerateToken
	 */
	public String generateToken(UserDetails userDetails, String os, String browser, String ip) {
		Map<String, Object> claims = new HashMap<>();
		String temp = doGenerateToken(claims, userDetails.getUsername(), os, browser, ip);
		return temp;
	}

	/**
	 * Secondary Method for generating the token
	 * 	1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	 * 	2. Sign the JWT using the HS512 algorithm and secret key.
	 * 	3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	 *
	 * @param  claims Map representing the claims of the JWT token
	 * @param  subject String representing the subject of the JWT token
	 * @param  os String representing the OS used for the JWT token
	 * @param  browser String representing the browser used for the JWT token
	 * @param  ip String representing the IP address used for the JWT token
	 * @return String representing the JWT token
	 */
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

	/**
	 * Method for getting browser of requesting client for storage in token
	 * @param  token String representing the requests sent JWT token
	 * @param  userDetails Object created from the token with information about PRINCIPLE
	 * @return Boolean representing whether this is a valid token or not
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	/**
	 * Method for getting browser of requesting client for storage in token
	 * @param  userAgent String representing the requests user-agent header
	 * @return String representing the Operating System for storage in the token
	 */
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

	/**
	 * Method for getting browser of requesting client for storage in token
	 * @param  user String representation of userAgent in lowercase
	 * @param  userAgent String representing the requests user-agent header
	 * @return String representing the browser for storage in the token
	 */
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

	/**
	 * Merthod for getting IP of requesting client for storage in token
	 * @param  request HttpServletRequest for parsing the header
	 * @return String representing the client IP address for storage in the token
	 */
	public String getIp(HttpServletRequest request){
		String ip = request.getHeader("X-FORWARDED-FOR");
		if (ip == null) { ip = request.getRemoteAddr(); }
		return ip
	}
}
