-- Создание схемы
CREATE SCHEMA IF NOT EXISTS s408402;

-- Создание последовательности для users
CREATE SEQUENCE IF NOT EXISTS s408402.users_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Создание таблицы users
CREATE TABLE IF NOT EXISTS s408402.users
(
    id bigint NOT NULL DEFAULT nextval('s408402.users_id_seq'::regclass),
    username character varying(255),
    password character varying(255),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT username_unique UNIQUE (username)
    );

-- Создание последовательности для results_table
CREATE SEQUENCE IF NOT EXISTS s408402.results_table_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Создание таблицы results_table
CREATE TABLE IF NOT EXISTS s408402.results_table
(
    id bigint NOT NULL DEFAULT nextval('s408402.results_table_id_seq'::regclass),
    x integer,
    y double precision,
    r integer,
    result boolean,
    user_id bigint NOT NULL,
    CONSTRAINT results_table_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id)
    REFERENCES s408402.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Создание индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_results_table_user_id ON s408402.results_table(user_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON s408402.users(username);