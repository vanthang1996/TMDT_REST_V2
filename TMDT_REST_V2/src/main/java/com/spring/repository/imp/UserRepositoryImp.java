package com.spring.repository.imp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.spring.domain.User;
import com.spring.repository.UserRepository;

@Repository
public class UserRepositoryImp implements UserRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImp.class);

	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public User getUserByUserID(String userID) {

		SqlSession session = sqlSessionFactory.openSession();
		User result = new User();
		try {
			result = session.selectOne("com.spring.mapper.UserMapper.getUserByUserID", userID);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@Override
	public LocalDateTime getLastPasswordResetDate(String userID) {
		SqlSession session = sqlSessionFactory.openSession();
		LocalDateTime result = null;
		try {
			result = session.selectOne("com.spring.mapper.UserActionMapper.getLastPasswordResetDate", userID);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public User getUserByEmail(String email) {
		SqlSession session = sqlSessionFactory.openSession();
		User user = new User();
		try {
			user = session.selectOne("com.spring.mapper.UserMapper.getUserByEmail", email);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}

		return user;
	}

	@Override
	public int CreateUser(String userName, String email, String password) {
		SqlSession session = sqlSessionFactory.openSession();
		int result = 0;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("userName", userName);
			param.put("email", email);
			param.put("password", this.passwordEncoder.encode(password));
			param.put("result", 0);
			session.insert("com.spring.mapper.UserMapper.createUser", param);
			result = (int) param.get("result");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public int changeUserStatus(String userID, int newStatus) {
		SqlSession session = sqlSessionFactory.openSession();
		int result = 0;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("userID", userID);
			param.put("newStatus", newStatus);
			param.put("result", 0);
			session.insert("com.spring.mapper.UserMapper.changeUserStatus", param);
			result = (int) param.get("result");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;

	}

	@Override
	public int changeUserPassword(String userID, String newPassword) {
		SqlSession session = sqlSessionFactory.openSession();
		int result = 0;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("userID", userID);
			param.put("newPassword", newPassword);
			param.put("result", 0);
			session.insert("com.spring.mapper.UserMapper.changeUserPassword", param);
			result = (int) param.get("result");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public int saveKeyReset(String userID, String keyReset) {
		SqlSession session = sqlSessionFactory.openSession();
		int result = 0;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("userID", userID);
			param.put("keyReset", keyReset);
			param.put("result", 0);
			session.insert("com.spring.mapper.UserActionMapper.saveKeyReset", param);
			result = (int) param.get("result");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@Override
	public int removeKeyReset(String userID, String keyReset) {
		SqlSession session = sqlSessionFactory.openSession();
		int result = 0;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("userID", userID);
			param.put("keyReset", keyReset);
			param.put("result", 0);
			session.insert("com.spring.mapper.UserActionMapper.removeKey", param);
			result = (int) param.get("result");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}
	
	@Override
	public List<String> getKeYResetByUserId(String userID) {
		SqlSession session = sqlSessionFactory.openSession();
		List<String>result = Collections.emptyList();
		try {
			result = session.selectList("com.spring.mapper.UserActionMapper.getKeYResetByUserId",userID);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}finally {
			session.close();
		}
		return result;
	}
}
