package src;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Service {
    static class Ticket {
        public String origin;
        public String destination;
        public String departure_time;
        public String arrival_time;
        public String carrier;
        public int price;
    }

    static class Tickets {
        public List<Ticket> tickets;
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Tickets ticketsData = mapper.readValue(new File("tickets.json"), Tickets.class);
        List<Ticket> tickets = ticketsData.tickets.stream()
                .filter(t -> t.origin.equals("VVO") && t.destination.equals("TLV"))
                .collect(Collectors.toList());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Map<String, Long> minFlightTimes = new HashMap<>();
        for (Ticket t : tickets) {
            long duration = timeFormat.parse(t.arrival_time).getTime() - timeFormat.parse(t.departure_time).getTime();
            duration = duration < 0 ? duration + 24 * 60 * 1000 : duration; 
            long minutes = duration / (1000 * 60);
            minFlightTimes.merge(t.carrier, minutes, Math::min);
        }

        System.out.println("Минимальное время полета (Владивосток -> Тель-Авив) для каждого перевозчика:");
        minFlightTimes.forEach(
                (carrier, minutes) -> System.out.printf("%s: %d ч %d мин\n", carrier, minutes / 60, minutes % 60));

        List<Integer> prices = tickets.stream().map(t -> t.price).sorted().collect(Collectors.toList());
        double average = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
        double median = prices.size() % 2 == 0
                ? (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2.0
                : prices.get(prices.size() / 2);

        System.out.printf("\nРазница между средней ценой и медианой: %.1f\n", Math.abs(average - median));
    }
}
