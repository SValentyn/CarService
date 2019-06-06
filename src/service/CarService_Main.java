package service;

import java.io.IOException;

/**
 * The main class of the software product.
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

