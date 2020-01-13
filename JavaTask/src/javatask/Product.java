/*
 * Represents one row (or several aggregated rows into one) of a CSV file.
 */

/**
 * @author Paulius Staisiunas.
 */
public class Product {
    private String name;
    private String code;
    private Integer quantity;
    private String expiration;
    
    public Product(String[] fields){
        // array bounds are checked in JavaTask
        name = fields[0];
        code = fields[1];
        quantity = Integer.parseInt(fields[2]);
        expiration = fields[3];
    }
    
    /**
    * Returns necessary fields for grouping.
    *
    * @return a splat of needed fields together.
    */
    public String getGrouping(){
        return name + code + expiration;
    }
    
    // Simple getters below
    
    public String getName(){
        return name;
    }
    
    public String getCode(){
        return code;
    }
    
    public String getExpr(){
        return expiration;
    }
    
    public int getQty(){
        return quantity;
    }
}
