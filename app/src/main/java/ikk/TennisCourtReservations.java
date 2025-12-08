package ikk;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TennisCourtReservations {
    static final Map<String, String> COURTS = new TreeMap<>(Map.of(
            "court1", "https://ca-berkeley.civicrec.com/CA/berkeley-ca/catalog/getFacilityHours/dd0760238c4ac06e1c3ebf74d9545eb7/68499/204636/",
            "court2","https://ca-berkeley.civicrec.com/CA/berkeley-ca/catalog/getFacilityHours/47ece1234a38ba0fef4b785507adc0f0/77102/483972/"
    ));

    static void main(String[] args) throws IOException, InterruptedException {
        if(args.length == 0)
            args = new String[]{"0"};
        String date = LocalDate.now().plusDays(Long.parseLong(args[0])).toString();
        IO.println(date);
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();
        for (String court : COURTS.keySet()) {
            String resStr = getResponse(client, COURTS.get(court) + date);
            IO.println(court);
            Response res = gson.fromJson(resStr, Response.class);
            for (Hours hours : res.hours)
                if (hours.description.equals("Adult  Non-Res 6"))
                    IO.println(hours.range());
        }
    }

    static String getResponse(HttpClient httpClient, String url) throws IOException, InterruptedException {
        for(int i = 0; i < 2; i++){
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .setHeader("Cookie", "PHPSESSID=on7rpa2eut4auev2qbo31j4p6l")
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if(res.body() != null)
                return res.body();
        }
        throw new RuntimeException("could NOT obtain response");
    }


    static class Response {
        List<Hours> hours = new ArrayList<>();
    }

    static class Hours {
        String startDtm, endDtm, description;

        String range() {
            return getTime(startDtm) + " - " + getTime(endDtm);
        }

        //2024-07-07 07:00:00
        String getTime(String s) {
            String time = s.split(" ")[1];
            String[] parts = time.split(":");
            return parts[0] + ":" + parts[1];
        }
    }
}
