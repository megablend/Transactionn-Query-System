package com.nibss.tqs.core.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
public  abstract class BaseEntity implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7292882792320173894L;
	
	@Getter @Setter
	@Column(name="date_created",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	protected Date dateCreated;
	
	@PrePersist
	protected void onCreate() {
		dateCreated = new Date();
	}
}
