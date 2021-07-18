package com.debojmuk.covidtracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.debojmuk.covidtracker.models.LocationStats;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CovidDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/%s.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>(); // while populating users can still access data
        HttpClient client = HttpClient.newHttpClient();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-uuuu"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(VIRUS_DATA_URL, today))).build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() != 200) {
            String yesterday = LocalDate.now().plusDays(-1).format(DateTimeFormatter.ofPattern("MM-dd-uuuu"));
            request = HttpRequest.newBuilder(URI.create(String.format(VIRUS_DATA_URL, yesterday))).build();
            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        StringReader stringReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(stringReader);
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province_State"));
            locationStats.setCountry(record.get("Country_Region"));
            locationStats
                    .setLatestTotalCases("".equals(record.get("Active")) ? 0 : Integer.parseInt(record.get("Active")));

            newStats.add(locationStats);
        }

        this.allStats = newStats;
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

}
