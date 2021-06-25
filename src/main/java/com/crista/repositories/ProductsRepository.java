package com.crista.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.crista.enties.Product;
import com.crista.utils.ProductDisplay;

public interface ProductsRepository extends PagingAndSortingRepository<Product,Long> {
  Product getByid(long id) ; 
  
  List<Product>  findByVendorname(String vendorname); 
  Product findById(long id) ;
  //@Query("select p.productname,p.id from product p where p.vendorname=:vendorname and p.imageurl=null")
  List<Product> findByVendornameAndImageurlIsNull(@Param("vendorname") String vendorname);

  Page<Product> findAll(Pageable pageable) ;
  
  List<Product> getByCategoryid(String categoryid) ;
  
   // write some native  query to query product randomly
  @Query(value = "{call GetProducts(:min,:max)}",nativeQuery=true)
  List<Product> findAll(@Param("min") int min,@Param("max") int max) ;
  
  // by category
  @Query(value = "{call GetProductsByCategoryid(:min,:max,:category)}",nativeQuery=true)
  List<Product> findAll(@Param("min") int min,@Param("max") int max,String  category) ;
 
   // by featured
  @Query(value = "{call GetProductsByFeatured(:min,:max)}",nativeQuery=true)
  List<Product> findByFeatured(@Param("min") int min,@Param("max") int max) ;
   
   // by special
  @Query(value = "{call GetProductsBySpecial(:min,:max)}",nativeQuery=true)
  List<Product> findBySpecial(@Param("min") int min,@Param("max") int max) ;
  
//  @Query("select p from Product p where p.special=:special")
//  Page<Product> getBySpecial(Pageable pageable,String special) ;
  
  // by new arrival
  @Query(value = "{call GetProductsByNewArrival(:min,:max)}",nativeQuery=true)
  List<Product> findByNewarrival(@Param("min") int min,@Param("max") int max) ;
}






















































































































































