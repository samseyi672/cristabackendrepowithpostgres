package com.crista.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.crista.enties.Category;
import com.crista.enties.Product;
import com.crista.repositories.ProductsRepository;
import com.crista.utils.Pagination;

@Service
public class ProductsService {

	@Autowired
	ProductsRepository prdRepo;

	@Autowired
	CategoriesService categoriesService ;
	
	public Product create(Product prd) {
		return prdRepo.save(prd);
	}

	public void delete(long id) {
		Product prd = prdRepo.getByid(id);
		if (prd == null) {
			throw new UsernameNotFoundException("products does not exists");
		}
		prdRepo.deleteById(id);
	}

	public void save(List<Product> list) {
		prdRepo.saveAll(list);
	}

	public Product findProduct(long id) {
		return prdRepo.findById(id);
	}

	public List<Product> findByVendorname(String vendorname) {
		return prdRepo.findByVendorname(vendorname);
	}

	public List<Product> findProductByImageUrl(String vendorname) {
		return prdRepo.findByVendornameAndImageurlIsNull(vendorname);
	}

	Map hashMap = new ConcurrentHashMap(); // configure pagination properties
	// paginated method

	@GetMapping("/allproduct")
	public String getAllproducts(int pageSize, int pageNumber) {
		String results = "";
		int page2 = pageNumber / pageSize;
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page2, pageSize, Sort.by("id").ascending());
		StringBuffer html = new StringBuffer();

