package com.insilicosoft.portal.ripsvr.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

enum FieldsSections {
  simulations
}

enum FieldsSimulation {
  modelId,
  pacingFrequency,
  pacingMaxTime,
  plasmaPoints
}

@Service
public class FileStorageServiceImpl implements FileStorageService {

  private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

  @Override
  @Async
  public void save(final MultipartFile file) {
    String content = null;
    try {
      content = new String(file.getBytes(), UTF_8);
    } catch (IOException|OutOfMemoryError e) {
      log.error("IOException|OutOfMemoryError : " + e.getMessage());
      e.printStackTrace();
      return;
    }

    JsonFactory jsonFactory = new JsonFactory();
    try {
      JsonParser jsonParser = jsonFactory.createParser(content);
      if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
        log.error("Content must be a JSON object");
        throw new UnsupportedOperationException("JSON must be an Object!");
      }

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String sectionName = jsonParser.getCurrentName();
        if (sectionName != null) {
          switch (FieldsSections.valueOf(sectionName)) {
            case simulations:
              parseSimulations(jsonParser);
              break;
            default:
              log.warn("Unrecognized section '{}", sectionName);
              break;
          }
        }
      }
      jsonParser.close();
    } catch (IOException|UnsupportedOperationException e) {
      e.printStackTrace();
    }
  }

  void parseSimulations(JsonParser jsonParser) throws IOException {
    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
      Integer modelId = null;
      float pacingFrequency = 0.0f;
      float pacingMaxTime = 0.0f;
      List<Float> plasmaPoints = new ArrayList<>();

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = jsonParser.getCurrentName();
        if (fieldName != null) {
          switch (FieldsSimulation.valueOf(fieldName)) {
            case modelId:
              jsonParser.nextToken();
              modelId = jsonParser.getIntValue(); 
              break;
            case pacingFrequency:
              jsonParser.nextToken();
              pacingFrequency = jsonParser.getFloatValue();
              break;
            case pacingMaxTime:
              jsonParser.nextToken();
              pacingMaxTime = jsonParser.getFloatValue();
              break;
            case plasmaPoints:
              jsonParser.nextToken();
              while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                plasmaPoints.add(jsonParser.getFloatValue());
              }
              break;
            default:
              log.warn("Unrecognized field '{}", fieldName);
              break;
          }
        }
      }
    }
  }

}