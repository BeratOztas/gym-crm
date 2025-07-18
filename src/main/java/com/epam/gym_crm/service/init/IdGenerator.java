package com.epam.gym_crm.service.init;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.utils.EntityType;

@Component
public class IdGenerator {

	private static final Logger logger = LoggerFactory.getLogger(IdGenerator.class);

	private final Map<EntityType, AtomicLong> idCounters = new ConcurrentHashMap<>();

	public IdGenerator() {
		for (EntityType et : EntityType.values()) {
			idCounters.put(et, new AtomicLong());
		}
		logger.info("IdGenerator bean initialized with ID counters for trainee, trainer, and training.");
	}

	public Long getNextId(EntityType entityType) {
		AtomicLong counter = idCounters.get(entityType);
		if (counter == null) {
			logger.error("ID counter not found for entity type '{}'. Please ensure it's initialized.", entityType);
			throw new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, 
                    "ID counter not found for entity type: " + entityType + ". Please check configuration."));
		}
		return counter.incrementAndGet();
	}

	public Map<EntityType, AtomicLong> getIdCounters() {
		return idCounters;
	}

	public void resetAllCounters() {
		idCounters.values().forEach(atomicLong -> atomicLong.set(0L));
		logger.info("All ID counters have been reset to 0.");
	}
}