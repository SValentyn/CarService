package processes;

import base.Client;
import base.Request;
import enums.TypeReq;
import service.CarService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Класс для генерирования случайным образом, массива заявок, на обслуживание автомобилей.
 */
public class GenerateRequests {

    private static final int WEEKDAY = 720;
    private static final int OFFDAY = 480;

    private CarService carService = CarService.getInstance();
    private Random random = new Random();

    public GenerateRequests() {
    }

    /**
     * Метод для создания массива заявок {@link #createArrayRequests()}
     * и отправки этого массива в дальнейшую обработку {@link CarService#receivingAndSendingRequests(Request[])}
     *
     * @throws IOException if file write error
     * @see #randomize()
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
            requests[i] = new Request(client, TypeReq.values()[i]);
        }
        shuffleArray(requests);

        return requests;
    }

    private void shuffleArray(Request[] requests) {
        List<Request> list = Arrays.asList(requests);
        Collections.shuffle(list);
    }

    /**
     * Метод генерирования запросов на формирование массива заявок по графику рабочей недели,
     * где длительность рабочего дня в будние дни - 12 часов, а в выходные - 8 часов;
     * в середине дня плотность возникновения запроса выше
     *
     * @throws InterruptedException if thread interrupted
     * @throws IOException          if file write error
     * @see #carService#generateRandomRequests()
     */
    public void randomize() throws InterruptedException, IOException {
        for (int day = 1; day <= 7; day++) {
            int step;

            if (day <= 5) {  /* для будних дней */
                for (int minute = 0; minute <= WEEKDAY; minute += step) {
                    if (minute <= 270 | minute >= 450) {
                        step = random.nextInt(31) + 30;
                    } else {    /* середина дня (заявки поступают чаще) */
                        step = random.nextInt(16) + 15;
                    }
                    createAndSendRequests();
                    Thread.sleep(step * 10);
                }
            } else {  /* для выходных дней */
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
        carService.setSignal(false); // завершение генерирования запросов
    }

}
