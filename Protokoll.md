# MongoDB

**Verfasser:** Dario Cikojevic

**Datum:** 26.05.2026

## 1. Grundlagen (GK)

### 1.1 JSON-Datenstruktur (Flaches Dokumentenmodell)

Da die Architektur über die Klasse `ProductData` flach vorgegeben ist, wird jeder Lagerbestandseintrag als einzelnes Dokument in der Collection `productData` gespeichert.

JSON

```
{
  "_id": "65f1a2b3c4d5e6f7a8b9c001",
  "warehouseID": "WH-001",
  "productID": "PROD-101",
  "productName": "Bio Orangensaft Sonne",
  "productCategory": "Getraenk",
  "productQuantity": 2500.0
}
```

### 1.2 Dokumentation: 5 CRUD Operationen in der Mongo Shell (`mongosh`)

#### 1. Create (Dokument erstellen)

![](C:\Users\Dario%20Cikojevic\AppData\Roaming\marktext\images\2026-05-26-15-58-09-image.png)

#### 2. Read (Dokument auslesen)

![](C:\Users\Dario%20Cikojevic\AppData\Roaming\marktext\images\2026-05-26-15-58-57-image.png)

#### 3. Update (Lagerbestand erhöhen / ändern)

![](C:\Users\Dario%20Cikojevic\AppData\Roaming\marktext\images\2026-05-26-15-59-27-image.png)

#### 4. Update (Kategorie korrigieren)

![](C:\Users\Dario%20Cikojevic\AppData\Roaming\marktext\images\2026-05-26-15-59-58-image.png)

#### 5. Delete (Eintrag löschen)

![](C:\Users\Dario%20Cikojevic\AppData\Roaming\marktext\images\2026-05-26-16-00-21-image.png)

## 2. Erweiterte Grundlagen (EK)

### 2.1 REST Controller (`WarehouseController.java`)

Vollständige Abbildung aller geforderten Schnittstellen auf das vorgegebene Repository.

Java

```
package warehouse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.model.ProductData;
import warehouse.repository.WarehouseRepository;
import java.util.List;

@RestController
@RequestMapping("")
public class WarehouseController {

    @Autowired
    private WarehouseRepository repository;

    @PostMapping("/warehouse")
    public ResponseEntity<ProductData> createWarehouse(@RequestBody ProductData data) {
        return ResponseEntity.ok(repository.save(data));
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<ProductData>> getAllWarehouses() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/warehouse/{id}")
    public ResponseEntity<List<ProductData>> getWarehouseById(@PathVariable String id) {
        List<ProductData> list = repository.findByWarehouseID(id);
        if (list.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/warehouse/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String id) {
        List<ProductData> list = repository.findByWarehouseID(id);
        if (list.isEmpty()) return ResponseEntity.notFound().build();
        repository.deleteAll(list);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/product")
    public ResponseEntity<ProductData> addProduct(@RequestBody ProductData data) {
        return ResponseEntity.ok(repository.save(data));
    }

    @GetMapping("/product")
    public ResponseEntity<List<ProductData>> getAllProducts() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductData> getProductById(@PathVariable String id) {
        ProductData data = repository.findByProductID(id);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        ProductData data = repository.findByProductID(id);
        if (data == null) return ResponseEntity.notFound().build();
        repository.delete(data);
        return ResponseEntity.ok().build();
    }
}
```

## 3. Vertiefung

### 3.1 Automatischer Testdaten-Generator

Generiert beim Systemstart **5 Warenhaeuser, 6 Produktkategorien und 300 Produkte** (60 pro Lager) unter exakter Verwendung des vorgegebenen Modells.

Java

```
package warehouse.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import warehouse.model.ProductData;
import warehouse.repository.WarehouseRepository;
import java.util.Random;

@Component
public class DataGenerator implements CommandLineRunner {

    @Autowired
    private WarehouseRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) return;

        String[] categories = {"Getraenk", "Waschmittel", "Elektronik", "Lebensmittel", "Kleidung", "Werkzeug"};
        Random random = new Random();

        for (int w = 1; w <= 5; w++) {
            String whID = "WH-00" + w;

            for (int p = 1; p <= 60; p++) {
                int globalNum = ((w - 1) * 60) + p;
                String prodID = (p <= 5) ? "PROD-GLOBAL-" + p : "PROD-UNIQUE-" + globalNum;
                String prodName = (p <= 5) ? "Globales Produkt " + p : "Spezifisches Produkt " + globalNum;
                String category = categories[random.nextInt(categories.length)];
                double quantity = 5.0 + (495.0 * random.nextDouble());

                ProductData entry = new ProductData(whID, prodID, prodName, category, quantity);
                repository.save(entry);
            }
        }
    }
}
```

### 3.2 Berichtswesen Abfragen (`mongosh`)

#### Fragestellung 1: Wie ist der Lagerbestand von einem Produkt X über alle Lagerstandorte?

JavaScript

```
db.productData.aggregate([
  { $match: { "productID": "PROD-GLOBAL-1" } },
  { $group: { _id: "$productID", Gesamtbestand: { $sum: "$productQuantity" } } }
])
```

#### Fragestellung 2: Welche Produkte haben einen Lagerbestand von unter 30 Stück über alle Lagerstandorte?

