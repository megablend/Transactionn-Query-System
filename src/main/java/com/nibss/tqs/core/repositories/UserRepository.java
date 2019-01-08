package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);

	int countByEmail(String email);

	List<User> findAdminUsersByOrganization(int organizationId, String roleName);

	int countUserRoleByOrganization(int orgId, String roleName);

	List<User> findByOrganization(int orgId);

	@Modifying
	@Query(value="UPDATE User u SET u.enabled=false WHERE u.lastLoginDate < :lastLoginDate")
	@Transactional
	int disableInactiveUsers(@Param("lastLoginDate") Date date);


	@Query("SELECT new com.nibss.tqs.ajax.AjaxOrganization(u.organization.id, u.organization.name, u.organization.organizationType) FROM User u WHERE u.id = ?1")
	IOrganization findUserOrganization(int userId);


	@Query("SELECT new com.nibss.tqs.core.entities.User(u, u.organization.organizationType) FROM User u WHERE u.email = ?1")
	User findByEmailLogin(String email);


	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.lastLoginDate = CURRENT_TIMESTAMP  WHERE u.id = ?1")
	int updateLastLoginDateForUser(int userId);


	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.password = ?2, u.passwordChanged= ?3  WHERE u.id = ?1 ")
	int updateUserPassword(int userId, String newPassword, boolean passwordChanged);

	@Query("SELECT new com.nibss.tqs.core.entities.User(u.id,u.firstName,u.lastName,u.email, u.enabled) FROM User u WHERE u.id = ?1")
	User getBasicUserDetails(int userId);

	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.enabled = ?2, u.passwordChanged = ?3 WHERE u.id = ?1")
	int updateUserEnabledStatus(int userId, boolean enabled, boolean passwordChanged);

}
