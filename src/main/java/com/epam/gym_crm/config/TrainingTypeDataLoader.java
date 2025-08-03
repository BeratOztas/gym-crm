package com.epam.gym_crm.config; 

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;

@Component 
public class TrainingTypeDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDataLoader.class);

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeDataLoader(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (trainingTypeRepository.count() == 0) { 
            logger.info("Initializing TrainingType data with predefined IDs...");

            Map<Long, String> predefinedTrainingTypes = new LinkedHashMap<>();
            predefinedTrainingTypes.put(1L, "FITNESS");
            predefinedTrainingTypes.put(2L, "YOGA");
            predefinedTrainingTypes.put(3L, "ZUMBA");
            predefinedTrainingTypes.put(4L, "STRETCHING");
            predefinedTrainingTypes.put(5L, "RESISTANCE");

            for (Map.Entry<Long, String> entry : predefinedTrainingTypes.entrySet()) {
                TrainingType trainingType = new TrainingType(entry.getKey(), entry.getValue());
                trainingTypeRepository.save(trainingType);
                logger.debug("Saved TrainingType: ID={}, Name={}", entry.getKey(), entry.getValue());
            }
            logger.info("TrainingType data initialization completed. Total {} types added.", predefinedTrainingTypes.size());
        } else {
            logger.info("TrainingType data already exists. Skipping initialization.");
        }
    }
}