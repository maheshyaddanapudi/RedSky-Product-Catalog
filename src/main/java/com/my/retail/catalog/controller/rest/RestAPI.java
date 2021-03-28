package com.my.retail.catalog.controller.rest;

import com.my.retail.catalog.db.entities.Product;
import com.my.retail.catalog.dto.response.base.BaseResponseDTO;
import com.my.retail.catalog.dto.response.product.ProductDTO;
import com.my.retail.catalog.mappers.ProductMapper;
import com.my.retail.catalog.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@SuppressWarnings({ "unchecked", "rawtypes" })
@CrossOrigin(origins = "*")
@RequestMapping("/products")
@Tag(name = "Product Catalog Controller", description = "The API provides the interface for adding / removing or updating products to database.")
public class RestAPI {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public RestAPI(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping(produces = "application/json")
    @Operation(summary = "Provides Product Info", description = "Returns JSON formatted Product Info", tags = { "product" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully returned Product Info",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))) ,
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> getAllProducts() {

        try{
            List<ProductDTO> products = new ArrayList<ProductDTO>();
            this.productService.findAllProducts().stream().forEach((product) -> {
                products.add((this.productMapper.mapProductToDto(product)));
            });
            return new ResponseEntity(products, HttpStatus.OK);
        }
        catch(IllegalStateException ise){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Provides Product Info", description = "Returns JSON formatted Product Info", tags = { "product" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully returned Product Info",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))) ,
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
    @ResponseBody
    public ResponseEntity<ProductDTO> getProductById(@PathVariable long id) {

        try{
            System.out.println("getProductById --> "+id);
            if(null!=this.productService.findProductById(id))
            {
                return new ResponseEntity(this.productMapper.mapProductToDto(this.productService.findProductById(id)), HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }
        catch(IllegalStateException ise){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @Operation(summary = "Create new Product with Product ID and rest of details.", description = "Taken in the Product ID , and price details. Returns a status of the action with a message.", tags = { "product" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated Product and persisted in Database",
                    content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
            @ApiResponse(responseCode = "406", description = "Duplicate Product Found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
    @ResponseBody
    public ResponseEntity<BaseResponseDTO> createProduct(@Valid @RequestBody ProductDTO productDto) {

        BaseResponseDTO response = new BaseResponseDTO();

        try{

            this.productService.findProductById(productDto.getId());
            response.setStatus(false);
            response.setMessage("Duplicate found.");
            return new ResponseEntity(response, HttpStatus.NOT_ACCEPTABLE);
        }
        catch(IllegalStateException ise){

            this.productService.createProduct(this.productMapper.mapDtoToProduct(productDto));
            response.setMessage("Product Created Successfully !!!");
            response.setStatus(true);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch(Exception e){
            response.setMessage(e.getMessage());
            response.setStatus(false);
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update existing Product i.e. Product ID and rest of details to update", description = "Takes in the Product ID , and price details. Returns a status of the action with a message.", tags = { "product" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated Product and persisted in Database",
                    content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
            @ApiResponse(responseCode = "400", description = "Product ID Not Found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
    @PutMapping(produces = "application/json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<BaseResponseDTO> updateProduct(@Valid @RequestBody ProductDTO productDto) {

        BaseResponseDTO response = new BaseResponseDTO();

        try{
            Product productFromDB = this.productService.findProductById(productDto.getId());
            if(null!=productFromDB && productFromDB.getProduct_id()>0)
            {
                this.productService.updateProduct(this.productMapper.mapDtoToProduct(productDto), productFromDB);
                response.setMessage("Product Updated Successfully !!!");
                response.setStatus(true);
                return new ResponseEntity(response, HttpStatus.OK);
            }
            else
            {
                response.setStatus(false);
                response.setMessage("Product ID NOT found.");
                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            }
        }
        catch(IllegalStateException ise){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){

            response.setMessage(response.getMessage());
            response.setStatus(false);
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete existing Product i.e. Product ID", description = "Takes in the Product ID. Returns a status of the action with a message.", tags = { "product" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted Product from Database",
                    content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
            @ApiResponse(responseCode = "400", description = "Product ID Not Found", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<BaseResponseDTO> deleteProduct(@PathVariable long id) {

        BaseResponseDTO response = new BaseResponseDTO();

        try{
            Product productFromDB = this.productService.findProductById(id);
            if(null!=productFromDB && productFromDB.getProduct_id()>0)
            {
                this.productService.deleteProduct(productFromDB);
                response.setMessage("Product Deleted Successfully !!!");
                response.setStatus(true);
                return new ResponseEntity(response, HttpStatus.OK);
            }
            else
            {
                response.setStatus(false);
                response.setMessage("Product ID NOT found.");
                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            }
        }
        catch(IllegalStateException ise){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){

            response.setMessage(response.getMessage());
            response.setStatus(false);
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
