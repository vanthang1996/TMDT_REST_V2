package com.spring.config.security;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.spring.domain.User;
import com.spring.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -3301605591108950415L;

	@Value("${app.name}")
	private String APP_NAME;

	@Value("${jwt.secret}")
	public String SECRET;

	@Value("${jwt.expiration}")
	private int EXPIRES_IN;

	 @Value("${jwt.mobile_expires_in}")
	private int MOBILE_EXPIRES_IN;

	@Value("${jwt.header}")
	private String AUTH_HEADER;

	@Value("${jwt.prefix}")
	private String prefix;

	 @Value("${jwt.cookie}")
	public String AUTH_COOKIE;
	
	
	static final String AUDIENCE_UNKNOWN = "unknown";
	public static final String AUDIENCE_WEB = "web";
	public static final String AUDIENCE_MOBILE = "mobile";
	static final String AUDIENCE_TABLET = "tablet";

	@Autowired
	private TimeProvider timeProvider;
	
	@Autowired
	private UserService userService;
	private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	public Date getIssuedAtDateFromToken(String token) {
		Date issueAt;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			issueAt = claims.getIssuedAt();
		} catch (Exception e) {
			issueAt = null;
		}
		return issueAt;
	}

	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			audience = claims.getAudience();
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}

	public String refreshToken(String token, Device device) {
		String refreshedToken;
		Date a = timeProvider.now();
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			claims.setIssuedAt(a);
			refreshedToken = Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate(device))
					.signWith(SIGNATURE_ALGORITHM, SECRET).compact();
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	public String generateToken(String username, Device device) {
		String audience = generateAudience(device);
		return this.prefix + " "
				+ Jwts.builder().setIssuer(APP_NAME).setSubject(username).setAudience(audience)
						.setIssuedAt(timeProvider.now()).setExpiration(generateExpirationDate(device))
						.signWith(SIGNATURE_ALGORITHM, SECRET).compact();
	}

	private String generateAudience(Device device) {
		String audience = AUDIENCE_UNKNOWN;
		if (device.isNormal()) {
			audience = AUDIENCE_WEB;
		} else if (device.isTablet()) {
			audience = AUDIENCE_TABLET;
		} else if (device.isMobile()) {
			audience = AUDIENCE_MOBILE;
		}
		return audience;
	}

	public Claims getAllClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

	private Date generateExpirationDate(Device device) {
		long expiresIn = device.isTablet() || device.isMobile() ? MOBILE_EXPIRES_IN : EXPIRES_IN;
		return new Date(timeProvider.now().getTime() + expiresIn * 1000);
	}

	public int getExpiredIn(Device device) {
		return device.isMobile() || device.isTablet() ? MOBILE_EXPIRES_IN : EXPIRES_IN;
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		User user = userService.getUserByEmail(userDetails.getUsername());
		final String username = getUsernameFromToken(token);
		final Date created = getIssuedAtDateFromToken(token);
		LocalDateTime dateTime = user.getLastPasswordResetDate();
		ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());
		Date date = Date.from(zdt.toInstant());
		return (username != null && username.equals(userDetails.getUsername())
				&& !isCreatedBeforeLastPasswordReset(created, date));
	}
		/**
		 * Check the valid token
		 * The user_name input must be the same as the user_name in the token
		 * Creation  date must be after the Last Password Reset
		 * @param token
		 * @param userName
		 * @return
		 */
	public Boolean validateToken(String token, String userName) {
		User user = userService.getUserByEmail(userName);
		final String username = getUsernameFromToken(token);
		final Date created = getIssuedAtDateFromToken(token);

		LocalDateTime dateTime = user.getLastPasswordResetDate();
		ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());
		Date date = Date.from(zdt.toInstant());
		return (username != null && username.equals(username) && !isCreatedBeforeLastPasswordReset(created, date));
	}

	public  Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	public String getToken(HttpServletRequest request) {
		/**
		 * Getting the token from Cookie store
		 */
		Cookie authCookie = getCookieValueByName(request, AUTH_COOKIE);
		if (authCookie != null) {
			return authCookie.getValue();
		}
		/**
		 * Getting the token from Authentication header e.g Bearer your_token
		 */
		String authHeader = request.getHeader(AUTH_HEADER);
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}

		return null;
	}

	/**
	 * Find a specific HTTP cookie in a request.
	 *
	 * @param request
	 *            The HTTP request object.
	 * @param name
	 *            The cookie name to look for.
	 * @return The cookie, or <code>null</code> if not found.
	 */
	public Cookie getCookieValueByName(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return null;
		}
		for (int i = 0; i < request.getCookies().length; i++) {
			if (request.getCookies()[i].getName().equals(name)) {
				return request.getCookies()[i];
			}
		}
		return null;
	}
}
