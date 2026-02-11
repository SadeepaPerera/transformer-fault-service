-- =========================================================
-- TMS: Image Fault Analysis storage
-- File: 001_tms_image_fault_analysis.sql
-- Purpose:
--   Stores Gemini AI output + Human review history per image_id
-- =========================================================

-- 1) Enum type (needed before table)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_type WHERE typname = 'tms_analysis_status_enum'
  ) THEN
    CREATE TYPE tms_analysis_status_enum AS ENUM (
      'AI_ANALYZED',
      'HUMAN_REVIEW_PENDING',
      'CONFIRMED'
    );
  END IF;
END $$;

-- 2) Sequence (needed before table default nextval)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_class WHERE relkind='S' AND relname='tms_image_fault_analysis_id_seq'
  ) THEN
    CREATE SEQUENCE tms_image_fault_analysis_id_seq
      START WITH 1
      INCREMENT BY 1
      NO MINVALUE
      NO MAXVALUE
      CACHE 1;
  END IF;
END $$;

-- 3) Table
CREATE TABLE IF NOT EXISTS public.tms_image_fault_analysis (
  id bigint NOT NULL DEFAULT nextval('tms_image_fault_analysis_id_seq'::regclass),
  image_id bigint NOT NULL,
  image_url text NOT NULL,
  status tms_analysis_status_enum NOT NULL DEFAULT 'AI_ANALYZED'::tms_analysis_status_enum,
  gemini_response jsonb NOT NULL,
  human_reviews jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);

-- 4) Constraints
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'tms_image_fault_analysis_pkey'
  ) THEN
    ALTER TABLE public.tms_image_fault_analysis
      ADD CONSTRAINT tms_image_fault_analysis_pkey PRIMARY KEY (id);
  END IF;
END $$;

-- unique image_id
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uq_tms_image_id'
  ) THEN
    ALTER TABLE public.tms_image_fault_analysis
      ADD CONSTRAINT uq_tms_image_id UNIQUE (image_id);
  END IF;
END $$;

-- 5) Indexes
CREATE INDEX IF NOT EXISTS idx_tms_fault_status
  ON public.tms_image_fault_analysis USING btree (status);
