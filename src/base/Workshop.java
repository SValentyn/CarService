package base;

import enums.EmploymentRate;
import enums.TypeReq;
import processes.Statistics;
import service.CarService;

import java.util.*;

/**
 * Класс, который является образцом для проектирования и функционирования цеха.
 * Имеет моногенную коллекцию в виде очереди из объектов класса Request.
 */
public class Workshop extends Thread {

    private CarService carService = CarService.getInstance();
    private Queue<Request> queue = new LinkedList<>();
    private Random random = new Random();

    /**
     * {@value #WEEK} 4,560 min * 10
     */
    private static final int WEEK = 45600;

    private TypeReq typeReq;
    private int number_of_mechanics;
    private int repair_cost;   // стоимость ремонта
    private int maxRepairTime; // максимальное время выполнения
    private int repair_time;   // время выполнения
    private int difference_V;  // отличие от repair_time

    private int allRequests;
    private int completedRequests;
    private int lostRequests;
    private long worktime;   // время полезной нагрузки
    private long downtime;   // время простоя
    private int total;   // доход от цеха
    private int salary;  // заработная плата
    private int profit;  // чистая прибыль

    /**
     * Constructor
     *
     * @param typeReq       тип цеха
     * @param repair_cost   стоимость ремонта
     * @param maxRepairTime максимальное время ремонта
     * @param difference_V  значение на которое отличается время ремонта от случайной величины
     */
    public Workshop(TypeReq typeReq, int repair_cost, int maxRepairTime, int difference_V) {
        this.typeReq = typeReq;
        this.number_of_mechanics = random.nextInt(6) + 2;
        this.repair_cost = repair_cost;
        this.maxRepairTime = maxRepairTime;
        this.repair_time = maxRepairTime / number_of_mechanics;
        this.difference_V = difference_V;
        Thread thread = new Thread(this);
        thread.start();
    }

    public TypeReq getTypeReq() {
        return typeReq;
    }

    public synchronized void addRequest(Request request) {
        queue.add(request);
        allRequests++;
        notify();
    }

    private void removeRequest() {
        queue.poll();
    }

