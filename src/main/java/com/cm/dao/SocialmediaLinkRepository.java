package com.cm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cm.entity.SocialmediaLinks;

public interface SocialmediaLinkRepository extends JpaRepository<SocialmediaLinks, Integer> {
	public SocialmediaLinks findById(int id);
	
	public SocialmediaLinks findByBrand(String brand);
}
