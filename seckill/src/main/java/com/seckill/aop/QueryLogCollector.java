package com.seckill.aop;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class QueryLogCollector {

    private static final Logger log = LoggerFactory.getLogger(QueryLogCollector.class);
    private static final int MAX_SIZE = 200;

    private final CopyOnWriteArrayList<QueryLogRecord> records = new CopyOnWriteArrayList<>();

    public void record(QueryLogRecord record) {
        records.add(record);
        if (records.size() > MAX_SIZE) {
            records.remove(0);
        }
    }

    public List<QueryLogRecord> getRecent() {
        int size = records.size();
        int from = Math.max(0, size - 50);
        return new ArrayList<>(records.subList(from, size));
    }

    public void clear() {
        records.clear();
    }
}
