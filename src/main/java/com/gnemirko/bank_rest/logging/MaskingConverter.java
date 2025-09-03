package com.gnemirko.bank_rest.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

import java.util.regex.Pattern;

public class MaskingConverter extends CompositeConverter<ILoggingEvent> {

    private static final Pattern PAN = Pattern.compile("\\b\\d{12,19}\\b");

    @Override
    protected String transform(ILoggingEvent event, String in) {
        if (in == null) return null;


        return PAN.matcher(in).replaceAll(m -> {
            String digits = m.group();
            String last4 = digits.substring(digits.length() - 4);
            return "**** **** **** " + last4;
        });
    }
}