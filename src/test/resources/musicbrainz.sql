--
-- PostgreSQL database dump
--

-- Dumped from database version 11.4
-- Dumped by pg_dump version 11.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: cover_art_archive; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA cover_art_archive;


--
-- Name: documentation; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA documentation;


--
-- Name: musicbrainz; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA musicbrainz;


--
-- Name: statistics; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA statistics;


--
-- Name: wikidocs; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA wikidocs;


--
-- Name: cover_art_presence; Type: TYPE; Schema: musicbrainz; Owner: -
--

CREATE TYPE musicbrainz.cover_art_presence AS ENUM (
    'absent',
    'present',
    'darkened'
);


--
-- Name: event_art_presence; Type: TYPE; Schema: musicbrainz; Owner: -
--

CREATE TYPE musicbrainz.event_art_presence AS ENUM (
    'absent',
    'present',
    'darkened'
);


--
-- Name: fluency; Type: TYPE; Schema: musicbrainz; Owner: -
--

CREATE TYPE musicbrainz.fluency AS ENUM (
    'basic',
    'intermediate',
    'advanced',
    'native'
);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: art_type; Type: TABLE; Schema: cover_art_archive; Owner: -
--

CREATE TABLE cover_art_archive.art_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: art_type_id_seq; Type: SEQUENCE; Schema: cover_art_archive; Owner: -
--

CREATE SEQUENCE cover_art_archive.art_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: art_type_id_seq; Type: SEQUENCE OWNED BY; Schema: cover_art_archive; Owner: -
--

ALTER SEQUENCE cover_art_archive.art_type_id_seq OWNED BY cover_art_archive.art_type.id;


--
-- Name: cover_art; Type: TABLE; Schema: cover_art_archive; Owner: -
--

CREATE TABLE cover_art_archive.cover_art (
    id bigint NOT NULL,
    release integer NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    edit integer NOT NULL,
    ordering integer NOT NULL,
    date_uploaded timestamp with time zone DEFAULT now() NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    mime_type text NOT NULL,
    filesize integer,
    thumb_250_filesize integer,
    thumb_500_filesize integer,
    thumb_1200_filesize integer,
    CONSTRAINT cover_art_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT cover_art_ordering_check CHECK ((ordering > 0))
);


--
-- Name: cover_art_type; Type: TABLE; Schema: cover_art_archive; Owner: -
--

CREATE TABLE cover_art_archive.cover_art_type (
    id bigint NOT NULL,
    type_id integer NOT NULL
);


--
-- Name: image_type; Type: TABLE; Schema: cover_art_archive; Owner: -
--

CREATE TABLE cover_art_archive.image_type (
    mime_type text NOT NULL,
    suffix text NOT NULL
);


--
-- Name: release_group_cover_art; Type: TABLE; Schema: cover_art_archive; Owner: -
--

CREATE TABLE cover_art_archive.release_group_cover_art (
    release_group integer NOT NULL,
    release integer NOT NULL
);


--
-- Name: l_area_area_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_area_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_artist_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_artist_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_event_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_event_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_instrument_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_instrument_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_label_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_label_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_area_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_area_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_artist_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_artist_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_event_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_event_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_instrument_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_instrument_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_label_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_label_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_artist_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_artist_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_event_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_event_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_instrument_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_instrument_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_label_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_label_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_event_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_event_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_instrument_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_instrument_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_label_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_label_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_instrument_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_instrument_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_label_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_label_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_label_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_label_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_place_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_place_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_place_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_place_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_recording_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_recording_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_recording_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_recording_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_group_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_group_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_group_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_group_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_group_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_group_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_group_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_group_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_release_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_release_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_release_group_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_release_group_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_release_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_release_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_series_series_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_series_series_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_series_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_series_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_series_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_series_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_url_url_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_url_url_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_url_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_url_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: l_work_work_example; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.l_work_work_example (
    id integer NOT NULL,
    published boolean NOT NULL,
    name text NOT NULL
);


--
-- Name: link_type_documentation; Type: TABLE; Schema: documentation; Owner: -
--

CREATE TABLE documentation.link_type_documentation (
    id integer NOT NULL,
    documentation text NOT NULL,
    examples_deleted smallint DEFAULT 0 NOT NULL
);


--
-- Name: alternative_medium; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.alternative_medium (
    id integer NOT NULL,
    medium integer NOT NULL,
    alternative_release integer NOT NULL,
    name character varying,
    CONSTRAINT alternative_medium_name_check CHECK (((name)::text <> ''::text))
);


--
-- Name: alternative_medium_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.alternative_medium_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alternative_medium_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.alternative_medium_id_seq OWNED BY musicbrainz.alternative_medium.id;


--
-- Name: alternative_medium_track; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.alternative_medium_track (
    alternative_medium integer NOT NULL,
    track integer NOT NULL,
    alternative_track integer NOT NULL
);


--
-- Name: alternative_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.alternative_release (
    id integer NOT NULL,
    gid uuid NOT NULL,
    release integer NOT NULL,
    name character varying,
    artist_credit integer,
    type integer NOT NULL,
    language integer NOT NULL,
    script integer NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    CONSTRAINT alternative_release_name_check CHECK (((name)::text <> ''::text))
);


--
-- Name: alternative_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.alternative_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alternative_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.alternative_release_id_seq OWNED BY musicbrainz.alternative_release.id;


--
-- Name: alternative_release_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.alternative_release_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: alternative_release_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.alternative_release_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alternative_release_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.alternative_release_type_id_seq OWNED BY musicbrainz.alternative_release_type.id;


--
-- Name: alternative_track; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.alternative_track (
    id integer NOT NULL,
    name character varying,
    artist_credit integer,
    ref_count integer DEFAULT 0 NOT NULL,
    CONSTRAINT alternative_track_check CHECK ((((name)::text <> ''::text) AND ((name IS NOT NULL) OR (artist_credit IS NOT NULL))))
);


--
-- Name: alternative_track_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.alternative_track_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alternative_track_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.alternative_track_id_seq OWNED BY musicbrainz.alternative_track.id;


--
-- Name: annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.annotation (
    id integer NOT NULL,
    editor integer NOT NULL,
    text text,
    changelog character varying(255),
    created timestamp with time zone DEFAULT now()
);


--
-- Name: annotation_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.annotation_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: annotation_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.annotation_id_seq OWNED BY musicbrainz.annotation.id;


--
-- Name: application; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.application (
    id integer NOT NULL,
    owner integer NOT NULL,
    name text NOT NULL,
    oauth_id text NOT NULL,
    oauth_secret text NOT NULL,
    oauth_redirect_uri text
);


--
-- Name: application_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.application_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: application_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.application_id_seq OWNED BY musicbrainz.application.id;


--
-- Name: area; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    type integer,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    ended boolean DEFAULT false NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    CONSTRAINT area_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT area_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: area_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_alias (
    id integer NOT NULL,
    area integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT area_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT area_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL)))
);


--
-- Name: area_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_alias_id_seq OWNED BY musicbrainz.area_alias.id;


--
-- Name: area_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: area_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_alias_type_id_seq OWNED BY musicbrainz.area_alias_type.id;


--
-- Name: area_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_annotation (
    area integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: area_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_attribute (
    id integer NOT NULL,
    area integer NOT NULL,
    area_attribute_type integer NOT NULL,
    area_attribute_type_allowed_value integer,
    area_attribute_text text,
    CONSTRAINT area_attribute_check CHECK ((((area_attribute_type_allowed_value IS NULL) AND (area_attribute_text IS NOT NULL)) OR ((area_attribute_type_allowed_value IS NOT NULL) AND (area_attribute_text IS NULL))))
);


--
-- Name: area_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_attribute_id_seq OWNED BY musicbrainz.area_attribute.id;


--
-- Name: area_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: area_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_attribute_type_allowed_value (
    id integer NOT NULL,
    area_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: area_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.area_attribute_type_allowed_value.id;


--
-- Name: area_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_attribute_type_id_seq OWNED BY musicbrainz.area_attribute_type.id;


--
-- Name: area_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: area_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_id_seq OWNED BY musicbrainz.area.id;


--
-- Name: area_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_tag (
    area integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: area_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_tag_raw (
    area integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: area_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.area_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: area_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.area_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: area_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.area_type_id_seq OWNED BY musicbrainz.area_type.id;


--
-- Name: artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    type integer,
    area integer,
    gender integer,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    ended boolean DEFAULT false NOT NULL,
    begin_area integer,
    end_area integer,
    CONSTRAINT artist_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT artist_ended_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL))))
);


--
-- Name: artist_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_alias (
    id integer NOT NULL,
    artist integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT artist_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT artist_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 3) OR ((type = 3) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL))))
);


--
-- Name: artist_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_alias_id_seq OWNED BY musicbrainz.artist_alias.id;


--
-- Name: artist_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: artist_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_alias_type_id_seq OWNED BY musicbrainz.artist_alias_type.id;


