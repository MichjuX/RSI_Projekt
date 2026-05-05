package com.bialystok.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventProposal", propOrder = {
    "id", "name", "type", "date", "description", "organizerName", "contactEmail"
})
public class EventProposal {

    @XmlElement(required = true) private int id;
    @XmlElement(required = true) private String name;
    @XmlElement(required = true) private String type;
    @XmlElement(required = true) private String date;
    @XmlElement(required = true) private String description;
    @XmlElement(required = true) private String organizerName;
    @XmlElement(required = true) private String contactEmail;

    public EventProposal() {}

    public EventProposal(int id, String name, String type, String date,
                         String description, String organizerName, String contactEmail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
        this.description = description;
        this.organizerName = organizerName;
        this.contactEmail = contactEmail;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getOrganizerName() { return organizerName; }
    public String getContactEmail() { return contactEmail; }
}
