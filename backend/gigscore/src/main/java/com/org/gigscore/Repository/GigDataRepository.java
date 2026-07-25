package com.org.gigscore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;

public interface GigDataRepository extends JpaRepository<GigData, Long> {

	Optional<GigData> findByUserAndPlatform(User user, String platform);
	List<GigData> findAllByUserAndPlatform(User user, String platform);
    List<GigData> findByUser(User user);
}
