package com.bialystok.events;

import javax.activation.DataHandler;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import java.util.List;

@WebService
@MTOM
@HandlerChain(file = "handlers.xml")
public interface BialystokEventService {

    @WebMethod
    List<Event> getEventsByDay(
        @WebParam(name = "date") String date
    );

    @WebMethod
    List<Event> getEventsByWeek(
        @WebParam(name = "week") int week,
        @WebParam(name = "year") int year
    );

    @WebMethod
    Event getEventDetails(
        @WebParam(name = "id") int id
    );

    @WebMethod
    boolean addEvent(
        @WebParam(name = "name") String name,
        @WebParam(name = "type") String type,
        @WebParam(name = "date") String date,
        @WebParam(name = "week") int week,
        @WebParam(name = "month") int month,
        @WebParam(name = "year") int year,
        @WebParam(name = "description") String description
    );

    @WebMethod
    boolean updateEvent(
        @WebParam(name = "id") int id,
        @WebParam(name = "name") String name,
        @WebParam(name = "type") String type,
        @WebParam(name = "date") String date,
        @WebParam(name = "week") int week,
        @WebParam(name = "month") int month,
        @WebParam(name = "year") int year,
        @WebParam(name = "description") String description
    );

    @WebMethod
    List<Event> getAllEvents();

    @WebMethod
    List<Event> getUpcomingEvents();

    @WebMethod
    boolean deleteEvent(
        @WebParam(name = "id") int id
    );

    @WebMethod
    boolean submitProposal(
        @WebParam(name = "name") String name,
        @WebParam(name = "type") String type,
        @WebParam(name = "date") String date,
        @WebParam(name = "description") String description,
        @WebParam(name = "organizerName") String organizerName,
        @WebParam(name = "contactEmail") String contactEmail
    );

    @WebMethod
    List<EventProposal> getProposals();

    @WebMethod
    boolean approveProposal(@WebParam(name = "id") int id);

    @WebMethod
    boolean rejectProposal(@WebParam(name = "id") int id);

    @WebMethod
    @XmlMimeType("application/pdf")
    DataHandler getEventSummaryPdf();
}