JavaScript

```
db.productData.aggregate([
  { $group: { _id: { id: "$productID", name: "$productName" }, Gesamtbestand: { $sum: "$productQuantity" } } },
  { $match: { "Gesamtbestand": { $lt: 30.0 } } }
])
```

#### Fragestellung 3: Wie hoch ist die gesamte Produktanzahl pro Kategorie je Lager aufgeteilt?

JavaScript

```
db.productData.aggregate([
  { $group: { _id: { Lager: "$warehouseID", Kategorie: "$productCategory" }, BestandsSumme: { $sum: "$productQuantity" } } },
  { $sort: { "_id.Lager": 1, "BestandsSumme": -1 } }
])
```

## 4. Beantwortung der Protokollfragen

### 4.1 Nennen Sie 4 Vorteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS

- **Flexibles, schemaloses Design:** Dokumente können dynamisch neue Felder erhalten, ohne dass aufwändige Tabellen-Migrationen (`ALTER TABLE`) nötig sind.

- **Horizontale Skalierbarkeit (Sharding):** NoSQL-Datenbanken wie MongoDB sind darauf ausgelegt, Daten nativ über viele Serverknoten hinweg aufzuteilen.

- **Hohe Performance durch Denormalisierung:** Daten, die zusammen gelesen werden, können zusammen gespeichert werden. Es entfallen teure `JOIN`-Operationen über mehrere Tabellen.

- **Direktes Objekt-Mapping:** Datenstrukturen (wie JSON/BSON) entsprechen der Objektstruktur moderner Programmiersprachen, was den Impedance Match minimiert.

### 4.2 Nennen Sie 4 Nachteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS

- **Fehlen standardisierter Joins:** Komplexe Verknüpfungen über Collections hinweg sind nur über Aggregationen lösbar und bei sehr großen Datenmengen ineffizienter als in RDBMS.

- **Schwächere Konsistenzgarantien:** Viele NoSQL-Systeme setzen primär auf *Eventual Consistency* statt auf strikte, sofortige ACID-Transaktionen über Tabellengrenzen hinweg.

- **Datenredundanz:** Um Joins zu vermeiden, werden Daten oft dupliziert, was den Speicherbedarf erhöht und Aktualisierungen (Updates) fehleranfälliger macht.

- **Keine standardisierte Abfragesprache:** Es gibt kein universelles SQL; Entwickler müssen für jede NoSQL-Datenbank eine eigene proprietäre API/Syntax lernen.

### 4.3 Welche Schwierigkeiten ergeben sich bei der Zusammenführung der Daten?

- **Dateninkonsistenz (Race Conditions):** Wenn dezentrale Standorte asynchron denselben Lagerbestand aktualisieren, kann es ohne zentrales Locking zu Überschreibungen kommen.

- **Strukturelle Varianz:** Da NoSQL kein Schema erzwingt, können verschiedene Lager-Clients unterschiedliche Datentypen oder Feldbezeichnungen für dieselbe Information senden.

- **Idempotenz-Probleme:** Bei Netzwerkproblemen senden Clients Requests doppelt. Die Middleware muss erkennen, ob ein Produkt neu hinzugefügt oder ein bestehendes fälschlicherweise addiert wird.

### 4.4 Welche Arten von NoSQL Datenbanken gibt es? Nennen Sie einen Vertreter für jede Art.

1. **Dokumentenorientiert (Document Store):** MongoDB

2. **Schlüssel-Wert (Key-Value Store):** Redis

3. **Spaltenorientiert (Wide-Column Store):** Apache Cassandra

4. **Graphdatenbank (Graph Database):** Neo4j

### 4.5 Beschreiben Sie die Abkürzungen CA, CP und AP in Bezug auf das CAP Theorem

Das CAP-Theorem besagt, dass ein verteiltes System nur zwei der folgenden drei Eigenschaften gleichzeitig garantieren kann:

- **CA (Consistency + Availability):** Das System liefert konsistente Daten und ist hochverfügbar, toleriert jedoch keine Netzwerk-Aufspaltungen (Partitions). In echten verteilten Systemen praktisch unmöglich, da Netzwerkausfälle real sind.

- **CP (Consistency + Partition Tolerance):** Bei einer Netzwerkstörung trennt sich das System von betroffenen Knoten, um Datenkonsistenz zu wahren. Die *Verfügbarkeit* bricht für diese Teile ab. (MongoDB agiert im Kern so).

- **AP (Availability + Partition Tolerance):** Das System bleibt bei Netzwerkfehlern voll verfügbar. Knoten antworten mit lokalen (eventuell veralteten) Daten. Die Konsistenz leidet temporär (*Eventual Consistency*).

### 4.6 Mit welchem Befehl koennen Sie den Lagerstand eines Produktes aller Lagerstandorte anzeigen.

JavaScript

```
db.productData.find({ "productID": "PROD-GLOBAL-1" }, { warehouseID: 1, productQuantity: 1, _id: 0 })
```

### 4.7 Mit welchem Befehl koennen Sie den Lagerstand eines Produktes eines bestimmten Lagerstandortes anzeigen.

JavaScript

```
db.productData.find({ "warehouseID": "WH-001", "productID": "PROD-GLOBAL-1" }, { productQuantity: 1, _id: 0 })
```
