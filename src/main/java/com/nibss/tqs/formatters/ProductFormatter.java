package com.nibss.tqs.formatters;

import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 8/19/2016.
 */
@Component
public class ProductFormatter implements Formatter<Product> {

    @Autowired
    private ProductRepository productRepository;


    @Override
    public Product parse(String s, Locale locale) throws ParseException {
        int productId = Integer.parseInt(s);
        return productRepository.findOne(productId);
    }

    @Override
    public String print(Product product, Locale locale) {
        return Integer.toString(product.getId());
    }
}
