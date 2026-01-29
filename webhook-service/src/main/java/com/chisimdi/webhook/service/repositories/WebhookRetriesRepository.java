package com.chisimdi.webhook.service.repositories;

import com.chisimdi.webhook.service.models.WebhookRetries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookRetriesRepository extends JpaRepository<WebhookRetries,Integer> {
    List<WebhookRetries>findByDoneAndNextRetryTimeBefore(Boolean done, LocalDateTime localDateTime);
}
