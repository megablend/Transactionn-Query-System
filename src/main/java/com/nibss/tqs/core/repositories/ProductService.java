package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 6/5/2017.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;


    @Cacheable(cacheNames = "products", unless = "#result == null")
    public Product findByName(String name) {
        return productRepository.findByName(name);
    }

    @Cacheable(cacheNames = "products", unless = "#result == null || #result.empty")
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Cacheable(cacheNames = "products", unless = "#result == null")
    public Product findByCode(String code) {
        return productRepository.findByCode(code);
    }


    @Cacheable(cacheNames = "products", unless = "#result == null || #result.empty")
    public List<Product> findAll(Iterable<Integer> ids) {
        return productRepository.findAll(ids);
    }

    @CacheEvict(cacheNames = "products")
    public Product save(final Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(cacheNames = "products")
    public List<Product> save(Iterable<Product> products) {
        return productRepository.save(products);
    }

    @Cacheable(cacheNames = "products")
    public List<IProduct> findAllByProjection() {
        return productRepository.findAllByProjection();
    }

    public List<IProduct> findByOrganizationProjecttion(int id) {
        return productRepository.findByOrganizationProjection(id);
    }

    public Set<Product> findByOrganization(int id) {
        return  productRepository.findByOrganization(id);
    }

    public int saveOrganizationProduct(int orgId, int producId) {
        return productRepository.saveOrganizationProduct(orgId, producId);
    }
}
