package com.bialystok.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Event", propOrder = {
    "id",
    "name",
    "type",
    "date",
    "week",
    "month",
    "year",
    "description"
})
public class Event {

    @XmlElement(required = true)
    private int id;

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String type;

    @XmlElement(required = true)
    private String date;

    @XmlElement(required = true)
    private int week;

    @XmlElement(required = true)
    private int month;

    @XmlElement(required = true)
    private int year;

    @XmlElement(required = true)
    private String description;

    // Constructors
    public Event() {
    }

    public Event(int id, String name, String type, String date, int week, int month, int year, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
        this.week = week;
        this.month = month;
        this.year = year;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getWeek() { return week; }
    public void setWeek(int week) { this.week = week; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
