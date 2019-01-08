package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 7/20/2016.
 */
public interface ProductRepository extends JpaRepository<Product,Integer> {
    Product findByName(String name);

    List<Product> findAll();

    Product findByCode(String code);

    @Query("SELECT p.code FROM Product p")
    List<String> findCodes();


    @Query("SELECT p.code FROM Product p, IN(p.organizations) o WHERE o.id = ?1")
    List<String> findCodesByOrganization(int orgId);


    @Query("SELECT new com.nibss.tqs.ajax.AjaxProduct(p.id, p.name,p.code) FROM Product p ORDER BY p.name")
    List<IProduct> findAllByProjection();

    @Query("SELECT  new com.nibss.tqs.ajax.AjaxProduct(p.id, p.name,p.code) FROM Product p, IN(p.organizations) o WHERE o.id = ?1 ORDER BY p.name")
    List<IProduct> findByOrganizationProjection(int id);

    @Query("SELECT p FROM  Product p, IN(p.organizations) o WHERE o.id = ?1")
    Set<Product> findByOrganization(int id);

    @Transactional
    @Modifying
    @Query(value = "insert into organization_products(organizations_id, products_id) values (?1,?2)", nativeQuery = true)
    int saveOrganizationProduct(int orgId, int productId);
}
