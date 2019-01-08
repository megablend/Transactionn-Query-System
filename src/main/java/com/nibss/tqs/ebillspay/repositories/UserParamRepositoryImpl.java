package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.UserParam;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaContext;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public class UserParamRepositoryImpl implements UserParamCustomRepo {

    private EntityManager em;

    @Autowired
    private ApplicationSettings applicationSettings;

    @Autowired
    public UserParamRepositoryImpl(final JpaContext jpaContext) {
        em = jpaContext.getEntityManagerByManagedType(UserParam.class);
    }

    @Override
    public List<String> getParamNamesForBiller(int billerId) {
        String query = "SELECT t.sessionId FROM BaseTransaction t WHERE t.biller.id = ?1 ORDER BY t.dateApproved DESC,t.transactionDate DESC";
        TypedQuery<String> trxnQ = em.createQuery(query,String.class);
        trxnQ.setParameter(1, billerId).setMaxResults(1);


        String sessionId = null;
        List<UserParam> params = null;
        try {
            sessionId  =  trxnQ.getSingleResult();
            if( null == sessionId)
                return new ArrayList<>(0);

            TypedQuery<UserParam> paramQ = em.createNamedQuery("UserParam.findBySessionId",UserParam.class);
            paramQ.setParameter(1, sessionId);
            params = paramQ.getResultList();

        } catch(Exception e) {
            //no transaction has been done by biller.
        }

        if( null == sessionId)
            return new ArrayList<>(0);


        if( null != params) {
            List<String> paramNames = params.stream().map( p -> p.getName()).collect(Collectors.toList());

            if( null != applicationSettings.excludedUserParams()) {
                Iterator<String> itr = paramNames.iterator();
                while(itr.hasNext()) {
                    String name = itr.next();
                    if( applicationSettings.excludedUserParams().stream().anyMatch( s-> s.equalsIgnoreCase(name)))
                        itr.remove();
                }
            }

            List<String> theParams = removeDuplicates(paramNames);

            return   new ArrayList<>( new HashSet<>(theParams) );
        }

        return  new ArrayList<>();
    }

    private List<String> removeDuplicates(List<String> params) {
        List<String> theParams = new ArrayList<>(params);
        theParams.sort( (a,b) -> a.compareTo(b));
        Iterator<String> itr = params.iterator();

        while (itr.hasNext()) {
            String x = itr.next();
            if(x.contains(" ")) {
                String y = x.replace(" ","");
                Optional<String> found = theParams.stream().filter(s -> s.equalsIgnoreCase(y)).findFirst();
                if(found.isPresent())
                    theParams.remove(found.get());
            }
        }

        return  theParams;
    }

}
