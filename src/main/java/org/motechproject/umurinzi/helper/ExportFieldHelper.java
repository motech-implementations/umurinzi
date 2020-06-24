package org.motechproject.umurinzi.helper;

import static org.motechproject.umurinzi.constants.UmurinziConstants.DATE_FIELD_TYPE;
import static org.motechproject.umurinzi.constants.UmurinziConstants.DATE_TIME_FIELD_TYPE;
import static org.motechproject.umurinzi.constants.UmurinziConstants.DOUBLE_FIELD_TYPE;
import static org.motechproject.umurinzi.constants.UmurinziConstants.VISIT_TYPE_FIELD_TYPE;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.dto.ExportField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportFieldHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExportFieldHelper.class);

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
  private static final DecimalFormat DOUBLE_FORMATTER = new DecimalFormat("#.##",
      DecimalFormatSymbols.getInstance(Locale.ENGLISH));

  public static String getFieldValue(Object entity, ExportField exportField) {
    Object value = entity;

    for (String fieldName : exportField.getPath()) {
      value = getFieldValue(value, fieldName);
    }

    return getStringValue(value, exportField.getFieldType());
  }

  public static String getStringValue(Object field, String fieldType) {
    if (field == null) {
      return "";
    }

    switch (fieldType) {
      case DATE_FIELD_TYPE:
        return DATE_FORMATTER.print((LocalDate) field);
      case DATE_TIME_FIELD_TYPE:
        return DATE_TIME_FORMATTER.print((DateTime) field);
      case DOUBLE_FIELD_TYPE:
        return DOUBLE_FORMATTER.format(field);
      case VISIT_TYPE_FIELD_TYPE:
        return ((VisitType) field).getDisplayValue();
      default:
        return field.toString();
    }
  }

  public static Object getFieldValue(Object entity, String fieldName) {
    if (entity == null) {
      return null;
    }

    try {
      return PropertyUtils.getProperty(entity, fieldName);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOGGER.error("There was a problem with get value of property {} in bean: {}", fieldName, entity, e);
    }

    return null;
  }
}
