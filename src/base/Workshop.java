package base;

import enums.EmploymentRate;
import enums.TypeWorkshop;
import service.CarService;

import java.util.*;

/**
 * Class, which is a model for the design and operation of the workshop.
 * It has a monogenic collection in the form of a requests of objects of the class Request.
 *
 * @author Syniuk Valentyn
 * @version 1.0
 */
public class Workshop extends Thread {

    /**
     * Presentation of one work week in seconds: {@value #WEEK = 4,560 min * 10}
     */
    private static final int WEEK = 45600;

    private CarService carService = CarService.getInstance();
    private Queue<Request> requests = new LinkedList<>();
    private Random random = new Random();

    private TypeWorkshop type;
    private int number_of_mechanics;
    private int repair_cost;
    private int maxRepairTime;
    private int repair_time;
    private int difference_V;  // time difference from repair_time

    private int allRequests;
    private int completedRequests;
    private int lostRequests;
    private long worktime;
    private long downtime;
    private int total;  // income from one workshop
    private int salary;
    private int profit;

    public Workshop(TypeWorkshop type, int repair_cost, int maxRepairTime, int difference_V) {
        this.type = type;
        this.number_of_mechanics = random.nextInt(6) + 2;
        this.repair_cost = repair_cost;
        this.maxRepairTime = maxRepairTime;
        this.repair_time = maxRepairTime / number_of_mechanics;
        this.difference_V = difference_V;
        new Thread(this).start();
    }

    public TypeWorkshop getType() {
        return type;
    }

    public synchronized void addRequest(Request request) {
        requests.add(request);
        allRequests++;
        notify();
    }

    private void removeRequest() {
        requests.poll();
    }

    private boolean checkIsEmpty() {
        return requests.isEmpty();
    }

