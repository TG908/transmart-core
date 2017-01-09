CREATE SEQUENCE  "I2B2DEMODATA"."STUDY_NUM_SEQ";

CREATE TABLE "I2B2DEMODATA"."STUDY"
  (	"STUDY_NUM" NUMBER(38,0) DEFAULT "I2B2DEMODATA"."STUDY_NUM_SEQ"."NEXTVAL",
"BIO_EXPERIMENT_ID" NUMBER(18,0),
"STUDY_ID" VARCHAR2(100 BYTE) NOT NULL ENABLE,
"SECURE_OBJ_TOKEN" NVARCHAR2(200) NOT NULL ENABLE,
 CONSTRAINT "STUDY_PK" PRIMARY KEY ("STUDY_NUM")
 USING INDEX
 TABLESPACE "TRANSMART"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
NOCOMPRESS LOGGING
 TABLESPACE "TRANSMART" ;

CREATE INDEX "I2B2DEMODATA"."IDX_STUDY_SECURE_OBJ_TOKEN" ON "I2B2DEMODATA"."STUDY" ("SECURE_OBJ_TOKEN")
TABLESPACE "INDX" ;

CREATE OR REPLACE EDITIONABLE TRIGGER "I2B2DEMODATA"."TRG_STUDY_NUM"
before insert on "I2B2DEMODATA"."STUDY"
for each row
begin
 if inserting then
  if :NEW."STUDY_NUM" is null then
   select STUDY_NUM_SEQ.nextval into :NEW."STUDY_NUM" from dual;
  end if;
 end if;
end;

/
ALTER TRIGGER "I2B2DEMODATA"."TRG_STUDY_NUM" ENABLE;