		org.springframework.data.domain.Page<Product> datas = prdRepo.findAll(pageable);
		System.out.println(datas);
		if (!datas.isEmpty()) {
			// process the pagination
			html.append("<div class=\"row products-admin ratio_asos\">");
			for (Product p : datas) {
				// html.append("<div class=\"row products-admin ratio_asos\">");
				html.append(" <div class=\"col-xl-3 col-sm-6\">");
				html.append("<div class=\"card\">");
				html.append("<div class=\"card-body product-box\">");
				html.append("<div class=\"img-wrapper\">");
				if (p.getProductstatus().equalsIgnoreCase("new")) {
					html.append(
							"<div class=\"lable-block\"><span class=\"lable3\">new</span> <span class=\"lable4\">on sale</span></div>");
				}
				html.append("<div class=\"front\">");
				//edit the below url later .dont forget 
				html.append("<a href=\"#\"><img src=\"https://jeteshop.herokuapp.com/jetstore/"+new File(p.getImageurl()).getName()+"\"class=\"img-fluid blur-up lazyload bg-img\" alt=\"\"></a>");
				html.append("</div></div> <div class=\"product-detail\">");
				html.append(
						"<div class=\"rating\"><i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i></div>");
				html.append("<a href=\"#\"><h6>"+p.getProductname()+"</h6></a>");
				html.append("<h4>"+p.getProductprice()+"<del>$600.00</del></h4>");
				html.append("</div></div></div></div>");
			}
			results += "</div>";
			hashMap.put("totalRows", datas.getTotalElements());
			hashMap.put("perPage", pageSize);
			hashMap.put("filter", "searchFilter");
			hashMap.put("currentPage", pageNumber);
			Pagination pagination = new Pagination(hashMap);
			System.out.println(html.toString());
			results += html.toString();
			// results += "<div style=\"margin-bottom:120px;\"></div>" ;
			results += pagination.paginate();
		} else {
			System.out.println("it is empty .....");
			results += "<center><h2> Product not available </h2></center>";
		}
		return results;
	}

	public List<Product> getProductForDisplay(int min, int max) {
		return prdRepo.findAll(min, max);
	}

	public List<Product> getProductForDisplayByFeatured(int min, int max) {
		return prdRepo.findAll(min, max);
	}

	public List<Product> getProductForDisplayBySpecial(int min, int max) {
		return prdRepo.findAll(min, max);
	}

	// paginate by special
	public String paginateProductBySpecial(int pageSize, int pageNumber, String hostpath) {
		int page2 = pageNumber / pageSize;
		String results = "";
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page2, pageSize, Sort.by("id").ascending());
		StringBuffer builder = new StringBuffer();
		builder.append("<div class=\"row no-slider\">");
		Page<Product> prd = prdRepo.findAll(pageable);
		if (!prd.isEmpty()) {
			for (Product p : prd) { // process the pageable product
				// System.out.println("product "+p);
				if (p.getSpecial() != null) {
					String image = new File(p.getImageurl()).getName();
					builder.append("<div class=\"product-box\">");
					builder.append("<div class=\"img-wrapper\">");
					builder.append("<div class=\"front\">");
					builder.append("<a href=\"#\" onclick=\"loadprd(" + p.getId() + ",'/jetcart/productpage')\">");
					builder.append("<img src=\"" + hostpath + "/jetstore/" + image
							+ "\" class=\"img-fluid blur-up lazyload bg-img\" alt=\"\"></a>");
					builder.append("</div>");
					builder.append("<div class=\"cart-info cart-wrap\">");
					builder.append("<button data-toggle=\"modal\" data-target=\"#addtocart\"");
					builder.append("title=\"Add to cart\">");
					builder.append("<i class=\"ti-shopping-cart\"></i></button>");
					builder.append("<a href=\"javascript:void(0)\" title=\"Add to Wishlist\">");
					builder.append("<i class=\"ti-heart\" aria-hidden=\"true\"></i> </a>");
					builder.append(
							"<a href=\"#\" data-toggle=\"modal\" data-target=\"#quick-view\" title=\"Quick View\">");
					builder.append("<i class=\"ti-search\" aria-hidden=\"true\"></i></a>");
					builder.append(
							"<a href=\"compare.html\" title=\"Compare\"><i class=\"ti-reload\" aria-hidden=\"true\"></i></a>");
					builder.append("</div></div>");
					builder.append("<div class=\"details-product\">");
					builder.append("<a href=\"/jetcart/productpage\" onclick=\"loadprd(2,'/jetcart/productpage')\">");
					builder.append("<h6>" + p.getProductname() + "</h6></a>");
					if (p.getProductoldprice() == null) {
						builder.append("<h4>" + p.getProductname() + "&nbsp;</h4>");
					} else {
						builder.append("<h4>" + p.getProductname() + "<del>" + p.getProductoldprice() + "</del></h4>");
					}
					builder.append("</div></div>");
				} else {
					continue;
				}
			}
			results += "</div>";
			hashMap.put("totalRows", prd.getTotalElements());
			hashMap.put("perPage", pageSize);
			hashMap.put("filter", "searchFilter2");
			hashMap.put("currentPage", pageNumber);
			Pagination pagination = new Pagination(hashMap);
			System.out.println(builder.toString());
			results += builder.toString();
			// results += "<div style=\"margin-bottom:120px;\"></div>" ;
			results += pagination.paginate();
			return results;
		} else {
			results = "<center><h2> Product not available </h2></center>";
		}
		return results;
	}

	public List<Product> getProductForDisplayByNewarrival(int min, int max) {
		return prdRepo.findAll(min, max);
	}

	public List<Product> getProductForDisplayByCategory(int min, int max, String category) {
		return prdRepo.findAll(min, max, category);
	}

	// paginate by featured
	public String paginateProductByFeatured(int pageSize, int pageNumber, String hostpath) {
		int page2 = pageNumber / pageSize;
		String results = "";
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page2, pageSize, Sort.by("id").ascending());
		StringBuffer builder = new StringBuffer();
		Page<Product> prd = prdRepo.findAll(pageable);
		System.out.println("featured " + prd.getSize());
		builder.append("<div class=\"row no-slider\">");
		int counter = 0;
		if (!prd.isEmpty()) {
			for (Product p : prd) { // process the pageable product
				// System.out.println("product "+p);
				if (p.getFeatured() != null) {
					System.out.println("featured " + p.getFeatured());
					counter++;
					String image = new File(p.getImageurl()).getName();
					builder.append("<div class=\"product-box\">");
					builder.append("<div class=\"img-wrapper\">");
					builder.append("<div class=\"front\">");
					builder.append("<a href=\"#\" onclick=\"loadprd(" + p.getId() + ",'/jetcart/productpage')\">");
					builder.append("<img src=\"" + hostpath + "/jetstore/" + image
							+ "\" class=\"img-fluid blur-up lazyload bg-img\" alt=\"\"></a>");
					builder.append("</div>");
					builder.append("<div class=\"cart-info cart-wrap\">");
					builder.append("<button data-toggle=\"modal\" data-target=\"#addtocart\"");
					builder.append("title=\"Add to cart\">");
					builder.append("<i class=\"ti-shopping-cart\"></i></button>");
					builder.append("<a href=\"javascript:void(0)\" title=\"Add to Wishlist\">");
					builder.append("<i class=\"ti-heart\" aria-hidden=\"true\"></i> </a>");
					builder.append(
							"<a href=\"#\" data-toggle=\"modal\" data-target=\"#quick-view\" title=\"Quick View\">");
					builder.append("<i class=\"ti-search\" aria-hidden=\"true\"></i></a>");
					builder.append(
							"<a href=\"compare.html\" title=\"Compare\"><i class=\"ti-reload\" aria-hidden=\"true\"></i></a>");
					builder.append("</div></div>");
					builder.append("<div class=\"details-product\">");
					builder.append("<a href=\"/jetcart/productpage\" onclick=\"loadprd(2,'/jetcart/productpage')\">");
					builder.append("<h6>" + p.getProductname() + "</h6></a>");
					if (p.getProductoldprice() == null) {
						builder.append("<h4>" + p.getProductname() + "&nbsp;</h4>");
					} else {
						builder.append("<h4>" + p.getProductname() + "<del>" + p.getProductoldprice() + "</del></h4>");
					}
					builder.append("</div></div>");
				}
			}
			results += "</div>";
			hashMap.put("totalRows", counter);
			hashMap.put("perPage", pageSize);
			hashMap.put("filter", "searchFeaturedFilter");
			hashMap.put("currentPage", pageNumber);
			Pagination pagination = new Pagination(hashMap);
			// System.out.println(builder.toString());
			results += builder.toString();
			// results += "<div style=\"margin-bottom:120px;\"></div>" ;
			results += pagination.paginate();
			return results;
		} else {
			results = "<center><h2> Product not available </h2></center>";
		}
		return results;
	}

	// by new arrival
	public String paginateProductByNewArrival(int pageSize, int pageNumber, String hostpath) {
		int page2 = pageNumber / pageSize;
		String results = "";
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page2, pageSize, Sort.by("id").ascending());
		StringBuffer builder = new StringBuffer();
		Page<Product> prd = prdRepo.findAll(pageable);
		System.out.println("featured " + prd.getSize());
		builder.append("<div class=\"row no-slider\">");
		AtomicInteger counter = new AtomicInteger();
		if (!prd.isEmpty()) {
			// use parrallel stream for this
			counter.set(0);
			prd.stream().filter(p -> p.getNewarrival() != null).forEach(p -> {
				System.out.println("new arrival " + p.getFeatured());
				counter.incrementAndGet();
				String image = new File(p.getImageurl()).getName();
				builder.append("<div class=\"product-box\">");
				builder.append("<div class=\"img-wrapper\">");
				builder.append("<div class=\"front\">");
				builder.append("<a href=\"#\" onclick=\"loadprd(" + p.getId() + ",'/jetcart/productpage')\">");
				builder.append("<img src=\"" + hostpath + "/jetstore/" + image
						+ "\" class=\"img-fluid blur-up lazyload bg-img\" alt=\"\"></a>");
				builder.append("</div>");
				builder.append("<div class=\"cart-info cart-wrap\">");
				builder.append("<button data-toggle=\"modal\" data-target=\"#addtocart\"");
				builder.append("title=\"Add to cart\">");
				builder.append("<i class=\"ti-shopping-cart\"></i></button>");
				builder.append("<a href=\"javascript:void(0)\" title=\"Add to Wishlist\">");
				builder.append("<i class=\"ti-heart\" aria-hidden=\"true\"></i> </a>");
				builder.append("<a href=\"#\" data-toggle=\"modal\" data-target=\"#quick-view\" title=\"Quick View\">");
				builder.append("<i class=\"ti-search\" aria-hidden=\"true\"></i></a>");
				builder.append(
						"<a href=\"compare.html\" title=\"Compare\"><i class=\"ti-reload\" aria-hidden=\"true\"></i></a>");
				builder.append("</div></div>");
				builder.append("<div class=\"details-product\">");
				builder.append("<a href=\"/jetcart/productpage\" onclick=\"loadprd(2,'/jetcart/productpage')\">");
				builder.append("<h6>" + p.getProductname() + "</h6></a>");
				if (p.getProductoldprice() == null) {
					builder.append("<h4>" + p.getProductname() + "&nbsp;</h4>");
				} else {
					builder.append("<h4>" + p.getProductname() + "<del>" + p.getProductoldprice() + "</del></h4>");
				}
				builder.append("</div></div>");
			});
			results += "</div>";
			hashMap.put("totalRows", counter.get());
			hashMap.put("perPage", pageSize);
			hashMap.put("filter", "searchNewArrivalFilter");
			hashMap.put("currentPage", pageNumber);
			Pagination pagination = new Pagination(hashMap);
			// System.out.println(builder.toString());
			results += builder.toString();
			results += pagination.paginate();
			return results;
		} else {
			results = "<center><h2> Product not available </h2></center>";
		}
		return results;

	}
       
	public Map<String,List<Product>> loadProductForMarketPlace(String backendurl) {
		List<Category> listofcat  = categoriesService.getAllCategories() ;
		System.out.println("category size "+ listofcat.size());
//		 List<Product> prdlist  = Collections.synchronizedList(new ArrayList<Product>()) ;
		 Map<String,List<Product>> prdtocategory  =  new ConcurrentHashMap<String, List<Product>>() ;
		 listofcat.parallelStream().forEach(cat->{
	List<Product> prdlist  = Collections.synchronizedList(prdRepo.findAll(0,4,cat.getCategoryname()));
	  System.out.println(" the list "+prdlist);  
	          if(!prdlist.isEmpty()) {
	         List<Product> mapPrdList  =Collections.synchronizedList(prdlist.stream().map(p->{
				 Product  newp  =  new Product() ;
				 newp.setProductname(p.getProductname());
				 newp.setProductprice(p.getProductprice());
				 newp.setProductreview(p.getProductreview());
				 System.out.println(p.getImageurl());
				 newp.setImageurl(new File(p.getImageurl()).getName());
				 newp.setId(p.getId());
				 newp.setCategoryid(p.getCategoryid());
				 newp.setProductoldprice(p.getProductoldprice());
				 System.out.println(" new "+newp.toString());
				 return newp;
			 }).collect(Collectors.toList()))  ;
	         prdtocategory.put(cat.getCategoryname(), mapPrdList)  ;
			  System.out.println(" the new  list "+mapPrdList);
		        }         
		        });	
		 return prdtocategory ;
//		 StringBuilder html  = new  StringBuilder() ;
//		// process category to product
//		 prdtocategory.forEach((category,prdlist)->{
//			 StringBuilder html2 = new  StringBuilder() ;
//			 StringBuilder html3 = new  StringBuilder() ;
//			 if(!category.isEmpty() && !prdlist.isEmpty()) {
//			 if(!category.isEmpty()) {				 
//			 html3.append("<section class=\" ratio_asos section-b-space\"><div class=\"title2\">") ;
//			 html3.append("<h2 class=\"title-inner2\">"+category+"</h2></div>") ;
//			 html3.append("<div class=\"container\"><div class=\"row\">"
//			    + "<div class=\"col\"><div class=\"product-4 product-m no-arrow\">"); 
//		           }
//			 if(!prdlist.isEmpty()) {
////		          <div class="product-box"><div class="img-wrapper"><div class="front">
////           <a href="/jetcart/productpageappliance"><img src="/assets/images/pro3/27.jpg" class="img-fluid blur-up lazyload bg-img" alt=""></a>
////                      </div><div class="back">
////           <a href="/jetcart/productpageappliance"><img src="/assets/images/pro3/28.jpg" class="img-fluid blur-up lazyload bg-img" alt=""></a>
////                      </div><div class="cart-info cart-wrap">
////          <button data-toggle="modal" data-target="#addtocart" title="Add to cart">
////		    <i class="ti-shopping-cart"></i></button> <a href="javascript:void(0)" title="Add to Wishlist"><i class="ti-heart" aria-hidden="true"></i></a> <a href="#" data-toggle="modal"
////          data-target="#quick-view" title="Quick View">
////		   <i class="ti-search" aria-hidden="true"></i></a> <a href="compare.html" title="Compare"><i class="ti-reload" aria-hidden="true"></i></a></div>
////          </div><div class="product-detail">
////          <div class="rating"><i class="fa fa-star"></i> <i class="fa fa-star"></i> 
////          <i class="fa fa-star"></i> <i class="fa fa-star"></i> <i class="fa fa-star"></i>
////           </div><a href="/jetcart/productpageappliance"><h6>Laptops</h6></a>
////          <h4>$500.00</h4></div></div>
//				 prdlist.forEach(p->{
//					  html2.append("<div class=\"product-box\"><div class=\"img-wrapper\"><div class=\"front\">")  ;
//				      html2.append("<a href=\"/jetcart/productpage\" onclick=\"loadprd("+p.getId()+",'/jetcart/productpage')\"><img src="+backendurl+"/jetstore/"+new File(p.getImageurl()).getName()+" class=\"img-fluid blur-up lazyload bg-img\" alt=\"\"></a>")   ;
//				      html2.append("</div><div class=\"back\">") ;
//				      html2.append("<a href=\"/jetcart/productpage\" onclick=\"loadprd("+p.getId()+",'/jetcart/productpage')\"><img src=\""+
//				      backendurl+"/jetstore/"+new File(p.getImageurl()).getName()+"\" class=\"img-fluid blur-up lazyload bg-img\" width=\"5px\" height=\"5px\" alt=\"\"></a>") ;
//				      System.out.println(p.getCategoryid()+"product "+backendurl+"/jetstore/"+new File(p.getImageurl()).getName());
//				      html2.append("</div><div class=\"cart-info cart-wrap\">")  ;
//				      html2.append("<button data-toggle=\"modal\" data-target=\"#addtocart\" title=\"Add to cart\"><i class=\"ti-shopping-cart\"></i></button> <a href=\"javascript:void(0)\" title=\"Add to Wishlist\"><i class=\"ti-heart\" aria-hidden=\"true\"></i></a> <a href=\"#\" data-toggle=\"modal\" data-target=\"#quick-view\" title=\"Quick View\"><i class=\"ti-search\" aria-hidden=\"true\"></i></a> <a href=\"compare.html\" title=\"Compare\"><i class=\"ti-reload\" aria-hidden=\"true\"></i></a></div>")  ;
//				      html2.append("</div><div class=\"product-detail\">")  ;
//				      html2.append("<div class=\"rating\"><i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i> <i class=\"fa fa-star\"></i>");
//				      html2.append("</div><a href=\"/jetcart/productpage\" onclick=\"loadprd("+p.getId()+",'/jetcart/productpage')\">") ;
//				      html2.append("<h6>"+category+"</h6></a><h4>"+p.getProductprice()+"</h4>")  ;
//				      html2.append("</div></div>")  ;
//				      System.out.println(" "+ p.getCategoryid());
//				      });
//				 // html2.append("</div></div></div></div>") ;			 
//				  html3.append(html2.toString())  ; 
//				  html3.append("</div></div></div></div>")  ;
//			            }
//		             }
//			 html.append(html3.toString()) ;
//		          }); 	 
				//return html.toString()+"</div></div></div></div>";
		    //return  null;
	     }
}




















































