    private boolean checkIsEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void run() {
        long initialTime = System.currentTimeMillis();
        while (carService.getSignal()) {
            try {
                if ((System.currentTimeMillis() - initialTime) < (WEEK * 2)) { // работа цеха (неделя) + генерирование заявок
                    synchronized (this) {
                        if (checkIsEmpty()) {
                            wait();   // ждём addRequest()
                        }
                        processing(); // обработка заявки
                    }
                } else {
                    this.join();  // пропускаем оставшиеся заявки без обработки
                }
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e);
            }
        }
        post_processing();
    }

    private void processing() throws InterruptedException {
        int tempV = (repair_time + generateDifferenceRepairTime()) * 10; // время обслуживания
        sleep(tempV);

        worktime += tempV;    // подсчитываем время работы
        total += repair_cost; // увеличиваем прибыль
        completedRequests++;  // указываем что заявка обработана
        removeRequest();      // удаляем выполненную заявку
    }

    /**
     * Метод генерирования случайного значения, на которое будет отличается фиксированное время выполнения одной заявки
     *
     * @return значение на которое отличается время выполения
     * @see #processing()
     */
    private int generateDifferenceRepairTime() {
        if (random.nextInt(10) == 0) {  // chance = 10%
            if (random.nextInt(2) == 0) { // inc or dec
                return random.nextInt(difference_V - 60) + 61;
            } else {
                return -(random.nextInt(repair_time - 1) + 1);
            }
        }
        return 0;
    }

    private void post_processing() {
        getLostRequests();   // получение необработанных заявок
        if (worktime >= WEEK) { // если цех был занят всё время
            worktime = WEEK;
        }
        downtime = WEEK - worktime;
        salary = payroll(total, number_of_mechanics);
        profit = total - salary * number_of_mechanics;
        carService.setTotal_number_of_mechanics(number_of_mechanics);
        carService.setTotal_allRequests(allRequests);
        carService.setTotal_completedRequests(completedRequests);
        carService.setTotal_lostRequests(lostRequests);
        carService.setTotal_profit(profit);
        carService.setTotal_loseProfit(lostRequests * repair_cost);
    }

    private void getLostRequests() {
        int allRequests = this.allRequests;
        while (((allRequests - completedRequests) * repair_time) > (WEEK / 10)) {
            allRequests--;
            lostRequests++;
        }
    }

    public int getNumber_of_mechanics() {
        return number_of_mechanics;
    }

    /**
     * Метод для подсчёта заработной платы механиков
     *
     * @param total               доход цеха
     * @param number_of_mechanics кол-во механиков
     * @return заработная плата
     * @see #post_processing()
     */
    private int payroll(int total, int number_of_mechanics) {
        if ((((total / 100) * 35) / number_of_mechanics) > 7000) {
            return ((total / 100) * 35) / number_of_mechanics;
        } else {
            return 7000;
        }
    }

    /**
     * Метод для строкового представления средней длины очереди, "обрезанное" до сотых
     *
     * @return средняя длина очереди
     * @see #expressAllStatistics_Workshop()
     */
    private Formatter expressAverageLengthQueue() {
        Formatter formatter = new Formatter(Locale.ENGLISH);
        formatter.format("%.2f", averageLengthQueue());
        return formatter;
    }

    private double averageLengthQueue() {
        return (double) allRequests / completedRequests;
    }

    private int averageRepairTime() {
        try {
            return (int) worktime / completedRequests;
        } catch (ArithmeticException e) {
            return repair_time * number_of_mechanics;
        }
    }

    /**
     * Метод для нахождения уровня занятости {@link EmploymentRate} рабочих в цеху
     *
     * @return уровень занятости рабочих
     * @see #showProcess_Workshop()
     * @see #expressAllStatistics_Workshop()
     * @see #expressRecommendations_Workshop()
     */
    private EmploymentRate employmentRate() {
        if ((2 * (WEEK - worktime)) <= worktime) {
            return EmploymentRate.HIGH;
        } else if ((WEEK - worktime) <= worktime) {
            return EmploymentRate.MIDDLE;
        } else {
            return EmploymentRate.LOW; // WEEK - worktime >= worktime
        }
    }

    public void showProcess_Workshop() {
        System.out.print("\u23FA Цех - \u00AB" + typeReq + "\u00BB: " + allRequests + " \u27A0 " + completedRequests +
                " \u26AF " + (averageRepairTime() / 10) + " min." + " \u27A0 " + employmentRate() + "\n");
    }

    public void showStatistics_Workshop() {
        System.out.println(expressAllStatistics_Workshop());
    }

    /**
     * Метод для представления всей статистики для цеха
     *
     * @return строковое представление информации о цехе
     * @see #carService#writeToFile_AllStatistics()
     */
    public String expressAllStatistics_Workshop() {
        return "\n<<< Цех - \u00AB" + typeReq + "\u00BB" +
                "\n<<< Механиков - " + number_of_mechanics +
                "\n\t\u23FA Общее число заявок: " + allRequests +
                "\n\t\u23FA Обслуженно заявок : " + completedRequests +
                "\n\t\u23FA Не будет обслуженно \u2248 " + lostRequests +
                "\n\t\u23FA Средняя длина очереди: " + expressAverageLengthQueue() +
                "\n\t\u23FA Фиксированное время обслуживания: " + repair_time + " min." +
                "\n\t\u23FA Среднее время обслуживания: " + (averageRepairTime() / 10) + " min." +
                "\n\t\u23FA Время полезной нагрузки: " + (worktime / 10) + " min." +
                "\n\t\u23FA Время простоя цеха: " + (downtime / 10) + " min." +
                "\n\t\u23FA Занятость рабочих: " + employmentRate() +
                "\n\t\u23FA Доход цеха: " + total + "\u20B4" +
                "\n\t\u23FA Зарплата механика: " + salary + "\u20B4" +
                "\n\t\u23FA Чистая прибыль цеха: " + profit + "\u20B4" +
                "\n\t\u23FA Потерянный доход \u2248 " + (lostRequests * repair_cost) + "\u20B4" +
                "\n" + carService.dividingLine();
    }

    public void showRecommendations_Workshop() {
        System.out.println(expressRecommendations_Workshop());
    }

    /**
     * Метод для представления рекомендаций для цеха, на основе собранных статистических данных
     *
     * @see #showRecommendations_Workshop
     */
    public String expressRecommendations_Workshop() {
        StringBuffer string = new StringBuffer();

        int number_of_mechanics = this.number_of_mechanics;
        int lostRequests = this.lostRequests;

        /* если есть необслуженные заявки */
        if (lostRequests > 0) {
            if (downtime == 0) { // если время простоя равно 0
                string.append("<<< Цех \u00AB").append(typeReq).append("\u00BB - несёт убытки!\n");
            } else {
                string.append("<<< Цех \u00AB").append(typeReq).append("\u00BB - может нести убытки!\n");
            }

            /* подсчёт необходимого кол-ва механиков, что бы необслуженных заявок не было */
            while (lostRequests > 0 & number_of_mechanics <= 7) {
                int repair_time = maxRepairTime / ++number_of_mechanics; // новое расчётное время ремонта
                int completedRequests = (WEEK / 10) / repair_time;       // новое кол-во обслуженных заявок

                /* подсчёт необслуженных заявок */
                int allRequests = this.allRequests;
                lostRequests = 0;
                while (((allRequests - completedRequests) * repair_time) > (WEEK / 10)) {
                    allRequests--;
                    lostRequests++;
                }
            }
            string.append("\t\u23FA Следует увеличить кол-во механиков на: ").append(number_of_mechanics - this.number_of_mechanics).
                    append("\n\t   \u27A5 Кол-во механиков станет: ").append(number_of_mechanics).
                    append("\n\t   \u27A5 Необслуженных заявок: ").append(lostRequests).append("\n\n");
        } else {
            string.append("<<< Цех \u00AB").append(typeReq).append("\u00BB - не несёт убытки.").append("\n");

            /* Узнаём уровень занятости рабочих в цехе */
            switch (employmentRate()) {
                case LOW: {
                    string.append("\t\u23FA Цех - низкоэффективный! Уровень занятости: ").append(EmploymentRate.LOW).append("\n");
                    break;
                }
                case MIDDLE: {
                    string.append("\t\u23FA Цех может быть улучшен! Уровень занятости: ").append(EmploymentRate.MIDDLE).append("\n");
                    break;
                }
                case HIGH: {
                    string.append("\t\u23FA Цех работает эффективно! Уровень занятости: ").append(EmploymentRate.HIGH).append("\n\n");
                    return String.valueOf(string);
                }
            }

            /* подсчёт времени обработки заявки с изменением кол-ва механиков */
            int repair_time = this.repair_time;
            int average_repair_time = (WEEK / 10) / allRequests;  // новое расчётное среднее время обслуживания
            while ((average_repair_time >= repair_time) & (number_of_mechanics >= 2)) {
                repair_time = maxRepairTime / --number_of_mechanics;
            }

            if (number_of_mechanics == this.number_of_mechanics) {
                string.append("\t\u23FA Кол-во механиков нет необходимости изменять." + "\n\t  \u27A5 Время обработки заявки: ").
                        append(repair_time).append(" min.").append("\n\n");
            } else {
                string.append("\t\u23FA Следует уменьшить кол-во механиков на: ").append(this.number_of_mechanics - number_of_mechanics).
                        append("\n\t   \u27A5 Кол-во механиков станет: ").append(number_of_mechanics).
                        append("\n\t\t  \u27A5 Время обработки заявки: ").append(repair_time).append(" min.").
                        append("\n\t\t  \u27A5 Уровень занятости: ").append(EmploymentRate.HIGH).append("\n\n");
            }
        }
        return String.valueOf(string);
    }

}
