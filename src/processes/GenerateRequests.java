package processes;

import base.Client;
import base.Request;
import enums.TypeWorkshop;
import service.CarService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Class for generating, randomly, an array of requests for car maintenance.
 */
public class GenerateRequests {

    private static final int WEEKDAY = 720;
    private static final int OFFDAY = 480;

    private CarService carService = CarService.getInstance();
    private Random random = new Random();

    public GenerateRequests() {
    }

    /**
     * Method to create an array of requests {@link #createArrayRequests()} and send this array for further processing.
     *
     * @throws IOException if file write error
     */
    private void createAndSendRequests() throws IOException {
        Request[] requests = createArrayRequests();

        int tempV = random.nextInt(100) + 1; // range: 1..100
        if (tempV == 100) {       // 1%
            requests = Arrays.copyOfRange(requests, 0, 4);
        } else if (tempV >= 95) { // 5%
            requests = Arrays.copyOfRange(requests, 0, 3);
        } else if (tempV >= 85) { // 10%
            requests = Arrays.copyOfRange(requests, 0, 2);
        } else {                  // 85%
            requests = Arrays.copyOfRange(requests, 0, 1);
        }

        carService.receivingAndSendingRequests(requests);
    }

    private Request[] createArrayRequests() {
        Client client = new Client();
        Request[] requests = new Request[4];

        for (int i = 0; i < requests.length; i++) {
            requests[i] = new Request(client, TypeWorkshop.values()[i]);
        }

        shuffleArray(requests);
        return requests;
    }

    private void shuffleArray(Request[] requests) {
        List<Request> list = Arrays.asList(requests);
        Collections.shuffle(list);
    }

    /**
     * The method of generating requests for the formation of an array of requests on the schedule of the working week,
     * where the working day on weekdays is 12 hours, and on weekends - 8 hours; in the middle of the day, the query flow density is above average.
     *
     * @throws InterruptedException if thread interrupted
     * @throws IOException          if file write error
     */
    public void randomize() throws InterruptedException, IOException {
        for (int day = 1; day <= 7; day++) {
            int step;

            if (day <= 5) {  // for weekdays
                for (int minute = 0; minute <= WEEKDAY; minute += step) {
                    if (minute <= 270 | minute >= 450) {
                        step = random.nextInt(31) + 30;
                    } else {    /* середина дня (заявки поступают чаще) */
                        step = random.nextInt(16) + 15;
                    }
                    createAndSendRequests();
                    Thread.sleep(step * 10);
                }
            } else {  // for off days
                for (int minute = 0; minute <= OFFDAY; minute += step) {
                    if (minute <= 180 | minute >= 300) {
                        step = random.nextInt(31) + 30;
                    } else {
                        step = random.nextInt(16) + 15;
                    }
                    createAndSendRequests();
                    Thread.sleep(step * 10);
                }
            }
        }
        carService.setSignal(false); // complete requests generation
    }

}
