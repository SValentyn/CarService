package processes;

import service.CarService;

/**
 * Class of visualization of the collected statistical data and recommendations for car service on the console.
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

        System.out.println("\nThe time of the work program: " + carService.getTotal_programTime() + " sec.");
    }
}
