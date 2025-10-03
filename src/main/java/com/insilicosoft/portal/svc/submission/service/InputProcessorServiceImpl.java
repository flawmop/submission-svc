package com.insilicosoft.portal.svc.submission.service;

import static com.insilicosoft.portal.svc.submission.SubmissionIdentifiers.BINDING_NAME_SIMULATION_INPUT;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.insilicosoft.portal.svc.submission.event.SimulationCreate;
import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.exception.InputVerificationException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.persistence.entity.Simulation;
import com.insilicosoft.portal.svc.submission.persistence.repository.SimulationRepository;
import com.insilicosoft.portal.svc.submission.value.MessageLevel;

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
  private final SubmissionService submissionService;

  /**
   * Initialising constructor.
   * 
   * @param simulationRepository Simulation repository.
   * @param streamBridge Event sending mechanism.
   * @param submissionService Submission service.
   */
  public InputProcessorServiceImpl(final SimulationRepository simulationRepository,
                                   final StreamBridge streamBridge,
                                   final SubmissionService submissionService) {
    this.simulationRepository = simulationRepository;
    this.streamBridge = streamBridge;
    this.submissionService = submissionService;
  }

  // Advice applied to this method to capture FileProcessingException
  @Override
  public void process(final long submissionId, final byte[] file) throws FileProcessingException,
                                                                         InputVerificationException {
    final String content = new String(file, UTF_8);

    final JsonFactory jsonFactory = new JsonFactory();
    final List<Simulation> simulations = new ArrayList<>();

    try {
      final JsonParser jsonParser = jsonFactory.createParser(content);
      if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
        final String message = "Content must be a JSON object";
        log.warn("~process() : ".concat(message));
        throw new FileProcessingException(message);
      }

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        final String sectionName = jsonParser.currentName();
        if (sectionName != null) {
          FieldsSections sectionField = null;
          try {
            sectionField = FieldsSections.valueOf(sectionName);
          } catch (IllegalArgumentException e) {
            final String message = "Unrecognised document section '" + sectionName + "', expected values are '" + Arrays.toString(FieldsSections.values()) + "'";
            log.warn("~process() : ".concat(message));
            throw new FileProcessingException(message);
          }
          switch (sectionField) {
            case simulations:
              parseSimulation(submissionId, jsonParser, simulations);
              break;
            default:
              final String message = "No implementation yet for document section '" + sectionName + "'";
              log.error("~process() : ".concat(message));
              throw new FileProcessingException(message);
          }
        }
      }
      jsonParser.close();
    } catch (IOException e) {
      final String message = e.getMessage();
      log.warn("~process() : IOException '{}'", message);
      throw new FileProcessingException(message);
    }

    if (simulations.isEmpty())
      throw new FileProcessingException("Could not generate any simulations");

    final Map<String, Set<Message>> problems = new HashMap<>();
    int simulationCnt = 1;
    for (Simulation simulation: simulations) {
      final Set<Message> messages = simulation.getMessages(MessageLevel.WARN);
      if (!messages.isEmpty()) {
        // Simulations not yet persisted, so no entity id to use.
        problems.put(simulation.getClientId()
                               .orElse(String.valueOf(submissionId).concat(".").concat(String.valueOf(simulationCnt))),
                     messages);
        simulationCnt++;
      }
    }

    if (!problems.isEmpty()) {
      submissionService.reject(submissionId, problems);
      throw new InputVerificationException("Problems encountered translating the simulation input");
    }

    final List<SimulationCreate> simulationCreateEvents = new ArrayList<>();
    for (Simulation simulation: simulations) {
      final Simulation saved = simulationRepository.save(simulation);
      simulationCreateEvents.add(saved.toCreate());
    }

    // Fire off events for, e.g. simulation runners and databases
    for (SimulationCreate simulationCreate: simulationCreateEvents) {
      try {
        streamBridge.send(BINDING_NAME_SIMULATION_INPUT, simulationCreate);
      } catch (Exception e) {
        // e.g. java.net.ConnectException
        final String message = e.getMessage();
        log.error("~process() : Exception '{}'", message);
      }
    }
  }

  private void parseSimulation(long submissionId, JsonParser jsonParser, List<Simulation> simulations)
                               throws FileProcessingException, IOException {
    JsonToken current = jsonParser.nextToken();
    if (current != JsonToken.START_ARRAY) {
      final String message = "'simulations' value must be an array";
      log.warn("~process() : ".concat(message));
      throw new FileProcessingException(message);
    }

    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
      Integer modelId = null;
      BigDecimal pacingFrequency = null;
      BigDecimal pacingMaxTime = null;
      List<BigDecimal> plasmaPoints = null;

      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = jsonParser.currentName();
        if (fieldName != null) {
          FieldsSimulation simulationField = null;
          try {
            simulationField = FieldsSimulation.valueOf(fieldName);
          } catch (IllegalArgumentException e) {
            final String message = "Unrecognised simulation section '" + fieldName + "', expected values are '" + Arrays.toString(FieldsSimulation.values()) + "'";
            log.warn("~process() : ".concat(message));
            throw new FileProcessingException(message);
          }
          switch (simulationField) {
            case modelId:
              jsonParser.nextToken();
              modelId = jsonParser.getIntValue(); 
              break;
            case pacingFrequency:
              jsonParser.nextToken();
              pacingFrequency = jsonParser.getDecimalValue();
              break;
            case pacingMaxTime:
              jsonParser.nextToken();
              pacingMaxTime = jsonParser.getDecimalValue();
              break;
            case plasmaPoints:
              jsonParser.nextToken();
              plasmaPoints = new ArrayList<>();
              while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                plasmaPoints.add(jsonParser.getDecimalValue());
              }
              break;
            default:
              final String message = "No implementation yet for simulation field '" + simulationField.toString() + "'";
              log.error("~process() : ".concat(message));
              throw new FileProcessingException(message);
          }
        }
      }

      simulations.add(new Simulation(submissionId, modelId, pacingFrequency, pacingMaxTime,
                                     plasmaPoints));
    }
  }

}