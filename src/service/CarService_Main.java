package service;

import java.io.IOException;

/**
 * Главный класс программного продукта.
 *
 * @author Syniuk Valentyn
 * @version 1.0
 */
public class CarService_Main {

    public static void main(String[] args) throws InterruptedException, IOException {

        CarService carService = CarService.getInstance();
        carService.createCarService();

    }
}

