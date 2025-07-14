package com.epam.gym_crm.service.init;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.utils.EntityType;
import com.epam.gym_crm.utils.JsonConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class InitialDataLoader {

	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

	private final Map<Long, Trainee> traineeStorageMap;
	private final Map<Long, Trainer> trainerStorageMap;
	private final Map<Long, Training> trainingStorageMap;

	private final IdGenerator idGenerator;
	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader;

	@Value("${storage.trainee.file}")
	private String traineeFilePath;
	@Value("${storage.trainer.file}")
	private String trainerFilePath;
	@Value("${storage.training.file}")
	private String trainingFilePath;

	public InitialDataLoader(@Qualifier("traineeStorageMap") Map<Long, Trainee> traineeStorageMap,
			@Qualifier("trainerStorageMap") Map<Long, Trainer> trainerStorageMap,
			@Qualifier("trainingStorageMap") Map<Long, Training> trainingStorageMap, IdGenerator idGenerator,
			ObjectMapper objectMapper, ResourceLoader resourceLoader) {
		this.traineeStorageMap = traineeStorageMap;
		this.trainerStorageMap = trainerStorageMap;
		this.trainingStorageMap = trainingStorageMap;
		this.idGenerator = idGenerator;
		this.objectMapper = objectMapper;
		this.resourceLoader = resourceLoader;

		logger.info("InitialDataLoader bean initialized.");
	}

	@PostConstruct
	public void init() {
		logger.info("Starting initial data loading from JSON files...");

		loadDataFromJson(traineeFilePath, Trainee.class, traineeStorageMap, EntityType.TRAINEE);
		loadDataFromJson(trainerFilePath, Trainer.class, trainerStorageMap, EntityType.TRAINER);
		loadTrainingDataFromJson(trainingFilePath);

		logger.info("Initial data loading completed successfully..");
	}

	private <T> void loadDataFromJson(String filePath, Class<T> entityTypeClass, Map<Long, T> targetMap,
			EntityType entityTypeEnum) {
		logger.debug("Loading {} data from: {}", entityTypeEnum.getNamespace(), filePath);
		try (InputStream is = resourceLoader.getResource(filePath).getInputStream()) {
			List<T> entities = objectMapper.readValue(is,
					objectMapper.getTypeFactory().constructCollectionType(List.class, entityTypeClass));
			for (T entity : entities) {

				Long id = idGenerator.getNextId(entityTypeEnum);

				if (entity instanceof Trainee) {
					((Trainee) entity).getUser().setId(id);
				} else if (entity instanceof Trainer) {
					((Trainer) entity).getUser().setId(id);
				} else {
					logger.warn("Unsupported entity type {} encountered during loading. Skipping ID assignment.",
							entityTypeClass.getSimpleName());
					continue;
				}
				targetMap.put(id, entity);
				if (entity instanceof com.epam.gym_crm.model.User) {
					logger.debug("Loaded {}: ID={}, Name={}", entityTypeEnum.getNamespace(), id,
							((com.epam.gym_crm.model.User) entity).getFirstName());
				} else {
					logger.debug("Loaded {}: ID={}", entityTypeEnum.getNamespace(), id);
				}
			}
			logger.info("Successfully loaded {} {} entities from {}.", entities.size(), entityTypeEnum.getNamespace(),
					filePath);
		} catch (Exception e) {
			logger.error("Failed to load {} data from {}: {}", entityTypeEnum.getNamespace(), filePath, e.getMessage(),
					e);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
					"Critical startup error during data loading: " + e.getMessage()));
		}
	}

	

	private void loadTrainingDataFromJson(String filePath) {
		logger.debug("Loading Training data from: {}", filePath);
		try (InputStream is = resourceLoader.getResource(filePath).getInputStream()) {
			List<Map<String, Object>> rawTrainings = objectMapper.readValue(is,
					objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

			for (Map<String, Object> rawTrainingData : rawTrainings) {
				try {
					Long id = idGenerator.getNextId(EntityType.TRAINING);
					Training training = new Training();
					training.setId(id);

					training.setTrainingName((String) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_NAME));
					training.setTrainingType(TrainingType.valueOf(
							((String) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_TYPE)).toUpperCase()));

					String dateString = (String) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_DATE);
					if (dateString != null) {
						training.setTrainingDate(LocalDate.parse(dateString));
					} else {
						logger.warn("Training '{}' has null trainingDate in JSON. Skipping date setting.",
								rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_NAME));
					}

					Number duration = (Number) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_DURATION);
					if (duration != null) {
						training.setTrainingDuration(duration.intValue());
					} else {
						logger.warn("Training '{}' has null trainingDuration in JSON. Skipping duration setting.",
								rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_NAME));
					}

					Long traineeId = ((Number) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINEE_ID)).longValue();
					Long trainerId = ((Number) rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINER_ID)).longValue();

					Trainee trainee = traineeStorageMap.get(traineeId);
					Trainer trainer = trainerStorageMap.get(trainerId);

					if (trainee != null && trainer != null) {
						training.setTrainee(trainee);
						training.setTrainer(trainer);
						trainingStorageMap.put(id, training);
						logger.debug("Loaded Training: ID={}, Name='{}', Trainee ID={}, Trainer ID={}", id,
								training.getTrainingName(), traineeId, trainerId);
					} else {
						logger.warn(
								"Skipping Training '{}' (ID: {}): Linked Trainee (ID: {}) or Trainer (ID: {}) not found in storage. Check load order or data integrity.",
								rawTrainingData.get(JsonConstants.JSON_FIELD_TRAINING_NAME), id, traineeId, trainerId);
					}
				} catch (Exception e) {
					
					logger.error("Error processing a training entry from JSON. Raw data: {}. Error: {}",
							rawTrainingData, e.getMessage(), e);
					throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
							"Error reading initial Training data from " + filePath + ": " + e.getMessage()));
				}
			}
			logger.info("Successfully loaded {} Training entities from {}.", trainingStorageMap.size(), filePath);
		} catch (Exception e) {
			logger.error("Failed to load Training data from {}: {}", filePath, e.getMessage(), e);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
					"An unexpected error occurred while loading initial Training data: " + e.getMessage()));
		}
	}
}