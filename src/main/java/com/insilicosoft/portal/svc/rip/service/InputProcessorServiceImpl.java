package com.insilicosoft.portal.svc.rip.service;

import static com.insilicosoft.portal.svc.rip.RipIdentifiers.BINDING_NAME_SIMULATION_INPUT;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.insilicosoft.portal.svc.rip.event.SimulationMessage;
import com.insilicosoft.portal.svc.rip.exception.FileProcessingException;
import com.insilicosoft.portal.svc.rip.persistence.entity.Simulation;
import com.insilicosoft.portal.svc.rip.persistence.repository.SimulationRepository;

enum FieldsSections {
  simulations
}

enum FieldsSimulation {
  modelId,
  pacingFrequency,
  pacingMaxTime,
  plasmaPoints
}

/**
 * Input processing implementation.
 *
 * @author geoff
 */
@Service
public class InputProcessorServiceImpl implements InputProcessorService {

  private static final Logger log = LoggerFactory.getLogger(InputProcessorServiceImpl.class);

  private final SimulationRepository simulationRepository;
  private final StreamBridge streamBridge;

  /**
   * Initialising constructor.
   * 
   * @param simulationRepository Simulation repository.
   * @param streamBridge Event sending mechanism.
   */
  public InputProcessorServiceImpl(final SimulationRepository simulationRepository,
                                   final StreamBridge streamBridge) {
    this.simulationRepository = simulationRepository;
    this.streamBridge = streamBridge;
  }

  @Override
  public String get() {
    return "All good from FileAsyncUploadController->InputProcessorService!!";
  }

  @Override
  @Async
  public void processAsync(final byte[] file) throws FileProcessingException {
    final String content = new String(file, UTF_8);

    final JsonFactory jsonFactory = new JsonFactory();
    final List<SimulationMessage> simulations = new ArrayList<>();

    try {
      final JsonParser jsonParser = jsonFactory.createParser(content);
      if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
        final String message = "Content must be a JSON object";
        log.warn("~processAsync() : ".concat(message));
        throw new FileProcessingException(message);
      }

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        final String sectionName = jsonParser.currentName();
        if (sectionName != null) {
          switch (FieldsSections.valueOf(sectionName)) {
            case simulations:
              parseSimulations(jsonParser, simulations);
              break;
            default:
              log.warn("~processAsync() : Unrecognized section '{}'", sectionName);
              break;
          }
        }
      }
      jsonParser.close();
    } catch (IOException e) {
      final String message = e.getMessage();
      log.warn("~processAsync() : IOException '{}'", message);
      throw new FileProcessingException(message);
    }

    // Verify the input was good

    // Record the simulation
    log.debug("~processAsync() : Saved : " + simulationRepository.save(new Simulation()).toString());

    // Fire off events for, e.g. simulation runners and databases
    for (SimulationMessage simulationMessage: simulations) {
      streamBridge.send(BINDING_NAME_SIMULATION_INPUT, simulationMessage);
    }
  }

  void parseSimulations(JsonParser jsonParser, List<SimulationMessage> simulations) throws IOException {
    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
      Integer modelId = null;
      Float pacingFrequency = null;
      Float pacingMaxTime = null;
      List<Float> plasmaPoints = null;

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = jsonParser.currentName();
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