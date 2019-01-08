package com.nibss.tqs.ebillspay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Emor on 7/2/16.
 */
@Entity
@Table(name="user_entered_params")
@NamedQueries(
        {
                @NamedQuery(name = "UserParam.findBySessionId", query = "SELECT u FROM UserParam u WHERE u.transaction.sessionId = ?1 ORDER BY u.name")
        }
)
public class UserParam implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3938882610523320837L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private long id;

    @Column(name="param_name",nullable = false)
    @Getter @Setter
    private String name;

    @Column(name="param_value")
    @Getter @Setter
    private String value;


    @ManyToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Getter @Setter
    private BaseTransaction transaction;
}
