package com.cm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cm.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	/*
		@Query("select u from User u where u.email = : email")
		public User getUserByEmail(@Param("email") String  email);
	*/
	
	public User findById(int id);
	
	public User findByEmail(String email);
}
