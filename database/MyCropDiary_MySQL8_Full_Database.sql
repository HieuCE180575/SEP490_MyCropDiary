-- ============================================================================
-- MyCropDiary - Complete database schema for MySQL 8.0+
-- Scope: Account, plots, seasons, diary, materials, expenses, checklist,
-- reports, AI/RAG, feedback + admin reply, notifications and audit logs.
-- Charset: utf8mb4 | Time zone used by application: Asia/Ho_Chi_Minh
-- ============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS my_crop_diary;
CREATE DATABASE my_crop_diary
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE my_crop_diary;

-- --------------------------------------------------------------------------
-- 1. ACCOUNT AND ACCESS MANAGEMENT
-- --------------------------------------------------------------------------

CREATE TABLE users (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email                 VARCHAR(255) NOT NULL,
  password_hash         VARCHAR(255) NOT NULL,
  full_name             VARCHAR(150) NOT NULL,
  phone_number          VARCHAR(20) NULL,
  avatar_url            VARCHAR(500) NULL,
  address               VARCHAR(500) NULL,
  role                  ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
  status                ENUM('PENDING_VERIFICATION','ACTIVE','LOCKED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
  email_verified_at     DATETIME(3) NULL,
  failed_login_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  locked_until          DATETIME(3) NULL,
  last_login_at         DATETIME(3) NULL,
  created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at            DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_status (role, status),
  KEY idx_users_full_name (full_name),
  CONSTRAINT chk_users_email CHECK (email = LOWER(TRIM(email))),
  CONSTRAINT chk_users_failed_attempts CHECK (failed_login_attempts <= 100)
) ENGINE=InnoDB;

CREATE TABLE email_verification_tokens (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  token_hash  CHAR(64) NOT NULL,
  expires_at  DATETIME(3) NOT NULL,
  used_at     DATETIME(3) NULL,
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_email_verify_token_hash (token_hash),
  KEY idx_email_verify_user_expiry (user_id, expires_at),
  CONSTRAINT fk_email_verify_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  token_hash  CHAR(64) NOT NULL,
  expires_at  DATETIME(3) NOT NULL,
  used_at     DATETIME(3) NULL,
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_password_reset_token_hash (token_hash),
  KEY idx_password_reset_user_expiry (user_id, expires_at),
  CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id       BIGINT UNSIGNED NOT NULL,
  token_hash    CHAR(64) NOT NULL,
  device_info   VARCHAR(500) NULL,
  ip_address    VARCHAR(45) NULL,
  expires_at    DATETIME(3) NOT NULL,
  revoked_at    DATETIME(3) NULL,
  revoke_reason VARCHAR(255) NULL,
  created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  KEY idx_refresh_user_active (user_id, revoked_at, expires_at),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 2. MASTER DATA, LAND PLOTS AND CROP SEASONS
-- --------------------------------------------------------------------------

CREATE TABLE crop_categories (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code              VARCHAR(50) NOT NULL,
  name              VARCHAR(150) NOT NULL,
  scientific_name   VARCHAR(200) NULL,
  description       TEXT NULL,
  default_cycle_days SMALLINT UNSIGNED NULL,
  image_url         VARCHAR(500) NULL,
  status            ENUM('ACTIVE','INACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  created_by        BIGINT UNSIGNED NULL,
  updated_by        BIGINT UNSIGNED NULL,
  created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_crop_categories_code (code),
  UNIQUE KEY uk_crop_categories_name (name),
  KEY idx_crop_categories_status_name (status, name),
  CONSTRAINT fk_crop_category_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_crop_category_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT chk_crop_cycle CHECK (default_cycle_days IS NULL OR default_cycle_days > 0)
) ENGINE=InnoDB;

CREATE TABLE land_plots (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id        BIGINT UNSIGNED NOT NULL,
  plot_code      VARCHAR(50) NOT NULL,
  name           VARCHAR(150) NOT NULL,
  area_value     DECIMAL(14,3) NOT NULL,
  area_unit      ENUM('M2','HECTARE') NOT NULL DEFAULT 'M2',
  address        VARCHAR(500) NULL,
  latitude       DECIMAL(10,7) NULL,
  longitude      DECIMAL(10,7) NULL,
  soil_type      VARCHAR(100) NULL,
  water_source   VARCHAR(150) NULL,
  notes          TEXT NULL,
  status         ENUM('ACTIVE','INACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  archived_at    DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_land_plots_user_code (user_id, plot_code),
  KEY idx_land_plots_user_status (user_id, status),
  KEY idx_land_plots_user_name (user_id, name),
  CONSTRAINT fk_land_plots_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_land_plot_area CHECK (area_value > 0),
  CONSTRAINT chk_land_plot_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
  CONSTRAINT chk_land_plot_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;

CREATE TABLE crop_seasons (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id             BIGINT UNSIGNED NOT NULL,
  land_plot_id        BIGINT UNSIGNED NOT NULL,
  crop_category_id    BIGINT UNSIGNED NOT NULL,
  season_code         VARCHAR(50) NOT NULL,
  name                VARCHAR(180) NOT NULL,
  expected_start_date DATE NOT NULL,
  expected_end_date   DATE NOT NULL,
  actual_start_date   DATE NULL,
  actual_end_date     DATE NULL,
  status              ENUM('PLANNED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLANNED',
  cultivation_method  VARCHAR(255) NULL,
  expected_yield      DECIMAL(14,3) NULL,
  expected_yield_unit ENUM('KG','TON','ITEM','OTHER') NULL,
  notes               TEXT NULL,
  completed_at        DATETIME(3) NULL,
  cancelled_at        DATETIME(3) NULL,
  created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_crop_seasons_user_code (user_id, season_code),
  KEY idx_crop_seasons_user_status (user_id, status),
  KEY idx_crop_seasons_plot_status (land_plot_id, status),
  KEY idx_crop_seasons_crop (crop_category_id),
  KEY idx_crop_seasons_dates (expected_start_date, expected_end_date),
  CONSTRAINT fk_crop_seasons_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_crop_seasons_plot FOREIGN KEY (land_plot_id) REFERENCES land_plots(id) ON DELETE RESTRICT,
  CONSTRAINT fk_crop_seasons_crop FOREIGN KEY (crop_category_id) REFERENCES crop_categories(id) ON DELETE RESTRICT,
  CONSTRAINT chk_season_expected_dates CHECK (expected_end_date >= expected_start_date),
  CONSTRAINT chk_season_actual_dates CHECK (actual_end_date IS NULL OR actual_start_date IS NULL OR actual_end_date >= actual_start_date),
  CONSTRAINT chk_season_expected_yield CHECK (expected_yield IS NULL OR expected_yield > 0)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 3. CULTIVATION DIARY, HARVEST, MATERIAL USAGE AND EXPENSES
-- --------------------------------------------------------------------------

CREATE TABLE farming_activities (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  season_id            BIGINT UNSIGNED NOT NULL,
  user_id              BIGINT UNSIGNED NOT NULL,
  activity_type        ENUM('PLANTING','FERTILIZING','SPRAYING','WATERING','CARE','HARVESTING','OTHER') NOT NULL,
  activity_date        DATE NOT NULL,
  title                VARCHAR(180) NOT NULL,
  description          TEXT NULL,
  responsible_person   VARCHAR(150) NULL,
  weather_conditions   VARCHAR(255) NULL,
  duration_minutes     INT UNSIGNED NULL,
  labor_count          SMALLINT UNSIGNED NULL,
  status               ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  created_at           DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at           DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at           DATETIME(3) NULL,
  PRIMARY KEY (id),
  KEY idx_activities_season_date (season_id, activity_date DESC),
  KEY idx_activities_user_type (user_id, activity_type),
  KEY idx_activities_status (season_id, status),
  FULLTEXT KEY ftx_activities_search (title, description, responsible_person),
  CONSTRAINT fk_activities_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_activity_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0),
  CONSTRAINT chk_activity_labor CHECK (labor_count IS NULL OR labor_count > 0)
) ENGINE=InnoDB;

CREATE TABLE harvest_records (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id     BIGINT UNSIGNED NOT NULL,
  season_id       BIGINT UNSIGNED NOT NULL,
  quantity        DECIMAL(14,3) NOT NULL,
  unit            ENUM('KG','TON','ITEM','OTHER') NOT NULL DEFAULT 'KG',
  quality_grade   VARCHAR(100) NULL,
  batch_code      VARCHAR(80) NULL,
  destination     VARCHAR(255) NULL,
  notes           TEXT NULL,
  created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_harvest_activity (activity_id),
  KEY idx_harvest_season (season_id),
  KEY idx_harvest_batch (batch_code),
  CONSTRAINT fk_harvest_activity FOREIGN KEY (activity_id) REFERENCES farming_activities(id) ON DELETE CASCADE,
  CONSTRAINT fk_harvest_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT chk_harvest_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE material_usages (
  id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  season_id              BIGINT UNSIGNED NOT NULL,
  activity_id            BIGINT UNSIGNED NULL,
  user_id                BIGINT UNSIGNED NOT NULL,
  material_name          VARCHAR(180) NOT NULL,
  material_type          ENUM('SEED','FERTILIZER','PESTICIDE','OTHER') NOT NULL,
  quantity               DECIMAL(14,3) NOT NULL,
  unit                   VARCHAR(40) NOT NULL,
  usage_date             DATE NOT NULL,
  purpose                VARCHAR(500) NULL,
  responsible_person     VARCHAR(150) NULL,
  supplier               VARCHAR(180) NULL,
  lot_number             VARCHAR(100) NULL,
  quarantine_period_days SMALLINT UNSIGNED NULL,
  quarantine_na_reason   VARCHAR(500) NULL,
  notes                  TEXT NULL,
  status                 ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  created_at             DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at             DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at             DATETIME(3) NULL,
  PRIMARY KEY (id),
  KEY idx_materials_season_date (season_id, usage_date DESC),
  KEY idx_materials_activity (activity_id),
  KEY idx_materials_user_type (user_id, material_type),
  FULLTEXT KEY ftx_materials_search (material_name, purpose, responsible_person),
  CONSTRAINT fk_materials_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT fk_materials_activity FOREIGN KEY (activity_id) REFERENCES farming_activities(id) ON DELETE SET NULL,
  CONSTRAINT fk_materials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_material_quantity CHECK (quantity > 0),
  CONSTRAINT chk_pesticide_quarantine CHECK (
    material_type <> 'PESTICIDE'
    OR quarantine_period_days IS NOT NULL
    OR NULLIF(TRIM(quarantine_na_reason), '') IS NOT NULL
  )
) ENGINE=InnoDB;

CREATE TABLE expenses (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  season_id        BIGINT UNSIGNED NOT NULL,
  activity_id      BIGINT UNSIGNED NULL,
  material_usage_id BIGINT UNSIGNED NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  category         ENUM('SEED','FERTILIZER','PESTICIDE','LABOR','WATER','EQUIPMENT','TRANSPORT','OTHER') NOT NULL,
  expense_date     DATE NOT NULL,
  description      VARCHAR(500) NOT NULL,
  amount           DECIMAL(18,2) NOT NULL,
  currency_code    CHAR(3) NOT NULL DEFAULT 'VND',
  vendor_name      VARCHAR(180) NULL,
  invoice_number   VARCHAR(100) NULL,
  receipt_url      VARCHAR(500) NULL,
  notes            TEXT NULL,
  status           ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at       DATETIME(3) NULL,
  PRIMARY KEY (id),
  KEY idx_expenses_season_date (season_id, expense_date DESC),
  KEY idx_expenses_season_category (season_id, category, status),
  KEY idx_expenses_activity (activity_id),
  KEY idx_expenses_material (material_usage_id),
  KEY idx_expenses_user (user_id),
  FULLTEXT KEY ftx_expenses_search (description, vendor_name, invoice_number),
  CONSTRAINT fk_expenses_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT fk_expenses_activity FOREIGN KEY (activity_id) REFERENCES farming_activities(id) ON DELETE SET NULL,
  CONSTRAINT fk_expenses_material FOREIGN KEY (material_usage_id) REFERENCES material_usages(id) ON DELETE SET NULL,
  CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_expense_amount CHECK (amount > 0),
  CONSTRAINT chk_expense_currency CHECK (currency_code = UPPER(currency_code))
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 4. VIETGAP/GAP-ORIENTED CHECKLIST
-- Rules are versioned. A run stores a snapshot so later rule changes do not
-- rewrite historical checklist results.
-- --------------------------------------------------------------------------

CREATE TABLE checklist_rules (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  rule_code           VARCHAR(80) NOT NULL,
  version_no          INT UNSIGNED NOT NULL DEFAULT 1,
  crop_category_id    BIGINT UNSIGNED NULL,
  activity_type       ENUM('PLANTING','FERTILIZING','SPRAYING','WATERING','CARE','HARVESTING','OTHER') NULL,
  field_code          VARCHAR(100) NOT NULL,
  title               VARCHAR(255) NOT NULL,
  description         TEXT NULL,
  requirement_level   ENUM('REQUIRED','RECOMMENDED','NOT_APPLICABLE') NOT NULL,
  evaluation_type     ENUM('FIELD_NOT_NULL','MIN_COUNT','CONDITIONAL','MANUAL') NOT NULL DEFAULT 'FIELD_NOT_NULL',
  evaluation_config   JSON NULL,
  display_order       INT UNSIGNED NOT NULL DEFAULT 0,
  status              ENUM('DRAFT','ACTIVE','INACTIVE','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  effective_from      DATETIME(3) NULL,
  effective_to        DATETIME(3) NULL,
  created_by          BIGINT UNSIGNED NOT NULL,
  updated_by          BIGINT UNSIGNED NULL,
  created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_checklist_rule_version (rule_code, version_no),
  KEY idx_checklist_rules_active (status, crop_category_id, activity_type),
  KEY idx_checklist_rules_field (field_code),
  CONSTRAINT fk_checklist_rule_crop FOREIGN KEY (crop_category_id) REFERENCES crop_categories(id) ON DELETE RESTRICT,
  CONSTRAINT fk_checklist_rule_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_checklist_rule_updater FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT chk_checklist_version CHECK (version_no > 0),
  CONSTRAINT chk_checklist_effective_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to > effective_from),
  CONSTRAINT chk_checklist_config_json CHECK (evaluation_config IS NULL OR JSON_VALID(evaluation_config))
) ENGINE=InnoDB;

CREATE TABLE checklist_runs (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  season_id             BIGINT UNSIGNED NOT NULL,
  run_by                 BIGINT UNSIGNED NOT NULL,
  total_items            INT UNSIGNED NOT NULL DEFAULT 0,
  required_items         INT UNSIGNED NOT NULL DEFAULT 0,
  completed_required     INT UNSIGNED NOT NULL DEFAULT 0,
  completion_percentage DECIMAL(5,2) NOT NULL DEFAULT 0,
  summary_status         ENUM('COMPLETE','INCOMPLETE') NOT NULL DEFAULT 'INCOMPLETE',
  created_at             DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_checklist_runs_season_time (season_id, created_at DESC),
  CONSTRAINT fk_checklist_run_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT fk_checklist_run_user FOREIGN KEY (run_by) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_checklist_percentage CHECK (completion_percentage BETWEEN 0 AND 100),
  CONSTRAINT chk_checklist_counts CHECK (completed_required <= required_items AND required_items <= total_items)
) ENGINE=InnoDB;

CREATE TABLE checklist_results (
  id                         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  checklist_run_id           BIGINT UNSIGNED NOT NULL,
  checklist_rule_id          BIGINT UNSIGNED NULL,
  rule_code_snapshot         VARCHAR(80) NOT NULL,
  rule_version_snapshot      INT UNSIGNED NOT NULL,
  title_snapshot             VARCHAR(255) NOT NULL,
  requirement_level_snapshot ENUM('REQUIRED','RECOMMENDED','NOT_APPLICABLE') NOT NULL,
  item_status                ENUM('COMPLETED','MISSING','RECOMMENDED_MISSING','NOT_APPLICABLE') NOT NULL,
  detected_value             TEXT NULL,
  missing_reason             VARCHAR(500) NULL,
  correction_path            VARCHAR(500) NULL,
  created_at                 DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_checklist_result_rule (checklist_run_id, rule_code_snapshot),
  KEY idx_checklist_results_status (checklist_run_id, item_status),
  CONSTRAINT fk_checklist_result_run FOREIGN KEY (checklist_run_id) REFERENCES checklist_runs(id) ON DELETE CASCADE,
  CONSTRAINT fk_checklist_result_rule FOREIGN KEY (checklist_rule_id) REFERENCES checklist_rules(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 5. SEASON REPORTS
-- --------------------------------------------------------------------------

CREATE TABLE season_reports (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  season_id         BIGINT UNSIGNED NOT NULL,
  checklist_run_id  BIGINT UNSIGNED NULL,
  requested_by      BIGINT UNSIGNED NOT NULL,
  report_code       VARCHAR(80) NOT NULL,
  report_version    INT UNSIGNED NOT NULL DEFAULT 1,
  status            ENUM('QUEUED','GENERATING','COMPLETED','FAILED') NOT NULL DEFAULT 'QUEUED',
  file_name         VARCHAR(255) NULL,
  file_url          VARCHAR(700) NULL,
  file_hash         CHAR(64) NULL,
  generated_at      DATETIME(3) NULL,
  failure_message   VARCHAR(1000) NULL,
  created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_season_report_code (report_code),
  UNIQUE KEY uk_season_report_version (season_id, report_version),
  KEY idx_season_reports_status (status, created_at),
  CONSTRAINT fk_season_report_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE CASCADE,
  CONSTRAINT fk_season_report_checklist FOREIGN KEY (checklist_run_id) REFERENCES checklist_runs(id) ON DELETE SET NULL,
  CONSTRAINT fk_season_report_requester FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_report_version CHECK (report_version > 0)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 6. KNOWLEDGE BASE AND AI ASSISTANT
-- --------------------------------------------------------------------------

CREATE TABLE knowledge_categories (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code        VARCHAR(50) NOT NULL,
  name        VARCHAR(150) NOT NULL,
  description VARCHAR(500) NULL,
  status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_knowledge_category_code (code),
  UNIQUE KEY uk_knowledge_category_name (name)
) ENGINE=InnoDB;

CREATE TABLE knowledge_articles (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_id           BIGINT UNSIGNED NULL,
  article_code          VARCHAR(80) NOT NULL,
  title                 VARCHAR(255) NOT NULL,
  summary               TEXT NULL,
  content               LONGTEXT NOT NULL,
  source_name           VARCHAR(255) NULL,
  source_url            VARCHAR(700) NULL,
  tags                  JSON NULL,
  status                ENUM('DRAFT','APPROVED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  version_no            INT UNSIGNED NOT NULL DEFAULT 1,
  created_by            BIGINT UNSIGNED NOT NULL,
  updated_by            BIGINT UNSIGNED NULL,
  approved_by           BIGINT UNSIGNED NULL,
  approved_at           DATETIME(3) NULL,
  created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_knowledge_article_code_version (article_code, version_no),
  KEY idx_knowledge_articles_status_category (status, category_id),
  FULLTEXT KEY ftx_knowledge_articles (title, summary, content),
  CONSTRAINT fk_knowledge_article_category FOREIGN KEY (category_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL,
  CONSTRAINT fk_knowledge_article_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_knowledge_article_updater FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_knowledge_article_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT chk_knowledge_article_version CHECK (version_no > 0),
  CONSTRAINT chk_knowledge_article_tags CHECK (tags IS NULL OR JSON_VALID(tags)),
  CONSTRAINT chk_knowledge_article_approval CHECK (
    status <> 'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE ai_conversations (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  season_id   BIGINT UNSIGNED NULL,
  title       VARCHAR(255) NULL,
  status      ENUM('ACTIVE','ARCHIVED','DELETED') NOT NULL DEFAULT 'ACTIVE',
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_conversations_user_time (user_id, updated_at DESC),
  KEY idx_ai_conversations_season (season_id),
  CONSTRAINT fk_ai_conversation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_ai_conversation_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE ai_messages (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id     BIGINT UNSIGNED NOT NULL,
  role                ENUM('USER','ASSISTANT','SYSTEM') NOT NULL,
  message_type        ENUM('QUESTION','ANSWER','FIELD_EXPLANATION','CHECKLIST_GUIDANCE','FALLBACK') NOT NULL DEFAULT 'QUESTION',
  content             LONGTEXT NOT NULL,
  model_name          VARCHAR(100) NULL,
  prompt_tokens       INT UNSIGNED NULL,
  completion_tokens   INT UNSIGNED NULL,
  response_time_ms    INT UNSIGNED NULL,
  grounding_status    ENUM('NOT_REQUIRED','GROUNDED','NO_SOURCE','FAILED') NOT NULL DEFAULT 'NOT_REQUIRED',
  safety_status       ENUM('SAFE','BLOCKED','REVIEW_REQUIRED') NOT NULL DEFAULT 'SAFE',
  request_id          VARCHAR(100) NULL,
  created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_message_request (request_id),
  KEY idx_ai_messages_conversation_time (conversation_id, created_at),
  KEY idx_ai_messages_grounding (grounding_status, created_at),
  CONSTRAINT fk_ai_message_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ai_message_sources (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ai_message_id         BIGINT UNSIGNED NOT NULL,
  knowledge_article_id  BIGINT UNSIGNED NULL,
  article_title_snapshot VARCHAR(255) NOT NULL,
  article_version_snapshot INT UNSIGNED NOT NULL,
  source_url_snapshot   VARCHAR(700) NULL,
  excerpt               TEXT NULL,
  relevance_score       DECIMAL(6,5) NULL,
  created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_message_source (ai_message_id, knowledge_article_id),
  CONSTRAINT fk_ai_source_message FOREIGN KEY (ai_message_id) REFERENCES ai_messages(id) ON DELETE CASCADE,
  CONSTRAINT fk_ai_source_article FOREIGN KEY (knowledge_article_id) REFERENCES knowledge_articles(id) ON DELETE SET NULL,
  CONSTRAINT chk_ai_relevance CHECK (relevance_score IS NULL OR relevance_score BETWEEN 0 AND 1)
) ENGINE=InnoDB;

CREATE TABLE ai_draft_extractions (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL,
  season_id         BIGINT UNSIGNED NULL,
  source_message_id BIGINT UNSIGNED NULL,
  target_form       ENUM('ACTIVITY','MATERIAL_USAGE','EXPENSE','SEASON') NOT NULL,
  original_text     TEXT NOT NULL,
  extracted_data    JSON NOT NULL,
  confidence_data   JSON NULL,
  status            ENUM('DRAFT','CONFIRMED','DISCARDED','EXPIRED') NOT NULL DEFAULT 'DRAFT',
  confirmed_at      DATETIME(3) NULL,
  expires_at        DATETIME(3) NULL,
  created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_drafts_user_status (user_id, status, created_at DESC),
  KEY idx_ai_drafts_season (season_id),
  CONSTRAINT fk_ai_draft_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_ai_draft_season FOREIGN KEY (season_id) REFERENCES crop_seasons(id) ON DELETE SET NULL,
  CONSTRAINT fk_ai_draft_message FOREIGN KEY (source_message_id) REFERENCES ai_messages(id) ON DELETE SET NULL,
  CONSTRAINT chk_ai_draft_data CHECK (JSON_VALID(extracted_data)),
  CONSTRAINT chk_ai_confidence_data CHECK (confidence_data IS NULL OR JSON_VALID(confidence_data))
) ENGINE=InnoDB;

CREATE TABLE ai_feedback (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL,
  ai_message_id         BIGINT UNSIGNED NOT NULL,
  rating                ENUM('HELPFUL','NOT_HELPFUL') NOT NULL,
  comment               TEXT NULL,
  review_status         ENUM('NEW','IN_REVIEW','RESOLVED','CLOSED') NOT NULL DEFAULT 'NEW',
  assigned_admin_id     BIGINT UNSIGNED NULL,
  resolution_note       TEXT NULL,
  resolved_at           DATETIME(3) NULL,
  created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_feedback_user_message (user_id, ai_message_id),
  KEY idx_ai_feedback_review (review_status, rating, created_at DESC),
  KEY idx_ai_feedback_admin (assigned_admin_id, review_status),
  CONSTRAINT fk_ai_feedback_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_ai_feedback_message FOREIGN KEY (ai_message_id) REFERENCES ai_messages(id) ON DELETE CASCADE,
  CONSTRAINT fk_ai_feedback_admin FOREIGN KEY (assigned_admin_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Added to support the approved extension: Administrator can reply to feedback.
CREATE TABLE ai_feedback_replies (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  feedback_id BIGINT UNSIGNED NOT NULL,
  admin_id    BIGINT UNSIGNED NOT NULL,
  content     TEXT NOT NULL,
  status      ENUM('VISIBLE','HIDDEN') NOT NULL DEFAULT 'VISIBLE',
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_feedback_replies_feedback_time (feedback_id, created_at),
  KEY idx_feedback_replies_admin (admin_id),
  CONSTRAINT fk_feedback_reply_feedback FOREIGN KEY (feedback_id) REFERENCES ai_feedback(id) ON DELETE CASCADE,
  CONSTRAINT fk_feedback_reply_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_feedback_reply_content CHECK (CHAR_LENGTH(TRIM(content)) BETWEEN 1 AND 5000)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 7. NOTIFICATIONS, CONFIGURATION AND AUDIT
-- --------------------------------------------------------------------------

CREATE TABLE notifications (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id        BIGINT UNSIGNED NOT NULL,
  type           ENUM('ACCOUNT','SEASON','CHECKLIST','REPORT','AI_FEEDBACK_REPLY','SYSTEM') NOT NULL,
  title          VARCHAR(255) NOT NULL,
  content        VARCHAR(1000) NOT NULL,
  reference_type VARCHAR(80) NULL,
  reference_id   BIGINT UNSIGNED NULL,
  action_url     VARCHAR(500) NULL,
  read_at        DATETIME(3) NULL,
  created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_notifications_user_unread (user_id, read_at, created_at DESC),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE system_settings (
  setting_key    VARCHAR(100) NOT NULL,
  setting_value  JSON NOT NULL,
  description    VARCHAR(500) NULL,
  is_public      BOOLEAN NOT NULL DEFAULT FALSE,
  updated_by     BIGINT UNSIGNED NULL,
  updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (setting_key),
  CONSTRAINT fk_system_setting_admin FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT chk_system_setting_json CHECK (JSON_VALID(setting_value))
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_user_id  BIGINT UNSIGNED NULL,
  action         VARCHAR(100) NOT NULL,
  entity_type    VARCHAR(100) NOT NULL,
  entity_id      VARCHAR(100) NULL,
  old_values     JSON NULL,
  new_values     JSON NULL,
  ip_address     VARCHAR(45) NULL,
  user_agent     VARCHAR(700) NULL,
  request_id     VARCHAR(100) NULL,
  created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_audit_actor_time (actor_user_id, created_at DESC),
  KEY idx_audit_entity_time (entity_type, entity_id, created_at DESC),
  KEY idx_audit_action_time (action, created_at DESC),
  KEY idx_audit_request (request_id),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT chk_audit_old_json CHECK (old_values IS NULL OR JSON_VALID(old_values)),
  CONSTRAINT chk_audit_new_json CHECK (new_values IS NULL OR JSON_VALID(new_values))
) ENGINE=InnoDB;

-- --------------------------------------------------------------------------
-- 8. VIEWS FOR DASHBOARD, COST, HARVEST AND ADMIN STATISTICS
-- --------------------------------------------------------------------------

CREATE OR REPLACE VIEW vw_season_cost_summary AS
SELECT
  s.id AS season_id,
  s.user_id,
  s.name AS season_name,
  s.status AS season_status,
  COALESCE(SUM(CASE WHEN e.status = 'ACTIVE' THEN e.amount ELSE 0 END), 0) AS total_cost,
  COUNT(CASE WHEN e.status = 'ACTIVE' THEN 1 END) AS expense_count,
  MAX(e.updated_at) AS last_expense_updated_at
FROM crop_seasons s
LEFT JOIN expenses e ON e.season_id = s.id
GROUP BY s.id, s.user_id, s.name, s.status;

CREATE OR REPLACE VIEW vw_season_cost_by_category AS
SELECT
  e.season_id,
  e.category,
  COUNT(*) AS expense_count,
  SUM(e.amount) AS total_amount
FROM expenses e
WHERE e.status = 'ACTIVE'
GROUP BY e.season_id, e.category;

CREATE OR REPLACE VIEW vw_season_harvest_summary AS
SELECT
  h.season_id,
  h.unit,
  COUNT(*) AS harvest_count,
  SUM(h.quantity) AS total_quantity,
  MIN(a.activity_date) AS first_harvest_date,
  MAX(a.activity_date) AS last_harvest_date
FROM harvest_records h
JOIN farming_activities a ON a.id = h.activity_id AND a.status = 'ACTIVE'
GROUP BY h.season_id, h.unit;

CREATE OR REPLACE VIEW vw_latest_checklist_run AS
SELECT cr.*
FROM checklist_runs cr
JOIN (
  SELECT season_id, MAX(id) AS latest_id
  FROM checklist_runs
  GROUP BY season_id
) latest ON latest.latest_id = cr.id;

CREATE OR REPLACE VIEW vw_season_dashboard AS
SELECT
  s.id AS season_id,
  s.user_id,
  s.name AS season_name,
  s.status,
  s.expected_start_date,
  s.expected_end_date,
  p.id AS land_plot_id,
  p.name AS land_plot_name,
  c.id AS crop_category_id,
  c.name AS crop_name,
  COUNT(DISTINCT CASE WHEN a.status = 'ACTIVE' THEN a.id END) AS activity_count,
  COUNT(DISTINCT CASE WHEN m.status = 'ACTIVE' THEN m.id END) AS material_usage_count,
  COALESCE(cost.total_cost, 0) AS total_cost,
  checklist.completion_percentage AS checklist_completion_percentage,
  checklist.summary_status AS checklist_status
FROM crop_seasons s
JOIN land_plots p ON p.id = s.land_plot_id
JOIN crop_categories c ON c.id = s.crop_category_id
LEFT JOIN farming_activities a ON a.season_id = s.id
LEFT JOIN material_usages m ON m.season_id = s.id
LEFT JOIN vw_season_cost_summary cost ON cost.season_id = s.id
LEFT JOIN vw_latest_checklist_run checklist ON checklist.season_id = s.id
GROUP BY s.id, s.user_id, s.name, s.status, s.expected_start_date, s.expected_end_date,
         p.id, p.name, c.id, c.name, cost.total_cost,
         checklist.completion_percentage, checklist.summary_status;

CREATE OR REPLACE VIEW vw_admin_system_statistics AS
SELECT
  (SELECT COUNT(*) FROM users WHERE deleted_at IS NULL) AS total_users,
  (SELECT COUNT(*) FROM users WHERE role = 'USER' AND status = 'ACTIVE' AND deleted_at IS NULL) AS active_users,
  (SELECT COUNT(*) FROM users WHERE status = 'LOCKED' AND deleted_at IS NULL) AS locked_users,
  (SELECT COUNT(*) FROM land_plots WHERE status <> 'ARCHIVED') AS active_plots,
  (SELECT COUNT(*) FROM crop_seasons) AS total_seasons,
  (SELECT COUNT(*) FROM crop_seasons WHERE status = 'IN_PROGRESS') AS in_progress_seasons,
  (SELECT COUNT(*) FROM farming_activities WHERE status = 'ACTIVE') AS total_activities,
  (SELECT COALESCE(SUM(amount),0) FROM expenses WHERE status = 'ACTIVE') AS total_recorded_cost,
  (SELECT COUNT(*) FROM season_reports WHERE status = 'COMPLETED') AS completed_reports,
  (SELECT COUNT(*) FROM ai_messages WHERE role = 'USER') AS ai_questions,
  (SELECT COUNT(*) FROM ai_feedback) AS ai_feedback_count,
  (SELECT COUNT(*) FROM ai_feedback WHERE review_status IN ('NEW','IN_REVIEW')) AS pending_ai_feedback;

-- --------------------------------------------------------------------------
-- 9. TRIGGERS FOR CROSS-TABLE OWNERSHIP AND CORE BUSINESS RULES
-- Application validation remains mandatory; triggers are a final safety layer.
-- --------------------------------------------------------------------------

DELIMITER $$

CREATE TRIGGER trg_crop_season_before_insert
BEFORE INSERT ON crop_seasons
FOR EACH ROW
BEGIN
  DECLARE v_plot_owner BIGINT UNSIGNED;
  DECLARE v_plot_status VARCHAR(20);
  DECLARE v_crop_status VARCHAR(20);
  DECLARE v_in_progress_count INT DEFAULT 0;

  SELECT user_id, status INTO v_plot_owner, v_plot_status
  FROM land_plots WHERE id = NEW.land_plot_id;

  SELECT status INTO v_crop_status
  FROM crop_categories WHERE id = NEW.crop_category_id;

  IF v_plot_owner IS NULL OR v_plot_owner <> NEW.user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Season owner must own the selected land plot.';
  END IF;
  IF v_plot_status <> 'ACTIVE' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Selected land plot is not active.';
  END IF;
  IF v_crop_status <> 'ACTIVE' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Selected crop category is not active.';
  END IF;
  IF NEW.status = 'IN_PROGRESS' THEN
    SELECT COUNT(*) INTO v_in_progress_count
    FROM crop_seasons
    WHERE land_plot_id = NEW.land_plot_id AND status = 'IN_PROGRESS';
    IF v_in_progress_count > 0 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This plot already has an in-progress crop season.';
    END IF;
  END IF;
END$$

CREATE TRIGGER trg_crop_season_before_update
BEFORE UPDATE ON crop_seasons
FOR EACH ROW
BEGIN
  DECLARE v_in_progress_count INT DEFAULT 0;
  IF NEW.status = 'IN_PROGRESS' AND (OLD.status <> 'IN_PROGRESS' OR OLD.land_plot_id <> NEW.land_plot_id) THEN
    SELECT COUNT(*) INTO v_in_progress_count
    FROM crop_seasons
    WHERE land_plot_id = NEW.land_plot_id
      AND status = 'IN_PROGRESS'
      AND id <> NEW.id;
    IF v_in_progress_count > 0 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This plot already has an in-progress crop season.';
    END IF;
  END IF;
  IF NEW.status = 'COMPLETED' AND NEW.actual_end_date IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed season requires an actual end date.';
  END IF;
  IF OLD.status IN ('COMPLETED','CANCELLED') AND NEW.status <> OLD.status THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed or cancelled season status cannot be changed.';
  END IF;
  IF NEW.status = 'COMPLETED' AND OLD.status <> 'COMPLETED' THEN
    SET NEW.completed_at = CURRENT_TIMESTAMP(3);
  END IF;
  IF NEW.status = 'CANCELLED' AND OLD.status <> 'CANCELLED' THEN
    SET NEW.cancelled_at = CURRENT_TIMESTAMP(3);
  END IF;
END$$

CREATE TRIGGER trg_activity_before_insert
BEFORE INSERT ON farming_activities
FOR EACH ROW
BEGIN
  DECLARE v_owner BIGINT UNSIGNED;
  DECLARE v_status VARCHAR(20);
  DECLARE v_start DATE;
  DECLARE v_end DATE;
  SELECT user_id, status, COALESCE(actual_start_date, expected_start_date),
         COALESCE(actual_end_date, expected_end_date)
    INTO v_owner, v_status, v_start, v_end
  FROM crop_seasons WHERE id = NEW.season_id;
  IF v_owner <> NEW.user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Activity owner must own the selected season.';
  END IF;
  IF v_status IN ('COMPLETED','CANCELLED') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed or cancelled seasons cannot accept new records.';
  END IF;
  IF NEW.activity_date < v_start OR NEW.activity_date > v_end THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Activity date is outside the permitted season period.';
  END IF;
END$$

CREATE TRIGGER trg_material_before_insert
BEFORE INSERT ON material_usages
FOR EACH ROW
BEGIN
  DECLARE v_owner BIGINT UNSIGNED;
  DECLARE v_status VARCHAR(20);
  SELECT user_id, status INTO v_owner, v_status FROM crop_seasons WHERE id = NEW.season_id;
  IF v_owner <> NEW.user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Material usage owner must own the selected season.';
  END IF;
  IF v_status IN ('COMPLETED','CANCELLED') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed or cancelled seasons cannot accept new records.';
  END IF;
END$$

CREATE TRIGGER trg_expense_before_insert
BEFORE INSERT ON expenses
FOR EACH ROW
BEGIN
  DECLARE v_owner BIGINT UNSIGNED;
  DECLARE v_status VARCHAR(20);
  SELECT user_id, status INTO v_owner, v_status FROM crop_seasons WHERE id = NEW.season_id;
  IF v_owner <> NEW.user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Expense owner must own the selected season.';
  END IF;
  IF v_status IN ('COMPLETED','CANCELLED') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed or cancelled seasons cannot accept new records.';
  END IF;
END$$

CREATE TRIGGER trg_feedback_reply_after_insert
AFTER INSERT ON ai_feedback_replies
FOR EACH ROW
BEGIN
  DECLARE v_feedback_owner BIGINT UNSIGNED;
  SELECT user_id INTO v_feedback_owner FROM ai_feedback WHERE id = NEW.feedback_id;
  INSERT INTO notifications(user_id, type, title, content, reference_type, reference_id, action_url)
  VALUES (
    v_feedback_owner,
    'AI_FEEDBACK_REPLY',
    'Quản trị viên đã phản hồi góp ý của bạn',
    LEFT(NEW.content, 1000),
    'AI_FEEDBACK',
    NEW.feedback_id,
    CONCAT('/ai/feedback/', NEW.feedback_id)
  );
END$$

DELIMITER ;

-- --------------------------------------------------------------------------
-- 10. STORED PROCEDURES
-- --------------------------------------------------------------------------

DELIMITER $$

CREATE PROCEDURE sp_update_season_status (
  IN p_season_id BIGINT UNSIGNED,
  IN p_user_id BIGINT UNSIGNED,
  IN p_new_status VARCHAR(20),
  IN p_actual_date DATE
)
BEGIN
  DECLARE v_owner BIGINT UNSIGNED;
  DECLARE v_old_status VARCHAR(20);
  DECLARE v_expected_start DATE;

  START TRANSACTION;
  SELECT user_id, status, expected_start_date
    INTO v_owner, v_old_status, v_expected_start
  FROM crop_seasons
  WHERE id = p_season_id
  FOR UPDATE;

  IF v_owner IS NULL OR v_owner <> p_user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Season not found or access denied.';
  END IF;
  IF NOT (
    (v_old_status = 'PLANNED' AND p_new_status IN ('IN_PROGRESS','CANCELLED')) OR
    (v_old_status = 'IN_PROGRESS' AND p_new_status IN ('COMPLETED','CANCELLED'))
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid season status transition.';
  END IF;

  IF p_new_status = 'IN_PROGRESS' THEN
    UPDATE crop_seasons
    SET status = 'IN_PROGRESS', actual_start_date = COALESCE(p_actual_date, CURRENT_DATE)
    WHERE id = p_season_id;
  ELSEIF p_new_status = 'COMPLETED' THEN
    IF p_actual_date IS NULL THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Actual end date is required.';
    END IF;
    UPDATE crop_seasons SET status = 'COMPLETED', actual_end_date = p_actual_date
    WHERE id = p_season_id;
  ELSE
    UPDATE crop_seasons SET status = 'CANCELLED' WHERE id = p_season_id;
  END IF;
  COMMIT;
END$$

CREATE PROCEDURE sp_lock_user (
  IN p_target_user_id BIGINT UNSIGNED,
  IN p_admin_id BIGINT UNSIGNED,
  IN p_reason VARCHAR(255)
)
BEGIN
  DECLARE v_admin_role VARCHAR(10);
  SELECT role INTO v_admin_role FROM users WHERE id = p_admin_id AND status = 'ACTIVE';
  IF v_admin_role <> 'ADMIN' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only an active administrator can lock users.';
  END IF;
  START TRANSACTION;
  UPDATE users SET status = 'LOCKED', locked_until = NULL WHERE id = p_target_user_id AND role = 'USER';
  UPDATE refresh_tokens
  SET revoked_at = CURRENT_TIMESTAMP(3), revoke_reason = COALESCE(p_reason, 'Account locked by administrator')
  WHERE user_id = p_target_user_id AND revoked_at IS NULL;
  INSERT INTO audit_logs(actor_user_id, action, entity_type, entity_id, new_values)
  VALUES (p_admin_id, 'LOCK_USER', 'USER', CAST(p_target_user_id AS CHAR),
          JSON_OBJECT('status','LOCKED','reason',p_reason));
  COMMIT;
END$$

DELIMITER ;

-- --------------------------------------------------------------------------
-- 11. INITIAL MASTER DATA
-- Passwords are intentionally not seeded. Create the first admin through a
-- secure deployment/bootstrap process using BCrypt or Argon2.
-- --------------------------------------------------------------------------

INSERT INTO crop_categories(code, name, scientific_name, default_cycle_days, status) VALUES
('LEAFY_VEGETABLE', 'Rau ăn lá', NULL, 35, 'ACTIVE'),
('FRUIT_VEGETABLE', 'Rau ăn quả', NULL, 90, 'ACTIVE'),
('ROOT_VEGETABLE', 'Rau ăn củ', NULL, 75, 'ACTIVE'),
('HERB', 'Rau gia vị', NULL, 50, 'ACTIVE');

INSERT INTO knowledge_categories(code, name, description) VALUES
('DIARY_GUIDE', 'Hướng dẫn ghi nhật ký', 'Giải thích các trường và cách ghi nhật ký canh tác.'),
('MATERIAL_GUIDE', 'Hướng dẫn sử dụng vật tư', 'Nội dung được phê duyệt liên quan đến ghi nhận vật tư.'),
('CHECKLIST_GUIDE', 'Hướng dẫn checklist', 'Giải thích các mục dữ liệu bắt buộc và khuyến nghị.'),
('SYSTEM_GUIDE', 'Hướng dẫn sử dụng hệ thống', 'Hướng dẫn thao tác MyCropDiary.');

INSERT INTO system_settings(setting_key, setting_value, description, is_public) VALUES
('APP_TIMEZONE', JSON_QUOTE('Asia/Ho_Chi_Minh'), 'Application display time zone', TRUE),
('DEFAULT_CURRENCY', JSON_QUOTE('VND'), 'Default expense currency', TRUE),
('AI_TIMEOUT_SECONDS', CAST(20 AS JSON), 'Maximum AI request duration', FALSE),
('MAX_LOGIN_ATTEMPTS', CAST(5 AS JSON), 'Failed login attempts before protection applies', FALSE),
('REPORT_DISCLAIMER', JSON_QUOTE('Báo cáo hỗ trợ chuẩn bị hồ sơ và không phải chứng nhận VietGAP/GLOBALG.A.P.'), 'Required report disclaimer', TRUE);

SET FOREIGN_KEY_CHECKS = 1;

-- End of MyCropDiary schema.
