CREATE TABLE "I2B2METADATA"."STUDY_DIMENSION_DESCRIPTIONS"
  (	"DIMENSION_DESCRIPTION_ID" NUMBER NOT NULL ENABLE,
"STUDY_ID" NUMBER NOT NULL ENABLE,
 PRIMARY KEY ("STUDY_ID", "DIMENSION_DESCRIPTION_ID")
 USING INDEX
 TABLESPACE "TRANSMART"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
NOCOMPRESS LOGGING
 TABLESPACE "TRANSMART" ;

ALTER TABLE "I2B2METADATA"."STUDY_DIMENSION_DESCRIPTIONS" ADD CONSTRAINT "FK_DIMENSION_DESCRIPTION_ID" FOREIGN KEY ("DIMENSION_DESCRIPTION_ID")
 REFERENCES "I2B2METADATA"."DIMENSION_DESCRIPTION" ("ID") ENABLE;