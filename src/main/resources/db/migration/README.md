# Flyway migrations

Flyway is disabled initially because the supplied full database script creates
the complete schema. After the team establishes the baseline database:

1. Create `V1__baseline.sql` from the stable schema without `CREATE DATABASE`.
2. Set `spring.flyway.enabled=true`.
3. Add every later change as a new immutable migration, for example:
   `V2__add_feedback_reply_status.sql`.

Do not edit a migration that has already run on staging.
