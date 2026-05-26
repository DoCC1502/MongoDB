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

        repository.deleteAll();
        System.out.println(">> Alte Demodaten erfolgreich gelöscht. Generiere Vertiefungsdaten...");

        String[] categories = {"Getraenk", "Waschmittel", "Elektronik", "Lebensmittel", "Kleidung", "Werkzeug"};
        Random random = new Random();

        // 5 Warenhäuser (WH-001 bis WH-005)
        for (int w = 1; w <= 5; w++) {
            String whID = "WH-00" + w;

            // 60 Produkte pro Lager = 300 Datensätze insgesamt
            for (int p = 1; p <= 60; p++) {
                int globalNum = ((w - 1) * 60) + p;

                String prodID;
                String prodName;

                if (p <= 5) {
                    prodID = "PROD-GLOBAL-" + p;
                    prodName = "Globales Produkt " + p;
                } else {
                    prodID = "PROD-UNIQUE-" + globalNum;
                    prodName = "Spezifisches Produkt " + globalNum;
                }

                String category = categories[random.nextInt(categories.length)];
                double quantity = 5.0 + (495.0 * random.nextDouble());
                quantity = Math.ceil(quantity);

                ProductData entry = new ProductData(whID, prodID, prodName, category, quantity);
                repository.save(entry);
            }
        }
        System.out.println(">> Vertiefung erfüllt: 300 flache ProductData-Dokumente erfolgreich angelegt!");
    }
}