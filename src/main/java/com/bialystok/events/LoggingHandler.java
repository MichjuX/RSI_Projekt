package com.bialystok.events;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.util.Set;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    private void logMessage(SOAPMessageContext context) {
        Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        System.out.println(isOutbound ? "\n--- Outbound SOAP Message ---" : "\n--- Inbound SOAP Message ---");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            context.getMessage().writeTo(baos);
            System.out.println(baos.toString());
        } catch (Exception e) {
            System.err.println("Error logging message: " + e.getMessage());
        }
    }
}
