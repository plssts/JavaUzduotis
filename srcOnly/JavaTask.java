/*
 * Self-contained application that handles Product entries and fetches results.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Paulius Staisiunas.
 */
public class JavaTask {
    private static List<Product> rows = new ArrayList<>();
    
    /**
    * Handles a very simple application flow.
    */
    public static void main(String[] args) {
        // nothing specified about path selection, so it's manually coded here
        String pathToCSV = "sample.csv";
        
        rows = readFile(pathToCSV);
        if (rows.isEmpty()){
            System.out.println("Nera jokiu produktu.");
            return;
        }
        
        menu();
    }
    
    /**
    * Reads CSV contents and maps them to Product objects.
    *
    * @param pathToCSV Relative location to the CSV file.
    * @return a list of Products where repeating entries have summed quantities instead
    */
    public static List<Product> readFile(String pathToCSV){
        String separator = ",";
        BufferedReader bufread = null;
        List<Product> allProducts = new ArrayList<>();

        try {
            bufread = new BufferedReader(new FileReader(pathToCSV));
            bufread.readLine(); // dump the header

            String line;

            while ((line = bufread.readLine()) != null){
                   String[] product = line.split(separator);
                   
                   if (product.length != 4){ // Product expects exactly 4 strings in array
                       System.out.println("Praleidziama eilute, neatitinkanti numatyto formato:\n" + line);
                       continue;
                   }

                   Product prodObj = new Product(product);
                   allProducts.add(prodObj);
            }
            
        } catch (FileNotFoundException ex) {
            System.out.println("Nerastas nurodytas failas: " + pathToCSV + ".");
        } catch (IOException ex){
            System.out.println("Klaida skaitant duomenis is: " + pathToCSV + ".");
        } finally {
            try {
                bufread.close();
            } catch (IOException ex) {
                Logger.getLogger(JavaTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return groupAll(allProducts);
    }
    
    /**
    * Performs grouping on all entries by name, code and expiration date.
    * Modifies the supplied list.
    *
    * @param allProducts a list of Products (CSV rows)
    * @return a list of Products where repeating entries have summed quantities instead
    */
    private static List<Product> groupAll(List<Product> allProducts){
        // clearing is performed on fields to preserve list indexing
        String[] empty = new String[]{"", "", "0", ""};
        List<Product> result = new ArrayList<>();
        
        for (ListIterator<Product> it = allProducts.listIterator(); it.hasNext(); ){
            Product p = it.next();
            
            if (p.getGrouping().equals("")){
                continue;
            }
            
            it.set(new Product(empty)); // clear this Product
            
            int totalQty = p.getQty();
            for (ListIterator<Product> midit = allProducts.listIterator(); midit.hasNext(); ){
                Product temp = midit.next();
                if (p.getGrouping().equals(temp.getGrouping())){
                    totalQty += temp.getQty();
                    midit.set(new Product(empty)); // clear this Product
                }
            }
            
            // include the final aggregated entry
            result.add(new Product(new String[]{p.getName(),p.getCode(),String.valueOf(totalQty),p.getExpr()}));
        }
        
        return result;
    }
    
    /**
    * Handles choices and performs calculations.
    * Formally calculations should be located somewhere else,
    * but they're rather simple one-liners.
    */
    public static void menu(){
        Scanner scan = new Scanner(System.in);
        
        while(true){
            System.out.println("\n============ Veiksmai su sistema ============");
            System.out.println("[1] Perziureti trukstamu prekiu kiekius");
            System.out.println("[2] Patikrinti prekes, kuriu galiojimo laikas\n"
                    + "    pasibaiges arba pasibaigs greitu metu");
            System.out.println("[3] Baigti darba");
            System.out.println("=============================================");
            
            int choice = 0;
            
            try {
                choice = scan.nextInt();
                scan.nextLine(); // consume "\n"

                switch (choice) {
                    case 1:
                        System.out.println("Koks bus tikrinamas prekiu likutis?");

                        try {
                            int qty = scan.nextInt();
                            scan.nextLine(); // consume "\n"

                            if (qty < 0){
                                throw new IOException();
                            }

                            List<Product> processedRows = rows.stream()
                                    .filter(elem -> elem.getQty() < qty)
                                    .sorted((elem1, elem2) -> elem1.getName().compareTo(elem2.getName()))
                                    .collect(Collectors.toList());

                            for (Product p : processedRows){
                                System.out.printf("%-16s %20s %6d %10s\n", p.getName(), p.getCode(), p.getQty(), p.getExpr());
                            }

                        } catch (InputMismatchException ex){
                            System.out.println("Prekiu likutis turetu buti naturalusis skaicius.");
                            scan.nextLine(); // consume "\n"
                        } catch (IOException ex){
                            System.out.println("Prekiu likutis turetu buti naturalusis skaicius.");
                        }
                        break;

                    case 2:
                        System.out.println("Kokia galiojimo data bus tikrinama?");
                        String expr = scan.nextLine();

                        LocalDate date;

                        try {
                            date = LocalDate.parse(expr, DateTimeFormatter.ISO_LOCAL_DATE);
                        } catch (DateTimeParseException ex){
                            System.out.println("Datos formatas turetu buti [yyyy-mm-dd]");
                            break;
                        }

                        List<Product> processedRows = rows.stream()
                                .filter(elem -> LocalDate.parse(elem.getExpr(), DateTimeFormatter.ISO_LOCAL_DATE).isBefore(date))
                                .sorted((elem1, elem2) -> elem1.getName().compareTo(elem2.getName()))
                                .collect(Collectors.toList());

                        for (Product p : processedRows){
                            System.out.printf("%-16s %20s %6d %10s\n", p.getName(), p.getCode(), p.getQty(), p.getExpr());
                        }
                        break;

                    case 3:
                        scan = null; // let GC deal with it instead of interfering with System.in
                        return;

                    default:
                        System.out.println("Pasirinkimai turetu tilpti i rezi [1;3]");
                }
            } catch (InputMismatchException ex){
                System.out.println("Pasirinkimai turetu buti naturalusis skaicius intervale [1;3]");
                scan.nextLine(); // consume "\n"
            }
        }
    }
}
