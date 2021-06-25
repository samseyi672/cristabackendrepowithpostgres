package com.crista.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.crista.enties.Product;
import com.crista.exceptions.ProductException;
import com.crista.service.MapValidationServiceError;
import com.crista.service.ProductsService;
import com.crista.utils.ProductAttributeSummary;
import com.crista.utils.ProductDisplay;

import io.jsonwebtoken.lang.Collections;

@RestController
@RequestMapping("/product")
public class ProductController {

	@Autowired
	ProductsService prdService;
	@Value("${upload.dir}")
	String fileDirectory;

	@Value("${backendurl.req}")
	String backendurl;

	@Autowired
	private MapValidationServiceError mapValidationErrorService;

	@PostMapping("/create")
	public ResponseEntity<?> create(@Valid @ModelAttribute Product product,
			@RequestParam(value = "prdfile") MultipartFile file) throws IOException {
		System.out.println(" product " + product);
		System.out.println("product files");
		File filetoupload = new File(fileDirectory + "/" + file.getOriginalFilename());
		filetoupload.createNewFile();
		FileOutputStream out = new FileOutputStream(filetoupload);
		out.write(file.getBytes());
		out.close();
		System.out.println("file name " + filetoupload.getName());
		product.setImageurl(filetoupload.getAbsolutePath());
		prdService.create(product);
		return new ResponseEntity<String>("successful", HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public Product findProductById(HttpServletRequest request, @PathVariable("id") long id,
			@RequestParam(value = "detail", required = false) String detail) {
		System.out.println("context path " + request.getContextPath() + " " + request.getRequestURI() + " address "
				+ request.getRemoteAddr());
		String requestUri = "details";
		if (detail != null) {
			System.out.println("in details ");
			if (detail.equalsIgnoreCase(requestUri)) {
				Product prd = prdService.findProduct(id);
				System.out.println("before " + prd.getImageurl());
				prd.setImageurl(new File(prd.getImageurl()).getName());
				System.out.println(prd.getImageurl());
				prd.setValnumber(null);
				prd.setVendorname(null);
				// prd.setShortsummary(null);
				prd.setMetatitle(null);
				prd.setMetadescription(null);
				prd.setExpirydate(null);
				prd.setDateofcreation(null);
				prd.setFrontpage(false);
				if (prd.getState().equalsIgnoreCase("enable")) {
					prd.setState(null);
				} else if (prd.getState().equalsIgnoreCase("disable")) {
					return new Product(); // return empty products to users
				}
				return prd;
			}
		}
		return prdService.findProduct(id);
	}

	@GetMapping("/findallproduct")
	public List<Product> findallproduct(HttpServletRequest request, @RequestParam("vendorname") String vendorname) {
		return prdService.findByVendorname(vendorname);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") long id) {
		prdService.delete(id);
		return new ResponseEntity<String>("Products removed", HttpStatus.OK);
	}

	@PostMapping("/product")
	public String product(@RequestParam(value = "prdfile") MultipartFile file) throws IOException {
		System.out.println("product files");
		File filetoupload = new File(fileDirectory + "/" + file.getOriginalFilename());
		filetoupload.createNewFile();
		FileOutputStream out = new FileOutputStream(filetoupload);
		out.write(file.getBytes());
		out.close();
		// System.out.println(product);
		// product.setImageurl(filetoupload.getAbsolutePath()); // save the package info
		System.out.println(" saving the product files ");
//		SparkSession spark  = SparkSession.builder().appName("JetCart")
//				.config("spark.sql.dir.warehouse.dir","file:///c:temp/")
//				.master("local[5]")
//				.getOrCreate() ;
		String back = "\\";
		String replace = "/";
		String filepath = filetoupload.getAbsolutePath().replace("\\", "/");
		System.out.println(filepath);
		SXSSFWorkbook workbook = new SXSSFWorkbook(100);
		FileInputStream excelFiletoProcessed = new FileInputStream(new File(filepath));
		Workbook myworkBook = new XSSFWorkbook(excelFiletoProcessed);
		System.out.println("excel file loaded .....");
		Sheet sheet = myworkBook.getSheetAt(0);
		Map<Integer, List<String>> data = new HashMap<>();
		int i = 0;
		for (Row row : sheet) {
			if (i == 0) {
				i++;
				continue;
			}
			data.put(i, new ArrayList<String>());
			for (Cell cell : row) {
				switch (cell.getCellTypeEnum()) {
				case STRING:
					data.get(new Integer(i)).add(cell.getRichStringCellValue().getString());
					System.out.print(" " + cell.getRichStringCellValue().getString() + " ");
					break;
				case NUMERIC:
					// if it is a date
					if (DateUtil.isCellDateFormatted(cell)) {
						data.get(new Integer(i)).add(cell.getDateCellValue() + "");
						System.out.print(" " + cell.getDateCellValue() + " ");
					} else {
						data.get(new Integer(i)).add(cell.getNumericCellValue() + "");
						System.out.print(" " + cell.getNumericCellValue() + " ");
					}
					break;
				case BOOLEAN:
					data.get(new Integer(i)).add(cell.getBooleanCellValue() + "");
					System.out.print(" " + cell.getBooleanCellValue() + " ");
					break;
				case FORMULA:
					data.get(new Integer(i)).add(cell.getCellFormula() + "");
					System.out.print(" " + cell.getCellFormula() + " ");
					break;
				default:
					data.get(new Integer(i)).add(" ");
					break;
				}

			}
			System.out.println("\nthis is a new row");
			i++;
		}
		System.out.println(" data " + data);

		System.out.println("processing  and uploading to database ");
		Collection<List<String>> filecontents = data.values();
		System.out.println(" contents " + filecontents);
		List<Product> products = filecontents.stream().map(mapper -> {
			System.out.println(" mapper " + mapper);
			Product p = new Product();
			String[] prditems = mapper.toArray(new String[mapper.size()]);
			int y = 0;
			for (String item : prditems) {
				System.out.println(" item " + y + " " + item);
				y++;
			}
			System.out.println(" prditems " + prditems[0]);
			p.setProductname(prditems[0]);
			p.setShortsummary(prditems[1]);
			p.setProdtype(prditems[2]);
			p.setMetatitle(prditems[3]);
			p.setCategoryid(prditems[4]);
			p.setProddescription(prditems[5]);
			p.setTax(prditems[6]);
			p.setValnumber(prditems[7]);
			p.setFrontpage(Boolean.parseBoolean(prditems[8].toLowerCase()));
			p.setProductprice(prditems[9]);
			p.setSubcategoryid(prditems[10]);
			p.setProductstatus(prditems[11]);
			p.setState(prditems[12]);
			// System.out.println(" date "+ prditems[13]);
			LocalDateTime ldt = LocalDateTime.parse(prditems[13],
					DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss zzz yyyy"));
			System.out.println(" date " + ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date date = sdt.parse(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				p.setExpirydate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			p.setVendorname(prditems[14]);
			p.setModel(prditems[15]);
			p.setMetadescription(prditems[16]);
			p.setProductcode(prditems[17]);
			p.setProductquantity(prditems[18]);
			p.setSize(prditems[19]);
			return p;
		}).collect(Collectors.toList());
		System.out.println("products " + products);
		prdService.save(products); // save products
////	Dataset<Row> excedata  = spark.read().format("com.crealytics.spark.excel")
////			.option("header","true")
////			.option("useHeader","true")
////			.option("inferSchema","true")
////			.load(filepath)  ;
////	        excedata.show(10);
//		prdService.save(products);
		System.out.println("preparing  to call the image api ");
		return "file uploaded";
	}

	@PostMapping(value = "/uploadfiles", consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = "application/json")
	public String uploadfiles(HttpServletRequest request, @RequestParam("vendorname") String vendorname)
			throws IOException {
		System.out.println("product images files ......" + " vendorname " + vendorname);
		// load images products where images are null
		// List<Product> list = prdService.findByVendorname(vendorname) ;
		// System.out.println(" list "+list+ " size "+list.size());
		List<Product> products = prdService.findProductByImageUrl(vendorname);
		System.out.println("Products " + products);
		MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
//		    System.out.println(" vendorname 2 "+multipart.getParameter("vendorname")) ;
		Iterator<String> iter = multipart.getFileNames();
		List<Product> listofnewproducts = java.util.Collections.synchronizedList(new ArrayList<>());
		int i = 0;
		while (iter.hasNext()) {
			String uploadfile = iter.next();
			MultipartFile file = multipart.getFile(uploadfile);
			// check the list for file name.
			File filetoupload = new File(fileDirectory + "/" + file.getOriginalFilename());
			filetoupload.createNewFile();
			System.out.println(" file name before creation " + filetoupload.getName());
			// check before writing to disk
			String filename = filetoupload.getName();
			// check the entire disk
			System.out.println(" products " + products + " filename " + filename);
			for (Product p : products) { //
				try {
					System.out.println(" p " + p);
					if (p.getProductname().equalsIgnoreCase(filename)) {
						System.out.println(" inside if ");
						p.setImageurl(filetoupload.getAbsolutePath());
						FileOutputStream out = new FileOutputStream(filetoupload);
						out.write(file.getBytes());
						out.close();
						listofnewproducts.add(p);
						System.out.println(" file name uploaded " + filetoupload.getName());
					}
					i++;
					System.out.println("counter " + i);
				} catch (Exception ex) {
					System.out.println("exception " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
		prdService.save(listofnewproducts);
		System.out.println("list of new" + listofnewproducts);
		return "All files uploaded  successfully ";
	}

	@GetMapping("/allproduct")
	public String getallProduct(@RequestParam("pageSize") int pageSize, @RequestParam("pageNumber") int pageNumber) {
		System.out.println("entered ............");
		return prdService.getAllproducts(pageSize, pageNumber);
	}

	@GetMapping("/display/{id}")
	public ProductAttributeSummary product(@PathVariable("id") long id) {
		System.out.println("entered ............");
		Product p = prdService.findProduct(id);
		if (p == null) {
			throw new ProductException("product not found ");
		}
		// take some properties out product
		ProductAttributeSummary productAttributeSummary = new ProductAttributeSummary();
		productAttributeSummary.setVendorname(p.getVendorname());
		productAttributeSummary.setId(p.getId());
		productAttributeSummary.setImageurl(new File(p.getImageurl()).getName());
		productAttributeSummary.setModel(p.getModel());
		productAttributeSummary.setCategoryid(p.getCategoryid());
		productAttributeSummary.setProddescription(p.getProddescription());
		productAttributeSummary.setProductname(p.getProductname());
		productAttributeSummary.setProductquantity(p.getProductquantity());
		productAttributeSummary.setProductstatus(p.getProductstatus());
		productAttributeSummary.setShortsummary(p.getShortsummary());
		productAttributeSummary.setSize(p.getSize());
		productAttributeSummary.setState(p.getState());
		productAttributeSummary.setTax(p.getTax());
		productAttributeSummary.setProductprice(p.getProductprice());
		return productAttributeSummary;
	}

	@GetMapping("/cartdisplay")
	public List<ProductDisplay> displayCart(@RequestParam("min") int min, @RequestParam("max") int max) {
		System.out.println("entered method ......");
		List<Product> cart = prdService.getProductForDisplay(min, max);
		List<ProductDisplay> display = cart.parallelStream().map(p -> {
			ProductDisplay prd = new ProductDisplay();
			prd.setId(p.getId());
			prd.setProductname(p.getProductname());
			prd.setProductprice(p.getProductprice());
			prd.setProductoldprice(p.getProductoldprice());
			prd.setImageurl(p.getImageurl());
			return prd;
		}).collect(Collectors.toList());
		return display;
	}

	@GetMapping("/cartspecial")
	public List<ProductDisplay> displayCartBySpecial(@RequestParam("min") int min, @RequestParam("max") int max) {
		System.out.println("entered method ......");
		List<Product> cart = prdService.getProductForDisplayBySpecial(min, max);
		List<ProductDisplay> display = cart.parallelStream().map(p -> {
			ProductDisplay prd = new ProductDisplay();
			prd.setId(p.getId());
			prd.setProductname(p.getProductname());
			prd.setProductprice(p.getProductprice());
			prd.setProductoldprice(p.getProductoldprice());
			prd.setImageurl(p.getImageurl());
			return prd;
		}).collect(Collectors.toList());
		return display;
	}

	@GetMapping("/cartnewarrival")
	public List<ProductDisplay> displayCartByNewArrival(@RequestParam("min") int min, @RequestParam("max") int max) {
		System.out.println("entered method ......");
		List<Product> cart = prdService.getProductForDisplayByNewarrival(min, max);
		List<ProductDisplay> display = cart.parallelStream().map(p -> {
			ProductDisplay prd = new ProductDisplay();
			prd.setId(p.getId());
			prd.setProductname(p.getProductname());
			prd.setProductprice(p.getProductprice());
			prd.setProductoldprice(p.getProductoldprice());
			prd.setImageurl(p.getImageurl());
			return prd;
		}).collect(Collectors.toList());
		return display;
	}

	@GetMapping("/cartfeatured")
	public List<ProductDisplay> displayCartByFeatured(@RequestParam("min") int min, @RequestParam("max") int max) {
		System.out.println("entered method ......");
		List<Product> cart = prdService.getProductForDisplayByFeatured(min, max);
		List<ProductDisplay> display = cart.parallelStream().map(p -> {
			ProductDisplay prd = new ProductDisplay();
			prd.setId(p.getId());
			prd.setProductname(p.getProductname());
			prd.setProductprice(p.getProductprice());
			prd.setProductoldprice(p.getProductoldprice());
			prd.setImageurl(p.getImageurl());
			return prd;
		}).collect(Collectors.toList());
		// process the dom images to display
		return display;
	}

	@GetMapping("/byspecial")
	public String productBySpecial(int pageSize, int pageNumber) {
		return prdService.paginateProductBySpecial(pageSize, pageNumber, backendurl);
	}

	@GetMapping("/byfeatured")
	public String productByFeatured(int pageSize, int pageNumber) {
		return prdService.paginateProductByFeatured(pageSize, pageNumber, backendurl);
	}

	@GetMapping("/bynewarrival")
	public String productByNewArrival(int pageSize, int pageNumber) {
		return prdService.paginateProductByNewArrival(pageSize, pageNumber, backendurl);
	}

	@GetMapping("/loadproduct")
	public Map<String,List<Product>> loadProductForMarketPlace() {
		return prdService.loadProductForMarketPlace(backendurl);
	}

	
	
}





















































