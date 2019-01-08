package com.nibss.tqs.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;



/**
 * Created by Emor on 7/2/16.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager();
        cacheManager.setCacheManager(ecache());
        return  cacheManager;
    }

    public net.sf.ehcache.CacheManager ecache() {
        EhCacheManagerFactoryBean cache = new EhCacheManagerFactoryBean();
        cache.setConfigLocation( new ClassPathResource("ehcache.xml"));

        cache.afterPropertiesSet();
        return cache.getObject();
    }
}