--
-- Name: artist_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_annotation (
    artist integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: artist_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_attribute (
    id integer NOT NULL,
    artist integer NOT NULL,
    artist_attribute_type integer NOT NULL,
    artist_attribute_type_allowed_value integer,
    artist_attribute_text text,
    CONSTRAINT artist_attribute_check CHECK ((((artist_attribute_type_allowed_value IS NULL) AND (artist_attribute_text IS NOT NULL)) OR ((artist_attribute_type_allowed_value IS NOT NULL) AND (artist_attribute_text IS NULL))))
);


--
-- Name: artist_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_attribute_id_seq OWNED BY musicbrainz.artist_attribute.id;


--
-- Name: artist_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: artist_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_attribute_type_allowed_value (
    id integer NOT NULL,
    artist_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: artist_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.artist_attribute_type_allowed_value.id;


--
-- Name: artist_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_attribute_type_id_seq OWNED BY musicbrainz.artist_attribute_type.id;


--
-- Name: artist_credit; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_credit (
    id integer NOT NULL,
    name character varying NOT NULL,
    artist_count smallint NOT NULL,
    ref_count integer DEFAULT 0,
    created timestamp with time zone DEFAULT now(),
    edits_pending integer DEFAULT 0 NOT NULL,
    CONSTRAINT artist_credit_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: artist_credit_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_credit_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_credit_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_credit_id_seq OWNED BY musicbrainz.artist_credit.id;


--
-- Name: artist_credit_name; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_credit_name (
    artist_credit integer NOT NULL,
    "position" smallint NOT NULL,
    artist integer NOT NULL,
    name character varying NOT NULL,
    join_phrase text DEFAULT ''::text NOT NULL
);


--
-- Name: artist_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: artist_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_id_seq OWNED BY musicbrainz.artist.id;


--
-- Name: artist_ipi; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_ipi (
    artist integer NOT NULL,
    ipi character(11) NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT artist_ipi_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT artist_ipi_ipi_check CHECK ((ipi ~ '^\d{11}$'::text))
);


--
-- Name: artist_isni; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_isni (
    artist integer NOT NULL,
    isni character(16) NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT artist_isni_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT artist_isni_isni_check CHECK ((isni ~ '^\d{15}[\dX]$'::text))
);


--
-- Name: artist_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_meta (
    id integer NOT NULL,
    rating smallint,
    rating_count integer,
    CONSTRAINT artist_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: artist_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_rating_raw (
    artist integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT artist_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: artist_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_tag (
    artist integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: artist_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_tag_raw (
    artist integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: artist_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.artist_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: artist_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.artist_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: artist_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.artist_type_id_seq OWNED BY musicbrainz.artist_type.id;


--
-- Name: autoeditor_election; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.autoeditor_election (
    id integer NOT NULL,
    candidate integer NOT NULL,
    proposer integer NOT NULL,
    seconder_1 integer,
    seconder_2 integer,
    status integer DEFAULT 1 NOT NULL,
    yes_votes integer DEFAULT 0 NOT NULL,
    no_votes integer DEFAULT 0 NOT NULL,
    propose_time timestamp with time zone DEFAULT now() NOT NULL,
    open_time timestamp with time zone,
    close_time timestamp with time zone,
    CONSTRAINT autoeditor_election_status_check CHECK ((status = ANY (ARRAY[1, 2, 3, 4, 5, 6])))
);


--
-- Name: autoeditor_election_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.autoeditor_election_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: autoeditor_election_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.autoeditor_election_id_seq OWNED BY musicbrainz.autoeditor_election.id;


--
-- Name: autoeditor_election_vote; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.autoeditor_election_vote (
    id integer NOT NULL,
    autoeditor_election integer NOT NULL,
    voter integer NOT NULL,
    vote integer NOT NULL,
    vote_time timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT autoeditor_election_vote_vote_check CHECK ((vote = ANY (ARRAY['-1'::integer, 0, 1])))
);


--
-- Name: autoeditor_election_vote_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.autoeditor_election_vote_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: autoeditor_election_vote_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.autoeditor_election_vote_id_seq OWNED BY musicbrainz.autoeditor_election_vote.id;


--
-- Name: cdtoc; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.cdtoc (
    id integer NOT NULL,
    discid character(28) NOT NULL,
    freedb_id character(8) NOT NULL,
    track_count integer NOT NULL,
    leadout_offset integer NOT NULL,
    track_offset integer[] NOT NULL,
    degraded boolean DEFAULT false NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: cdtoc_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.cdtoc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cdtoc_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.cdtoc_id_seq OWNED BY musicbrainz.cdtoc.id;


--
-- Name: cdtoc_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.cdtoc_raw (
    id integer NOT NULL,
    release integer NOT NULL,
    discid character(28) NOT NULL,
    track_count integer NOT NULL,
    leadout_offset integer NOT NULL,
    track_offset integer[] NOT NULL
);


--
-- Name: cdtoc_raw_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.cdtoc_raw_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cdtoc_raw_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.cdtoc_raw_id_seq OWNED BY musicbrainz.cdtoc_raw.id;


--
-- Name: country_area; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.country_area (
    area integer NOT NULL
);


--
-- Name: deleted_entity; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.deleted_entity (
    gid uuid NOT NULL,
    data text NOT NULL,
    deleted_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: edit; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit (
    id integer NOT NULL,
    editor integer NOT NULL,
    type smallint NOT NULL,
    status smallint NOT NULL,
    autoedit smallint DEFAULT 0 NOT NULL,
    open_time timestamp with time zone DEFAULT now(),
    close_time timestamp with time zone,
    expire_time timestamp with time zone NOT NULL,
    language integer,
    quality smallint DEFAULT 1 NOT NULL
);


--
-- Name: edit_area; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_area (
    edit integer NOT NULL,
    area integer NOT NULL
);


--
-- Name: edit_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_artist (
    edit integer NOT NULL,
    artist integer NOT NULL,
    status smallint NOT NULL
);


--
-- Name: edit_data; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_data (
    edit integer NOT NULL,
    data text NOT NULL
);


--
-- Name: edit_event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_event (
    edit integer NOT NULL,
    event integer NOT NULL
);


--
-- Name: edit_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.edit_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: edit_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.edit_id_seq OWNED BY musicbrainz.edit.id;


--
-- Name: edit_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_instrument (
    edit integer NOT NULL,
    instrument integer NOT NULL
);


--
-- Name: edit_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_label (
    edit integer NOT NULL,
    label integer NOT NULL,
    status smallint NOT NULL
);


--
-- Name: edit_note; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_note (
    id integer NOT NULL,
    editor integer NOT NULL,
    edit integer NOT NULL,
    text text NOT NULL,
    post_time timestamp with time zone DEFAULT now()
);


--
-- Name: edit_note_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.edit_note_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: edit_note_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.edit_note_id_seq OWNED BY musicbrainz.edit_note.id;


--
-- Name: edit_note_recipient; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_note_recipient (
    recipient integer NOT NULL,
    edit_note integer NOT NULL
);


--
-- Name: edit_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_place (
    edit integer NOT NULL,
    place integer NOT NULL
);


--
-- Name: edit_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_recording (
    edit integer NOT NULL,
    recording integer NOT NULL
);


--
-- Name: edit_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_release (
    edit integer NOT NULL,
    release integer NOT NULL
);


--
-- Name: edit_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_release_group (
    edit integer NOT NULL,
    release_group integer NOT NULL
);


--
-- Name: edit_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_series (
    edit integer NOT NULL,
    series integer NOT NULL
);


--
-- Name: edit_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_url (
    edit integer NOT NULL,
    url integer NOT NULL
);


--
-- Name: edit_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.edit_work (
    edit integer NOT NULL,
    work integer NOT NULL
);


--
-- Name: editor; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    privs integer DEFAULT 0,
    email character varying(64) DEFAULT NULL::character varying,
    website character varying(255) DEFAULT NULL::character varying,
    bio text,
    member_since timestamp with time zone DEFAULT now(),
    email_confirm_date timestamp with time zone,
    last_login_date timestamp with time zone DEFAULT now(),
    last_updated timestamp with time zone DEFAULT now(),
    birth_date date,
    gender integer,
    area integer,
    password character varying(128) NOT NULL,
    ha1 character(32) NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


--
-- Name: editor_collection; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection (
    id integer NOT NULL,
    gid uuid NOT NULL,
    editor integer NOT NULL,
    name character varying NOT NULL,
    public boolean DEFAULT false NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    type integer NOT NULL
);


--
-- Name: editor_collection_area; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_area (
    collection integer NOT NULL,
    area integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_area_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_artist (
    collection integer NOT NULL,
    artist integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_artist_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_collaborator; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_collaborator (
    collection integer NOT NULL,
    editor integer NOT NULL
);


--
-- Name: editor_collection_deleted_entity; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_deleted_entity (
    collection integer NOT NULL,
    gid uuid NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_deleted_entity_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_event (
    collection integer NOT NULL,
    event integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_event_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_collection_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_collection_id_seq OWNED BY musicbrainz.editor_collection.id;


--
-- Name: editor_collection_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_instrument (
    collection integer NOT NULL,
    instrument integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_instrument_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_label (
    collection integer NOT NULL,
    label integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_label_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_place (
    collection integer NOT NULL,
    place integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_place_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_recording (
    collection integer NOT NULL,
    recording integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_recording_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_release (
    collection integer NOT NULL,
    release integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_release_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_release_group (
    collection integer NOT NULL,
    release_group integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_release_group_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_series (
    collection integer NOT NULL,
    series integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_series_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_collection_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    entity_type character varying(50) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: editor_collection_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_collection_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_collection_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_collection_type_id_seq OWNED BY musicbrainz.editor_collection_type.id;


--
-- Name: editor_collection_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_collection_work (
    collection integer NOT NULL,
    work integer NOT NULL,
    added timestamp with time zone DEFAULT now(),
    "position" integer DEFAULT 0 NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    CONSTRAINT editor_collection_work_position_check CHECK (("position" >= 0))
);


--
-- Name: editor_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_id_seq OWNED BY musicbrainz.editor.id;


--
-- Name: editor_language; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_language (
    editor integer NOT NULL,
    language integer NOT NULL,
    fluency musicbrainz.fluency NOT NULL
);


--
-- Name: editor_oauth_token; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_oauth_token (
    id integer NOT NULL,
    editor integer NOT NULL,
    application integer NOT NULL,
    authorization_code text,
    refresh_token text,
    access_token text,
    expire_time timestamp with time zone NOT NULL,
    scope integer DEFAULT 0 NOT NULL,
    granted timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: editor_oauth_token_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_oauth_token_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_oauth_token_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_oauth_token_id_seq OWNED BY musicbrainz.editor_oauth_token.id;


--
-- Name: editor_preference; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_preference (
    id integer NOT NULL,
    editor integer NOT NULL,
    name character varying(50) NOT NULL,
    value character varying(100) NOT NULL
);


--
-- Name: editor_preference_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_preference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_preference_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_preference_id_seq OWNED BY musicbrainz.editor_preference.id;


--
-- Name: editor_subscribe_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_artist (
    id integer NOT NULL,
    editor integer NOT NULL,
    artist integer NOT NULL,
    last_edit_sent integer NOT NULL
);


--
-- Name: editor_subscribe_artist_deleted; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_artist_deleted (
    editor integer NOT NULL,
    gid uuid NOT NULL,
    deleted_by integer NOT NULL
);


--
-- Name: editor_subscribe_artist_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_subscribe_artist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_subscribe_artist_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_subscribe_artist_id_seq OWNED BY musicbrainz.editor_subscribe_artist.id;


--
-- Name: editor_subscribe_collection; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_collection (
    id integer NOT NULL,
    editor integer NOT NULL,
    collection integer NOT NULL,
    last_edit_sent integer NOT NULL,
    available boolean DEFAULT true NOT NULL,
    last_seen_name character varying(255)
);


--
-- Name: editor_subscribe_collection_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_subscribe_collection_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_subscribe_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_subscribe_collection_id_seq OWNED BY musicbrainz.editor_subscribe_collection.id;


--
-- Name: editor_subscribe_editor; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_editor (
    id integer NOT NULL,
    editor integer NOT NULL,
    subscribed_editor integer NOT NULL,
    last_edit_sent integer NOT NULL
);


--
-- Name: editor_subscribe_editor_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_subscribe_editor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_subscribe_editor_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_subscribe_editor_id_seq OWNED BY musicbrainz.editor_subscribe_editor.id;


--
-- Name: editor_subscribe_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_label (
    id integer NOT NULL,
    editor integer NOT NULL,
    label integer NOT NULL,
    last_edit_sent integer NOT NULL
);


--
-- Name: editor_subscribe_label_deleted; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_label_deleted (
    editor integer NOT NULL,
    gid uuid NOT NULL,
    deleted_by integer NOT NULL
);


--
-- Name: editor_subscribe_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_subscribe_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_subscribe_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_subscribe_label_id_seq OWNED BY musicbrainz.editor_subscribe_label.id;


--
-- Name: editor_subscribe_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_series (
    id integer NOT NULL,
    editor integer NOT NULL,
    series integer NOT NULL,
    last_edit_sent integer NOT NULL
);


--
-- Name: editor_subscribe_series_deleted; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_subscribe_series_deleted (
    editor integer NOT NULL,
    gid uuid NOT NULL,
    deleted_by integer NOT NULL
);


--
-- Name: editor_subscribe_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.editor_subscribe_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: editor_subscribe_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.editor_subscribe_series_id_seq OWNED BY musicbrainz.editor_subscribe_series.id;


--
-- Name: editor_watch_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_watch_artist (
    artist integer NOT NULL,
    editor integer NOT NULL
);


--
-- Name: editor_watch_preferences; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_watch_preferences (
    editor integer NOT NULL,
    notify_via_email boolean DEFAULT true NOT NULL,
    notification_timeframe interval DEFAULT '7 days'::interval NOT NULL,
    last_checked timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: editor_watch_release_group_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_watch_release_group_type (
    editor integer NOT NULL,
    release_group_type integer NOT NULL
);


--
-- Name: editor_watch_release_status; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.editor_watch_release_status (
    editor integer NOT NULL,
    release_status integer NOT NULL
);


--
-- Name: event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    "time" time without time zone,
    type integer,
    cancelled boolean DEFAULT false NOT NULL,
    setlist text,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT event_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT event_ended_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL))))
);


--
-- Name: event_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_alias (
    id integer NOT NULL,
    event integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT event_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT event_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL))))
);


--
-- Name: event_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_alias_id_seq OWNED BY musicbrainz.event_alias.id;


--
-- Name: event_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: event_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_alias_type_id_seq OWNED BY musicbrainz.event_alias_type.id;


--
-- Name: event_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_annotation (
    event integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: event_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_attribute (
    id integer NOT NULL,
    event integer NOT NULL,
    event_attribute_type integer NOT NULL,
    event_attribute_type_allowed_value integer,
    event_attribute_text text,
    CONSTRAINT event_attribute_check CHECK ((((event_attribute_type_allowed_value IS NULL) AND (event_attribute_text IS NOT NULL)) OR ((event_attribute_type_allowed_value IS NOT NULL) AND (event_attribute_text IS NULL))))
);


--
-- Name: event_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_attribute_id_seq OWNED BY musicbrainz.event_attribute.id;


--
-- Name: event_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: event_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_attribute_type_allowed_value (
    id integer NOT NULL,
    event_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: event_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.event_attribute_type_allowed_value.id;


--
-- Name: event_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_attribute_type_id_seq OWNED BY musicbrainz.event_attribute_type.id;


--
-- Name: event_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_id_seq OWNED BY musicbrainz.event.id;


--
-- Name: event_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_meta (
    id integer NOT NULL,
    rating smallint,
    rating_count integer,
    event_art_presence musicbrainz.event_art_presence DEFAULT 'absent'::musicbrainz.event_art_presence NOT NULL,
    CONSTRAINT event_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: event_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_rating_raw (
    event integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT event_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: l_event_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: link; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link (
    id integer NOT NULL,
    link_type integer NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    attribute_count integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT link_ended_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL))))
);


--
-- Name: link_attribute_text_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_attribute_text_value (
    link integer NOT NULL,
    attribute_type integer NOT NULL,
    text_value text NOT NULL
);


--
-- Name: link_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_type (
    id integer NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    gid uuid NOT NULL,
    entity_type0 character varying(50) NOT NULL,
    entity_type1 character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    link_phrase character varying(255) NOT NULL,
    reverse_link_phrase character varying(255) NOT NULL,
    long_link_phrase character varying(255) NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    is_deprecated boolean DEFAULT false NOT NULL,
    has_dates boolean DEFAULT true NOT NULL,
    entity0_cardinality integer DEFAULT 0 NOT NULL,
    entity1_cardinality integer DEFAULT 0 NOT NULL
);


--
-- Name: series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    type integer NOT NULL,
    ordering_attribute integer NOT NULL,
    ordering_type integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT series_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: event_series; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.event_series AS
 SELECT lrs.entity0 AS event,
    lrs.entity1 AS series,
    lrs.id AS relationship,
    lrs.link_order,
    lrs.link,
    COALESCE(latv.text_value, ''::text) AS text_value
   FROM ((((musicbrainz.l_event_series lrs
     JOIN musicbrainz.series s ON ((s.id = lrs.entity1)))
     JOIN musicbrainz.link l ON ((l.id = lrs.link)))
     JOIN musicbrainz.link_type lt ON (((lt.id = l.link_type) AND (lt.gid = '707d947d-9563-328a-9a7d-0c5b9c3a9791'::uuid))))
     LEFT JOIN musicbrainz.link_attribute_text_value latv ON (((latv.attribute_type = s.ordering_attribute) AND (latv.link = l.id))))
  ORDER BY lrs.entity1, lrs.link_order;


--
-- Name: event_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_tag (
    event integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: event_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_tag_raw (
    event integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: event_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.event_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: event_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.event_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.event_type_id_seq OWNED BY musicbrainz.event_type.id;


--
-- Name: gender; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.gender (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: gender_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.gender_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gender_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.gender_id_seq OWNED BY musicbrainz.gender.id;


--
-- Name: genre; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.genre (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT genre_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: genre_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.genre_alias (
    id integer NOT NULL,
    genre integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    primary_for_locale boolean DEFAULT false NOT NULL,
    CONSTRAINT genre_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL)))
);


--
-- Name: genre_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.genre_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: genre_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.genre_alias_id_seq OWNED BY musicbrainz.genre_alias.id;


--
-- Name: genre_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.genre_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: genre_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.genre_id_seq OWNED BY musicbrainz.genre.id;


--
-- Name: instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    type integer,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    CONSTRAINT instrument_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: instrument_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_alias (
    id integer NOT NULL,
    instrument integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT instrument_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT instrument_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL))))
);


--
-- Name: instrument_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_alias_id_seq OWNED BY musicbrainz.instrument_alias.id;


--
-- Name: instrument_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: instrument_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_alias_type_id_seq OWNED BY musicbrainz.instrument_alias_type.id;


--
-- Name: instrument_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_annotation (
    instrument integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: instrument_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_attribute (
    id integer NOT NULL,
    instrument integer NOT NULL,
    instrument_attribute_type integer NOT NULL,
    instrument_attribute_type_allowed_value integer,
    instrument_attribute_text text,
    CONSTRAINT instrument_attribute_check CHECK ((((instrument_attribute_type_allowed_value IS NULL) AND (instrument_attribute_text IS NOT NULL)) OR ((instrument_attribute_type_allowed_value IS NOT NULL) AND (instrument_attribute_text IS NULL))))
);


--
-- Name: instrument_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_attribute_id_seq OWNED BY musicbrainz.instrument_attribute.id;


--
-- Name: instrument_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: instrument_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_attribute_type_allowed_value (
    id integer NOT NULL,
    instrument_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: instrument_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.instrument_attribute_type_allowed_value.id;


--
-- Name: instrument_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_attribute_type_id_seq OWNED BY musicbrainz.instrument_attribute_type.id;


--
-- Name: instrument_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: instrument_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_id_seq OWNED BY musicbrainz.instrument.id;


--
-- Name: instrument_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_tag (
    instrument integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: instrument_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_tag_raw (
    instrument integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: instrument_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.instrument_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: instrument_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.instrument_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instrument_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.instrument_type_id_seq OWNED BY musicbrainz.instrument_type.id;


--
-- Name: iso_3166_1; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.iso_3166_1 (
    area integer NOT NULL,
    code character(2) NOT NULL
);


--
-- Name: iso_3166_2; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.iso_3166_2 (
    area integer NOT NULL,
    code character varying(10) NOT NULL
);


--
-- Name: iso_3166_3; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.iso_3166_3 (
    area integer NOT NULL,
    code character(4) NOT NULL
);


--
-- Name: isrc; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.isrc (
    id integer NOT NULL,
    recording integer NOT NULL,
    isrc character(12) NOT NULL,
    source smallint,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT isrc_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT isrc_isrc_check CHECK ((isrc ~ '^[A-Z]{2}[A-Z0-9]{3}[0-9]{7}$'::text))
);


--
-- Name: isrc_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.isrc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: isrc_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.isrc_id_seq OWNED BY musicbrainz.isrc.id;


--
-- Name: iswc; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.iswc (
    id integer NOT NULL,
    work integer NOT NULL,
    iswc character(15),
    source smallint,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT iswc_iswc_check CHECK ((iswc ~ '^T-?\d{3}.?\d{3}.?\d{3}[-.]?\d$'::text))
);


--
-- Name: iswc_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.iswc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: iswc_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.iswc_id_seq OWNED BY musicbrainz.iswc.id;


--
-- Name: l_area_area; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_area (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_area_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_area_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_area_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_area_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_area_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_area_id_seq OWNED BY musicbrainz.l_area_area.id;


--
-- Name: l_area_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_artist (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_artist_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_artist_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_artist_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_artist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_artist_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_artist_id_seq OWNED BY musicbrainz.l_area_artist.id;


--
-- Name: l_area_event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_event (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_event_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_event_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_event_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_event_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_event_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_event_id_seq OWNED BY musicbrainz.l_area_event.id;


--
-- Name: l_area_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_instrument (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_instrument_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_instrument_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_instrument_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_instrument_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_instrument_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_instrument_id_seq OWNED BY musicbrainz.l_area_instrument.id;


--
-- Name: l_area_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_label (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_label_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_label_id_seq OWNED BY musicbrainz.l_area_label.id;


--
-- Name: l_area_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_place_id_seq OWNED BY musicbrainz.l_area_place.id;


--
-- Name: l_area_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_recording_id_seq OWNED BY musicbrainz.l_area_recording.id;


--
-- Name: l_area_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_release_group_id_seq OWNED BY musicbrainz.l_area_release_group.id;


--
-- Name: l_area_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_release_id_seq OWNED BY musicbrainz.l_area_release.id;


--
-- Name: l_area_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_series_id_seq OWNED BY musicbrainz.l_area_series.id;


--
-- Name: l_area_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_url_id_seq OWNED BY musicbrainz.l_area_url.id;


--
-- Name: l_area_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_area_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_area_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_area_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_area_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_area_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_area_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_area_work_id_seq OWNED BY musicbrainz.l_area_work.id;


--
-- Name: l_artist_artist; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_artist (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_artist_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_artist_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_artist_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_artist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_artist_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_artist_id_seq OWNED BY musicbrainz.l_artist_artist.id;


--
-- Name: l_artist_event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_event (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_event_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_event_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_event_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_event_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_event_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_event_id_seq OWNED BY musicbrainz.l_artist_event.id;


--
-- Name: l_artist_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_instrument (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_instrument_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_instrument_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_instrument_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_instrument_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_instrument_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_instrument_id_seq OWNED BY musicbrainz.l_artist_instrument.id;


--
-- Name: l_artist_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_label (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_label_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_label_id_seq OWNED BY musicbrainz.l_artist_label.id;


--
-- Name: l_artist_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_place_id_seq OWNED BY musicbrainz.l_artist_place.id;


--
-- Name: l_artist_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_recording_id_seq OWNED BY musicbrainz.l_artist_recording.id;


--
-- Name: l_artist_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_release_group_id_seq OWNED BY musicbrainz.l_artist_release_group.id;


--
-- Name: l_artist_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_release_id_seq OWNED BY musicbrainz.l_artist_release.id;


--
-- Name: l_artist_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_series_id_seq OWNED BY musicbrainz.l_artist_series.id;


--
-- Name: l_artist_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_url_id_seq OWNED BY musicbrainz.l_artist_url.id;


--
-- Name: l_artist_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_artist_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_artist_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_artist_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_artist_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_artist_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_artist_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_artist_work_id_seq OWNED BY musicbrainz.l_artist_work.id;


--
-- Name: l_event_event; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_event (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_event_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_event_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_event_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_event_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_event_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_event_id_seq OWNED BY musicbrainz.l_event_event.id;


--
-- Name: l_event_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_instrument (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_instrument_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_instrument_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_instrument_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_instrument_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_instrument_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_instrument_id_seq OWNED BY musicbrainz.l_event_instrument.id;


--
-- Name: l_event_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_label (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_label_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_label_id_seq OWNED BY musicbrainz.l_event_label.id;


--
-- Name: l_event_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_place_id_seq OWNED BY musicbrainz.l_event_place.id;


--
-- Name: l_event_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_recording_id_seq OWNED BY musicbrainz.l_event_recording.id;


--
-- Name: l_event_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_release_group_id_seq OWNED BY musicbrainz.l_event_release_group.id;


--
-- Name: l_event_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_release_id_seq OWNED BY musicbrainz.l_event_release.id;


--
-- Name: l_event_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_series_id_seq OWNED BY musicbrainz.l_event_series.id;


--
-- Name: l_event_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_url_id_seq OWNED BY musicbrainz.l_event_url.id;


--
-- Name: l_event_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_event_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_event_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_event_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_event_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_event_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_event_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_event_work_id_seq OWNED BY musicbrainz.l_event_work.id;


--
-- Name: l_instrument_instrument; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_instrument (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_instrument_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_instrument_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_instrument_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_instrument_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_instrument_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_instrument_id_seq OWNED BY musicbrainz.l_instrument_instrument.id;


--
-- Name: l_instrument_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_label (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_label_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_label_id_seq OWNED BY musicbrainz.l_instrument_label.id;


--
-- Name: l_instrument_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_place_id_seq OWNED BY musicbrainz.l_instrument_place.id;


--
-- Name: l_instrument_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_recording_id_seq OWNED BY musicbrainz.l_instrument_recording.id;


--
-- Name: l_instrument_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_release_group_id_seq OWNED BY musicbrainz.l_instrument_release_group.id;


--
-- Name: l_instrument_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_release_id_seq OWNED BY musicbrainz.l_instrument_release.id;


--
-- Name: l_instrument_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_series_id_seq OWNED BY musicbrainz.l_instrument_series.id;


--
-- Name: l_instrument_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_url_id_seq OWNED BY musicbrainz.l_instrument_url.id;


--
-- Name: l_instrument_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_instrument_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_instrument_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_instrument_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_instrument_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_instrument_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_instrument_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_instrument_work_id_seq OWNED BY musicbrainz.l_instrument_work.id;


--
-- Name: l_label_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_label (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_label_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_label_id_seq OWNED BY musicbrainz.l_label_label.id;


--
-- Name: l_label_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_place_id_seq OWNED BY musicbrainz.l_label_place.id;


--
-- Name: l_label_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_recording_id_seq OWNED BY musicbrainz.l_label_recording.id;


--
-- Name: l_label_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_release_group_id_seq OWNED BY musicbrainz.l_label_release_group.id;


--
-- Name: l_label_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_release_id_seq OWNED BY musicbrainz.l_label_release.id;


--
-- Name: l_label_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_series_id_seq OWNED BY musicbrainz.l_label_series.id;


--
-- Name: l_label_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_url_id_seq OWNED BY musicbrainz.l_label_url.id;


--
-- Name: l_label_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_label_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_label_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_label_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_label_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_label_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_label_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_label_work_id_seq OWNED BY musicbrainz.l_label_work.id;


--
-- Name: l_place_place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_place (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_place_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_place_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_place_id_seq OWNED BY musicbrainz.l_place_place.id;


--
-- Name: l_place_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_recording_id_seq OWNED BY musicbrainz.l_place_recording.id;


--
-- Name: l_place_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_release_group_id_seq OWNED BY musicbrainz.l_place_release_group.id;


--
-- Name: l_place_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_release_id_seq OWNED BY musicbrainz.l_place_release.id;


--
-- Name: l_place_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_series_id_seq OWNED BY musicbrainz.l_place_series.id;


--
-- Name: l_place_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_url_id_seq OWNED BY musicbrainz.l_place_url.id;


--
-- Name: l_place_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_place_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_place_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_place_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_place_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_place_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_place_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_place_work_id_seq OWNED BY musicbrainz.l_place_work.id;


--
-- Name: l_recording_recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_recording (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_recording_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_recording_id_seq OWNED BY musicbrainz.l_recording_recording.id;


--
-- Name: l_recording_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_release_group_id_seq OWNED BY musicbrainz.l_recording_release_group.id;


--
-- Name: l_recording_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_release_id_seq OWNED BY musicbrainz.l_recording_release.id;


--
-- Name: l_recording_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_series_id_seq OWNED BY musicbrainz.l_recording_series.id;


--
-- Name: l_recording_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_url_id_seq OWNED BY musicbrainz.l_recording_url.id;


--
-- Name: l_recording_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_recording_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_recording_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_recording_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_recording_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_recording_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_recording_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_recording_work_id_seq OWNED BY musicbrainz.l_recording_work.id;


--
-- Name: l_release_group_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_group_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_group_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_group_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_group_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_group_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_group_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_group_release_group_id_seq OWNED BY musicbrainz.l_release_group_release_group.id;


--
-- Name: l_release_group_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_group_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_group_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_group_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_group_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_group_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_group_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_group_series_id_seq OWNED BY musicbrainz.l_release_group_series.id;


--
-- Name: l_release_group_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_group_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_group_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_group_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_group_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_group_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_group_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_group_url_id_seq OWNED BY musicbrainz.l_release_group_url.id;


--
-- Name: l_release_group_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_group_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_group_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_group_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_group_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_group_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_group_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_group_work_id_seq OWNED BY musicbrainz.l_release_group_work.id;


--
-- Name: l_release_release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_release (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_release_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_release_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_release_group (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_release_group_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_release_group_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_release_group_id_seq OWNED BY musicbrainz.l_release_release_group.id;


--
-- Name: l_release_release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_release_id_seq OWNED BY musicbrainz.l_release_release.id;


--
-- Name: l_release_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_series_id_seq OWNED BY musicbrainz.l_release_series.id;


--
-- Name: l_release_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_url_id_seq OWNED BY musicbrainz.l_release_url.id;


--
-- Name: l_release_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_release_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_release_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_release_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_release_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_release_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_release_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_release_work_id_seq OWNED BY musicbrainz.l_release_work.id;


--
-- Name: l_series_series; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_series_series (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_series_series_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_series_series_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_series_series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_series_series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_series_series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_series_series_id_seq OWNED BY musicbrainz.l_series_series.id;


--
-- Name: l_series_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_series_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_series_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_series_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_series_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_series_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_series_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_series_url_id_seq OWNED BY musicbrainz.l_series_url.id;


--
-- Name: l_series_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_series_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_series_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_series_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_series_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_series_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_series_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_series_work_id_seq OWNED BY musicbrainz.l_series_work.id;


--
-- Name: l_url_url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_url_url (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_url_url_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_url_url_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_url_url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_url_url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_url_url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_url_url_id_seq OWNED BY musicbrainz.l_url_url.id;


--
-- Name: l_url_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_url_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_url_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_url_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_url_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_url_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_url_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_url_work_id_seq OWNED BY musicbrainz.l_url_work.id;


--
-- Name: l_work_work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.l_work_work (
    id integer NOT NULL,
    link integer NOT NULL,
    entity0 integer NOT NULL,
    entity1 integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    link_order integer DEFAULT 0 NOT NULL,
    entity0_credit text DEFAULT ''::text NOT NULL,
    entity1_credit text DEFAULT ''::text NOT NULL,
    CONSTRAINT l_work_work_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT l_work_work_link_order_check CHECK ((link_order >= 0))
);


--
-- Name: l_work_work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.l_work_work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: l_work_work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.l_work_work_id_seq OWNED BY musicbrainz.l_work_work.id;


--
-- Name: label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    label_code integer,
    type integer,
    area integer,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT label_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT label_ended_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT label_label_code_check CHECK (((label_code > 0) AND (label_code < 100000)))
);


--
-- Name: label_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_alias (
    id integer NOT NULL,
    label integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT label_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT label_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL))))
);


--
-- Name: label_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_alias_id_seq OWNED BY musicbrainz.label_alias.id;


--
-- Name: label_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: label_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_alias_type_id_seq OWNED BY musicbrainz.label_alias_type.id;


--
-- Name: label_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_annotation (
    label integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: label_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_attribute (
    id integer NOT NULL,
    label integer NOT NULL,
    label_attribute_type integer NOT NULL,
    label_attribute_type_allowed_value integer,
    label_attribute_text text,
    CONSTRAINT label_attribute_check CHECK ((((label_attribute_type_allowed_value IS NULL) AND (label_attribute_text IS NOT NULL)) OR ((label_attribute_type_allowed_value IS NOT NULL) AND (label_attribute_text IS NULL))))
);


--
-- Name: label_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_attribute_id_seq OWNED BY musicbrainz.label_attribute.id;


--
-- Name: label_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: label_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_attribute_type_allowed_value (
    id integer NOT NULL,
    label_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: label_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.label_attribute_type_allowed_value.id;


--
-- Name: label_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_attribute_type_id_seq OWNED BY musicbrainz.label_attribute_type.id;


--
-- Name: label_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_id_seq OWNED BY musicbrainz.label.id;


--
-- Name: label_ipi; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_ipi (
    label integer NOT NULL,
    ipi character(11) NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT label_ipi_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT label_ipi_ipi_check CHECK ((ipi ~ '^\d{11}$'::text))
);


--
-- Name: label_isni; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_isni (
    label integer NOT NULL,
    isni character(16) NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT label_isni_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT label_isni_isni_check CHECK ((isni ~ '^\d{15}[\dX]$'::text))
);


--
-- Name: label_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_meta (
    id integer NOT NULL,
    rating smallint,
    rating_count integer,
    CONSTRAINT label_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: label_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_rating_raw (
    label integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT label_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: label_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_tag (
    label integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: label_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_tag_raw (
    label integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: label_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.label_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: label_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.label_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.label_type_id_seq OWNED BY musicbrainz.label_type.id;


--
-- Name: language; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.language (
    id integer NOT NULL,
    iso_code_2t character(3),
    iso_code_2b character(3),
    iso_code_1 character(2),
    name character varying(100) NOT NULL,
    frequency integer DEFAULT 0 NOT NULL,
    iso_code_3 character(3),
    CONSTRAINT iso_code_check CHECK (((iso_code_2t IS NOT NULL) OR (iso_code_3 IS NOT NULL)))
);


--
-- Name: language_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.language_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.language_id_seq OWNED BY musicbrainz.language.id;


--
-- Name: link_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_attribute (
    link integer NOT NULL,
    attribute_type integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: link_attribute_credit; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_attribute_credit (
    link integer NOT NULL,
    attribute_type integer NOT NULL,
    credited_as text NOT NULL
);


--
-- Name: link_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_attribute_type (
    id integer NOT NULL,
    parent integer,
    root integer NOT NULL,
    child_order integer DEFAULT 0 NOT NULL,
    gid uuid NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: link_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.link_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.link_attribute_type_id_seq OWNED BY musicbrainz.link_attribute_type.id;


--
-- Name: link_creditable_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_creditable_attribute_type (
    attribute_type integer NOT NULL
);


--
-- Name: link_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.link_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.link_id_seq OWNED BY musicbrainz.link.id;


--
-- Name: link_text_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_text_attribute_type (
    attribute_type integer NOT NULL
);


--
-- Name: link_type_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.link_type_attribute_type (
    link_type integer NOT NULL,
    attribute_type integer NOT NULL,
    min smallint,
    max smallint,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: link_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.link_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.link_type_id_seq OWNED BY musicbrainz.link_type.id;


--
-- Name: medium; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium (
    id integer NOT NULL,
    release integer NOT NULL,
    "position" integer NOT NULL,
    format integer,
    name character varying DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    track_count integer DEFAULT 0 NOT NULL,
    CONSTRAINT medium_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: medium_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_attribute (
    id integer NOT NULL,
    medium integer NOT NULL,
    medium_attribute_type integer NOT NULL,
    medium_attribute_type_allowed_value integer,
    medium_attribute_text text,
    CONSTRAINT medium_attribute_check CHECK ((((medium_attribute_type_allowed_value IS NULL) AND (medium_attribute_text IS NOT NULL)) OR ((medium_attribute_type_allowed_value IS NOT NULL) AND (medium_attribute_text IS NULL))))
);


--
-- Name: medium_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_attribute_id_seq OWNED BY musicbrainz.medium_attribute.id;


--
-- Name: medium_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: medium_attribute_type_allowed_format; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_attribute_type_allowed_format (
    medium_format integer NOT NULL,
    medium_attribute_type integer NOT NULL
);


--
-- Name: medium_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_attribute_type_allowed_value (
    id integer NOT NULL,
    medium_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: medium_attribute_type_allowed_value_allowed_format; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_attribute_type_allowed_value_allowed_format (
    medium_format integer NOT NULL,
    medium_attribute_type_allowed_value integer NOT NULL
);


--
-- Name: medium_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.medium_attribute_type_allowed_value.id;


--
-- Name: medium_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_attribute_type_id_seq OWNED BY musicbrainz.medium_attribute_type.id;


--
-- Name: medium_cdtoc; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_cdtoc (
    id integer NOT NULL,
    medium integer NOT NULL,
    cdtoc integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT medium_cdtoc_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: medium_cdtoc_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_cdtoc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_cdtoc_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_cdtoc_id_seq OWNED BY musicbrainz.medium_cdtoc.id;


--
-- Name: medium_format; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_format (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    year smallint,
    has_discids boolean DEFAULT false NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: medium_format_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_format_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_format_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_format_id_seq OWNED BY musicbrainz.medium_format.id;


--
-- Name: medium_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.medium_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: medium_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.medium_id_seq OWNED BY musicbrainz.medium.id;


--
-- Name: medium_index; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.medium_index (
    medium integer NOT NULL,
    toc text
);


--
-- Name: old_editor_name; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.old_editor_name (
    name character varying(64) NOT NULL
);


--
-- Name: orderable_link_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.orderable_link_type (
    link_type integer NOT NULL,
    direction smallint DEFAULT 1 NOT NULL,
    CONSTRAINT orderable_link_type_direction_check CHECK (((direction = 1) OR (direction = 2)))
);


--
-- Name: place; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    type integer,
    address character varying DEFAULT ''::character varying NOT NULL,
    area integer,
    coordinates point,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT place_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT place_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: place_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_alias (
    id integer NOT NULL,
    place integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT place_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT place_alias_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL))))
);


--
-- Name: place_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_alias_id_seq OWNED BY musicbrainz.place_alias.id;


--
-- Name: place_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: place_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_alias_type_id_seq OWNED BY musicbrainz.place_alias_type.id;


--
-- Name: place_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_annotation (
    place integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: place_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_attribute (
    id integer NOT NULL,
    place integer NOT NULL,
    place_attribute_type integer NOT NULL,
    place_attribute_type_allowed_value integer,
    place_attribute_text text,
    CONSTRAINT place_attribute_check CHECK ((((place_attribute_type_allowed_value IS NULL) AND (place_attribute_text IS NOT NULL)) OR ((place_attribute_type_allowed_value IS NOT NULL) AND (place_attribute_text IS NULL))))
);


--
-- Name: place_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_attribute_id_seq OWNED BY musicbrainz.place_attribute.id;


--
-- Name: place_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: place_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_attribute_type_allowed_value (
    id integer NOT NULL,
    place_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: place_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.place_attribute_type_allowed_value.id;


--
-- Name: place_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_attribute_type_id_seq OWNED BY musicbrainz.place_attribute_type.id;


--
-- Name: place_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: place_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_id_seq OWNED BY musicbrainz.place.id;


--
-- Name: place_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_tag (
    place integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: place_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_tag_raw (
    place integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: place_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.place_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: place_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.place_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: place_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.place_type_id_seq OWNED BY musicbrainz.place_type.id;


--
-- Name: recording; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    artist_credit integer NOT NULL,
    length integer,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    video boolean DEFAULT false NOT NULL,
    CONSTRAINT recording_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT recording_length_check CHECK (((length IS NULL) OR (length > 0)))
);


--
-- Name: recording_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_alias (
    id integer NOT NULL,
    recording integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT recording_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT recording_alias_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: recording_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_alias_id_seq OWNED BY musicbrainz.recording_alias.id;


--
-- Name: recording_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: recording_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_alias_type_id_seq OWNED BY musicbrainz.recording_alias_type.id;


--
-- Name: recording_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_annotation (
    recording integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: recording_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_attribute (
    id integer NOT NULL,
    recording integer NOT NULL,
    recording_attribute_type integer NOT NULL,
    recording_attribute_type_allowed_value integer,
    recording_attribute_text text,
    CONSTRAINT recording_attribute_check CHECK ((((recording_attribute_type_allowed_value IS NULL) AND (recording_attribute_text IS NOT NULL)) OR ((recording_attribute_type_allowed_value IS NOT NULL) AND (recording_attribute_text IS NULL))))
);


--
-- Name: recording_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_attribute_id_seq OWNED BY musicbrainz.recording_attribute.id;


--
-- Name: recording_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: recording_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_attribute_type_allowed_value (
    id integer NOT NULL,
    recording_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: recording_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.recording_attribute_type_allowed_value.id;


--
-- Name: recording_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_attribute_type_id_seq OWNED BY musicbrainz.recording_attribute_type.id;


--
-- Name: recording_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: recording_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.recording_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recording_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.recording_id_seq OWNED BY musicbrainz.recording.id;


--
-- Name: recording_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_meta (
    id integer NOT NULL,
    rating smallint,
    rating_count integer,
    CONSTRAINT recording_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: recording_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_rating_raw (
    recording integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT recording_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: recording_series; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.recording_series AS
 SELECT lrs.entity0 AS recording,
    lrs.entity1 AS series,
    lrs.id AS relationship,
    lrs.link_order,
    lrs.link,
    COALESCE(latv.text_value, ''::text) AS text_value
   FROM ((((musicbrainz.l_recording_series lrs
     JOIN musicbrainz.series s ON ((s.id = lrs.entity1)))
     JOIN musicbrainz.link l ON ((l.id = lrs.link)))
     JOIN musicbrainz.link_type lt ON (((lt.id = l.link_type) AND (lt.gid = 'ea6f0698-6782-30d6-b16d-293081b66774'::uuid))))
     LEFT JOIN musicbrainz.link_attribute_text_value latv ON (((latv.attribute_type = s.ordering_attribute) AND (latv.link = l.id))))
  ORDER BY lrs.entity1, lrs.link_order;


--
-- Name: recording_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_tag (
    recording integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: recording_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.recording_tag_raw (
    recording integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: release; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    artist_credit integer NOT NULL,
    release_group integer NOT NULL,
    status integer,
    packaging integer,
    language integer,
    script integer,
    barcode character varying(255),
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    quality smallint DEFAULT '-1'::integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT release_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: release_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_alias (
    id integer NOT NULL,
    release integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT release_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT release_alias_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: release_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_alias_id_seq OWNED BY musicbrainz.release_alias.id;


--
-- Name: release_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_alias_type_id_seq OWNED BY musicbrainz.release_alias_type.id;


--
-- Name: release_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_annotation (
    release integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: release_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_attribute (
    id integer NOT NULL,
    release integer NOT NULL,
    release_attribute_type integer NOT NULL,
    release_attribute_type_allowed_value integer,
    release_attribute_text text,
    CONSTRAINT release_attribute_check CHECK ((((release_attribute_type_allowed_value IS NULL) AND (release_attribute_text IS NOT NULL)) OR ((release_attribute_type_allowed_value IS NOT NULL) AND (release_attribute_text IS NULL))))
);


--
-- Name: release_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_attribute_id_seq OWNED BY musicbrainz.release_attribute.id;


--
-- Name: release_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_attribute_type_allowed_value (
    id integer NOT NULL,
    release_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.release_attribute_type_allowed_value.id;


--
-- Name: release_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_attribute_type_id_seq OWNED BY musicbrainz.release_attribute_type.id;


--
-- Name: release_country; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_country (
    release integer NOT NULL,
    country integer NOT NULL,
    date_year smallint,
    date_month smallint,
    date_day smallint
);


--
-- Name: release_coverart; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_coverart (
    id integer NOT NULL,
    last_updated timestamp with time zone,
    cover_art_url character varying(255)
);


--
-- Name: release_unknown_country; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_unknown_country (
    release integer NOT NULL,
    date_year smallint,
    date_month smallint,
    date_day smallint
);


--
-- Name: release_event; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.release_event AS
 SELECT q.release,
    q.date_year,
    q.date_month,
    q.date_day,
    q.country
   FROM ( SELECT release_country.release,
            release_country.date_year,
            release_country.date_month,
            release_country.date_day,
            release_country.country
           FROM musicbrainz.release_country
        UNION ALL
         SELECT release_unknown_country.release,
            release_unknown_country.date_year,
            release_unknown_country.date_month,
            release_unknown_country.date_day,
            NULL::integer
           FROM musicbrainz.release_unknown_country) q;


--
-- Name: release_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: release_group; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    artist_credit integer NOT NULL,
    type integer,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT release_group_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: release_group_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_alias (
    id integer NOT NULL,
    release_group integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT release_group_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT release_group_alias_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: release_group_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_alias_id_seq OWNED BY musicbrainz.release_group_alias.id;


--
-- Name: release_group_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_group_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_alias_type_id_seq OWNED BY musicbrainz.release_group_alias_type.id;


--
-- Name: release_group_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_annotation (
    release_group integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: release_group_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_attribute (
    id integer NOT NULL,
    release_group integer NOT NULL,
    release_group_attribute_type integer NOT NULL,
    release_group_attribute_type_allowed_value integer,
    release_group_attribute_text text,
    CONSTRAINT release_group_attribute_check CHECK ((((release_group_attribute_type_allowed_value IS NULL) AND (release_group_attribute_text IS NOT NULL)) OR ((release_group_attribute_type_allowed_value IS NOT NULL) AND (release_group_attribute_text IS NULL))))
);


--
-- Name: release_group_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_attribute_id_seq OWNED BY musicbrainz.release_group_attribute.id;


--
-- Name: release_group_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_group_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_attribute_type_allowed_value (
    id integer NOT NULL,
    release_group_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_group_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.release_group_attribute_type_allowed_value.id;


--
-- Name: release_group_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_attribute_type_id_seq OWNED BY musicbrainz.release_group_attribute_type.id;


--
-- Name: release_group_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: release_group_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_id_seq OWNED BY musicbrainz.release_group.id;


--
-- Name: release_group_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_meta (
    id integer NOT NULL,
    release_count integer DEFAULT 0 NOT NULL,
    first_release_date_year smallint,
    first_release_date_month smallint,
    first_release_date_day smallint,
    rating smallint,
    rating_count integer,
    CONSTRAINT release_group_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: release_group_primary_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_primary_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_group_primary_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_primary_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_primary_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_primary_type_id_seq OWNED BY musicbrainz.release_group_primary_type.id;


--
-- Name: release_group_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_rating_raw (
    release_group integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT release_group_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: release_group_secondary_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_secondary_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_group_secondary_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_group_secondary_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_group_secondary_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_group_secondary_type_id_seq OWNED BY musicbrainz.release_group_secondary_type.id;


--
-- Name: release_group_secondary_type_join; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_secondary_type_join (
    release_group integer NOT NULL,
    secondary_type integer NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: release_group_series; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.release_group_series AS
 SELECT lrgs.entity0 AS release_group,
    lrgs.entity1 AS series,
    lrgs.id AS relationship,
    lrgs.link_order,
    lrgs.link,
    COALESCE(latv.text_value, ''::text) AS text_value
   FROM ((((musicbrainz.l_release_group_series lrgs
     JOIN musicbrainz.series s ON ((s.id = lrgs.entity1)))
     JOIN musicbrainz.link l ON ((l.id = lrgs.link)))
     JOIN musicbrainz.link_type lt ON (((lt.id = l.link_type) AND (lt.gid = '01018437-91d8-36b9-bf89-3f885d53b5bd'::uuid))))
     LEFT JOIN musicbrainz.link_attribute_text_value latv ON (((latv.attribute_type = s.ordering_attribute) AND (latv.link = l.id))))
  ORDER BY lrgs.entity1, lrgs.link_order;


--
-- Name: release_group_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_tag (
    release_group integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: release_group_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_group_tag_raw (
    release_group integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: release_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_id_seq OWNED BY musicbrainz.release.id;


--
-- Name: release_label; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_label (
    id integer NOT NULL,
    release integer NOT NULL,
    label integer,
    catalog_number character varying(255),
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: release_label_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_label_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_label_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_label_id_seq OWNED BY musicbrainz.release_label.id;


--
-- Name: release_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_meta (
    id integer NOT NULL,
    date_added timestamp with time zone DEFAULT now(),
    info_url character varying(255),
    amazon_asin character varying(10),
    amazon_store character varying(20),
    cover_art_presence musicbrainz.cover_art_presence DEFAULT 'absent'::musicbrainz.cover_art_presence NOT NULL
);


--
-- Name: release_packaging; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_packaging (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_packaging_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_packaging_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_packaging_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_packaging_id_seq OWNED BY musicbrainz.release_packaging.id;


--
-- Name: release_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_raw (
    id integer NOT NULL,
    title character varying(255) NOT NULL,
    artist character varying(255),
    added timestamp with time zone DEFAULT now(),
    last_modified timestamp with time zone DEFAULT now(),
    lookup_count integer DEFAULT 0,
    modify_count integer DEFAULT 0,
    source integer DEFAULT 0,
    barcode character varying(255),
    comment character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- Name: release_raw_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_raw_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_raw_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_raw_id_seq OWNED BY musicbrainz.release_raw.id;


--
-- Name: release_series; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.release_series AS
 SELECT lrs.entity0 AS release,
    lrs.entity1 AS series,
    lrs.id AS relationship,
    lrs.link_order,
    lrs.link,
    COALESCE(latv.text_value, ''::text) AS text_value
   FROM ((((musicbrainz.l_release_series lrs
     JOIN musicbrainz.series s ON ((s.id = lrs.entity1)))
     JOIN musicbrainz.link l ON ((l.id = lrs.link)))
     JOIN musicbrainz.link_type lt ON (((lt.id = l.link_type) AND (lt.gid = '3fa29f01-8e13-3e49-9b0a-ad212aa2f81d'::uuid))))
     LEFT JOIN musicbrainz.link_attribute_text_value latv ON (((latv.attribute_type = s.ordering_attribute) AND (latv.link = l.id))))
  ORDER BY lrs.entity1, lrs.link_order;


--
-- Name: release_status; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_status (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: release_status_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.release_status_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: release_status_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.release_status_id_seq OWNED BY musicbrainz.release_status.id;


--
-- Name: release_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_tag (
    release integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: release_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.release_tag_raw (
    release integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: replication_control; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.replication_control (
    id integer NOT NULL,
    current_schema_sequence integer NOT NULL,
    current_replication_sequence integer,
    last_replication_date timestamp with time zone
);


--
-- Name: replication_control_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.replication_control_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: replication_control_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.replication_control_id_seq OWNED BY musicbrainz.replication_control.id;


--
-- Name: script; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.script (
    id integer NOT NULL,
    iso_code character(4) NOT NULL,
    iso_number character(3) NOT NULL,
    name character varying(100) NOT NULL,
    frequency integer DEFAULT 0 NOT NULL
);


--
-- Name: script_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.script_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.script_id_seq OWNED BY musicbrainz.script.id;


--
-- Name: series_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_alias (
    id integer NOT NULL,
    series integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL)))),
    CONSTRAINT series_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT series_alias_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: series_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_alias_id_seq OWNED BY musicbrainz.series_alias.id;


--
-- Name: series_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: series_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_alias_type_id_seq OWNED BY musicbrainz.series_alias_type.id;


--
-- Name: series_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_annotation (
    series integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: series_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_attribute (
    id integer NOT NULL,
    series integer NOT NULL,
    series_attribute_type integer NOT NULL,
    series_attribute_type_allowed_value integer,
    series_attribute_text text,
    CONSTRAINT series_attribute_check CHECK ((((series_attribute_type_allowed_value IS NULL) AND (series_attribute_text IS NOT NULL)) OR ((series_attribute_type_allowed_value IS NOT NULL) AND (series_attribute_text IS NULL))))
);


--
-- Name: series_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_attribute_id_seq OWNED BY musicbrainz.series_attribute.id;


--
-- Name: series_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: series_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_attribute_type_allowed_value (
    id integer NOT NULL,
    series_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: series_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.series_attribute_type_allowed_value.id;


--
-- Name: series_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_attribute_type_id_seq OWNED BY musicbrainz.series_attribute_type.id;


--
-- Name: series_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: series_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_id_seq OWNED BY musicbrainz.series.id;


--
-- Name: series_ordering_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_ordering_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: series_ordering_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_ordering_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_ordering_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_ordering_type_id_seq OWNED BY musicbrainz.series_ordering_type.id;


--
-- Name: series_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_tag (
    series integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: series_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_tag_raw (
    series integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: series_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.series_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    entity_type character varying(50) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: series_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.series_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: series_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.series_type_id_seq OWNED BY musicbrainz.series_type.id;


--
-- Name: tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.tag (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    ref_count integer DEFAULT 0 NOT NULL
);


--
-- Name: tag_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.tag_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tag_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.tag_id_seq OWNED BY musicbrainz.tag.id;


--
-- Name: tag_relation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.tag_relation (
    tag1 integer NOT NULL,
    tag2 integer NOT NULL,
    weight integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT tag_relation_check CHECK ((tag1 < tag2))
);


--
-- Name: track; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.track (
    id integer NOT NULL,
    gid uuid NOT NULL,
    recording integer NOT NULL,
    medium integer NOT NULL,
    "position" integer NOT NULL,
    number text NOT NULL,
    name character varying NOT NULL,
    artist_credit integer NOT NULL,
    length integer,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    is_data_track boolean DEFAULT false NOT NULL,
    CONSTRAINT track_edits_pending_check CHECK ((edits_pending >= 0)),
    CONSTRAINT track_length_check CHECK (((length IS NULL) OR (length > 0)))
);


--
-- Name: track_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.track_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: track_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.track_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: track_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.track_id_seq OWNED BY musicbrainz.track.id;


--
-- Name: track_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.track_raw (
    id integer NOT NULL,
    release integer NOT NULL,
    title character varying(255) NOT NULL,
    artist character varying(255),
    sequence integer NOT NULL
);


--
-- Name: track_raw_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.track_raw_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: track_raw_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.track_raw_id_seq OWNED BY musicbrainz.track_raw.id;


--
-- Name: url; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.url (
    id integer NOT NULL,
    gid uuid NOT NULL,
    url text NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT url_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: url_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.url_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: url_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.url_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: url_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.url_id_seq OWNED BY musicbrainz.url.id;


--
-- Name: vote; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.vote (
    id integer NOT NULL,
    editor integer NOT NULL,
    edit integer NOT NULL,
    vote smallint NOT NULL,
    vote_time timestamp with time zone DEFAULT now(),
    superseded boolean DEFAULT false NOT NULL
);


--
-- Name: vote_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.vote_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: vote_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.vote_id_seq OWNED BY musicbrainz.vote.id;


--
-- Name: work; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work (
    id integer NOT NULL,
    gid uuid NOT NULL,
    name character varying NOT NULL,
    type integer,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    CONSTRAINT work_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: work_alias; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_alias (
    id integer NOT NULL,
    work integer NOT NULL,
    name character varying NOT NULL,
    locale text,
    edits_pending integer DEFAULT 0 NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    type integer,
    sort_name character varying NOT NULL,
    begin_date_year smallint,
    begin_date_month smallint,
    begin_date_day smallint,
    end_date_year smallint,
    end_date_month smallint,
    end_date_day smallint,
    primary_for_locale boolean DEFAULT false NOT NULL,
    ended boolean DEFAULT false NOT NULL,
    CONSTRAINT primary_check CHECK ((((locale IS NULL) AND (primary_for_locale IS FALSE)) OR (locale IS NOT NULL))),
    CONSTRAINT search_hints_are_empty CHECK (((type <> 2) OR ((type = 2) AND ((sort_name)::text = (name)::text) AND (begin_date_year IS NULL) AND (begin_date_month IS NULL) AND (begin_date_day IS NULL) AND (end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL) AND (primary_for_locale IS FALSE) AND (locale IS NULL)))),
    CONSTRAINT work_alias_check CHECK (((((end_date_year IS NOT NULL) OR (end_date_month IS NOT NULL) OR (end_date_day IS NOT NULL)) AND (ended = true)) OR ((end_date_year IS NULL) AND (end_date_month IS NULL) AND (end_date_day IS NULL)))),
    CONSTRAINT work_alias_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: work_alias_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_alias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_alias_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_alias_id_seq OWNED BY musicbrainz.work_alias.id;


--
-- Name: work_alias_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_alias_type (
    id integer NOT NULL,
    name text NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: work_alias_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_alias_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_alias_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_alias_type_id_seq OWNED BY musicbrainz.work_alias_type.id;


--
-- Name: work_annotation; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_annotation (
    work integer NOT NULL,
    annotation integer NOT NULL
);


--
-- Name: work_attribute; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_attribute (
    id integer NOT NULL,
    work integer NOT NULL,
    work_attribute_type integer NOT NULL,
    work_attribute_type_allowed_value integer,
    work_attribute_text text,
    CONSTRAINT work_attribute_check CHECK ((((work_attribute_type_allowed_value IS NULL) AND (work_attribute_text IS NOT NULL)) OR ((work_attribute_type_allowed_value IS NOT NULL) AND (work_attribute_text IS NULL))))
);


--
-- Name: work_attribute_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_attribute_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_attribute_id_seq OWNED BY musicbrainz.work_attribute.id;


--
-- Name: work_attribute_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_attribute_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    comment character varying(255) DEFAULT ''::character varying NOT NULL,
    free_text boolean NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: work_attribute_type_allowed_value; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_attribute_type_allowed_value (
    id integer NOT NULL,
    work_attribute_type integer NOT NULL,
    value text,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: work_attribute_type_allowed_value_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_attribute_type_allowed_value_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_attribute_type_allowed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_attribute_type_allowed_value_id_seq OWNED BY musicbrainz.work_attribute_type_allowed_value.id;


--
-- Name: work_attribute_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_attribute_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_attribute_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_attribute_type_id_seq OWNED BY musicbrainz.work_attribute_type.id;


--
-- Name: work_gid_redirect; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_gid_redirect (
    gid uuid NOT NULL,
    new_id integer NOT NULL,
    created timestamp with time zone DEFAULT now()
);


--
-- Name: work_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_id_seq OWNED BY musicbrainz.work.id;


--
-- Name: work_language; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_language (
    work integer NOT NULL,
    language integer NOT NULL,
    edits_pending integer DEFAULT 0 NOT NULL,
    created timestamp with time zone DEFAULT now(),
    CONSTRAINT work_language_edits_pending_check CHECK ((edits_pending >= 0))
);


--
-- Name: work_meta; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_meta (
    id integer NOT NULL,
    rating smallint,
    rating_count integer,
    CONSTRAINT work_meta_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: work_rating_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_rating_raw (
    work integer NOT NULL,
    editor integer NOT NULL,
    rating smallint NOT NULL,
    CONSTRAINT work_rating_raw_rating_check CHECK (((rating >= 0) AND (rating <= 100)))
);


--
-- Name: work_series; Type: VIEW; Schema: musicbrainz; Owner: -
--

CREATE VIEW musicbrainz.work_series AS
 SELECT lsw.entity1 AS work,
    lsw.entity0 AS series,
    lsw.id AS relationship,
    lsw.link_order,
    lsw.link,
    COALESCE(latv.text_value, ''::text) AS text_value
   FROM ((((musicbrainz.l_series_work lsw
     JOIN musicbrainz.series s ON ((s.id = lsw.entity0)))
     JOIN musicbrainz.link l ON ((l.id = lsw.link)))
     JOIN musicbrainz.link_type lt ON (((lt.id = l.link_type) AND (lt.gid = 'b0d44366-cdf0-3acb-bee6-0f65a77a6ef0'::uuid))))
     LEFT JOIN musicbrainz.link_attribute_text_value latv ON (((latv.attribute_type = s.ordering_attribute) AND (latv.link = l.id))))
  ORDER BY lsw.entity0, lsw.link_order;


--
-- Name: work_tag; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_tag (
    work integer NOT NULL,
    tag integer NOT NULL,
    count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now()
);


--
-- Name: work_tag_raw; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_tag_raw (
    work integer NOT NULL,
    editor integer NOT NULL,
    tag integer NOT NULL,
    is_upvote boolean DEFAULT true NOT NULL
);


--
-- Name: work_type; Type: TABLE; Schema: musicbrainz; Owner: -
--

CREATE TABLE musicbrainz.work_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    parent integer,
    child_order integer DEFAULT 0 NOT NULL,
    description text,
    gid uuid NOT NULL
);


--
-- Name: work_type_id_seq; Type: SEQUENCE; Schema: musicbrainz; Owner: -
--

CREATE SEQUENCE musicbrainz.work_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: work_type_id_seq; Type: SEQUENCE OWNED BY; Schema: musicbrainz; Owner: -
--

ALTER SEQUENCE musicbrainz.work_type_id_seq OWNED BY musicbrainz.work_type.id;


--
-- Name: log_statistic; Type: TABLE; Schema: statistics; Owner: -
--

CREATE TABLE statistics.log_statistic (
    name text NOT NULL,
    category text NOT NULL,
    "timestamp" timestamp with time zone DEFAULT now() NOT NULL,
    data text NOT NULL
);


--
-- Name: statistic; Type: TABLE; Schema: statistics; Owner: -
--

CREATE TABLE statistics.statistic (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    value integer NOT NULL,
    date_collected date DEFAULT now() NOT NULL
);


--
-- Name: statistic_event; Type: TABLE; Schema: statistics; Owner: -
--

CREATE TABLE statistics.statistic_event (
    date date NOT NULL,
    title text NOT NULL,
    link text NOT NULL,
    description text NOT NULL,
    CONSTRAINT statistic_event_date_check CHECK ((date >= '2000-01-01'::date))
);


--
-- Name: statistic_id_seq; Type: SEQUENCE; Schema: statistics; Owner: -
--

CREATE SEQUENCE statistics.statistic_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: statistic_id_seq; Type: SEQUENCE OWNED BY; Schema: statistics; Owner: -
--

ALTER SEQUENCE statistics.statistic_id_seq OWNED BY statistics.statistic.id;


--
-- Name: wikidocs_index; Type: TABLE; Schema: wikidocs; Owner: -
--

CREATE TABLE wikidocs.wikidocs_index (
    page_name text NOT NULL,
    revision integer NOT NULL
);


--
-- Name: art_type id; Type: DEFAULT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.art_type ALTER COLUMN id SET DEFAULT nextval('cover_art_archive.art_type_id_seq'::regclass);


--
-- Name: alternative_medium id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_medium ALTER COLUMN id SET DEFAULT nextval('musicbrainz.alternative_medium_id_seq'::regclass);


--
-- Name: alternative_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.alternative_release_id_seq'::regclass);


--
-- Name: alternative_release_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_release_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.alternative_release_type_id_seq'::regclass);


--
-- Name: alternative_track id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_track ALTER COLUMN id SET DEFAULT nextval('musicbrainz.alternative_track_id_seq'::regclass);


--
-- Name: annotation id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.annotation ALTER COLUMN id SET DEFAULT nextval('musicbrainz.annotation_id_seq'::regclass);


--
-- Name: application id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.application ALTER COLUMN id SET DEFAULT nextval('musicbrainz.application_id_seq'::regclass);


--
-- Name: area id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_id_seq'::regclass);


--
-- Name: area_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_alias_id_seq'::regclass);


--
-- Name: area_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_alias_type_id_seq'::regclass);


--
-- Name: area_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_attribute_id_seq'::regclass);


--
-- Name: area_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_attribute_type_id_seq'::regclass);


--
-- Name: area_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: area_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.area_type_id_seq'::regclass);


--
-- Name: artist id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_id_seq'::regclass);


--
-- Name: artist_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_alias_id_seq'::regclass);


--
-- Name: artist_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_alias_type_id_seq'::regclass);


--
-- Name: artist_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_attribute_id_seq'::regclass);


--
-- Name: artist_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_attribute_type_id_seq'::regclass);


--
-- Name: artist_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: artist_credit id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_credit ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_credit_id_seq'::regclass);


--
-- Name: artist_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.artist_type_id_seq'::regclass);


--
-- Name: autoeditor_election id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.autoeditor_election ALTER COLUMN id SET DEFAULT nextval('musicbrainz.autoeditor_election_id_seq'::regclass);


--
-- Name: autoeditor_election_vote id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.autoeditor_election_vote ALTER COLUMN id SET DEFAULT nextval('musicbrainz.autoeditor_election_vote_id_seq'::regclass);


--
-- Name: cdtoc id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.cdtoc ALTER COLUMN id SET DEFAULT nextval('musicbrainz.cdtoc_id_seq'::regclass);


--
-- Name: cdtoc_raw id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.cdtoc_raw ALTER COLUMN id SET DEFAULT nextval('musicbrainz.cdtoc_raw_id_seq'::regclass);


--
-- Name: edit id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit ALTER COLUMN id SET DEFAULT nextval('musicbrainz.edit_id_seq'::regclass);


--
-- Name: edit_note id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_note ALTER COLUMN id SET DEFAULT nextval('musicbrainz.edit_note_id_seq'::regclass);


--
-- Name: editor id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_id_seq'::regclass);


--
-- Name: editor_collection id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_collection_id_seq'::regclass);


--
-- Name: editor_collection_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_collection_type_id_seq'::regclass);


--
-- Name: editor_oauth_token id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_oauth_token ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_oauth_token_id_seq'::regclass);


--
-- Name: editor_preference id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_preference ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_preference_id_seq'::regclass);


--
-- Name: editor_subscribe_artist id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_artist ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_subscribe_artist_id_seq'::regclass);


--
-- Name: editor_subscribe_collection id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_collection ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_subscribe_collection_id_seq'::regclass);


--
-- Name: editor_subscribe_editor id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_editor ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_subscribe_editor_id_seq'::regclass);


--
-- Name: editor_subscribe_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_subscribe_label_id_seq'::regclass);


--
-- Name: editor_subscribe_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.editor_subscribe_series_id_seq'::regclass);


--
-- Name: event id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_id_seq'::regclass);


--
-- Name: event_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_alias_id_seq'::regclass);


--
-- Name: event_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_alias_type_id_seq'::regclass);


--
-- Name: event_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_attribute_id_seq'::regclass);


--
-- Name: event_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_attribute_type_id_seq'::regclass);


--
-- Name: event_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: event_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.event_type_id_seq'::regclass);


--
-- Name: gender id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.gender ALTER COLUMN id SET DEFAULT nextval('musicbrainz.gender_id_seq'::regclass);


--
-- Name: genre id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.genre ALTER COLUMN id SET DEFAULT nextval('musicbrainz.genre_id_seq'::regclass);


--
-- Name: genre_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.genre_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.genre_alias_id_seq'::regclass);


--
-- Name: instrument id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_id_seq'::regclass);


--
-- Name: instrument_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_alias_id_seq'::regclass);


--
-- Name: instrument_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_alias_type_id_seq'::regclass);


--
-- Name: instrument_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_attribute_id_seq'::regclass);


--
-- Name: instrument_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_attribute_type_id_seq'::regclass);


--
-- Name: instrument_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: instrument_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.instrument_type_id_seq'::regclass);


--
-- Name: isrc id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.isrc ALTER COLUMN id SET DEFAULT nextval('musicbrainz.isrc_id_seq'::regclass);


--
-- Name: iswc id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.iswc ALTER COLUMN id SET DEFAULT nextval('musicbrainz.iswc_id_seq'::regclass);


--
-- Name: l_area_area id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_area ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_area_id_seq'::regclass);


--
-- Name: l_area_artist id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_artist ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_artist_id_seq'::regclass);


--
-- Name: l_area_event id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_event ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_event_id_seq'::regclass);


--
-- Name: l_area_instrument id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_instrument ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_instrument_id_seq'::regclass);


--
-- Name: l_area_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_label_id_seq'::regclass);


--
-- Name: l_area_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_place_id_seq'::regclass);


--
-- Name: l_area_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_recording_id_seq'::regclass);


--
-- Name: l_area_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_release_id_seq'::regclass);


--
-- Name: l_area_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_release_group_id_seq'::regclass);


--
-- Name: l_area_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_series_id_seq'::regclass);


--
-- Name: l_area_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_url_id_seq'::regclass);


--
-- Name: l_area_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_area_work_id_seq'::regclass);


--
-- Name: l_artist_artist id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_artist ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_artist_id_seq'::regclass);


--
-- Name: l_artist_event id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_event ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_event_id_seq'::regclass);


--
-- Name: l_artist_instrument id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_instrument ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_instrument_id_seq'::regclass);


--
-- Name: l_artist_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_label_id_seq'::regclass);


--
-- Name: l_artist_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_place_id_seq'::regclass);


--
-- Name: l_artist_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_recording_id_seq'::regclass);


--
-- Name: l_artist_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_release_id_seq'::regclass);


--
-- Name: l_artist_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_release_group_id_seq'::regclass);


--
-- Name: l_artist_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_series_id_seq'::regclass);


--
-- Name: l_artist_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_url_id_seq'::regclass);


--
-- Name: l_artist_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_artist_work_id_seq'::regclass);


--
-- Name: l_event_event id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_event ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_event_id_seq'::regclass);


--
-- Name: l_event_instrument id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_instrument ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_instrument_id_seq'::regclass);


--
-- Name: l_event_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_label_id_seq'::regclass);


--
-- Name: l_event_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_place_id_seq'::regclass);


--
-- Name: l_event_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_recording_id_seq'::regclass);


--
-- Name: l_event_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_release_id_seq'::regclass);


--
-- Name: l_event_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_release_group_id_seq'::regclass);


--
-- Name: l_event_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_series_id_seq'::regclass);


--
-- Name: l_event_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_url_id_seq'::regclass);


--
-- Name: l_event_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_event_work_id_seq'::regclass);


--
-- Name: l_instrument_instrument id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_instrument ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_instrument_id_seq'::regclass);


--
-- Name: l_instrument_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_label_id_seq'::regclass);


--
-- Name: l_instrument_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_place_id_seq'::regclass);


--
-- Name: l_instrument_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_recording_id_seq'::regclass);


--
-- Name: l_instrument_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_release_id_seq'::regclass);


--
-- Name: l_instrument_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_release_group_id_seq'::regclass);


--
-- Name: l_instrument_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_series_id_seq'::regclass);


--
-- Name: l_instrument_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_url_id_seq'::regclass);


--
-- Name: l_instrument_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_instrument_work_id_seq'::regclass);


--
-- Name: l_label_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_label_id_seq'::regclass);


--
-- Name: l_label_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_place_id_seq'::regclass);


--
-- Name: l_label_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_recording_id_seq'::regclass);


--
-- Name: l_label_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_release_id_seq'::regclass);


--
-- Name: l_label_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_release_group_id_seq'::regclass);


--
-- Name: l_label_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_series_id_seq'::regclass);


--
-- Name: l_label_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_url_id_seq'::regclass);


--
-- Name: l_label_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_label_work_id_seq'::regclass);


--
-- Name: l_place_place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_place_id_seq'::regclass);


--
-- Name: l_place_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_recording_id_seq'::regclass);


--
-- Name: l_place_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_release_id_seq'::regclass);


--
-- Name: l_place_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_release_group_id_seq'::regclass);


--
-- Name: l_place_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_series_id_seq'::regclass);


--
-- Name: l_place_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_url_id_seq'::regclass);


--
-- Name: l_place_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_place_work_id_seq'::regclass);


--
-- Name: l_recording_recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_recording_id_seq'::regclass);


--
-- Name: l_recording_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_release_id_seq'::regclass);


--
-- Name: l_recording_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_release_group_id_seq'::regclass);


--
-- Name: l_recording_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_series_id_seq'::regclass);


--
-- Name: l_recording_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_url_id_seq'::regclass);


--
-- Name: l_recording_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_recording_work_id_seq'::regclass);


--
-- Name: l_release_group_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_group_release_group_id_seq'::regclass);


--
-- Name: l_release_group_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_group_series_id_seq'::regclass);


--
-- Name: l_release_group_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_group_url_id_seq'::regclass);


--
-- Name: l_release_group_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_group_work_id_seq'::regclass);


--
-- Name: l_release_release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_release_id_seq'::regclass);


--
-- Name: l_release_release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_release_group_id_seq'::regclass);


--
-- Name: l_release_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_series_id_seq'::regclass);


--
-- Name: l_release_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_url_id_seq'::regclass);


--
-- Name: l_release_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_release_work_id_seq'::regclass);


--
-- Name: l_series_series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_series_series_id_seq'::regclass);


--
-- Name: l_series_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_series_url_id_seq'::regclass);


--
-- Name: l_series_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_series_work_id_seq'::regclass);


--
-- Name: l_url_url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_url_url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_url_url_id_seq'::regclass);


--
-- Name: l_url_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_url_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_url_work_id_seq'::regclass);


--
-- Name: l_work_work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_work_work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.l_work_work_id_seq'::regclass);


--
-- Name: label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_id_seq'::regclass);


--
-- Name: label_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_alias_id_seq'::regclass);


--
-- Name: label_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_alias_type_id_seq'::regclass);


--
-- Name: label_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_attribute_id_seq'::regclass);


--
-- Name: label_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_attribute_type_id_seq'::regclass);


--
-- Name: label_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: label_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.label_type_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.language ALTER COLUMN id SET DEFAULT nextval('musicbrainz.language_id_seq'::regclass);


--
-- Name: link id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link ALTER COLUMN id SET DEFAULT nextval('musicbrainz.link_id_seq'::regclass);


--
-- Name: link_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.link_attribute_type_id_seq'::regclass);


--
-- Name: link_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.link_type_id_seq'::regclass);


--
-- Name: medium id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_id_seq'::regclass);


--
-- Name: medium_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_attribute_id_seq'::regclass);


--
-- Name: medium_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_attribute_type_id_seq'::regclass);


--
-- Name: medium_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: medium_cdtoc id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_cdtoc ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_cdtoc_id_seq'::regclass);


--
-- Name: medium_format id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_format ALTER COLUMN id SET DEFAULT nextval('musicbrainz.medium_format_id_seq'::regclass);


--
-- Name: place id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_id_seq'::regclass);


--
-- Name: place_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_alias_id_seq'::regclass);


--
-- Name: place_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_alias_type_id_seq'::regclass);


--
-- Name: place_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_attribute_id_seq'::regclass);


--
-- Name: place_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_attribute_type_id_seq'::regclass);


--
-- Name: place_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: place_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.place_type_id_seq'::regclass);


--
-- Name: recording id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_id_seq'::regclass);


--
-- Name: recording_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_alias_id_seq'::regclass);


--
-- Name: recording_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_alias_type_id_seq'::regclass);


--
-- Name: recording_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_attribute_id_seq'::regclass);


--
-- Name: recording_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_attribute_type_id_seq'::regclass);


--
-- Name: recording_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.recording_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: release id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_id_seq'::regclass);


--
-- Name: release_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_alias_id_seq'::regclass);


--
-- Name: release_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_alias_type_id_seq'::regclass);


--
-- Name: release_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_attribute_id_seq'::regclass);


--
-- Name: release_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_attribute_type_id_seq'::regclass);


--
-- Name: release_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: release_group id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_id_seq'::regclass);


--
-- Name: release_group_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_alias_id_seq'::regclass);


--
-- Name: release_group_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_alias_type_id_seq'::regclass);


--
-- Name: release_group_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_attribute_id_seq'::regclass);


--
-- Name: release_group_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_attribute_type_id_seq'::regclass);


--
-- Name: release_group_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: release_group_primary_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_primary_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_primary_type_id_seq'::regclass);


--
-- Name: release_group_secondary_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_secondary_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_group_secondary_type_id_seq'::regclass);


--
-- Name: release_label id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_label ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_label_id_seq'::regclass);


--
-- Name: release_packaging id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_packaging ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_packaging_id_seq'::regclass);


--
-- Name: release_raw id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_raw ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_raw_id_seq'::regclass);


--
-- Name: release_status id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_status ALTER COLUMN id SET DEFAULT nextval('musicbrainz.release_status_id_seq'::regclass);


--
-- Name: replication_control id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.replication_control ALTER COLUMN id SET DEFAULT nextval('musicbrainz.replication_control_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.script ALTER COLUMN id SET DEFAULT nextval('musicbrainz.script_id_seq'::regclass);


--
-- Name: series id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_id_seq'::regclass);


--
-- Name: series_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_alias_id_seq'::regclass);


--
-- Name: series_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_alias_type_id_seq'::regclass);


--
-- Name: series_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_attribute_id_seq'::regclass);


--
-- Name: series_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_attribute_type_id_seq'::regclass);


--
-- Name: series_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: series_ordering_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_ordering_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_ordering_type_id_seq'::regclass);


--
-- Name: series_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.series_type_id_seq'::regclass);


--
-- Name: tag id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.tag ALTER COLUMN id SET DEFAULT nextval('musicbrainz.tag_id_seq'::regclass);


--
-- Name: track id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.track ALTER COLUMN id SET DEFAULT nextval('musicbrainz.track_id_seq'::regclass);


--
-- Name: track_raw id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.track_raw ALTER COLUMN id SET DEFAULT nextval('musicbrainz.track_raw_id_seq'::regclass);


--
-- Name: url id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.url ALTER COLUMN id SET DEFAULT nextval('musicbrainz.url_id_seq'::regclass);


--
-- Name: vote id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.vote ALTER COLUMN id SET DEFAULT nextval('musicbrainz.vote_id_seq'::regclass);


--
-- Name: work id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_id_seq'::regclass);


--
-- Name: work_alias id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_alias ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_alias_id_seq'::regclass);


--
-- Name: work_alias_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_alias_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_alias_type_id_seq'::regclass);


--
-- Name: work_attribute id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_attribute_id_seq'::regclass);


--
-- Name: work_attribute_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_attribute_type_id_seq'::regclass);


--
-- Name: work_attribute_type_allowed_value id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute_type_allowed_value ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_attribute_type_allowed_value_id_seq'::regclass);


--
-- Name: work_type id; Type: DEFAULT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_type ALTER COLUMN id SET DEFAULT nextval('musicbrainz.work_type_id_seq'::regclass);


--
-- Name: statistic id; Type: DEFAULT; Schema: statistics; Owner: -
--

ALTER TABLE ONLY statistics.statistic ALTER COLUMN id SET DEFAULT nextval('statistics.statistic_id_seq'::regclass);


--
-- Name: art_type art_type_pkey; Type: CONSTRAINT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.art_type
    ADD CONSTRAINT art_type_pkey PRIMARY KEY (id);


--
-- Name: cover_art cover_art_pkey; Type: CONSTRAINT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.cover_art
    ADD CONSTRAINT cover_art_pkey PRIMARY KEY (id);


--
-- Name: cover_art_type cover_art_type_pkey; Type: CONSTRAINT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.cover_art_type
    ADD CONSTRAINT cover_art_type_pkey PRIMARY KEY (id, type_id);


--
-- Name: image_type image_type_pkey; Type: CONSTRAINT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.image_type
    ADD CONSTRAINT image_type_pkey PRIMARY KEY (mime_type);


--
-- Name: release_group_cover_art release_group_cover_art_pkey; Type: CONSTRAINT; Schema: cover_art_archive; Owner: -
--

ALTER TABLE ONLY cover_art_archive.release_group_cover_art
    ADD CONSTRAINT release_group_cover_art_pkey PRIMARY KEY (release_group);


--
-- Name: l_area_area_example l_area_area_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_area_example
    ADD CONSTRAINT l_area_area_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_artist_example l_area_artist_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_artist_example
    ADD CONSTRAINT l_area_artist_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_event_example l_area_event_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_event_example
    ADD CONSTRAINT l_area_event_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_instrument_example l_area_instrument_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_instrument_example
    ADD CONSTRAINT l_area_instrument_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_label_example l_area_label_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_label_example
    ADD CONSTRAINT l_area_label_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_place_example l_area_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_place_example
    ADD CONSTRAINT l_area_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_recording_example l_area_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_recording_example
    ADD CONSTRAINT l_area_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_release_example l_area_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_release_example
    ADD CONSTRAINT l_area_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_release_group_example l_area_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_release_group_example
    ADD CONSTRAINT l_area_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_series_example l_area_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_series_example
    ADD CONSTRAINT l_area_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_url_example l_area_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_url_example
    ADD CONSTRAINT l_area_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_area_work_example l_area_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_area_work_example
    ADD CONSTRAINT l_area_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_artist_example l_artist_artist_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_artist_example
    ADD CONSTRAINT l_artist_artist_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_event_example l_artist_event_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_event_example
    ADD CONSTRAINT l_artist_event_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_instrument_example l_artist_instrument_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_instrument_example
    ADD CONSTRAINT l_artist_instrument_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_label_example l_artist_label_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_label_example
    ADD CONSTRAINT l_artist_label_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_place_example l_artist_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_place_example
    ADD CONSTRAINT l_artist_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_recording_example l_artist_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_recording_example
    ADD CONSTRAINT l_artist_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_release_example l_artist_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_release_example
    ADD CONSTRAINT l_artist_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_release_group_example l_artist_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_release_group_example
    ADD CONSTRAINT l_artist_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_series_example l_artist_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_series_example
    ADD CONSTRAINT l_artist_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_url_example l_artist_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_url_example
    ADD CONSTRAINT l_artist_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_artist_work_example l_artist_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_artist_work_example
    ADD CONSTRAINT l_artist_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_event_example l_event_event_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_event_example
    ADD CONSTRAINT l_event_event_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_instrument_example l_event_instrument_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_instrument_example
    ADD CONSTRAINT l_event_instrument_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_label_example l_event_label_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_label_example
    ADD CONSTRAINT l_event_label_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_place_example l_event_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_place_example
    ADD CONSTRAINT l_event_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_recording_example l_event_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_recording_example
    ADD CONSTRAINT l_event_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_release_example l_event_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_release_example
    ADD CONSTRAINT l_event_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_release_group_example l_event_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_release_group_example
    ADD CONSTRAINT l_event_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_series_example l_event_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_series_example
    ADD CONSTRAINT l_event_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_url_example l_event_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_url_example
    ADD CONSTRAINT l_event_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_event_work_example l_event_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_event_work_example
    ADD CONSTRAINT l_event_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_instrument_example l_instrument_instrument_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_instrument_example
    ADD CONSTRAINT l_instrument_instrument_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_label_example l_instrument_label_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_label_example
    ADD CONSTRAINT l_instrument_label_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_place_example l_instrument_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_place_example
    ADD CONSTRAINT l_instrument_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_recording_example l_instrument_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_recording_example
    ADD CONSTRAINT l_instrument_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_release_example l_instrument_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_release_example
    ADD CONSTRAINT l_instrument_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_release_group_example l_instrument_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_release_group_example
    ADD CONSTRAINT l_instrument_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_series_example l_instrument_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_series_example
    ADD CONSTRAINT l_instrument_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_url_example l_instrument_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_url_example
    ADD CONSTRAINT l_instrument_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_work_example l_instrument_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_instrument_work_example
    ADD CONSTRAINT l_instrument_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_label_example l_label_label_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_label_example
    ADD CONSTRAINT l_label_label_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_place_example l_label_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_place_example
    ADD CONSTRAINT l_label_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_recording_example l_label_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_recording_example
    ADD CONSTRAINT l_label_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_release_example l_label_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_release_example
    ADD CONSTRAINT l_label_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_release_group_example l_label_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_release_group_example
    ADD CONSTRAINT l_label_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_series_example l_label_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_series_example
    ADD CONSTRAINT l_label_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_url_example l_label_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_url_example
    ADD CONSTRAINT l_label_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_label_work_example l_label_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_label_work_example
    ADD CONSTRAINT l_label_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_place_example l_place_place_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_place_example
    ADD CONSTRAINT l_place_place_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_recording_example l_place_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_recording_example
    ADD CONSTRAINT l_place_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_release_example l_place_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_release_example
    ADD CONSTRAINT l_place_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_release_group_example l_place_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_release_group_example
    ADD CONSTRAINT l_place_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_series_example l_place_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_series_example
    ADD CONSTRAINT l_place_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_url_example l_place_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_url_example
    ADD CONSTRAINT l_place_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_place_work_example l_place_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_place_work_example
    ADD CONSTRAINT l_place_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_recording_example l_recording_recording_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_recording_example
    ADD CONSTRAINT l_recording_recording_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_release_example l_recording_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_release_example
    ADD CONSTRAINT l_recording_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_release_group_example l_recording_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_release_group_example
    ADD CONSTRAINT l_recording_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_series_example l_recording_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_series_example
    ADD CONSTRAINT l_recording_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_url_example l_recording_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_url_example
    ADD CONSTRAINT l_recording_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_recording_work_example l_recording_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_recording_work_example
    ADD CONSTRAINT l_recording_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_release_group_example l_release_group_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_group_release_group_example
    ADD CONSTRAINT l_release_group_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_series_example l_release_group_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_group_series_example
    ADD CONSTRAINT l_release_group_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_url_example l_release_group_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_group_url_example
    ADD CONSTRAINT l_release_group_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_work_example l_release_group_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_group_work_example
    ADD CONSTRAINT l_release_group_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_release_example l_release_release_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_release_example
    ADD CONSTRAINT l_release_release_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_release_group_example l_release_release_group_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_release_group_example
    ADD CONSTRAINT l_release_release_group_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_series_example l_release_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_series_example
    ADD CONSTRAINT l_release_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_url_example l_release_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_url_example
    ADD CONSTRAINT l_release_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_release_work_example l_release_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_release_work_example
    ADD CONSTRAINT l_release_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_series_series_example l_series_series_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_series_series_example
    ADD CONSTRAINT l_series_series_example_pkey PRIMARY KEY (id);


--
-- Name: l_series_url_example l_series_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_series_url_example
    ADD CONSTRAINT l_series_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_series_work_example l_series_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_series_work_example
    ADD CONSTRAINT l_series_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_url_url_example l_url_url_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_url_url_example
    ADD CONSTRAINT l_url_url_example_pkey PRIMARY KEY (id);


--
-- Name: l_url_work_example l_url_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_url_work_example
    ADD CONSTRAINT l_url_work_example_pkey PRIMARY KEY (id);


--
-- Name: l_work_work_example l_work_work_example_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.l_work_work_example
    ADD CONSTRAINT l_work_work_example_pkey PRIMARY KEY (id);


--
-- Name: link_type_documentation link_type_documentation_pkey; Type: CONSTRAINT; Schema: documentation; Owner: -
--

ALTER TABLE ONLY documentation.link_type_documentation
    ADD CONSTRAINT link_type_documentation_pkey PRIMARY KEY (id);


--
-- Name: alternative_medium alternative_medium_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_medium
    ADD CONSTRAINT alternative_medium_pkey PRIMARY KEY (id);


--
-- Name: alternative_medium_track alternative_medium_track_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_medium_track
    ADD CONSTRAINT alternative_medium_track_pkey PRIMARY KEY (alternative_medium, track);


--
-- Name: alternative_release alternative_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_release
    ADD CONSTRAINT alternative_release_pkey PRIMARY KEY (id);


--
-- Name: alternative_release_type alternative_release_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_release_type
    ADD CONSTRAINT alternative_release_type_pkey PRIMARY KEY (id);


--
-- Name: alternative_track alternative_track_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.alternative_track
    ADD CONSTRAINT alternative_track_pkey PRIMARY KEY (id);


--
-- Name: annotation annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.annotation
    ADD CONSTRAINT annotation_pkey PRIMARY KEY (id);


--
-- Name: application application_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.application
    ADD CONSTRAINT application_pkey PRIMARY KEY (id);


--
-- Name: area_alias area_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_alias
    ADD CONSTRAINT area_alias_pkey PRIMARY KEY (id);


--
-- Name: area_alias_type area_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_alias_type
    ADD CONSTRAINT area_alias_type_pkey PRIMARY KEY (id);


--
-- Name: area_annotation area_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_annotation
    ADD CONSTRAINT area_annotation_pkey PRIMARY KEY (area, annotation);


--
-- Name: area_attribute area_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute
    ADD CONSTRAINT area_attribute_pkey PRIMARY KEY (id);


--
-- Name: area_attribute_type_allowed_value area_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute_type_allowed_value
    ADD CONSTRAINT area_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: area_attribute_type area_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_attribute_type
    ADD CONSTRAINT area_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: area_gid_redirect area_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_gid_redirect
    ADD CONSTRAINT area_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: area area_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area
    ADD CONSTRAINT area_pkey PRIMARY KEY (id);


--
-- Name: area_tag area_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_tag
    ADD CONSTRAINT area_tag_pkey PRIMARY KEY (area, tag);


--
-- Name: area_tag_raw area_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_tag_raw
    ADD CONSTRAINT area_tag_raw_pkey PRIMARY KEY (area, editor, tag);


--
-- Name: area_type area_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.area_type
    ADD CONSTRAINT area_type_pkey PRIMARY KEY (id);


--
-- Name: artist_alias artist_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_alias
    ADD CONSTRAINT artist_alias_pkey PRIMARY KEY (id);


--
-- Name: artist_alias_type artist_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_alias_type
    ADD CONSTRAINT artist_alias_type_pkey PRIMARY KEY (id);


--
-- Name: artist_annotation artist_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_annotation
    ADD CONSTRAINT artist_annotation_pkey PRIMARY KEY (artist, annotation);


--
-- Name: artist_attribute artist_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute
    ADD CONSTRAINT artist_attribute_pkey PRIMARY KEY (id);


--
-- Name: artist_attribute_type_allowed_value artist_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute_type_allowed_value
    ADD CONSTRAINT artist_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: artist_attribute_type artist_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_attribute_type
    ADD CONSTRAINT artist_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: artist_credit_name artist_credit_name_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_credit_name
    ADD CONSTRAINT artist_credit_name_pkey PRIMARY KEY (artist_credit, "position");


--
-- Name: artist_credit artist_credit_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_credit
    ADD CONSTRAINT artist_credit_pkey PRIMARY KEY (id);


--
-- Name: artist_gid_redirect artist_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_gid_redirect
    ADD CONSTRAINT artist_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: artist_ipi artist_ipi_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_ipi
    ADD CONSTRAINT artist_ipi_pkey PRIMARY KEY (artist, ipi);


--
-- Name: artist_isni artist_isni_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_isni
    ADD CONSTRAINT artist_isni_pkey PRIMARY KEY (artist, isni);


--
-- Name: artist_meta artist_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_meta
    ADD CONSTRAINT artist_meta_pkey PRIMARY KEY (id);


--
-- Name: artist artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist
    ADD CONSTRAINT artist_pkey PRIMARY KEY (id);


--
-- Name: artist_rating_raw artist_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_rating_raw
    ADD CONSTRAINT artist_rating_raw_pkey PRIMARY KEY (artist, editor);


--
-- Name: artist_tag artist_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_tag
    ADD CONSTRAINT artist_tag_pkey PRIMARY KEY (artist, tag);


--
-- Name: artist_tag_raw artist_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_tag_raw
    ADD CONSTRAINT artist_tag_raw_pkey PRIMARY KEY (artist, editor, tag);


--
-- Name: artist_type artist_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.artist_type
    ADD CONSTRAINT artist_type_pkey PRIMARY KEY (id);


--
-- Name: autoeditor_election autoeditor_election_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.autoeditor_election
    ADD CONSTRAINT autoeditor_election_pkey PRIMARY KEY (id);


--
-- Name: autoeditor_election_vote autoeditor_election_vote_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.autoeditor_election_vote
    ADD CONSTRAINT autoeditor_election_vote_pkey PRIMARY KEY (id);


--
-- Name: cdtoc cdtoc_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.cdtoc
    ADD CONSTRAINT cdtoc_pkey PRIMARY KEY (id);


--
-- Name: cdtoc_raw cdtoc_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.cdtoc_raw
    ADD CONSTRAINT cdtoc_raw_pkey PRIMARY KEY (id);


--
-- Name: country_area country_area_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.country_area
    ADD CONSTRAINT country_area_pkey PRIMARY KEY (area);


--
-- Name: deleted_entity deleted_entity_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.deleted_entity
    ADD CONSTRAINT deleted_entity_pkey PRIMARY KEY (gid);


--
-- Name: edit_area edit_area_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_area
    ADD CONSTRAINT edit_area_pkey PRIMARY KEY (edit, area);


--
-- Name: edit_artist edit_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_artist
    ADD CONSTRAINT edit_artist_pkey PRIMARY KEY (edit, artist);


--
-- Name: edit_data edit_data_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_data
    ADD CONSTRAINT edit_data_pkey PRIMARY KEY (edit);


--
-- Name: edit_event edit_event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_event
    ADD CONSTRAINT edit_event_pkey PRIMARY KEY (edit, event);


--
-- Name: edit_instrument edit_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_instrument
    ADD CONSTRAINT edit_instrument_pkey PRIMARY KEY (edit, instrument);


--
-- Name: edit_label edit_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_label
    ADD CONSTRAINT edit_label_pkey PRIMARY KEY (edit, label);


--
-- Name: edit_note edit_note_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_note
    ADD CONSTRAINT edit_note_pkey PRIMARY KEY (id);


--
-- Name: edit_note_recipient edit_note_recipient_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_note_recipient
    ADD CONSTRAINT edit_note_recipient_pkey PRIMARY KEY (recipient, edit_note);


--
-- Name: edit edit_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit
    ADD CONSTRAINT edit_pkey PRIMARY KEY (id);


--
-- Name: edit_place edit_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_place
    ADD CONSTRAINT edit_place_pkey PRIMARY KEY (edit, place);


--
-- Name: edit_recording edit_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_recording
    ADD CONSTRAINT edit_recording_pkey PRIMARY KEY (edit, recording);


--
-- Name: edit_release_group edit_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_release_group
    ADD CONSTRAINT edit_release_group_pkey PRIMARY KEY (edit, release_group);


--
-- Name: edit_release edit_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_release
    ADD CONSTRAINT edit_release_pkey PRIMARY KEY (edit, release);


--
-- Name: edit_series edit_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_series
    ADD CONSTRAINT edit_series_pkey PRIMARY KEY (edit, series);


--
-- Name: edit_url edit_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_url
    ADD CONSTRAINT edit_url_pkey PRIMARY KEY (edit, url);


--
-- Name: edit_work edit_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.edit_work
    ADD CONSTRAINT edit_work_pkey PRIMARY KEY (edit, work);


--
-- Name: editor_collection_area editor_collection_area_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_area
    ADD CONSTRAINT editor_collection_area_pkey PRIMARY KEY (collection, area);


--
-- Name: editor_collection_artist editor_collection_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_artist
    ADD CONSTRAINT editor_collection_artist_pkey PRIMARY KEY (collection, artist);


--
-- Name: editor_collection_collaborator editor_collection_collaborator_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_collaborator
    ADD CONSTRAINT editor_collection_collaborator_pkey PRIMARY KEY (collection, editor);


--
-- Name: editor_collection_deleted_entity editor_collection_deleted_entity_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_deleted_entity
    ADD CONSTRAINT editor_collection_deleted_entity_pkey PRIMARY KEY (collection, gid);


--
-- Name: editor_collection_event editor_collection_event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_event
    ADD CONSTRAINT editor_collection_event_pkey PRIMARY KEY (collection, event);


--
-- Name: editor_collection_instrument editor_collection_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_instrument
    ADD CONSTRAINT editor_collection_instrument_pkey PRIMARY KEY (collection, instrument);


--
-- Name: editor_collection_label editor_collection_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_label
    ADD CONSTRAINT editor_collection_label_pkey PRIMARY KEY (collection, label);


--
-- Name: editor_collection editor_collection_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection
    ADD CONSTRAINT editor_collection_pkey PRIMARY KEY (id);


--
-- Name: editor_collection_place editor_collection_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_place
    ADD CONSTRAINT editor_collection_place_pkey PRIMARY KEY (collection, place);


--
-- Name: editor_collection_recording editor_collection_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_recording
    ADD CONSTRAINT editor_collection_recording_pkey PRIMARY KEY (collection, recording);


--
-- Name: editor_collection_release_group editor_collection_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_release_group
    ADD CONSTRAINT editor_collection_release_group_pkey PRIMARY KEY (collection, release_group);


--
-- Name: editor_collection_release editor_collection_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_release
    ADD CONSTRAINT editor_collection_release_pkey PRIMARY KEY (collection, release);


--
-- Name: editor_collection_series editor_collection_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_series
    ADD CONSTRAINT editor_collection_series_pkey PRIMARY KEY (collection, series);


--
-- Name: editor_collection_type editor_collection_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_type
    ADD CONSTRAINT editor_collection_type_pkey PRIMARY KEY (id);


--
-- Name: editor_collection_work editor_collection_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_collection_work
    ADD CONSTRAINT editor_collection_work_pkey PRIMARY KEY (collection, work);


--
-- Name: editor_language editor_language_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_language
    ADD CONSTRAINT editor_language_pkey PRIMARY KEY (editor, language);


--
-- Name: editor_oauth_token editor_oauth_token_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_oauth_token
    ADD CONSTRAINT editor_oauth_token_pkey PRIMARY KEY (id);


--
-- Name: editor editor_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor
    ADD CONSTRAINT editor_pkey PRIMARY KEY (id);


--
-- Name: editor_preference editor_preference_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_preference
    ADD CONSTRAINT editor_preference_pkey PRIMARY KEY (id);


--
-- Name: editor_subscribe_artist_deleted editor_subscribe_artist_deleted_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_artist_deleted
    ADD CONSTRAINT editor_subscribe_artist_deleted_pkey PRIMARY KEY (editor, gid);


--
-- Name: editor_subscribe_artist editor_subscribe_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_artist
    ADD CONSTRAINT editor_subscribe_artist_pkey PRIMARY KEY (id);


--
-- Name: editor_subscribe_collection editor_subscribe_collection_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_collection
    ADD CONSTRAINT editor_subscribe_collection_pkey PRIMARY KEY (id);


--
-- Name: editor_subscribe_editor editor_subscribe_editor_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_editor
    ADD CONSTRAINT editor_subscribe_editor_pkey PRIMARY KEY (id);


--
-- Name: editor_subscribe_label_deleted editor_subscribe_label_deleted_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_label_deleted
    ADD CONSTRAINT editor_subscribe_label_deleted_pkey PRIMARY KEY (editor, gid);


--
-- Name: editor_subscribe_label editor_subscribe_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_label
    ADD CONSTRAINT editor_subscribe_label_pkey PRIMARY KEY (id);


--
-- Name: editor_subscribe_series_deleted editor_subscribe_series_deleted_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_series_deleted
    ADD CONSTRAINT editor_subscribe_series_deleted_pkey PRIMARY KEY (editor, gid);


--
-- Name: editor_subscribe_series editor_subscribe_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_subscribe_series
    ADD CONSTRAINT editor_subscribe_series_pkey PRIMARY KEY (id);


--
-- Name: editor_watch_artist editor_watch_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_watch_artist
    ADD CONSTRAINT editor_watch_artist_pkey PRIMARY KEY (artist, editor);


--
-- Name: editor_watch_preferences editor_watch_preferences_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_watch_preferences
    ADD CONSTRAINT editor_watch_preferences_pkey PRIMARY KEY (editor);


--
-- Name: editor_watch_release_group_type editor_watch_release_group_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_watch_release_group_type
    ADD CONSTRAINT editor_watch_release_group_type_pkey PRIMARY KEY (editor, release_group_type);


--
-- Name: editor_watch_release_status editor_watch_release_status_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.editor_watch_release_status
    ADD CONSTRAINT editor_watch_release_status_pkey PRIMARY KEY (editor, release_status);


--
-- Name: event_alias event_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_alias
    ADD CONSTRAINT event_alias_pkey PRIMARY KEY (id);


--
-- Name: event_alias_type event_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_alias_type
    ADD CONSTRAINT event_alias_type_pkey PRIMARY KEY (id);


--
-- Name: event_annotation event_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_annotation
    ADD CONSTRAINT event_annotation_pkey PRIMARY KEY (event, annotation);


--
-- Name: event_attribute event_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute
    ADD CONSTRAINT event_attribute_pkey PRIMARY KEY (id);


--
-- Name: event_attribute_type_allowed_value event_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute_type_allowed_value
    ADD CONSTRAINT event_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: event_attribute_type event_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_attribute_type
    ADD CONSTRAINT event_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: event_gid_redirect event_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_gid_redirect
    ADD CONSTRAINT event_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: event_meta event_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_meta
    ADD CONSTRAINT event_meta_pkey PRIMARY KEY (id);


--
-- Name: event event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


--
-- Name: event_rating_raw event_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_rating_raw
    ADD CONSTRAINT event_rating_raw_pkey PRIMARY KEY (event, editor);


--
-- Name: event_tag event_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_tag
    ADD CONSTRAINT event_tag_pkey PRIMARY KEY (event, tag);


--
-- Name: event_tag_raw event_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_tag_raw
    ADD CONSTRAINT event_tag_raw_pkey PRIMARY KEY (event, editor, tag);


--
-- Name: event_type event_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.event_type
    ADD CONSTRAINT event_type_pkey PRIMARY KEY (id);


--
-- Name: gender gender_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.gender
    ADD CONSTRAINT gender_pkey PRIMARY KEY (id);


--
-- Name: genre_alias genre_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.genre_alias
    ADD CONSTRAINT genre_alias_pkey PRIMARY KEY (id);


--
-- Name: genre genre_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.genre
    ADD CONSTRAINT genre_pkey PRIMARY KEY (id);


--
-- Name: instrument_alias instrument_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_alias
    ADD CONSTRAINT instrument_alias_pkey PRIMARY KEY (id);


--
-- Name: instrument_alias_type instrument_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_alias_type
    ADD CONSTRAINT instrument_alias_type_pkey PRIMARY KEY (id);


--
-- Name: instrument_annotation instrument_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_annotation
    ADD CONSTRAINT instrument_annotation_pkey PRIMARY KEY (instrument, annotation);


--
-- Name: instrument_attribute instrument_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute
    ADD CONSTRAINT instrument_attribute_pkey PRIMARY KEY (id);


--
-- Name: instrument_attribute_type_allowed_value instrument_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute_type_allowed_value
    ADD CONSTRAINT instrument_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: instrument_attribute_type instrument_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_attribute_type
    ADD CONSTRAINT instrument_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: instrument_gid_redirect instrument_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_gid_redirect
    ADD CONSTRAINT instrument_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: instrument instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument
    ADD CONSTRAINT instrument_pkey PRIMARY KEY (id);


--
-- Name: instrument_tag instrument_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_tag
    ADD CONSTRAINT instrument_tag_pkey PRIMARY KEY (instrument, tag);


--
-- Name: instrument_tag_raw instrument_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_tag_raw
    ADD CONSTRAINT instrument_tag_raw_pkey PRIMARY KEY (instrument, editor, tag);


--
-- Name: instrument_type instrument_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.instrument_type
    ADD CONSTRAINT instrument_type_pkey PRIMARY KEY (id);


--
-- Name: iso_3166_1 iso_3166_1_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.iso_3166_1
    ADD CONSTRAINT iso_3166_1_pkey PRIMARY KEY (code);


--
-- Name: iso_3166_2 iso_3166_2_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.iso_3166_2
    ADD CONSTRAINT iso_3166_2_pkey PRIMARY KEY (code);


--
-- Name: iso_3166_3 iso_3166_3_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.iso_3166_3
    ADD CONSTRAINT iso_3166_3_pkey PRIMARY KEY (code);


--
-- Name: isrc isrc_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.isrc
    ADD CONSTRAINT isrc_pkey PRIMARY KEY (id);


--
-- Name: iswc iswc_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.iswc
    ADD CONSTRAINT iswc_pkey PRIMARY KEY (id);


--
-- Name: l_area_area l_area_area_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_area
    ADD CONSTRAINT l_area_area_pkey PRIMARY KEY (id);


--
-- Name: l_area_artist l_area_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_artist
    ADD CONSTRAINT l_area_artist_pkey PRIMARY KEY (id);


--
-- Name: l_area_event l_area_event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_event
    ADD CONSTRAINT l_area_event_pkey PRIMARY KEY (id);


--
-- Name: l_area_instrument l_area_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_instrument
    ADD CONSTRAINT l_area_instrument_pkey PRIMARY KEY (id);


--
-- Name: l_area_label l_area_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_label
    ADD CONSTRAINT l_area_label_pkey PRIMARY KEY (id);


--
-- Name: l_area_place l_area_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_place
    ADD CONSTRAINT l_area_place_pkey PRIMARY KEY (id);


--
-- Name: l_area_recording l_area_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_recording
    ADD CONSTRAINT l_area_recording_pkey PRIMARY KEY (id);


--
-- Name: l_area_release_group l_area_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_release_group
    ADD CONSTRAINT l_area_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_area_release l_area_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_release
    ADD CONSTRAINT l_area_release_pkey PRIMARY KEY (id);


--
-- Name: l_area_series l_area_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_series
    ADD CONSTRAINT l_area_series_pkey PRIMARY KEY (id);


--
-- Name: l_area_url l_area_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_url
    ADD CONSTRAINT l_area_url_pkey PRIMARY KEY (id);


--
-- Name: l_area_work l_area_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_area_work
    ADD CONSTRAINT l_area_work_pkey PRIMARY KEY (id);


--
-- Name: l_artist_artist l_artist_artist_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_artist
    ADD CONSTRAINT l_artist_artist_pkey PRIMARY KEY (id);


--
-- Name: l_artist_event l_artist_event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_event
    ADD CONSTRAINT l_artist_event_pkey PRIMARY KEY (id);


--
-- Name: l_artist_instrument l_artist_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_instrument
    ADD CONSTRAINT l_artist_instrument_pkey PRIMARY KEY (id);


--
-- Name: l_artist_label l_artist_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_label
    ADD CONSTRAINT l_artist_label_pkey PRIMARY KEY (id);


--
-- Name: l_artist_place l_artist_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_place
    ADD CONSTRAINT l_artist_place_pkey PRIMARY KEY (id);


--
-- Name: l_artist_recording l_artist_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_recording
    ADD CONSTRAINT l_artist_recording_pkey PRIMARY KEY (id);


--
-- Name: l_artist_release_group l_artist_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_release_group
    ADD CONSTRAINT l_artist_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_artist_release l_artist_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_release
    ADD CONSTRAINT l_artist_release_pkey PRIMARY KEY (id);


--
-- Name: l_artist_series l_artist_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_series
    ADD CONSTRAINT l_artist_series_pkey PRIMARY KEY (id);


--
-- Name: l_artist_url l_artist_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_url
    ADD CONSTRAINT l_artist_url_pkey PRIMARY KEY (id);


--
-- Name: l_artist_work l_artist_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_artist_work
    ADD CONSTRAINT l_artist_work_pkey PRIMARY KEY (id);


--
-- Name: l_event_event l_event_event_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_event
    ADD CONSTRAINT l_event_event_pkey PRIMARY KEY (id);


--
-- Name: l_event_instrument l_event_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_instrument
    ADD CONSTRAINT l_event_instrument_pkey PRIMARY KEY (id);


--
-- Name: l_event_label l_event_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_label
    ADD CONSTRAINT l_event_label_pkey PRIMARY KEY (id);


--
-- Name: l_event_place l_event_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_place
    ADD CONSTRAINT l_event_place_pkey PRIMARY KEY (id);


--
-- Name: l_event_recording l_event_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_recording
    ADD CONSTRAINT l_event_recording_pkey PRIMARY KEY (id);


--
-- Name: l_event_release_group l_event_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_release_group
    ADD CONSTRAINT l_event_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_event_release l_event_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_release
    ADD CONSTRAINT l_event_release_pkey PRIMARY KEY (id);


--
-- Name: l_event_series l_event_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_series
    ADD CONSTRAINT l_event_series_pkey PRIMARY KEY (id);


--
-- Name: l_event_url l_event_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_url
    ADD CONSTRAINT l_event_url_pkey PRIMARY KEY (id);


--
-- Name: l_event_work l_event_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_event_work
    ADD CONSTRAINT l_event_work_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_instrument l_instrument_instrument_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_instrument
    ADD CONSTRAINT l_instrument_instrument_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_label l_instrument_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_label
    ADD CONSTRAINT l_instrument_label_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_place l_instrument_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_place
    ADD CONSTRAINT l_instrument_place_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_recording l_instrument_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_recording
    ADD CONSTRAINT l_instrument_recording_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_release_group l_instrument_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_release_group
    ADD CONSTRAINT l_instrument_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_release l_instrument_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_release
    ADD CONSTRAINT l_instrument_release_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_series l_instrument_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_series
    ADD CONSTRAINT l_instrument_series_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_url l_instrument_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_url
    ADD CONSTRAINT l_instrument_url_pkey PRIMARY KEY (id);


--
-- Name: l_instrument_work l_instrument_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_instrument_work
    ADD CONSTRAINT l_instrument_work_pkey PRIMARY KEY (id);


--
-- Name: l_label_label l_label_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_label
    ADD CONSTRAINT l_label_label_pkey PRIMARY KEY (id);


--
-- Name: l_label_place l_label_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_place
    ADD CONSTRAINT l_label_place_pkey PRIMARY KEY (id);


--
-- Name: l_label_recording l_label_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_recording
    ADD CONSTRAINT l_label_recording_pkey PRIMARY KEY (id);


--
-- Name: l_label_release_group l_label_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_release_group
    ADD CONSTRAINT l_label_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_label_release l_label_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_release
    ADD CONSTRAINT l_label_release_pkey PRIMARY KEY (id);


--
-- Name: l_label_series l_label_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_series
    ADD CONSTRAINT l_label_series_pkey PRIMARY KEY (id);


--
-- Name: l_label_url l_label_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_url
    ADD CONSTRAINT l_label_url_pkey PRIMARY KEY (id);


--
-- Name: l_label_work l_label_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_label_work
    ADD CONSTRAINT l_label_work_pkey PRIMARY KEY (id);


--
-- Name: l_place_place l_place_place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_place
    ADD CONSTRAINT l_place_place_pkey PRIMARY KEY (id);


--
-- Name: l_place_recording l_place_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_recording
    ADD CONSTRAINT l_place_recording_pkey PRIMARY KEY (id);


--
-- Name: l_place_release_group l_place_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_release_group
    ADD CONSTRAINT l_place_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_place_release l_place_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_release
    ADD CONSTRAINT l_place_release_pkey PRIMARY KEY (id);


--
-- Name: l_place_series l_place_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_series
    ADD CONSTRAINT l_place_series_pkey PRIMARY KEY (id);


--
-- Name: l_place_url l_place_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_url
    ADD CONSTRAINT l_place_url_pkey PRIMARY KEY (id);


--
-- Name: l_place_work l_place_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_place_work
    ADD CONSTRAINT l_place_work_pkey PRIMARY KEY (id);


--
-- Name: l_recording_recording l_recording_recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_recording
    ADD CONSTRAINT l_recording_recording_pkey PRIMARY KEY (id);


--
-- Name: l_recording_release_group l_recording_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_release_group
    ADD CONSTRAINT l_recording_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_recording_release l_recording_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_release
    ADD CONSTRAINT l_recording_release_pkey PRIMARY KEY (id);


--
-- Name: l_recording_series l_recording_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_series
    ADD CONSTRAINT l_recording_series_pkey PRIMARY KEY (id);


--
-- Name: l_recording_url l_recording_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_url
    ADD CONSTRAINT l_recording_url_pkey PRIMARY KEY (id);


--
-- Name: l_recording_work l_recording_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_recording_work
    ADD CONSTRAINT l_recording_work_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_release_group l_release_group_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_release_group
    ADD CONSTRAINT l_release_group_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_series l_release_group_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_series
    ADD CONSTRAINT l_release_group_series_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_url l_release_group_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_url
    ADD CONSTRAINT l_release_group_url_pkey PRIMARY KEY (id);


--
-- Name: l_release_group_work l_release_group_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_group_work
    ADD CONSTRAINT l_release_group_work_pkey PRIMARY KEY (id);


--
-- Name: l_release_release_group l_release_release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_release_group
    ADD CONSTRAINT l_release_release_group_pkey PRIMARY KEY (id);


--
-- Name: l_release_release l_release_release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_release
    ADD CONSTRAINT l_release_release_pkey PRIMARY KEY (id);


--
-- Name: l_release_series l_release_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_series
    ADD CONSTRAINT l_release_series_pkey PRIMARY KEY (id);


--
-- Name: l_release_url l_release_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_url
    ADD CONSTRAINT l_release_url_pkey PRIMARY KEY (id);


--
-- Name: l_release_work l_release_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_release_work
    ADD CONSTRAINT l_release_work_pkey PRIMARY KEY (id);


--
-- Name: l_series_series l_series_series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_series
    ADD CONSTRAINT l_series_series_pkey PRIMARY KEY (id);


--
-- Name: l_series_url l_series_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_url
    ADD CONSTRAINT l_series_url_pkey PRIMARY KEY (id);


--
-- Name: l_series_work l_series_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_series_work
    ADD CONSTRAINT l_series_work_pkey PRIMARY KEY (id);


--
-- Name: l_url_url l_url_url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_url_url
    ADD CONSTRAINT l_url_url_pkey PRIMARY KEY (id);


--
-- Name: l_url_work l_url_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_url_work
    ADD CONSTRAINT l_url_work_pkey PRIMARY KEY (id);


--
-- Name: l_work_work l_work_work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.l_work_work
    ADD CONSTRAINT l_work_work_pkey PRIMARY KEY (id);


--
-- Name: label_alias label_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_alias
    ADD CONSTRAINT label_alias_pkey PRIMARY KEY (id);


--
-- Name: label_alias_type label_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_alias_type
    ADD CONSTRAINT label_alias_type_pkey PRIMARY KEY (id);


--
-- Name: label_annotation label_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_annotation
    ADD CONSTRAINT label_annotation_pkey PRIMARY KEY (label, annotation);


--
-- Name: label_attribute label_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute
    ADD CONSTRAINT label_attribute_pkey PRIMARY KEY (id);


--
-- Name: label_attribute_type_allowed_value label_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute_type_allowed_value
    ADD CONSTRAINT label_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: label_attribute_type label_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_attribute_type
    ADD CONSTRAINT label_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: label_gid_redirect label_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_gid_redirect
    ADD CONSTRAINT label_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: label_ipi label_ipi_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_ipi
    ADD CONSTRAINT label_ipi_pkey PRIMARY KEY (label, ipi);


--
-- Name: label_isni label_isni_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_isni
    ADD CONSTRAINT label_isni_pkey PRIMARY KEY (label, isni);


--
-- Name: label_meta label_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_meta
    ADD CONSTRAINT label_meta_pkey PRIMARY KEY (id);


--
-- Name: label label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label
    ADD CONSTRAINT label_pkey PRIMARY KEY (id);


--
-- Name: label_rating_raw label_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_rating_raw
    ADD CONSTRAINT label_rating_raw_pkey PRIMARY KEY (label, editor);


--
-- Name: label_tag label_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_tag
    ADD CONSTRAINT label_tag_pkey PRIMARY KEY (label, tag);


--
-- Name: label_tag_raw label_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_tag_raw
    ADD CONSTRAINT label_tag_raw_pkey PRIMARY KEY (label, editor, tag);


--
-- Name: label_type label_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.label_type
    ADD CONSTRAINT label_type_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: link_attribute_credit link_attribute_credit_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_attribute_credit
    ADD CONSTRAINT link_attribute_credit_pkey PRIMARY KEY (link, attribute_type);


--
-- Name: link_attribute link_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_attribute
    ADD CONSTRAINT link_attribute_pkey PRIMARY KEY (link, attribute_type);


--
-- Name: link_attribute_text_value link_attribute_text_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_attribute_text_value
    ADD CONSTRAINT link_attribute_text_value_pkey PRIMARY KEY (link, attribute_type);


--
-- Name: link_attribute_type link_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_attribute_type
    ADD CONSTRAINT link_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: link_creditable_attribute_type link_creditable_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_creditable_attribute_type
    ADD CONSTRAINT link_creditable_attribute_type_pkey PRIMARY KEY (attribute_type);


--
-- Name: link link_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link
    ADD CONSTRAINT link_pkey PRIMARY KEY (id);


--
-- Name: link_text_attribute_type link_text_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_text_attribute_type
    ADD CONSTRAINT link_text_attribute_type_pkey PRIMARY KEY (attribute_type);


--
-- Name: link_type_attribute_type link_type_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_type_attribute_type
    ADD CONSTRAINT link_type_attribute_type_pkey PRIMARY KEY (link_type, attribute_type);


--
-- Name: link_type link_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.link_type
    ADD CONSTRAINT link_type_pkey PRIMARY KEY (id);


--
-- Name: medium_attribute medium_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute
    ADD CONSTRAINT medium_attribute_pkey PRIMARY KEY (id);


--
-- Name: medium_attribute_type_allowed_format medium_attribute_type_allowed_format_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type_allowed_format
    ADD CONSTRAINT medium_attribute_type_allowed_format_pkey PRIMARY KEY (medium_format, medium_attribute_type);


--
-- Name: medium_attribute_type_allowed_value_allowed_format medium_attribute_type_allowed_value_allowed_format_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type_allowed_value_allowed_format
    ADD CONSTRAINT medium_attribute_type_allowed_value_allowed_format_pkey PRIMARY KEY (medium_format, medium_attribute_type_allowed_value);


--
-- Name: medium_attribute_type_allowed_value medium_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type_allowed_value
    ADD CONSTRAINT medium_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: medium_attribute_type medium_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_attribute_type
    ADD CONSTRAINT medium_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: medium_cdtoc medium_cdtoc_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_cdtoc
    ADD CONSTRAINT medium_cdtoc_pkey PRIMARY KEY (id);


--
-- Name: medium_format medium_format_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_format
    ADD CONSTRAINT medium_format_pkey PRIMARY KEY (id);


--
-- Name: medium_index medium_index_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium_index
    ADD CONSTRAINT medium_index_pkey PRIMARY KEY (medium);


--
-- Name: medium medium_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.medium
    ADD CONSTRAINT medium_pkey PRIMARY KEY (id);


--
-- Name: orderable_link_type orderable_link_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.orderable_link_type
    ADD CONSTRAINT orderable_link_type_pkey PRIMARY KEY (link_type);


--
-- Name: place_alias place_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_alias
    ADD CONSTRAINT place_alias_pkey PRIMARY KEY (id);


--
-- Name: place_alias_type place_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_alias_type
    ADD CONSTRAINT place_alias_type_pkey PRIMARY KEY (id);


--
-- Name: place_annotation place_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_annotation
    ADD CONSTRAINT place_annotation_pkey PRIMARY KEY (place, annotation);


--
-- Name: place_attribute place_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute
    ADD CONSTRAINT place_attribute_pkey PRIMARY KEY (id);


--
-- Name: place_attribute_type_allowed_value place_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute_type_allowed_value
    ADD CONSTRAINT place_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: place_attribute_type place_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_attribute_type
    ADD CONSTRAINT place_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: place_gid_redirect place_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_gid_redirect
    ADD CONSTRAINT place_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: place place_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place
    ADD CONSTRAINT place_pkey PRIMARY KEY (id);


--
-- Name: place_tag place_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_tag
    ADD CONSTRAINT place_tag_pkey PRIMARY KEY (place, tag);


--
-- Name: place_tag_raw place_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_tag_raw
    ADD CONSTRAINT place_tag_raw_pkey PRIMARY KEY (place, editor, tag);


--
-- Name: place_type place_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.place_type
    ADD CONSTRAINT place_type_pkey PRIMARY KEY (id);


--
-- Name: recording_alias recording_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_alias
    ADD CONSTRAINT recording_alias_pkey PRIMARY KEY (id);


--
-- Name: recording_alias_type recording_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_alias_type
    ADD CONSTRAINT recording_alias_type_pkey PRIMARY KEY (id);


--
-- Name: recording_annotation recording_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_annotation
    ADD CONSTRAINT recording_annotation_pkey PRIMARY KEY (recording, annotation);


--
-- Name: recording_attribute recording_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute
    ADD CONSTRAINT recording_attribute_pkey PRIMARY KEY (id);


--
-- Name: recording_attribute_type_allowed_value recording_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute_type_allowed_value
    ADD CONSTRAINT recording_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: recording_attribute_type recording_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_attribute_type
    ADD CONSTRAINT recording_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: recording_gid_redirect recording_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_gid_redirect
    ADD CONSTRAINT recording_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: recording_meta recording_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_meta
    ADD CONSTRAINT recording_meta_pkey PRIMARY KEY (id);


--
-- Name: recording recording_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording
    ADD CONSTRAINT recording_pkey PRIMARY KEY (id);


--
-- Name: recording_rating_raw recording_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_rating_raw
    ADD CONSTRAINT recording_rating_raw_pkey PRIMARY KEY (recording, editor);


--
-- Name: recording_tag recording_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_tag
    ADD CONSTRAINT recording_tag_pkey PRIMARY KEY (recording, tag);


--
-- Name: recording_tag_raw recording_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.recording_tag_raw
    ADD CONSTRAINT recording_tag_raw_pkey PRIMARY KEY (recording, editor, tag);


--
-- Name: release_alias release_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_alias
    ADD CONSTRAINT release_alias_pkey PRIMARY KEY (id);


--
-- Name: release_alias_type release_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_alias_type
    ADD CONSTRAINT release_alias_type_pkey PRIMARY KEY (id);


--
-- Name: release_annotation release_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_annotation
    ADD CONSTRAINT release_annotation_pkey PRIMARY KEY (release, annotation);


--
-- Name: release_attribute release_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute
    ADD CONSTRAINT release_attribute_pkey PRIMARY KEY (id);


--
-- Name: release_attribute_type_allowed_value release_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute_type_allowed_value
    ADD CONSTRAINT release_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: release_attribute_type release_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_attribute_type
    ADD CONSTRAINT release_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: release_country release_country_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_country
    ADD CONSTRAINT release_country_pkey PRIMARY KEY (release, country);


--
-- Name: release_coverart release_coverart_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_coverart
    ADD CONSTRAINT release_coverart_pkey PRIMARY KEY (id);


--
-- Name: release_gid_redirect release_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_gid_redirect
    ADD CONSTRAINT release_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: release_group_alias release_group_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_alias
    ADD CONSTRAINT release_group_alias_pkey PRIMARY KEY (id);


--
-- Name: release_group_alias_type release_group_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_alias_type
    ADD CONSTRAINT release_group_alias_type_pkey PRIMARY KEY (id);


--
-- Name: release_group_annotation release_group_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_annotation
    ADD CONSTRAINT release_group_annotation_pkey PRIMARY KEY (release_group, annotation);


--
-- Name: release_group_attribute release_group_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute
    ADD CONSTRAINT release_group_attribute_pkey PRIMARY KEY (id);


--
-- Name: release_group_attribute_type_allowed_value release_group_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute_type_allowed_value
    ADD CONSTRAINT release_group_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: release_group_attribute_type release_group_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_attribute_type
    ADD CONSTRAINT release_group_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: release_group_gid_redirect release_group_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_gid_redirect
    ADD CONSTRAINT release_group_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: release_group_meta release_group_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_meta
    ADD CONSTRAINT release_group_meta_pkey PRIMARY KEY (id);


--
-- Name: release_group release_group_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group
    ADD CONSTRAINT release_group_pkey PRIMARY KEY (id);


--
-- Name: release_group_primary_type release_group_primary_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_primary_type
    ADD CONSTRAINT release_group_primary_type_pkey PRIMARY KEY (id);


--
-- Name: release_group_rating_raw release_group_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_rating_raw
    ADD CONSTRAINT release_group_rating_raw_pkey PRIMARY KEY (release_group, editor);


--
-- Name: release_group_secondary_type_join release_group_secondary_type_join_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_secondary_type_join
    ADD CONSTRAINT release_group_secondary_type_join_pkey PRIMARY KEY (release_group, secondary_type);


--
-- Name: release_group_secondary_type release_group_secondary_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_secondary_type
    ADD CONSTRAINT release_group_secondary_type_pkey PRIMARY KEY (id);


--
-- Name: release_group_tag release_group_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_tag
    ADD CONSTRAINT release_group_tag_pkey PRIMARY KEY (release_group, tag);


--
-- Name: release_group_tag_raw release_group_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_group_tag_raw
    ADD CONSTRAINT release_group_tag_raw_pkey PRIMARY KEY (release_group, editor, tag);


--
-- Name: release_label release_label_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_label
    ADD CONSTRAINT release_label_pkey PRIMARY KEY (id);


--
-- Name: release_meta release_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_meta
    ADD CONSTRAINT release_meta_pkey PRIMARY KEY (id);


--
-- Name: release_packaging release_packaging_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_packaging
    ADD CONSTRAINT release_packaging_pkey PRIMARY KEY (id);


--
-- Name: release release_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release
    ADD CONSTRAINT release_pkey PRIMARY KEY (id);


--
-- Name: release_raw release_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_raw
    ADD CONSTRAINT release_raw_pkey PRIMARY KEY (id);


--
-- Name: release_status release_status_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_status
    ADD CONSTRAINT release_status_pkey PRIMARY KEY (id);


--
-- Name: release_tag release_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_tag
    ADD CONSTRAINT release_tag_pkey PRIMARY KEY (release, tag);


--
-- Name: release_tag_raw release_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_tag_raw
    ADD CONSTRAINT release_tag_raw_pkey PRIMARY KEY (release, editor, tag);


--
-- Name: release_unknown_country release_unknown_country_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.release_unknown_country
    ADD CONSTRAINT release_unknown_country_pkey PRIMARY KEY (release);


--
-- Name: replication_control replication_control_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.replication_control
    ADD CONSTRAINT replication_control_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: series_alias series_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_alias
    ADD CONSTRAINT series_alias_pkey PRIMARY KEY (id);


--
-- Name: series_alias_type series_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_alias_type
    ADD CONSTRAINT series_alias_type_pkey PRIMARY KEY (id);


--
-- Name: series_annotation series_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_annotation
    ADD CONSTRAINT series_annotation_pkey PRIMARY KEY (series, annotation);


--
-- Name: series_attribute series_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute
    ADD CONSTRAINT series_attribute_pkey PRIMARY KEY (id);


--
-- Name: series_attribute_type_allowed_value series_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute_type_allowed_value
    ADD CONSTRAINT series_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: series_attribute_type series_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_attribute_type
    ADD CONSTRAINT series_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: series_gid_redirect series_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_gid_redirect
    ADD CONSTRAINT series_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: series_ordering_type series_ordering_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_ordering_type
    ADD CONSTRAINT series_ordering_type_pkey PRIMARY KEY (id);


--
-- Name: series series_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series
    ADD CONSTRAINT series_pkey PRIMARY KEY (id);


--
-- Name: series_tag series_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_tag
    ADD CONSTRAINT series_tag_pkey PRIMARY KEY (series, tag);


--
-- Name: series_tag_raw series_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_tag_raw
    ADD CONSTRAINT series_tag_raw_pkey PRIMARY KEY (series, editor, tag);


--
-- Name: series_type series_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.series_type
    ADD CONSTRAINT series_type_pkey PRIMARY KEY (id);


--
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (id);


--
-- Name: tag_relation tag_relation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.tag_relation
    ADD CONSTRAINT tag_relation_pkey PRIMARY KEY (tag1, tag2);


--
-- Name: track_gid_redirect track_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.track_gid_redirect
    ADD CONSTRAINT track_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: track track_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.track
    ADD CONSTRAINT track_pkey PRIMARY KEY (id);


--
-- Name: track_raw track_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.track_raw
    ADD CONSTRAINT track_raw_pkey PRIMARY KEY (id);


--
-- Name: url_gid_redirect url_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.url_gid_redirect
    ADD CONSTRAINT url_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: url url_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.url
    ADD CONSTRAINT url_pkey PRIMARY KEY (id);


--
-- Name: vote vote_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.vote
    ADD CONSTRAINT vote_pkey PRIMARY KEY (id);


--
-- Name: work_alias work_alias_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_alias
    ADD CONSTRAINT work_alias_pkey PRIMARY KEY (id);


--
-- Name: work_alias_type work_alias_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_alias_type
    ADD CONSTRAINT work_alias_type_pkey PRIMARY KEY (id);


--
-- Name: work_annotation work_annotation_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_annotation
    ADD CONSTRAINT work_annotation_pkey PRIMARY KEY (work, annotation);


--
-- Name: work_attribute work_attribute_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute
    ADD CONSTRAINT work_attribute_pkey PRIMARY KEY (id);


--
-- Name: work_attribute_type_allowed_value work_attribute_type_allowed_value_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute_type_allowed_value
    ADD CONSTRAINT work_attribute_type_allowed_value_pkey PRIMARY KEY (id);


--
-- Name: work_attribute_type work_attribute_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_attribute_type
    ADD CONSTRAINT work_attribute_type_pkey PRIMARY KEY (id);


--
-- Name: work_gid_redirect work_gid_redirect_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_gid_redirect
    ADD CONSTRAINT work_gid_redirect_pkey PRIMARY KEY (gid);


--
-- Name: work_language work_language_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_language
    ADD CONSTRAINT work_language_pkey PRIMARY KEY (work, language);


--
-- Name: work_meta work_meta_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_meta
    ADD CONSTRAINT work_meta_pkey PRIMARY KEY (id);


--
-- Name: work work_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work
    ADD CONSTRAINT work_pkey PRIMARY KEY (id);


--
-- Name: work_rating_raw work_rating_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_rating_raw
    ADD CONSTRAINT work_rating_raw_pkey PRIMARY KEY (work, editor);


--
-- Name: work_tag work_tag_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_tag
    ADD CONSTRAINT work_tag_pkey PRIMARY KEY (work, tag);


--
-- Name: work_tag_raw work_tag_raw_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_tag_raw
    ADD CONSTRAINT work_tag_raw_pkey PRIMARY KEY (work, editor, tag);


--
-- Name: work_type work_type_pkey; Type: CONSTRAINT; Schema: musicbrainz; Owner: -
--

ALTER TABLE ONLY musicbrainz.work_type
    ADD CONSTRAINT work_type_pkey PRIMARY KEY (id);


--
-- Name: log_statistic log_statistic_pkey; Type: CONSTRAINT; Schema: statistics; Owner: -
--

ALTER TABLE ONLY statistics.log_statistic
    ADD CONSTRAINT log_statistic_pkey PRIMARY KEY (name, category, "timestamp");


--
-- Name: statistic_event statistic_event_pkey; Type: CONSTRAINT; Schema: statistics; Owner: -
--

ALTER TABLE ONLY statistics.statistic_event
    ADD CONSTRAINT statistic_event_pkey PRIMARY KEY (date);


--
-- Name: statistic statistic_pkey; Type: CONSTRAINT; Schema: statistics; Owner: -
--

ALTER TABLE ONLY statistics.statistic
    ADD CONSTRAINT statistic_pkey PRIMARY KEY (id);


--
-- Name: wikidocs_index wikidocs_index_pkey; Type: CONSTRAINT; Schema: wikidocs; Owner: -
--

ALTER TABLE ONLY wikidocs.wikidocs_index
    ADD CONSTRAINT wikidocs_index_pkey PRIMARY KEY (page_name);


--
-- Name: art_type_idx_gid; Type: INDEX; Schema: cover_art_archive; Owner: -
--

CREATE UNIQUE INDEX art_type_idx_gid ON cover_art_archive.art_type USING btree (gid);


--
-- Name: cover_art_idx_release; Type: INDEX; Schema: cover_art_archive; Owner: -
--

CREATE INDEX cover_art_idx_release ON cover_art_archive.cover_art USING btree (release);


--
-- Name: alternative_medium_idx_alternative_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_medium_idx_alternative_release ON musicbrainz.alternative_medium USING btree (alternative_release);


--
-- Name: alternative_release_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_release_idx_artist_credit ON musicbrainz.alternative_release USING btree (artist_credit);


--
-- Name: alternative_release_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX alternative_release_idx_gid ON musicbrainz.alternative_release USING btree (gid);


--
-- Name: alternative_release_idx_language_script; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_release_idx_language_script ON musicbrainz.alternative_release USING btree (language, script);


--
-- Name: alternative_release_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_release_idx_name ON musicbrainz.alternative_release USING btree (name);


--
-- Name: alternative_release_idx_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_release_idx_release ON musicbrainz.alternative_release USING btree (release);


--
-- Name: alternative_track_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_track_idx_artist_credit ON musicbrainz.alternative_track USING btree (artist_credit);


--
-- Name: alternative_track_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX alternative_track_idx_name ON musicbrainz.alternative_track USING btree (name);


--
-- Name: application_idx_oauth_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX application_idx_oauth_id ON musicbrainz.application USING btree (oauth_id);


--
-- Name: application_idx_owner; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX application_idx_owner ON musicbrainz.application USING btree (owner);


--
-- Name: area_alias_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_alias_idx_area ON musicbrainz.area_alias USING btree (area);


--
-- Name: area_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_alias_idx_primary ON musicbrainz.area_alias USING btree (area, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: area_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_alias_type_idx_gid ON musicbrainz.area_alias_type USING btree (gid);


--
-- Name: area_attribute_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_attribute_idx_area ON musicbrainz.area_attribute USING btree (area);


--
-- Name: area_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_attribute_type_allowed_value_idx_gid ON musicbrainz.area_attribute_type_allowed_value USING btree (gid);


--
-- Name: area_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_attribute_type_allowed_value_idx_name ON musicbrainz.area_attribute_type_allowed_value USING btree (area_attribute_type);


--
-- Name: area_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_attribute_type_idx_gid ON musicbrainz.area_attribute_type USING btree (gid);


--
-- Name: area_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_gid_redirect_idx_new_id ON musicbrainz.area_gid_redirect USING btree (new_id);


--
-- Name: area_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_idx_gid ON musicbrainz.area USING btree (gid);


--
-- Name: area_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_idx_name ON musicbrainz.area USING btree (name);


--
-- Name: area_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_tag_idx_tag ON musicbrainz.area_tag USING btree (tag);


--
-- Name: area_tag_raw_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_tag_raw_idx_area ON musicbrainz.area_tag_raw USING btree (area);


--
-- Name: area_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_tag_raw_idx_editor ON musicbrainz.area_tag_raw USING btree (editor);


--
-- Name: area_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX area_tag_raw_idx_tag ON musicbrainz.area_tag_raw USING btree (tag);


--
-- Name: area_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX area_type_idx_gid ON musicbrainz.area_type USING btree (gid);


--
-- Name: artist_alias_idx_artist; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_alias_idx_artist ON musicbrainz.artist_alias USING btree (artist);


--
-- Name: artist_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_alias_idx_primary ON musicbrainz.artist_alias USING btree (artist, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: artist_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_alias_type_idx_gid ON musicbrainz.artist_alias_type USING btree (gid);


--
-- Name: artist_attribute_idx_artist; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_attribute_idx_artist ON musicbrainz.artist_attribute USING btree (artist);


--
-- Name: artist_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_attribute_type_allowed_value_idx_gid ON musicbrainz.artist_attribute_type_allowed_value USING btree (gid);


--
-- Name: artist_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_attribute_type_allowed_value_idx_name ON musicbrainz.artist_attribute_type_allowed_value USING btree (artist_attribute_type);


--
-- Name: artist_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_attribute_type_idx_gid ON musicbrainz.artist_attribute_type USING btree (gid);


--
-- Name: artist_credit_name_idx_artist; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_credit_name_idx_artist ON musicbrainz.artist_credit_name USING btree (artist);


--
-- Name: artist_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_gid_redirect_idx_new_id ON musicbrainz.artist_gid_redirect USING btree (new_id);


--
-- Name: artist_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_area ON musicbrainz.artist USING btree (area);


--
-- Name: artist_idx_begin_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_begin_area ON musicbrainz.artist USING btree (begin_area);


--
-- Name: artist_idx_end_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_end_area ON musicbrainz.artist USING btree (end_area);


--
-- Name: artist_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_idx_gid ON musicbrainz.artist USING btree (gid);


--
-- Name: artist_idx_lower_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_lower_name ON musicbrainz.artist USING btree (lower((name)::text));


--
-- Name: artist_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_name ON musicbrainz.artist USING btree (name);


--
-- Name: artist_idx_null_comment; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_idx_null_comment ON musicbrainz.artist USING btree (name) WHERE (comment IS NULL);


--
-- Name: artist_idx_sort_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_idx_sort_name ON musicbrainz.artist USING btree (sort_name);


--
-- Name: artist_idx_uniq_name_comment; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_idx_uniq_name_comment ON musicbrainz.artist USING btree (name, comment) WHERE (comment IS NOT NULL);


--
-- Name: artist_rating_raw_idx_artist; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_rating_raw_idx_artist ON musicbrainz.artist_rating_raw USING btree (artist);


--
-- Name: artist_rating_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_rating_raw_idx_editor ON musicbrainz.artist_rating_raw USING btree (editor);


--
-- Name: artist_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_tag_idx_tag ON musicbrainz.artist_tag USING btree (tag);


--
-- Name: artist_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_tag_raw_idx_editor ON musicbrainz.artist_tag_raw USING btree (editor);


--
-- Name: artist_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX artist_tag_raw_idx_tag ON musicbrainz.artist_tag_raw USING btree (tag);


--
-- Name: artist_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX artist_type_idx_gid ON musicbrainz.artist_type USING btree (gid);


--
-- Name: cdtoc_idx_discid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX cdtoc_idx_discid ON musicbrainz.cdtoc USING btree (discid);


--
-- Name: cdtoc_idx_freedb_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX cdtoc_idx_freedb_id ON musicbrainz.cdtoc USING btree (freedb_id);


--
-- Name: cdtoc_raw_discid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX cdtoc_raw_discid ON musicbrainz.cdtoc_raw USING btree (discid);


--
-- Name: cdtoc_raw_toc; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX cdtoc_raw_toc ON musicbrainz.cdtoc_raw USING btree (track_count, leadout_offset, track_offset);


--
-- Name: cdtoc_raw_track_offset; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX cdtoc_raw_track_offset ON musicbrainz.cdtoc_raw USING btree (track_offset);


--
-- Name: edit_area_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_area_idx ON musicbrainz.edit_area USING btree (area);


--
-- Name: edit_artist_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_artist_idx ON musicbrainz.edit_artist USING btree (artist);


--
-- Name: edit_artist_idx_status; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_artist_idx_status ON musicbrainz.edit_artist USING btree (status);


--
-- Name: edit_event_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_event_idx ON musicbrainz.edit_event USING btree (event);


--
-- Name: edit_idx_close_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_close_time ON musicbrainz.edit USING btree (close_time);


--
-- Name: edit_idx_editor_id_desc; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_editor_id_desc ON musicbrainz.edit USING btree (editor, id DESC);


--
-- Name: edit_idx_editor_open_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_editor_open_time ON musicbrainz.edit USING btree (editor, open_time);


--
-- Name: edit_idx_expire_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_expire_time ON musicbrainz.edit USING btree (expire_time);


--
-- Name: edit_idx_open_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_open_time ON musicbrainz.edit USING btree (open_time);


--
-- Name: edit_idx_status_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_status_id ON musicbrainz.edit USING btree (status, id) WHERE (status <> 2);


--
-- Name: edit_idx_type_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_idx_type_id ON musicbrainz.edit USING btree (type, id);


--
-- Name: edit_instrument_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_instrument_idx ON musicbrainz.edit_instrument USING btree (instrument);


--
-- Name: edit_label_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_label_idx ON musicbrainz.edit_label USING btree (label);


--
-- Name: edit_label_idx_status; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_label_idx_status ON musicbrainz.edit_label USING btree (status);


--
-- Name: edit_note_idx_edit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_note_idx_edit ON musicbrainz.edit_note USING btree (edit);


--
-- Name: edit_note_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_note_idx_editor ON musicbrainz.edit_note USING btree (editor);


--
-- Name: edit_note_idx_post_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_note_idx_post_time ON musicbrainz.edit_note USING btree (post_time);


--
-- Name: edit_note_idx_post_time_edit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_note_idx_post_time_edit ON musicbrainz.edit_note USING btree (post_time DESC NULLS LAST, edit DESC);


--
-- Name: edit_note_recipient_idx_recipient; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_note_recipient_idx_recipient ON musicbrainz.edit_note_recipient USING btree (recipient);


--
-- Name: edit_place_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_place_idx ON musicbrainz.edit_place USING btree (place);


--
-- Name: edit_recording_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_recording_idx ON musicbrainz.edit_recording USING btree (recording);


--
-- Name: edit_release_group_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_release_group_idx ON musicbrainz.edit_release_group USING btree (release_group);


--
-- Name: edit_release_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_release_idx ON musicbrainz.edit_release USING btree (release);


--
-- Name: edit_series_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_series_idx ON musicbrainz.edit_series USING btree (series);


--
-- Name: edit_url_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_url_idx ON musicbrainz.edit_url USING btree (url);


--
-- Name: edit_work_idx; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX edit_work_idx ON musicbrainz.edit_work USING btree (work);


--
-- Name: editor_collection_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_collection_idx_editor ON musicbrainz.editor_collection USING btree (editor);


--
-- Name: editor_collection_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_collection_idx_gid ON musicbrainz.editor_collection USING btree (gid);


--
-- Name: editor_collection_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_collection_idx_name ON musicbrainz.editor_collection USING btree (name);


--
-- Name: editor_collection_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_collection_type_idx_gid ON musicbrainz.editor_collection_type USING btree (gid);


--
-- Name: editor_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_idx_name ON musicbrainz.editor USING btree (lower((name)::text));


--
-- Name: editor_language_idx_language; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_language_idx_language ON musicbrainz.editor_language USING btree (language);


--
-- Name: editor_oauth_token_idx_access_token; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_oauth_token_idx_access_token ON musicbrainz.editor_oauth_token USING btree (access_token);


--
-- Name: editor_oauth_token_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_oauth_token_idx_editor ON musicbrainz.editor_oauth_token USING btree (editor);


--
-- Name: editor_oauth_token_idx_refresh_token; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_oauth_token_idx_refresh_token ON musicbrainz.editor_oauth_token USING btree (refresh_token);


--
-- Name: editor_preference_idx_editor_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_preference_idx_editor_name ON musicbrainz.editor_preference USING btree (editor, name);


--
-- Name: editor_subscribe_artist_idx_artist; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_subscribe_artist_idx_artist ON musicbrainz.editor_subscribe_artist USING btree (artist);


--
-- Name: editor_subscribe_artist_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_subscribe_artist_idx_uniq ON musicbrainz.editor_subscribe_artist USING btree (editor, artist);


--
-- Name: editor_subscribe_collection_idx_collection; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_subscribe_collection_idx_collection ON musicbrainz.editor_subscribe_collection USING btree (collection);


--
-- Name: editor_subscribe_collection_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_subscribe_collection_idx_uniq ON musicbrainz.editor_subscribe_collection USING btree (editor, collection);


--
-- Name: editor_subscribe_editor_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_subscribe_editor_idx_uniq ON musicbrainz.editor_subscribe_editor USING btree (editor, subscribed_editor);


--
-- Name: editor_subscribe_label_idx_label; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_subscribe_label_idx_label ON musicbrainz.editor_subscribe_label USING btree (label);


--
-- Name: editor_subscribe_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_subscribe_label_idx_uniq ON musicbrainz.editor_subscribe_label USING btree (editor, label);


--
-- Name: editor_subscribe_series_idx_series; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX editor_subscribe_series_idx_series ON musicbrainz.editor_subscribe_series USING btree (series);


--
-- Name: editor_subscribe_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX editor_subscribe_series_idx_uniq ON musicbrainz.editor_subscribe_series USING btree (editor, series);


--
-- Name: event_alias_idx_event; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_alias_idx_event ON musicbrainz.event_alias USING btree (event);


--
-- Name: event_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_alias_idx_primary ON musicbrainz.event_alias USING btree (event, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: event_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_alias_type_idx_gid ON musicbrainz.event_alias_type USING btree (gid);


--
-- Name: event_attribute_idx_event; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_attribute_idx_event ON musicbrainz.event_attribute USING btree (event);


--
-- Name: event_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_attribute_type_allowed_value_idx_gid ON musicbrainz.event_attribute_type_allowed_value USING btree (gid);


--
-- Name: event_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_attribute_type_allowed_value_idx_name ON musicbrainz.event_attribute_type_allowed_value USING btree (event_attribute_type);


--
-- Name: event_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_attribute_type_idx_gid ON musicbrainz.event_attribute_type USING btree (gid);


--
-- Name: event_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_gid_redirect_idx_new_id ON musicbrainz.event_gid_redirect USING btree (new_id);


--
-- Name: event_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_idx_gid ON musicbrainz.event USING btree (gid);


--
-- Name: event_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_idx_name ON musicbrainz.event USING btree (name);


--
-- Name: event_rating_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_rating_raw_idx_editor ON musicbrainz.event_rating_raw USING btree (editor);


--
-- Name: event_rating_raw_idx_event; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_rating_raw_idx_event ON musicbrainz.event_rating_raw USING btree (event);


--
-- Name: event_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_tag_idx_tag ON musicbrainz.event_tag USING btree (tag);


--
-- Name: event_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_tag_raw_idx_editor ON musicbrainz.event_tag_raw USING btree (editor);


--
-- Name: event_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX event_tag_raw_idx_tag ON musicbrainz.event_tag_raw USING btree (tag);


--
-- Name: event_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX event_type_idx_gid ON musicbrainz.event_type USING btree (gid);


--
-- Name: gender_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX gender_idx_gid ON musicbrainz.gender USING btree (gid);


--
-- Name: genre_alias_idx_genre; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX genre_alias_idx_genre ON musicbrainz.genre_alias USING btree (genre);


--
-- Name: genre_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX genre_alias_idx_primary ON musicbrainz.genre_alias USING btree (genre, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: genre_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX genre_idx_gid ON musicbrainz.genre USING btree (gid);


--
-- Name: genre_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX genre_idx_name ON musicbrainz.genre USING btree (lower((name)::text));


--
-- Name: idx_artist_name_lower; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX idx_artist_name_lower ON musicbrainz.artist_credit USING btree (lower((name)::text));


--
-- Name: idx_release_group_name_lower; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX idx_release_group_name_lower ON musicbrainz.release_group USING btree (lower((name)::text));


--
-- Name: instrument_alias_idx_instrument; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_alias_idx_instrument ON musicbrainz.instrument_alias USING btree (instrument);


--
-- Name: instrument_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_alias_idx_primary ON musicbrainz.instrument_alias USING btree (instrument, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: instrument_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_alias_type_idx_gid ON musicbrainz.instrument_alias_type USING btree (gid);


--
-- Name: instrument_attribute_idx_instrument; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_attribute_idx_instrument ON musicbrainz.instrument_attribute USING btree (instrument);


--
-- Name: instrument_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_attribute_type_allowed_value_idx_gid ON musicbrainz.instrument_attribute_type_allowed_value USING btree (gid);


--
-- Name: instrument_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_attribute_type_allowed_value_idx_name ON musicbrainz.instrument_attribute_type_allowed_value USING btree (instrument_attribute_type);


--
-- Name: instrument_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_attribute_type_idx_gid ON musicbrainz.instrument_attribute_type USING btree (gid);


--
-- Name: instrument_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_gid_redirect_idx_new_id ON musicbrainz.instrument_gid_redirect USING btree (new_id);


--
-- Name: instrument_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_idx_gid ON musicbrainz.instrument USING btree (gid);


--
-- Name: instrument_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_idx_name ON musicbrainz.instrument USING btree (name);


--
-- Name: instrument_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_tag_idx_tag ON musicbrainz.instrument_tag USING btree (tag);


--
-- Name: instrument_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_tag_raw_idx_editor ON musicbrainz.instrument_tag_raw USING btree (editor);


--
-- Name: instrument_tag_raw_idx_instrument; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_tag_raw_idx_instrument ON musicbrainz.instrument_tag_raw USING btree (instrument);


--
-- Name: instrument_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX instrument_tag_raw_idx_tag ON musicbrainz.instrument_tag_raw USING btree (tag);


--
-- Name: instrument_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX instrument_type_idx_gid ON musicbrainz.instrument_type USING btree (gid);


--
-- Name: iso_3166_1_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX iso_3166_1_idx_area ON musicbrainz.iso_3166_1 USING btree (area);


--
-- Name: iso_3166_2_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX iso_3166_2_idx_area ON musicbrainz.iso_3166_2 USING btree (area);


--
-- Name: iso_3166_3_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX iso_3166_3_idx_area ON musicbrainz.iso_3166_3 USING btree (area);


--
-- Name: isrc_idx_isrc; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX isrc_idx_isrc ON musicbrainz.isrc USING btree (isrc);


--
-- Name: isrc_idx_isrc_recording; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX isrc_idx_isrc_recording ON musicbrainz.isrc USING btree (isrc, recording);


--
-- Name: isrc_idx_recording; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX isrc_idx_recording ON musicbrainz.isrc USING btree (recording);


--
-- Name: iswc_idx_iswc; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX iswc_idx_iswc ON musicbrainz.iswc USING btree (iswc, work);


--
-- Name: iswc_idx_work; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX iswc_idx_work ON musicbrainz.iswc USING btree (work);


--
-- Name: l_area_area_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_area_idx_entity1 ON musicbrainz.l_area_area USING btree (entity1);


--
-- Name: l_area_area_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_area_idx_uniq ON musicbrainz.l_area_area USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_artist_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_artist_idx_entity1 ON musicbrainz.l_area_artist USING btree (entity1);


--
-- Name: l_area_artist_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_artist_idx_uniq ON musicbrainz.l_area_artist USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_event_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_event_idx_entity1 ON musicbrainz.l_area_event USING btree (entity1);


--
-- Name: l_area_event_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_event_idx_uniq ON musicbrainz.l_area_event USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_instrument_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_instrument_idx_entity1 ON musicbrainz.l_area_instrument USING btree (entity1);


--
-- Name: l_area_instrument_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_instrument_idx_uniq ON musicbrainz.l_area_instrument USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_label_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_label_idx_entity1 ON musicbrainz.l_area_label USING btree (entity1);


--
-- Name: l_area_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_label_idx_uniq ON musicbrainz.l_area_label USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_place_idx_entity1 ON musicbrainz.l_area_place USING btree (entity1);


--
-- Name: l_area_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_place_idx_uniq ON musicbrainz.l_area_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_recording_idx_entity1 ON musicbrainz.l_area_recording USING btree (entity1);


--
-- Name: l_area_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_recording_idx_uniq ON musicbrainz.l_area_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_release_group_idx_entity1 ON musicbrainz.l_area_release_group USING btree (entity1);


--
-- Name: l_area_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_release_group_idx_uniq ON musicbrainz.l_area_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_release_idx_entity1 ON musicbrainz.l_area_release USING btree (entity1);


--
-- Name: l_area_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_release_idx_uniq ON musicbrainz.l_area_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_series_idx_entity1 ON musicbrainz.l_area_series USING btree (entity1);


--
-- Name: l_area_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_series_idx_uniq ON musicbrainz.l_area_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_url_idx_entity1 ON musicbrainz.l_area_url USING btree (entity1);


--
-- Name: l_area_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_url_idx_uniq ON musicbrainz.l_area_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_area_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_area_work_idx_entity1 ON musicbrainz.l_area_work USING btree (entity1);


--
-- Name: l_area_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_area_work_idx_uniq ON musicbrainz.l_area_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_artist_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_artist_idx_entity1 ON musicbrainz.l_artist_artist USING btree (entity1);


--
-- Name: l_artist_artist_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_artist_idx_uniq ON musicbrainz.l_artist_artist USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_event_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_event_idx_entity1 ON musicbrainz.l_artist_event USING btree (entity1);


--
-- Name: l_artist_event_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_event_idx_uniq ON musicbrainz.l_artist_event USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_instrument_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_instrument_idx_entity1 ON musicbrainz.l_artist_instrument USING btree (entity1);


--
-- Name: l_artist_instrument_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_instrument_idx_uniq ON musicbrainz.l_artist_instrument USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_label_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_label_idx_entity1 ON musicbrainz.l_artist_label USING btree (entity1);


--
-- Name: l_artist_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_label_idx_uniq ON musicbrainz.l_artist_label USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_place_idx_entity1 ON musicbrainz.l_artist_place USING btree (entity1);


--
-- Name: l_artist_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_place_idx_uniq ON musicbrainz.l_artist_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_recording_idx_entity1 ON musicbrainz.l_artist_recording USING btree (entity1);


--
-- Name: l_artist_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_recording_idx_uniq ON musicbrainz.l_artist_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_release_group_idx_entity1 ON musicbrainz.l_artist_release_group USING btree (entity1);


--
-- Name: l_artist_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_release_group_idx_uniq ON musicbrainz.l_artist_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_release_idx_entity1 ON musicbrainz.l_artist_release USING btree (entity1);


--
-- Name: l_artist_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_release_idx_uniq ON musicbrainz.l_artist_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_series_idx_entity1 ON musicbrainz.l_artist_series USING btree (entity1);


--
-- Name: l_artist_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_series_idx_uniq ON musicbrainz.l_artist_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_url_idx_entity1 ON musicbrainz.l_artist_url USING btree (entity1);


--
-- Name: l_artist_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_url_idx_uniq ON musicbrainz.l_artist_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_artist_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_artist_work_idx_entity1 ON musicbrainz.l_artist_work USING btree (entity1);


--
-- Name: l_artist_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_artist_work_idx_uniq ON musicbrainz.l_artist_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_event_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_event_idx_entity1 ON musicbrainz.l_event_event USING btree (entity1);


--
-- Name: l_event_event_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_event_idx_uniq ON musicbrainz.l_event_event USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_instrument_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_instrument_idx_entity1 ON musicbrainz.l_event_instrument USING btree (entity1);


--
-- Name: l_event_instrument_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_instrument_idx_uniq ON musicbrainz.l_event_instrument USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_label_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_label_idx_entity1 ON musicbrainz.l_event_label USING btree (entity1);


--
-- Name: l_event_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_label_idx_uniq ON musicbrainz.l_event_label USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_place_idx_entity1 ON musicbrainz.l_event_place USING btree (entity1);


--
-- Name: l_event_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_place_idx_uniq ON musicbrainz.l_event_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_recording_idx_entity1 ON musicbrainz.l_event_recording USING btree (entity1);


--
-- Name: l_event_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_recording_idx_uniq ON musicbrainz.l_event_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_release_group_idx_entity1 ON musicbrainz.l_event_release_group USING btree (entity1);


--
-- Name: l_event_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_release_group_idx_uniq ON musicbrainz.l_event_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_release_idx_entity1 ON musicbrainz.l_event_release USING btree (entity1);


--
-- Name: l_event_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_release_idx_uniq ON musicbrainz.l_event_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_series_idx_entity1 ON musicbrainz.l_event_series USING btree (entity1);


--
-- Name: l_event_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_series_idx_uniq ON musicbrainz.l_event_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_url_idx_entity1 ON musicbrainz.l_event_url USING btree (entity1);


--
-- Name: l_event_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_url_idx_uniq ON musicbrainz.l_event_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_event_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_event_work_idx_entity1 ON musicbrainz.l_event_work USING btree (entity1);


--
-- Name: l_event_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_event_work_idx_uniq ON musicbrainz.l_event_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_instrument_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_instrument_idx_entity1 ON musicbrainz.l_instrument_instrument USING btree (entity1);


--
-- Name: l_instrument_instrument_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_instrument_idx_uniq ON musicbrainz.l_instrument_instrument USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_label_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_label_idx_entity1 ON musicbrainz.l_instrument_label USING btree (entity1);


--
-- Name: l_instrument_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_label_idx_uniq ON musicbrainz.l_instrument_label USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_place_idx_entity1 ON musicbrainz.l_instrument_place USING btree (entity1);


--
-- Name: l_instrument_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_place_idx_uniq ON musicbrainz.l_instrument_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_recording_idx_entity1 ON musicbrainz.l_instrument_recording USING btree (entity1);


--
-- Name: l_instrument_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_recording_idx_uniq ON musicbrainz.l_instrument_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_release_group_idx_entity1 ON musicbrainz.l_instrument_release_group USING btree (entity1);


--
-- Name: l_instrument_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_release_group_idx_uniq ON musicbrainz.l_instrument_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_release_idx_entity1 ON musicbrainz.l_instrument_release USING btree (entity1);


--
-- Name: l_instrument_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_release_idx_uniq ON musicbrainz.l_instrument_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_series_idx_entity1 ON musicbrainz.l_instrument_series USING btree (entity1);


--
-- Name: l_instrument_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_series_idx_uniq ON musicbrainz.l_instrument_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_url_idx_entity1 ON musicbrainz.l_instrument_url USING btree (entity1);


--
-- Name: l_instrument_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_url_idx_uniq ON musicbrainz.l_instrument_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_instrument_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_instrument_work_idx_entity1 ON musicbrainz.l_instrument_work USING btree (entity1);


--
-- Name: l_instrument_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_instrument_work_idx_uniq ON musicbrainz.l_instrument_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_label_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_label_idx_entity1 ON musicbrainz.l_label_label USING btree (entity1);


--
-- Name: l_label_label_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_label_idx_uniq ON musicbrainz.l_label_label USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_place_idx_entity1 ON musicbrainz.l_label_place USING btree (entity1);


--
-- Name: l_label_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_place_idx_uniq ON musicbrainz.l_label_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_recording_idx_entity1 ON musicbrainz.l_label_recording USING btree (entity1);


--
-- Name: l_label_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_recording_idx_uniq ON musicbrainz.l_label_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_release_group_idx_entity1 ON musicbrainz.l_label_release_group USING btree (entity1);


--
-- Name: l_label_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_release_group_idx_uniq ON musicbrainz.l_label_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_release_idx_entity1 ON musicbrainz.l_label_release USING btree (entity1);


--
-- Name: l_label_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_release_idx_uniq ON musicbrainz.l_label_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_series_idx_entity1 ON musicbrainz.l_label_series USING btree (entity1);


--
-- Name: l_label_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_series_idx_uniq ON musicbrainz.l_label_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_url_idx_entity1 ON musicbrainz.l_label_url USING btree (entity1);


--
-- Name: l_label_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_url_idx_uniq ON musicbrainz.l_label_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_label_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_label_work_idx_entity1 ON musicbrainz.l_label_work USING btree (entity1);


--
-- Name: l_label_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_label_work_idx_uniq ON musicbrainz.l_label_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_place_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_place_idx_entity1 ON musicbrainz.l_place_place USING btree (entity1);


--
-- Name: l_place_place_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_place_idx_uniq ON musicbrainz.l_place_place USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_recording_idx_entity1 ON musicbrainz.l_place_recording USING btree (entity1);


--
-- Name: l_place_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_recording_idx_uniq ON musicbrainz.l_place_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_release_group_idx_entity1 ON musicbrainz.l_place_release_group USING btree (entity1);


--
-- Name: l_place_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_release_group_idx_uniq ON musicbrainz.l_place_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_release_idx_entity1 ON musicbrainz.l_place_release USING btree (entity1);


--
-- Name: l_place_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_release_idx_uniq ON musicbrainz.l_place_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_series_idx_entity1 ON musicbrainz.l_place_series USING btree (entity1);


--
-- Name: l_place_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_series_idx_uniq ON musicbrainz.l_place_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_url_idx_entity1 ON musicbrainz.l_place_url USING btree (entity1);


--
-- Name: l_place_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_url_idx_uniq ON musicbrainz.l_place_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_place_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_place_work_idx_entity1 ON musicbrainz.l_place_work USING btree (entity1);


--
-- Name: l_place_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_place_work_idx_uniq ON musicbrainz.l_place_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_recording_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_recording_idx_entity1 ON musicbrainz.l_recording_recording USING btree (entity1);


--
-- Name: l_recording_recording_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_recording_idx_uniq ON musicbrainz.l_recording_recording USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_release_group_idx_entity1 ON musicbrainz.l_recording_release_group USING btree (entity1);


--
-- Name: l_recording_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_release_group_idx_uniq ON musicbrainz.l_recording_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_release_idx_entity1 ON musicbrainz.l_recording_release USING btree (entity1);


--
-- Name: l_recording_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_release_idx_uniq ON musicbrainz.l_recording_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_series_idx_entity1 ON musicbrainz.l_recording_series USING btree (entity1);


--
-- Name: l_recording_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_series_idx_uniq ON musicbrainz.l_recording_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_url_idx_entity1 ON musicbrainz.l_recording_url USING btree (entity1);


--
-- Name: l_recording_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_url_idx_uniq ON musicbrainz.l_recording_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_recording_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_recording_work_idx_entity1 ON musicbrainz.l_recording_work USING btree (entity1);


--
-- Name: l_recording_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_recording_work_idx_uniq ON musicbrainz.l_recording_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_group_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_group_release_group_idx_entity1 ON musicbrainz.l_release_group_release_group USING btree (entity1);


--
-- Name: l_release_group_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_group_release_group_idx_uniq ON musicbrainz.l_release_group_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_group_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_group_series_idx_entity1 ON musicbrainz.l_release_group_series USING btree (entity1);


--
-- Name: l_release_group_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_group_series_idx_uniq ON musicbrainz.l_release_group_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_group_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_group_url_idx_entity1 ON musicbrainz.l_release_group_url USING btree (entity1);


--
-- Name: l_release_group_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_group_url_idx_uniq ON musicbrainz.l_release_group_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_group_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_group_work_idx_entity1 ON musicbrainz.l_release_group_work USING btree (entity1);


--
-- Name: l_release_group_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_group_work_idx_uniq ON musicbrainz.l_release_group_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_release_group_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_release_group_idx_entity1 ON musicbrainz.l_release_release_group USING btree (entity1);


--
-- Name: l_release_release_group_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_release_group_idx_uniq ON musicbrainz.l_release_release_group USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_release_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_release_idx_entity1 ON musicbrainz.l_release_release USING btree (entity1);


--
-- Name: l_release_release_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_release_idx_uniq ON musicbrainz.l_release_release USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_series_idx_entity1 ON musicbrainz.l_release_series USING btree (entity1);


--
-- Name: l_release_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_series_idx_uniq ON musicbrainz.l_release_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_url_idx_entity1 ON musicbrainz.l_release_url USING btree (entity1);


--
-- Name: l_release_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_url_idx_uniq ON musicbrainz.l_release_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_release_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_release_work_idx_entity1 ON musicbrainz.l_release_work USING btree (entity1);


--
-- Name: l_release_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_release_work_idx_uniq ON musicbrainz.l_release_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_series_series_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_series_series_idx_entity1 ON musicbrainz.l_series_series USING btree (entity1);


--
-- Name: l_series_series_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_series_series_idx_uniq ON musicbrainz.l_series_series USING btree (entity0, entity1, link, link_order);


--
-- Name: l_series_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_series_url_idx_entity1 ON musicbrainz.l_series_url USING btree (entity1);


--
-- Name: l_series_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_series_url_idx_uniq ON musicbrainz.l_series_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_series_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_series_work_idx_entity1 ON musicbrainz.l_series_work USING btree (entity1);


--
-- Name: l_series_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_series_work_idx_uniq ON musicbrainz.l_series_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_url_url_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_url_url_idx_entity1 ON musicbrainz.l_url_url USING btree (entity1);


--
-- Name: l_url_url_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_url_url_idx_uniq ON musicbrainz.l_url_url USING btree (entity0, entity1, link, link_order);


--
-- Name: l_url_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_url_work_idx_entity1 ON musicbrainz.l_url_work USING btree (entity1);


--
-- Name: l_url_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_url_work_idx_uniq ON musicbrainz.l_url_work USING btree (entity0, entity1, link, link_order);


--
-- Name: l_work_work_idx_entity1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX l_work_work_idx_entity1 ON musicbrainz.l_work_work USING btree (entity1);


--
-- Name: l_work_work_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX l_work_work_idx_uniq ON musicbrainz.l_work_work USING btree (entity0, entity1, link, link_order);


--
-- Name: label_alias_idx_label; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_alias_idx_label ON musicbrainz.label_alias USING btree (label);


--
-- Name: label_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_alias_idx_primary ON musicbrainz.label_alias USING btree (label, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: label_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_alias_type_idx_gid ON musicbrainz.label_alias_type USING btree (gid);


--
-- Name: label_attribute_idx_label; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_attribute_idx_label ON musicbrainz.label_attribute USING btree (label);


--
-- Name: label_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_attribute_type_allowed_value_idx_gid ON musicbrainz.label_attribute_type_allowed_value USING btree (gid);


--
-- Name: label_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_attribute_type_allowed_value_idx_name ON musicbrainz.label_attribute_type_allowed_value USING btree (label_attribute_type);


--
-- Name: label_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_attribute_type_idx_gid ON musicbrainz.label_attribute_type USING btree (gid);


--
-- Name: label_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_gid_redirect_idx_new_id ON musicbrainz.label_gid_redirect USING btree (new_id);


--
-- Name: label_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_idx_area ON musicbrainz.label USING btree (area);


--
-- Name: label_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_idx_gid ON musicbrainz.label USING btree (gid);


--
-- Name: label_idx_lower_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_idx_lower_name ON musicbrainz.label USING btree (lower((name)::text));


--
-- Name: label_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_idx_name ON musicbrainz.label USING btree (name);


--
-- Name: label_idx_null_comment; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_idx_null_comment ON musicbrainz.label USING btree (name) WHERE (comment IS NULL);


--
-- Name: label_idx_uniq_name_comment; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_idx_uniq_name_comment ON musicbrainz.label USING btree (name, comment) WHERE (comment IS NOT NULL);


--
-- Name: label_rating_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_rating_raw_idx_editor ON musicbrainz.label_rating_raw USING btree (editor);


--
-- Name: label_rating_raw_idx_label; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_rating_raw_idx_label ON musicbrainz.label_rating_raw USING btree (label);


--
-- Name: label_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_tag_idx_tag ON musicbrainz.label_tag USING btree (tag);


--
-- Name: label_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_tag_raw_idx_editor ON musicbrainz.label_tag_raw USING btree (editor);


--
-- Name: label_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX label_tag_raw_idx_tag ON musicbrainz.label_tag_raw USING btree (tag);


--
-- Name: label_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX label_type_idx_gid ON musicbrainz.label_type USING btree (gid);


--
-- Name: language_idx_iso_code_1; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX language_idx_iso_code_1 ON musicbrainz.language USING btree (iso_code_1);


--
-- Name: language_idx_iso_code_2b; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX language_idx_iso_code_2b ON musicbrainz.language USING btree (iso_code_2b);


--
-- Name: language_idx_iso_code_2t; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX language_idx_iso_code_2t ON musicbrainz.language USING btree (iso_code_2t);


--
-- Name: language_idx_iso_code_3; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX language_idx_iso_code_3 ON musicbrainz.language USING btree (iso_code_3);


--
-- Name: link_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX link_attribute_type_idx_gid ON musicbrainz.link_attribute_type USING btree (gid);


--
-- Name: link_idx_type_attr; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX link_idx_type_attr ON musicbrainz.link USING btree (link_type, attribute_count);


--
-- Name: link_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX link_type_idx_gid ON musicbrainz.link_type USING btree (gid);


--
-- Name: medium_attribute_idx_medium; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_attribute_idx_medium ON musicbrainz.medium_attribute USING btree (medium);


--
-- Name: medium_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX medium_attribute_type_allowed_value_idx_gid ON musicbrainz.medium_attribute_type_allowed_value USING btree (gid);


--
-- Name: medium_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_attribute_type_allowed_value_idx_name ON musicbrainz.medium_attribute_type_allowed_value USING btree (medium_attribute_type);


--
-- Name: medium_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX medium_attribute_type_idx_gid ON musicbrainz.medium_attribute_type USING btree (gid);


--
-- Name: medium_cdtoc_idx_cdtoc; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_cdtoc_idx_cdtoc ON musicbrainz.medium_cdtoc USING btree (cdtoc);


--
-- Name: medium_cdtoc_idx_medium; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_cdtoc_idx_medium ON musicbrainz.medium_cdtoc USING btree (medium);


--
-- Name: medium_cdtoc_idx_uniq; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX medium_cdtoc_idx_uniq ON musicbrainz.medium_cdtoc USING btree (medium, cdtoc);


--
-- Name: medium_format_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX medium_format_idx_gid ON musicbrainz.medium_format USING btree (gid);


--
-- Name: medium_idx_release_position; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_idx_release_position ON musicbrainz.medium USING btree (release, "position");


--
-- Name: medium_idx_track_count; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX medium_idx_track_count ON musicbrainz.medium USING btree (track_count);


--
-- Name: old_editor_name_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX old_editor_name_idx_name ON musicbrainz.old_editor_name USING btree (lower((name)::text));


--
-- Name: place_alias_idx_place; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_alias_idx_place ON musicbrainz.place_alias USING btree (place);


--
-- Name: place_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_alias_idx_primary ON musicbrainz.place_alias USING btree (place, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: place_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_alias_type_idx_gid ON musicbrainz.place_alias_type USING btree (gid);


--
-- Name: place_attribute_idx_place; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_attribute_idx_place ON musicbrainz.place_attribute USING btree (place);


--
-- Name: place_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_attribute_type_allowed_value_idx_gid ON musicbrainz.place_attribute_type_allowed_value USING btree (gid);


--
-- Name: place_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_attribute_type_allowed_value_idx_name ON musicbrainz.place_attribute_type_allowed_value USING btree (place_attribute_type);


--
-- Name: place_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_attribute_type_idx_gid ON musicbrainz.place_attribute_type USING btree (gid);


--
-- Name: place_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_gid_redirect_idx_new_id ON musicbrainz.place_gid_redirect USING btree (new_id);


--
-- Name: place_idx_area; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_idx_area ON musicbrainz.place USING btree (area);


--
-- Name: place_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_idx_gid ON musicbrainz.place USING btree (gid);


--
-- Name: place_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_idx_name ON musicbrainz.place USING btree (name);


--
-- Name: place_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_tag_idx_tag ON musicbrainz.place_tag USING btree (tag);


--
-- Name: place_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_tag_raw_idx_editor ON musicbrainz.place_tag_raw USING btree (editor);


--
-- Name: place_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX place_tag_raw_idx_tag ON musicbrainz.place_tag_raw USING btree (tag);


--
-- Name: place_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX place_type_idx_gid ON musicbrainz.place_type USING btree (gid);


--
-- Name: recording_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX recording_alias_idx_primary ON musicbrainz.recording_alias USING btree (recording, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: recording_alias_idx_recording; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_alias_idx_recording ON musicbrainz.recording_alias USING btree (recording);


--
-- Name: recording_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX recording_alias_type_idx_gid ON musicbrainz.recording_alias_type USING btree (gid);


--
-- Name: recording_attribute_idx_recording; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_attribute_idx_recording ON musicbrainz.recording_attribute USING btree (recording);


--
-- Name: recording_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX recording_attribute_type_allowed_value_idx_gid ON musicbrainz.recording_attribute_type_allowed_value USING btree (gid);


--
-- Name: recording_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_attribute_type_allowed_value_idx_name ON musicbrainz.recording_attribute_type_allowed_value USING btree (recording_attribute_type);


--
-- Name: recording_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX recording_attribute_type_idx_gid ON musicbrainz.recording_attribute_type USING btree (gid);


--
-- Name: recording_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_gid_redirect_idx_new_id ON musicbrainz.recording_gid_redirect USING btree (new_id);


--
-- Name: recording_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_idx_artist_credit ON musicbrainz.recording USING btree (artist_credit);


--
-- Name: recording_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX recording_idx_gid ON musicbrainz.recording USING btree (gid);


--
-- Name: recording_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_idx_name ON musicbrainz.recording USING btree (name);


--
-- Name: recording_rating_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_rating_raw_idx_editor ON musicbrainz.recording_rating_raw USING btree (editor);


--
-- Name: recording_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_tag_idx_tag ON musicbrainz.recording_tag USING btree (tag);


--
-- Name: recording_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_tag_raw_idx_editor ON musicbrainz.recording_tag_raw USING btree (editor);


--
-- Name: recording_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_tag_raw_idx_tag ON musicbrainz.recording_tag_raw USING btree (tag);


--
-- Name: recording_tag_raw_idx_track; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX recording_tag_raw_idx_track ON musicbrainz.recording_tag_raw USING btree (recording);


--
-- Name: release_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_alias_idx_primary ON musicbrainz.release_alias USING btree (release, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: release_alias_idx_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_alias_idx_release ON musicbrainz.release_alias USING btree (release);


--
-- Name: release_attribute_idx_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_attribute_idx_release ON musicbrainz.release_attribute USING btree (release);


--
-- Name: release_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_attribute_type_allowed_value_idx_gid ON musicbrainz.release_attribute_type_allowed_value USING btree (gid);


--
-- Name: release_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_attribute_type_allowed_value_idx_name ON musicbrainz.release_attribute_type_allowed_value USING btree (release_attribute_type);


--
-- Name: release_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_attribute_type_idx_gid ON musicbrainz.release_attribute_type USING btree (gid);


--
-- Name: release_country_idx_country; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_country_idx_country ON musicbrainz.release_country USING btree (country);


--
-- Name: release_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_gid_redirect_idx_new_id ON musicbrainz.release_gid_redirect USING btree (new_id);


--
-- Name: release_group_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_alias_idx_primary ON musicbrainz.release_group_alias USING btree (release_group, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: release_group_alias_idx_release_group; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_alias_idx_release_group ON musicbrainz.release_group_alias USING btree (release_group);


--
-- Name: release_group_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_alias_type_idx_gid ON musicbrainz.release_group_alias_type USING btree (gid);


--
-- Name: release_group_attribute_idx_release_group; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_attribute_idx_release_group ON musicbrainz.release_group_attribute USING btree (release_group);


--
-- Name: release_group_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_attribute_type_allowed_value_idx_gid ON musicbrainz.release_group_attribute_type_allowed_value USING btree (gid);


--
-- Name: release_group_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_attribute_type_allowed_value_idx_name ON musicbrainz.release_group_attribute_type_allowed_value USING btree (release_group_attribute_type);


--
-- Name: release_group_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_attribute_type_idx_gid ON musicbrainz.release_group_attribute_type USING btree (gid);


--
-- Name: release_group_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_gid_redirect_idx_new_id ON musicbrainz.release_group_gid_redirect USING btree (new_id);


--
-- Name: release_group_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_idx_artist_credit ON musicbrainz.release_group USING btree (artist_credit);


--
-- Name: release_group_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_idx_gid ON musicbrainz.release_group USING btree (gid);


--
-- Name: release_group_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_idx_name ON musicbrainz.release_group USING btree (name);


--
-- Name: release_group_primary_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_primary_type_idx_gid ON musicbrainz.release_group_primary_type USING btree (gid);


--
-- Name: release_group_rating_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_rating_raw_idx_editor ON musicbrainz.release_group_rating_raw USING btree (editor);


--
-- Name: release_group_rating_raw_idx_release_group; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_rating_raw_idx_release_group ON musicbrainz.release_group_rating_raw USING btree (release_group);


--
-- Name: release_group_secondary_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_group_secondary_type_idx_gid ON musicbrainz.release_group_secondary_type USING btree (gid);


--
-- Name: release_group_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_tag_idx_tag ON musicbrainz.release_group_tag USING btree (tag);


--
-- Name: release_group_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_tag_raw_idx_editor ON musicbrainz.release_group_tag_raw USING btree (editor);


--
-- Name: release_group_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_group_tag_raw_idx_tag ON musicbrainz.release_group_tag_raw USING btree (tag);


--
-- Name: release_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_idx_artist_credit ON musicbrainz.release USING btree (artist_credit);


--
-- Name: release_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_idx_gid ON musicbrainz.release USING btree (gid);


--
-- Name: release_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_idx_name ON musicbrainz.release USING btree (name);


--
-- Name: release_idx_release_group; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_idx_release_group ON musicbrainz.release USING btree (release_group);


--
-- Name: release_label_idx_label; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_label_idx_label ON musicbrainz.release_label USING btree (label);


--
-- Name: release_label_idx_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_label_idx_release ON musicbrainz.release_label USING btree (release);


--
-- Name: release_packaging_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_packaging_idx_gid ON musicbrainz.release_packaging USING btree (gid);


--
-- Name: release_raw_idx_last_modified; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_raw_idx_last_modified ON musicbrainz.release_raw USING btree (last_modified);


--
-- Name: release_raw_idx_lookup_count; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_raw_idx_lookup_count ON musicbrainz.release_raw USING btree (lookup_count);


--
-- Name: release_raw_idx_modify_count; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_raw_idx_modify_count ON musicbrainz.release_raw USING btree (modify_count);


--
-- Name: release_status_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX release_status_idx_gid ON musicbrainz.release_status USING btree (gid);


--
-- Name: release_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_tag_idx_tag ON musicbrainz.release_tag USING btree (tag);


--
-- Name: release_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_tag_raw_idx_editor ON musicbrainz.release_tag_raw USING btree (editor);


--
-- Name: release_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX release_tag_raw_idx_tag ON musicbrainz.release_tag_raw USING btree (tag);


--
-- Name: script_idx_iso_code; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX script_idx_iso_code ON musicbrainz.script USING btree (iso_code);


--
-- Name: series_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_alias_idx_primary ON musicbrainz.series_alias USING btree (series, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: series_alias_idx_series; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_alias_idx_series ON musicbrainz.series_alias USING btree (series);


--
-- Name: series_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_alias_type_idx_gid ON musicbrainz.series_alias_type USING btree (gid);


--
-- Name: series_attribute_idx_series; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_attribute_idx_series ON musicbrainz.series_attribute USING btree (series);


--
-- Name: series_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_attribute_type_allowed_value_idx_gid ON musicbrainz.series_attribute_type_allowed_value USING btree (gid);


--
-- Name: series_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_attribute_type_allowed_value_idx_name ON musicbrainz.series_attribute_type_allowed_value USING btree (series_attribute_type);


--
-- Name: series_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_attribute_type_idx_gid ON musicbrainz.series_attribute_type USING btree (gid);


--
-- Name: series_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_gid_redirect_idx_new_id ON musicbrainz.series_gid_redirect USING btree (new_id);


--
-- Name: series_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_idx_gid ON musicbrainz.series USING btree (gid);


--
-- Name: series_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_idx_name ON musicbrainz.series USING btree (name);


--
-- Name: series_ordering_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_ordering_type_idx_gid ON musicbrainz.series_ordering_type USING btree (gid);


--
-- Name: series_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_tag_idx_tag ON musicbrainz.series_tag USING btree (tag);


--
-- Name: series_tag_raw_idx_editor; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_tag_raw_idx_editor ON musicbrainz.series_tag_raw USING btree (editor);


--
-- Name: series_tag_raw_idx_series; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_tag_raw_idx_series ON musicbrainz.series_tag_raw USING btree (series);


--
-- Name: series_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX series_tag_raw_idx_tag ON musicbrainz.series_tag_raw USING btree (tag);


--
-- Name: series_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX series_type_idx_gid ON musicbrainz.series_type USING btree (gid);


--
-- Name: tag_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX tag_idx_name ON musicbrainz.tag USING btree (name);


--
-- Name: track_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_gid_redirect_idx_new_id ON musicbrainz.track_gid_redirect USING btree (new_id);


--
-- Name: track_idx_artist_credit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_idx_artist_credit ON musicbrainz.track USING btree (artist_credit);


--
-- Name: track_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX track_idx_gid ON musicbrainz.track USING btree (gid);


--
-- Name: track_idx_medium_position; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_idx_medium_position ON musicbrainz.track USING btree (medium, "position");


--
-- Name: track_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_idx_name ON musicbrainz.track USING btree (name);


--
-- Name: track_idx_recording; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_idx_recording ON musicbrainz.track USING btree (recording);


--
-- Name: track_raw_idx_release; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX track_raw_idx_release ON musicbrainz.track_raw USING btree (release);


--
-- Name: url_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX url_gid_redirect_idx_new_id ON musicbrainz.url_gid_redirect USING btree (new_id);


--
-- Name: url_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX url_idx_gid ON musicbrainz.url USING btree (gid);


--
-- Name: url_idx_url; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX url_idx_url ON musicbrainz.url USING btree (url);


--
-- Name: vote_idx_edit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX vote_idx_edit ON musicbrainz.vote USING btree (edit);


--
-- Name: vote_idx_editor_edit; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX vote_idx_editor_edit ON musicbrainz.vote USING btree (editor, edit) WHERE (superseded = false);


--
-- Name: vote_idx_editor_vote_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX vote_idx_editor_vote_time ON musicbrainz.vote USING btree (editor, vote_time);


--
-- Name: vote_idx_vote_time; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX vote_idx_vote_time ON musicbrainz.vote USING btree (vote_time);


--
-- Name: work_alias_idx_primary; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_alias_idx_primary ON musicbrainz.work_alias USING btree (work, locale) WHERE ((primary_for_locale = true) AND (locale IS NOT NULL));


--
-- Name: work_alias_idx_work; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_alias_idx_work ON musicbrainz.work_alias USING btree (work);


--
-- Name: work_alias_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_alias_type_idx_gid ON musicbrainz.work_alias_type USING btree (gid);


--
-- Name: work_attribute_idx_work; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_attribute_idx_work ON musicbrainz.work_attribute USING btree (work);


--
-- Name: work_attribute_type_allowed_value_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_attribute_type_allowed_value_idx_gid ON musicbrainz.work_attribute_type_allowed_value USING btree (gid);


--
-- Name: work_attribute_type_allowed_value_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_attribute_type_allowed_value_idx_name ON musicbrainz.work_attribute_type_allowed_value USING btree (work_attribute_type);


--
-- Name: work_attribute_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_attribute_type_idx_gid ON musicbrainz.work_attribute_type USING btree (gid);


--
-- Name: work_gid_redirect_idx_new_id; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_gid_redirect_idx_new_id ON musicbrainz.work_gid_redirect USING btree (new_id);


--
-- Name: work_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_idx_gid ON musicbrainz.work USING btree (gid);


--
-- Name: work_idx_name; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_idx_name ON musicbrainz.work USING btree (name);


--
-- Name: work_tag_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_tag_idx_tag ON musicbrainz.work_tag USING btree (tag);


--
-- Name: work_tag_raw_idx_tag; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE INDEX work_tag_raw_idx_tag ON musicbrainz.work_tag_raw USING btree (tag);


--
-- Name: work_type_idx_gid; Type: INDEX; Schema: musicbrainz; Owner: -
--

CREATE UNIQUE INDEX work_type_idx_gid ON musicbrainz.work_type USING btree (gid);


--
-- Name: statistic_name; Type: INDEX; Schema: statistics; Owner: -
--

CREATE INDEX statistic_name ON statistics.statistic USING btree (name);


--
-- Name: statistic_name_date_collected; Type: INDEX; Schema: statistics; Owner: -
--

CREATE UNIQUE INDEX statistic_name_date_collected ON statistics.statistic USING btree (name, date_collected);


--
-- PostgreSQL database dump complete
--

