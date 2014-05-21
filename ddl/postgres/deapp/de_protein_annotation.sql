--
-- Name: de_protein_annotation; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_protein_annotation (
    id bigint NOT NULL,
    gpl_id character varying(50) NOT NULL,
    peptide character varying(200) NOT NULL,
    uniprot_id character varying(50),
    biomarker_id character varying(200),
    organism character varying(200),
    uniprot_name character varying(200)
);

--
-- Name: de_protein_annotation_pkey; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_protein_annotation
    ADD CONSTRAINT de_protein_annotation_pkey PRIMARY KEY (id);

