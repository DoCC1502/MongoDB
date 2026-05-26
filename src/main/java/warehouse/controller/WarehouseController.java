package warehouse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.model.ProductData;
import warehouse.repository.WarehouseRepository;

import java.util.List;

@RestController
@RequestMapping("") // Direkt auf Root mappen, wie in der Aufgabenstellung gefordert
public class WarehouseController {

    @Autowired
    private WarehouseRepository repository;

    // POST /warehouse: fügt einen neuen Lagerstandort hinzu (als ersten Eintrag)
    @PostMapping("/warehouse")
    public ResponseEntity<ProductData> createWarehouse(@RequestBody ProductData data) {
        return ResponseEntity.ok(repository.save(data));
    }

    // GET /warehouse: abrufen aller Lagerstandorte und deren Lagerbestand
    @GetMapping("/warehouse")
    public ResponseEntity<List<ProductData>> getAllWarehouses() {
        return ResponseEntity.ok(repository.findAll());
    }

    // GET /warehouse/{id}: abrufen eines Lagerstandortes id und dessen Lagerbestand
    @GetMapping("/warehouse/{id}")
    public ResponseEntity<List<ProductData>> getWarehouseById(@PathVariable String id) {
        List<ProductData> list = repository.findByWarehouseID(id);
        if (list.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(list);
    }

    // DELETE /warehouse/{id}: löschen eines Lagerstandortes id (alle zugehörigen Produkte)
    @DeleteMapping("/warehouse/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String id) {
        List<ProductData> list = repository.findByWarehouseID(id);
        if (list.isEmpty()) return ResponseEntity.notFound().build();
        repository.deleteAll(list);
        return ResponseEntity.ok().build();
    }

    // POST /product: fügt ein neues Produkt und dessen Lagerbestand zu einem Lagerstandort hinzu
    @PostMapping("/product")
    public ResponseEntity<ProductData> addProduct(@RequestBody ProductData data) {
        return ResponseEntity.ok(repository.save(data));
    }

    // GET /product: abrufen aller Produkte/Lagerbestand und deren Lagerstandort
    @GetMapping("/product")
    public ResponseEntity<List<ProductData>> getAllProducts() {
        return ResponseEntity.ok(repository.findAll());
    }

    // GET /product/{id}: abrufen eines Produktes id und dessen Lagerstandorte
    // Da findByProductID laut Vorgabe nur ein einzelnes Objekt liefert:
    @GetMapping("/product/{id}")
    public ResponseEntity<ProductData> getProductById(@PathVariable String id) {
        ProductData data = repository.findByProductID(id);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    // DELETE /product/{id}: löschen eines Produktes id auf einem Lagerstandort
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        ProductData data = repository.findByProductID(id);
        if (data == null) return ResponseEntity.notFound().build();
        repository.delete(data);
        return ResponseEntity.ok().build();
    }
}