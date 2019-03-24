package service;

import base.Mechanic;
import base.Request;
import base.Workshop;
import enums.TypeReq;
import processes.GenerateRequests;
import processes.Statistics;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Класс для работы с автосервисом.
 * Содержит реализацию потокобезопасного паттерна ООП – Singleton.
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
     * Массив цехов {@link Workshop} разных типов {@link TypeReq} из которых состоит автосервис.
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
     * Метод для создания автосервиса из массива цехов {@link #station},
     * запуска потоков на выполнение ({@link Workshop} and {@link Statistics}), записи в файлы
     *
     * @throws InterruptedException if threads interrupted
     * @throws IOException          if files write error
     * @see CarService_Main#main(String[])
     */
    void createCarService() throws IOException, InterruptedException {

        station[0] = new Workshop(TypeReq.Техосмотр, 1500, 420, 360);
        station[1] = new Workshop(TypeReq.Шиномонтаж, 3000, 560, 720);
        station[2] = new Workshop(TypeReq.Кузовной_ремонт, 4500, 770, 1080);
        station[3] = new Workshop(TypeReq.Ремонт_двигателя, 7500, 910, 1440);

        createFiles();
        writeToFile_AllMechanics();
        generateRandomRequests();
        new Statistics();   // run thread class Statistics
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
     * Метод для создания случайно сгенерированных заявок
     *
     * @throws InterruptedException if thread interrupted
     * @throws IOException          if file write error
     */
    private void generateRandomRequests() throws InterruptedException, IOException {
        GenerateRequests generateRequests = new GenerateRequests();
        generateRequests.randomize();
    }

    /**
     * Метод получения готового массива необходимых услуг для одного клиента {@link GenerateRequests()#createAndSendRequests()},
     * сравнения типов {@code if (workshop.getTypeReq() == request.getTypeReq()) {...}},
     * отправки каждой заявки в нужный цех {@link Workshop#addRequest(Request)}
     * и запись клиента, который относится к заявке, в файл: "AllClients.txt" {@link FileWriter#write(String)}
     *
     * @param requests массив необходимых услуг
     * @throws IOException if file write error
     */
    public void receivingAndSendingRequests(Request[] requests) throws IOException {
        try (FileWriter writer = new FileWriter("src/files/AllClients.txt", true)) {
            for (Workshop workshop : station) {
                for (Request request : requests) {
                    if (workshop.getTypeReq() == request.getTypeReq()) {
                        writer.write("\tclient : " + request.getClient().getSurname() + " " + request.getClient().getName() + ", \u00AB" + request.getTypeReq() + "\u00BB\n");
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
        System.out.println("         тип цеха: allR \u27A0 compR \u26AF averageT \u27A0 rate");
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
     * Метод для представления общей статистики автосервиса
     *
     * @return строковое представление общей статистики автосервиса
     * @see #writeToFile_AllStatistics()
     */
    private String expressGeneralStatistic() {
        return "\n\t\u23FA Механиков в автосервисе: " + total_number_of_mechanics + "\n" +
                "\t\u23FA Всего заявок получено: " + total_allRequests + "\n" +
                "\t\u23FA Обработанно заявок: " + total_completedRequests + "\n" +
                "\t\u23FA Не обработанно заявок \u2248 " + total_lostRequests + "\n" +
                "\t\u23FA Доход автосервиса: " + total_profit + "\u20B4" + "\n" +
                "\t\u23FA Потерянный доход \u2248 " + total_loseProfit + "\u20B4";
    }

    public void showRecommendations() {
        for (Workshop workshop : station) {
            workshop.showRecommendations_Workshop();
        }
    }

    /**
     * Метод для записи в файл: "AllMechanics.txt", всех механиков, каждого отдельного цеха {@link Workshop} автосервиса
     *
     * @throws IOException if file write error
     */
    private void writeToFile_AllMechanics() throws IOException {
        for (Workshop workshop : station) {
            try (FileWriter writer = new FileWriter("src/files/AllMechanics.txt", true)) {
                writer.write("\u23FA Цех - \u00AB" + workshop.getTypeReq() + "\u00BB:\n");
                for (int i = 0; i < workshop.getNumber_of_mechanics(); i++) {
                    Mechanic mechanic = new Mechanic();
                    writer.write("\tmechanic : " + mechanic.getSurname() + " " + mechanic.getName() + "\n");
                }
            }
        }
    }

    /**
     * Метод записи в файл: "AllStatistics.txt", всей статистики для каждого отдельного цеха {@link Workshop},
     * а также общей статистики {@link #expressGeneralStatistic()} и рекомендаций {@link Workshop#expressRecommendations_Workshop()}
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
