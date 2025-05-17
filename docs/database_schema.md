# Self Progress Tracking Service - Database Schema

## Entity Relationship Diagram

```
User (1) ----< Syllabus (1) ----< Subject (1) ----< Topic (1) ----< SubTopic
  |                |                 |                |                |
  |                |                 |                |                |
  +----< ProgressEntry              Resource >-------+----------------+
```

## Tables and Relationships

### User
- **id**: BIGSERIAL PRIMARY KEY
- **username**: VARCHAR(255) NOT NULL UNIQUE
- **email**: VARCHAR(255) NOT NULL UNIQUE
- **password**: VARCHAR(255) NOT NULL
- **role**: VARCHAR(50) NOT NULL
- **email_verified**: BOOLEAN DEFAULT FALSE
- **verification_token**: VARCHAR(255)

### Syllabus
- **id**: BIGSERIAL PRIMARY KEY
- **user_id**: BIGINT NOT NULL REFERENCES users(id)
- **title**: VARCHAR(255) NOT NULL
- **description**: TEXT
- **is_public**: BOOLEAN DEFAULT FALSE
- **shareable_link**: VARCHAR(255)
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP

### Subject
- **id**: BIGSERIAL PRIMARY KEY
- **syllabus_id**: BIGINT NOT NULL REFERENCES syllabi(id)
- **title**: VARCHAR(255) NOT NULL
- **description**: TEXT
- **display_order**: INTEGER NOT NULL
- **target_completion_date**: DATE

### Topic
- **id**: BIGSERIAL PRIMARY KEY
- **subject_id**: BIGINT NOT NULL REFERENCES subjects(id)
- **title**: VARCHAR(255) NOT NULL
- **description**: TEXT
- **display_order**: INTEGER NOT NULL
- **target_completion_date**: DATE

### SubTopic
- **id**: BIGSERIAL PRIMARY KEY
- **topic_id**: BIGINT NOT NULL REFERENCES topics(id)
- **title**: VARCHAR(255) NOT NULL
- **description**: TEXT
- **display_order**: INTEGER NOT NULL
- **target_completion_date**: DATE

### ProgressEntry
- **id**: BIGSERIAL PRIMARY KEY
- **user_id**: BIGINT NOT NULL REFERENCES users(id)
- **item_id**: BIGINT NOT NULL
- **item_type**: VARCHAR(50) NOT NULL
- **date**: DATE NOT NULL
- **status**: VARCHAR(50) NOT NULL
- **time_spent_minutes**: INTEGER
- **notes**: TEXT
- **created_at**: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

### Resource
- **id**: BIGSERIAL PRIMARY KEY
- **subject_id**: BIGINT REFERENCES subjects(id)
- **topic_id**: BIGINT REFERENCES topics(id)
- **subtopic_id**: BIGINT REFERENCES subtopics(id)
- **item_type**: VARCHAR(50) NOT NULL
- **resource_type**: VARCHAR(50) NOT NULL
- **content**: TEXT NOT NULL
- **created_at**: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

## Key Design Aspects

### Hierarchical Structure
The database implements a hierarchical structure for learning content:
- A User can have multiple Syllabi
- Each Syllabus contains multiple Subjects
- Each Subject contains multiple Topics
- Each Topic contains multiple SubTopics

### Progress Tracking
- ProgressEntry records are associated with a specific User
- Each entry references an item (Syllabus, Subject, Topic, or SubTopic) via item_id and item_type
- Status values include: NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED

### Resource Management
- Resources can be attached to Subjects, Topics, or SubTopics
- The constraint ensures a resource is attached to exactly one parent item
- Resource types include: LINK, NOTE, FILE

### Sharing and Visibility
- Syllabi can be marked as public or private
- Public syllabi are visible to all users
- Private syllabi can be shared via a shareable link
