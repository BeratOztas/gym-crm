package com.epam.gym_crm.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.utils.EntityType;

@Component
public class Storage {

    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    private Map<Long, Trainee> traineeStorageMap;
    private Map<Long, Trainer> trainerStorageMap;
    private Map<Long, Training> trainingStorageMap;

    private final Map<EntityType, Map<Long, ?>> allStorageMaps = new ConcurrentHashMap<>();

    public Storage() {
        logger.info("Storage bean constructor called.");
    }

    @Autowired
    public void setTraineeStorageMap(@Qualifier("traineeStorageMap") Map<Long, Trainee> traineeStorageMap) {
        this.traineeStorageMap = traineeStorageMap;
        allStorageMaps.put(EntityType.TRAINEE, traineeStorageMap);
    }

    @Autowired
    public void setTrainerStorageMap(@Qualifier("trainerStorageMap") Map<Long, Trainer> trainerStorageMap) {
        this.trainerStorageMap = trainerStorageMap;
        allStorageMaps.put(EntityType.TRAINER, trainerStorageMap);
    }

    @Autowired
    public void setTrainingStorageMap(@Qualifier("trainingStorageMap") Map<Long, Training> trainingStorageMap) {
        this.trainingStorageMap = trainingStorageMap;
        allStorageMaps.put(EntityType.TRAINING, trainingStorageMap);
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Long, T> getStorageMap(EntityType entityType) {
        Map<Long, ?> map = allStorageMaps.get(entityType);
        if (map == null) {
            logger.error("No storage map found for entity type: {}", entityType);
            throw new IllegalArgumentException("No storage map found for entity type: " + entityType);
        }
        return (Map<Long, T>) map;
    }

    public Map<Long, Trainee> getTraineeStorageMap() {
        return traineeStorageMap;
    }

    public Map<Long, Trainer> getTrainerStorageMap() {
        return trainerStorageMap;
    }

    public Map<Long, Training> getTrainingStorageMap() {
        return trainingStorageMap;
    }

    public void clearAllStorage() {
        traineeStorageMap.clear();
        trainerStorageMap.clear();
        trainingStorageMap.clear();
        logger.info("All in-memory storage maps have been cleared.");
    }

    public int getTotalEntityCount() {
        return traineeStorageMap.size() + trainerStorageMap.size() + trainingStorageMap.size();
    }
}
