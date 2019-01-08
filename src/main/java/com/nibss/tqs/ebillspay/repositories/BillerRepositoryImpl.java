package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.Biller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by eoriarewo on 7/11/2016.
 */
public class BillerRepositoryImpl implements BillerCustomRepo {

    private EntityManager em;

    @Autowired
    public void setJpaContext(final JpaContext jpaContext) {
        em = jpaContext.getEntityManagerByManagedType(Biller.class);
    }

    @Override
    //TODO: make this cacheable
    public List<Biller> findByIds(List<Integer> ids) {
        TypedQuery<Biller> bQ = em.createQuery("SELECT b FROM Biller b WHERE b.id IN :ids",Biller.class);
        bQ.setParameter("ids",ids);
        return bQ.getResultList();
    }
}
