package com.bialystok.events;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebService(endpointInterface = "com.bialystok.events.BialystokEventService")
public class BialystokEventServiceImpl implements BialystokEventService {

    private static final Path DATA_FILE = Path.of("events.json");
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<Event>>(){}.getType();

    private List<Event> events;
    private int currentId;

    public BialystokEventServiceImpl() {
        if (Files.exists(DATA_FILE)) {
            try {
                String json = Files.readString(DATA_FILE);
                events = GSON.fromJson(json, LIST_TYPE);

                int maxId = 0;
                for (Event e : events) {
                    if (e.getId() > maxId) {
                        maxId = e.getId();
                    }
                }
                currentId = maxId + 1;
            } catch (IOException e) {
                events = new ArrayList<>();
                currentId = 1;
            }
        } else {
            events = new ArrayList<>();
            currentId = 1;
        }
    }

    private void save() {
        try {
            Files.writeString(DATA_FILE, GSON.toJson(events));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    @Override
    public List<Event> getEventsByDay(String date) {
        return events.stream()
                .filter(e -> e.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByWeek(int week, int year) {
        return events.stream()
                .filter(e -> e.getWeek() == week && e.getYear() == year)
                .collect(Collectors.toList());
    }

    @Override
    public Event getEventDetails(int id) {
        return events.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean addEvent(String name, String type, String date, int week, int month, int year, String description) {
        Event newEvent = new Event(currentId++, name, type, date, week, month, year, description);
        boolean result = events.add(newEvent);
        save();
        return result;
    }

    @Override
    public boolean updateEvent(int id, String name, String type, String date, int week, int month, int year, String description) {
        Event event = getEventDetails(id);
        if (event != null) {
            event.setName(name);
            event.setType(type);
            event.setDate(date);
            event.setWeek(week);
            event.setMonth(month);
            event.setYear(year);
            event.setDescription(description);
            save();
            return true;
        }
        return false;
    }

    private String sanitizeForPdf(String input) {
        if (input == null) return "";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("ł", "l").replaceAll("Ł", "L");
    }

    @Override
    public DataHandler getEventSummaryPdf() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Zestawienie Wydarzen - Bialystok");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 670);
                
                int yOffset = 670;
                for (Event event : events) {
                    if (yOffset < 100) break;
                    String text = "- " + event.getName() + " (" + event.getDate() + ")";
                    contentStream.showText(sanitizeForPdf(text));
                    contentStream.newLineAtOffset(0, -20);
                    yOffset -= 20;
                }
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);

            ByteArrayDataSource dataSource = new ByteArrayDataSource(baos.toByteArray(), "application/pdf");
            return new DataHandler(dataSource);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