    @Override
    public void run() {
        long initialTime = System.currentTimeMillis();
        while (carService.getSignal()) {
            try {
                if ((System.currentTimeMillis() - initialTime) < (WEEK * 2)) { // workshop work (week) + generating requests
                    synchronized (this) {
                        if (checkIsEmpty()) {
                            wait();   // waiting for addRequest() method
                        }
                        processing();
                    }
                } else {
                    this.join();  // skip remaining requests without processing
                }
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e);
            }
        }
        post_processing();
    }

    private void processing() throws InterruptedException {
        int tempV = (repair_time + generateDifferenceRepairTime()) * 10; // service time
        sleep(tempV);

        worktime += tempV;    // calculate the processing time
        total += repair_cost; // value of profit increases
        completedRequests++;  // indicate that the application has been processed
        removeRequest();      // delete completed request
    }

    /**
     * The method of generating a random value by which the fixed execution time of a single request will differ.
     *
     * @return the value for which the execution time differs
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
        getLostRequests();                     // receipt of unprocessed requests
        if (worktime >= WEEK) worktime = WEEK; // if the workshop was busy all the time

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
     * Method for calculating the wages of mechanics.
     */
    private int payroll(int total, int number_of_mechanics) {
        if ((((total / 100) * 35) / number_of_mechanics) > 7000) {
            return ((total / 100) * 35) / number_of_mechanics;
        } else {
            return 7000;
        }
    }

    /**
     * Method for string representation of the average queue length ("trimmed" to the hundredth).
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
     * Method for finding the level of employment {@link EmploymentRate} of mechanics in the workshop.
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
        System.out.print("\u23FA Workshop - \u00AB" + type + "\u00BB: " + allRequests + " \u27A0 " + completedRequests +
                " \u26AF " + (averageRepairTime() / 10) + " min." + " \u27A0 " + employmentRate() + "\n");
    }

    public void showStatistics_Workshop() {
        System.out.println(expressAllStatistics_Workshop());
    }

    /**
     * Method for presenting all statistics for the workshop.
     *
     * @see #carService#writeToFile_AllStatistics()
     */
    public String expressAllStatistics_Workshop() {
        return "\n<<< Workshop - \u00AB" + type + "\u00BB" +
                "\n<<< Mechanics - " + number_of_mechanics +
                "\n\t\u23FA Total number of requests: " + allRequests +
                "\n\t\u23FA Serviced requests: " + completedRequests +
                "\n\t\u23FA Will not be served \u2248 " + lostRequests +
                "\n\t\u23FA Average queue length: " + expressAverageLengthQueue() +
                "\n\t\u23FA Fixed service time: " + repair_time + " min." +
                "\n\t\u23FA Average service time: " + (averageRepairTime() / 10) + " min." +
                "\n\t\u23FA Worktime: " + (worktime / 10) + " min." +
                "\n\t\u23FA Downtime: " + (downtime / 10) + " min." +
                "\n\t\u23FA Employment of workers: " + employmentRate() +
                "\n\t\u23FA base.Workshop revenue: " + total + "\u20B4" +
                "\n\t\u23FA Salary mechanic: " + salary + "\u20B4" +
                "\n\t\u23FA Net profit of the workshop: " + profit + "\u20B4" +
                "\n\t\u23FA Lost income \u2248 " + (lostRequests * repair_cost) + "\u20B4" +
                "\n" + carService.dividingLine();
    }

    public void showRecommendations_Workshop() {
        System.out.println(expressRecommendations_Workshop());
    }

    /**
     * Method for presenting recommendations for the workshop, based on the collected statistical data.
     */
    public String expressRecommendations_Workshop() {
        StringBuffer resultStr = new StringBuffer();

        int number_of_mechanics = this.number_of_mechanics;
        int lostRequests = this.lostRequests;

        /* If there are unserved requests */
        if (lostRequests > 0) {
            if (downtime == 0) {
                resultStr.append("<<< Workshop \u00AB").append(type).append("\u00BB - incurs losses!\n");
            } else {
                resultStr.append("<<< Workshop \u00AB").append(type).append("\u00BB - may incurs losses!\n");
            }

            /* Calculation of the necessary number of mechanics that would not be unserved requests */
            while (lostRequests > 0 & number_of_mechanics <= 7) {
                int repair_time = maxRepairTime / ++number_of_mechanics; // new estimated repair time
                int completedRequests = (WEEK / 10) / repair_time;       // new number of requests served

                /* Counting unserved requests */
                int allRequests = this.allRequests;
                lostRequests = 0;
                while (((allRequests - completedRequests) * repair_time) > (WEEK / 10)) {
                    allRequests--;
                    lostRequests++;
                }
            }
            resultStr.append("\t\u23FA It is necessary to increase the number of mechanics on: ").append(number_of_mechanics - this.number_of_mechanics).
                    append("\n\t   \u27A5 Number of mechanics will be: ").append(number_of_mechanics).
                    append("\n\t   \u27A5 Not served requests will be: ").append(lostRequests).append("\n\n");
        } else {
            resultStr.append("<<< Workshop \u00AB").append(type).append("\u00BB - does not incur losses.").append("\n");

            /* Finding out the level of employment of mechanics in the workshop */
            switch (employmentRate()) {
                case LOW: {
                    resultStr.append("\t\u23FA Workshop - low efficiency! Employment rate: ").append(EmploymentRate.LOW).append("\n");
                    break;
                }
                case MIDDLE: {
                    resultStr.append("\t\u23FA Workshop can be improved! Employment rate: ").append(EmploymentRate.MIDDLE).append("\n");
                    break;
                }
                case HIGH: {
                    resultStr.append("\t\u23FA Workshop works effectively! Employment rate: ").append(EmploymentRate.HIGH).append("\n\n");
                    return String.valueOf(resultStr);
                }
            }

            /* Calculation of the processing time of the request with the change in the number of mechanics */
            int repair_time = this.repair_time;
            int average_repair_time = (WEEK / 10) / allRequests;  // new estimated average service time
            while ((average_repair_time >= repair_time) & (number_of_mechanics >= 2)) {
                repair_time = maxRepairTime / --number_of_mechanics;
            }

            if (number_of_mechanics == this.number_of_mechanics) {
                resultStr.append("\t\u23FA The number of mechanics is not necessary to change." +
                        "\n\t  \u27A5 Request processing time: ").append(repair_time).append(" min.").append("\n\n");
            } else {
                resultStr.append("\t\u23FA It is necessary to reduce the number of mechanics on: ").append(this.number_of_mechanics - number_of_mechanics).
                        append("\n\t   \u27A5 Number of mechanics will be: ").append(number_of_mechanics).
                        append("\n\t\t  \u27A5 Request processing time: ").append(repair_time).append(" min.").
                        append("\n\t\t  \u27A5 Employment rate: ").append(EmploymentRate.HIGH).append("\n\n");
            }
        }
        return String.valueOf(resultStr);
    }

}
