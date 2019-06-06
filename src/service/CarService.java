package service;

import base.Mechanic;
import base.Request;
import base.Workshop;
import enums.TypeWorkshop;
import processes.GenerateRequests;
import processes.Statistics;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Class interactions with auto service.
 * Contains implementation of thread-safe OOP pattern - Singleton.
 *
 * @author Syniuk Valentyn
 * @version 1.0
 */
public class CarService {

    private boolean signal = true;
    private long total_programTime;
    private int total_number_of_mechanics;
    private int total_allRequests;
    private int total_completedRequests;
    private int total_lostRequests;
    private int total_profit;
    private int total_loseProfit;

    private static CarService carService;

    /**
     * An array of workshop {@link Workshop} of different types {@link TypeWorkshop}.
     */
    private Workshop[] station = new Workshop[4];

    private CarService() {
        total_programTime = System.currentTimeMillis();
    }

    public static synchronized CarService getInstance() {
        if (carService == null) {
            synchronized (CarService.class) {
                if (carService == null) {
                    carService = new CarService();
                }
            }
        }
        return carService;
    }

    /**
     * The method is designed to create a car service from an array of several workshops {@link #station},
     * start threads for execution ({@link Workshop} and {@link Statistics), start recording to files.
     *
     * @throws InterruptedException if threads interrupted
     * @throws IOException          if files write error
     * @see CarService_Main#main(String[])
     */
    void createCarService() throws IOException, InterruptedException {

        station[0] = new Workshop(TypeWorkshop.Vehicle_inspection, 1500, 420, 360);
        station[1] = new Workshop(TypeWorkshop.Tire_fitting, 3000, 560, 720);
        station[2] = new Workshop(TypeWorkshop.Body_repair, 4500, 770, 1080);
        station[3] = new Workshop(TypeWorkshop.Engine_repair, 7500, 910, 1440);

        createFiles();              // creating files with the insertion of the file header
        writeToFile_AllMechanics(); // creating randomly generated requests
        generateRandomRequests();
        new Statistics();           // run thread class Statistics
        writeToFile_AllStatistics();
    }


    private void createFiles() throws IOException {
        try (FileWriter writer_AllClients = new FileWriter("src/files/AllClients.txt")) {
            writer_AllClients.write(dividingLine() + "\n");
            writer_AllClients.write("         <<< List of clients in the car service >>>\n");
            writer_AllClients.write(dividingLine() + "\n\n");
        }

        try (FileWriter writer_AllMechanics = new FileWriter("src/files/AllMechanics.txt")) {
            writer_AllMechanics.write(dividingLine() + "\n");
            writer_AllMechanics.write("        <<< List of mechanics in the car service >>>\n");
            writer_AllMechanics.write(dividingLine() + "\n\n");
        }

        try (FileWriter writer_AllStatistics = new FileWriter("src/files/AllStatistics.txt")) {
            writer_AllStatistics.write(dividingLine() + "\n");
            writer_AllStatistics.write("           <<< All statistics the car service >>>\n");
            writer_AllStatistics.write(dividingLine() + "\n");
        }
    }

    /**
     * Method to create randomly generated requests.
     *
     * @throws InterruptedException if thread interrupted
     * @throws IOException          if file write error
     */
    private void generateRandomRequests() throws InterruptedException, IOException {
        GenerateRequests generateRequests = new GenerateRequests();
        generateRequests.randomize();
    }

    /**
     * Method of obtaining a ready-made array of necessary services for a single client,
     * comparing types {@code if (workshop.getType () == request.getType ()) {...}},
     * sending each request to the right workshop and write the client to the file: "AllClients.txt".
     *
     * @param requests      array of required services
     * @throws IOException  if file write error
     */
    public void receivingAndSendingRequests(Request[] requests) throws IOException {
        try (FileWriter writer = new FileWriter("src/files/AllClients.txt", true)) {
            for (Workshop workshop : station) {
                for (Request request : requests) {
                    if (workshop.getType() == request.getType()) {
                        writer.write("\tclient : " + request.getClient().getSurname() + " " + request.getClient().getName() + ", \u00AB" + request.getType() + "\u00BB\n");
                        workshop.addRequest(request);
                    }
                }
            }
        }
        showProcesses();
    }

