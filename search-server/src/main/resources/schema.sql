-- http://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html

-- index queue
CREATE TABLE index_queue (
  id VARCHAR(255),
  path VARCHAR(255),
  status VARCHAR(20) DEFAULT 'QUEUED',
  operation VARCHAR(20),
  queued_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE index_queue ADD CONSTRAINT pk_index_queue PRIMARY KEY (id, path, operation);