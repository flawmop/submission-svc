package com.insilicosoft.portal.svc.rip.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.insilicosoft.portal.svc.rip.event.SimulationMessage;

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
public class InputProcessorServiceImpl implements InputProcessorService {

  private static final Logger log = LoggerFactory.getLogger(InputProcessorServiceImpl.class);

  private final StreamBridge streamBridge;

  public InputProcessorServiceImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  @Async
  public void process(final MultipartFile file) {
    String content = null;
    try {
      content = new String(file.getBytes(), UTF_8);
    } catch (IOException|OutOfMemoryError e) {
      log.error("~process() : IOException|OutOfMemoryError : " + e.getMessage());
      e.printStackTrace();
      return;
    }

    JsonFactory jsonFactory = new JsonFactory();
    final List<SimulationMessage> simulations = new ArrayList<>();

    try {
      JsonParser jsonParser = jsonFactory.createParser(content);
      if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
        log.error("~process() : Content must be a JSON object");
        throw new UnsupportedOperationException("JSON must be an Object!");
      }

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String sectionName = jsonParser.getCurrentName();
        if (sectionName != null) {
          switch (FieldsSections.valueOf(sectionName)) {
            case simulations:
              parseSimulations(jsonParser, simulations);
              break;
            default:
              log.warn("~process() : Unrecognized section '{}'", sectionName);
              break;
          }
        }
      }
      jsonParser.close();
    } catch (IOException|UnsupportedOperationException e) {
      e.printStackTrace();
    }

    // Verify the input was good

    // Fire off events for, e.g. simulation runners and databases
    for (SimulationMessage simulationMessage: simulations) {
      streamBridge.send("simulation-input", simulationMessage);
    }

  }

  void parseSimulations(JsonParser jsonParser, List<SimulationMessage> simulations) throws IOException {
    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
      Integer modelId = null;
      Float pacingFrequency = null;
      Float pacingMaxTime = null;
      List<Float> plasmaPoints = null;

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
              plasmaPoints = new ArrayList<>();
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

      simulations.add(new SimulationMessage(modelId, pacingFrequency, pacingMaxTime, plasmaPoints));
    }
  }

}
