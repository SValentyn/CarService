package processes;

import service.CarService;

/**
 * Класс для визуализации на консоле, собранных статистических данных и рекомендаций для автосервиса.
 *
 * @author Valentyn
 * @version 1.0
 * @see CarService#showStatistics()
 * @see CarService#showGeneralStatistic()
 * @see CarService#showRecommendations()
 */
public class Statistics implements Runnable {

    private CarService carService = CarService.getInstance();

    public Statistics() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        System.out.println("\n" + carService.dividingLine());
        System.out.println("                <<< Statistics Workshops >>>");
        System.out.println(carService.dividingLine());
        carService.showStatistics();

        System.out.println("\n" + carService.dividingLine());
        System.out.println("               <<< Statistics Car Service >>>");
        System.out.println(carService.dividingLine());
        carService.showGeneralStatistic();
        System.out.println(carService.dividingLine());

        System.out.println("\n" + carService.dividingLine());
        System.out.println("            <<< Recommendations Car Service >>>");
        System.out.println(carService.dividingLine());
        carService.showRecommendations();
        System.out.println(carService.dividingLine());

        System.out.println("\nВремя работы программы: " + carService.getTotal_programTime() + " сек.");
    }
}