    public void setSignal(boolean signal) {
        this.signal = signal;
    }

    public boolean getSignal() {
        return signal;
    }

    public long getTotal_programTime() {
        return (System.currentTimeMillis() - total_programTime) / 1000;
    }

    public void setTotal_number_of_mechanics(int number_of_mechanics) {
        total_number_of_mechanics += number_of_mechanics;
    }

    public void setTotal_allRequests(int allRequests) {
        total_allRequests += allRequests;
    }

    public void setTotal_completedRequests(int completedRequests) {
        total_completedRequests += completedRequests;
    }

    public void setTotal_lostRequests(int lostRequests) {
        total_lostRequests += lostRequests;
    }

    public void setTotal_profit(int profit) {
        total_profit += profit;
    }

    public void setTotal_loseProfit(int loseProfit) {
        total_loseProfit += loseProfit;
    }

    private void showProcesses() {
        System.out.println("\ttype of workshop: allR \u27A0 compR \u26AF averageT \u27A0 rate");
        for (Workshop workshop : station) {
            workshop.showProcess_Workshop();
        }
        System.out.println("=============================================================");
    }

    public void showStatistics() {
        for (Workshop workshop : station) {
            workshop.showStatistics_Workshop();
        }
    }

    public void showGeneralStatistic() {
        System.out.println(expressGeneralStatistic());
    }

    /**
     * Method for representing the general statistics of Car Service.
     *
     * @return a string representation of the general statistics of Car Service
     * @see #writeToFile_AllStatistics()
     */
    private String expressGeneralStatistic() {
        return "\n\t\u23FA Mechanics in the Car Service: " + total_number_of_mechanics + "\n" +
                "\t\u23FA Total requests received: " + total_allRequests + "\n" +
                "\t\u23FA Requests processed: " + total_completedRequests + "\n" +
                "\t\u23FA Not processed requests \u2248 " + total_lostRequests + "\n" +
                "\t\u23FA Total Car Service revenue: " + total_profit + "\u20B4" + "\n" +
                "\t\u23FA Lost income \u2248 " + total_loseProfit + "\u20B4";
    }

    public void showRecommendations() {
        for (Workshop workshop : station) {
            workshop.showRecommendations_Workshop();
        }
    }

    /**
     * Method to write to the file: "AllMechanics.txt", all the mechanics, each individual workshop Car Service.
     *
     * @throws IOException if file write error
     */
    private void writeToFile_AllMechanics() throws IOException {
        for (Workshop workshop : station) {
            try (FileWriter writer = new FileWriter("src/files/AllMechanics.txt", true)) {
                writer.write("\u23FA Workshop - \u00AB" + workshop.getType() + "\u00BB:\n");
                for (int i = 0; i < workshop.getNumber_of_mechanics(); i++) {
                    Mechanic mechanic = new Mechanic();
                    writer.write("\tmechanic : " + mechanic.getSurname() + " " + mechanic.getName() + "\n");
                }
            }
        }
    }

    /**
     * The method of writing to the file: "AllStatistics.txt", all statistics for each individual workshop,
     * as well as general statistics and recommendations.
     *
     * @throws IOException if file write error
     */
    private void writeToFile_AllStatistics() throws IOException {
        try (FileWriter writer = new FileWriter("src/files/AllStatistics.txt", true)) {
            for (Workshop workshop : station) {
                writer.write(workshop.expressAllStatistics_Workshop());
            }

            writer.write("\n" + dividingLine() + "\n");
            writer.write("        <<< General statistic in the car service >>>");
            writer.write("\n" + dividingLine() + "\n");
            writer.write(expressGeneralStatistic());
            writer.write("\n\n" + dividingLine() + "\n");
            writer.write(dividingLine() + "\n");
            writer.write("        <<< Recommendations for the car service >>>");
            writer.write("\n" + dividingLine() + "\n\n");

            for (Workshop workshop : station) {
                writer.write(workshop.expressRecommendations_Workshop());
            }

            writer.write(dividingLine());
        }
    }

    public String dividingLine() {
        return "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
    }

}
