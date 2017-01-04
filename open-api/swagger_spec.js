{
    "swagger": "2.0",
    "info": {
        "version": "17.1.0",
        "title": "Transmart"
    },
    "schemes": [
        "http"
    ],
    "consumes": [
        "application/json"
    ],
    "produces": [
        "application/json"
    ],
    "securityDefinitions": {
        "oauth": {
            "type": "oauth2",
            "flow": "implicit",
            "authorizationUrl": "/oauth/authorize?response_type=token&client_id={client_id}&redirect_uri={redirect}",
            "scopes": {
                "basic": "to be able to interact with transmart REST-API"
            }
        }
    },
    "security": [
        {
            "oauth": [
                "basic"
            ]
        }
    ],
    "paths": {
        "/v1/studies": {
            "get": {
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "$ref": "#/definitions/StudyResponse"
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}": {
            "get": {
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "username to fetch",
                        "required": true,
                        "type": "string"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "StudiesResponse",
                            "type": "object",
                            "items": {
                                "title": "StudyArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/concepts": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/concepts/{conceptPath}": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "conceptPath",
                        "in": "path",
                        "description": "Concept path for which info will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/subjects": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/subjects/{subjectid}": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "subjectid",
                        "in": "path",
                        "description": "Subject ID of the subject which will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/concepts/{conceptPath}/subjects": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "conceptPath",
                        "in": "path",
                        "description": "Concept path for which info will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/studies/{studyid}/observations": {
            "get": {
                "parameters": [
                    {
                        "name": "studyid",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    }
                ],
                "description": "Gets all `Study` objects.\n",
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response",
                        "schema": {
                            "title": "ConceptsResponse",
                            "type": "object",
                            "items": {
                                "title": "ConceptArray",
                                "type": "array",
                                "items": {
                                    "title": "study",
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "single": {
                                            "type": "boolean"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v1/observations": {
            "get": {
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response"
                    }
                }
            }
        },
        "/v1/studies/{studyId}/concepts/{conceptPath}/observations": {
            "get": {
                "parameters": [
                    {
                        "name": "studyId",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "conceptPath",
                        "in": "path",
                        "description": "Concept path",
                        "required": true,
                        "type": "string"
                    }
                ],
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response"
                    }
                }
            }
        },
        "/v1/patient_sets/": {
            "post": {
                "parameters": [
                    {
                        "name": "i2b2query_xml",
                        "in": "query",
                        "description": "user to add to the system",
                        "required": true,
                        "type": "string"
                    }
                ],
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response"
                    }
                }
            },
            "get": {
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successfull response"
                    }
                }
            }
        },
        "/v1/patient_sets/{resultInstanceId}": {
            "get": {
                "parameters": [
                    {
                        "name": "resultInstanceId",
                        "in": "path",
                        "description": "ID of the patient set, called resultInstance ID because internally it refers to the result of a query",
                        "required": true,
                        "type": "string"
                    }
                ],
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successfull response"
                    }
                }
            }
        },
        "/v1/studies/{studyId}/concepts/{conceptPath}/highdim": {
            "get": {
                "parameters": [
                    {
                        "name": "studyId",
                        "in": "path",
                        "description": "Study ID of the study for which concepts will be fetched",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "conceptPath",
                        "in": "path",
                        "description": "Concept path",
                        "required": true,
                        "type": "string"
                    },
                    {
                        "name": "dataType",
                        "in": "query",
                        "description": "Data Type constraint",
                        "required": false,
                        "type": "string"
                    },
                    {
                        "name": "projection",
                        "in": "query",
                        "description": "Projection applied to the HDD",
                        "required": false,
                        "type": "string"
                    },
                    {
                        "name": "assayConstraints",
                        "in": "query",
                        "description": "Assay Constraints",
                        "required": false,
                        "type": "string"
                    },
                    {
                        "name": "dataConstraints",
                        "in": "query",
                        "description": "Data constraint",
                        "required": false,
                        "type": "string"
                    }
                ],
                "tags": [
                    "v1"
                ],
                "responses": {
                    "200": {
                        "description": "Successful response"
                    }
                }
            }
        },
        "/v2/observations": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "produces": [
                    "application/json",
                    "application/x-protobuf"
                ],
                "parameters": [
                    {
                        "name": "constraint",
                        "required": true,
                        "in": "query",
                        "description": "json that describes the request, Example: {\"type\":\"StudyNameConstraint\",\"studyId\":\"EHR\"}",
                        "type": "string"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "return the observations that match the constaint",
                        "schema": {
                            "$ref": "#/definitions/observations"
                        }
                    }
                }
            }
        },
        "/v2/observations/aggregate": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "constraint",
                        "required": true,
                        "in": "query",
                        "description": "json that describes the request, Example: {\"type\":\"ConceptConstraint\",\"path\":\"\\\\Public Studies\\\\EHR\\\\Vital Signs\\\\Heart Rate\\\\\"}",
                        "type": "string"
                    },
                    {
                        "name": "type",
                        "required": true,
                        "in": "query",
                        "description": "min, max, average",
                        "type": "string"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "return the result in json Example: {min:56}",
                        "schema": {
                            "type": "object",
                            "description": "only the value from the type in the request will be present",
                            "properties": {
                                "min": {
                                    "type": "number"
                                },
                                "max": {
                                    "type": "number"
                                },
                                "average": {
                                    "type": "number"
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v2/patients": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "constraint",
                        "required": true,
                        "in": "query",
                        "description": "json that describes the request, Example: {\"type\":\"StudyNameConstraint\",\"studyId\":\"EHR\"}",
                        "type": "string"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "patients": {
                                    "type": "array",
                                    "items": {
                                        "$ref": "#/definitions/patient"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v2/patient_sets": {
            "post": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "name",
                        "required": true,
                        "in": "query",
                        "type": "string"
                    },
                    {
                        "name": "constraint",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "description": "json that describes the set, Example: {\"type\":\"StudyNameConstraint\",\"studyId\":\"EHR\"}",
                            "type": "string"
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "description": {
                                    "type": "string"
                                },
                                "errorMessage": {
                                    "description": "null if there are no errors",
                                    "type": "string"
                                },
                                "id": {
                                    "type": "integer"
                                },
                                "setSize": {
                                    "type": "integer"
                                },
                                "status": {
                                    "type": "string"
                                },
                                "username": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v2/high_dim": {
            "get": {
                "summary": "",
                "description": "",
                "produces": [
                    "application/json",
                    "application/x-protobuf"
                ],
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "assay_constraint",
                        "required": true,
                        "in": "query",
                        "description": "json that describes the assays, Example: {\"type\":\"ConceptConstraint\",\"path\":\"\\\\Public Studies\\\\CLINICAL_TRIAL_HIGHDIM\\\\High Dimensional data\\\\Expression Lung\\\\\"}",
                        "type": "string"
                    },
                    {
                        "name": "biomarker_constraint",
                        "in": "query",
                        "description": "json that describes the biomarker, Example: {\"type\":\"BiomarkerConstraint\",\"biomarkerType\":\"genes\",\"params\":{\"names\":[\"TP53\"]}}",
                        "type": "string"
                    },
                    {
                        "name": "projection",
                        "in": "query",
                        "description": "The projection Example: all_data, zscore, log_intensity",
                        "type": "string"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "return the observations that match the constaint",
                        "schema": {
                            "$ref": "#/definitions/observations"
                        }
                    }
                }
            }
        },
        "/v2/tree_nodes": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "root",
                        "in": "query",
                        "type": "string",
                        "description": "The node the requested tree starts from. Example: \\Public Studies\\SHARED_CONCEPTS_STUDY_A\\ "
                    },
                    {
                        "name": "depth",
                        "in": "query",
                        "type": "integer",
                        "description": "the max node depth returned"
                    },
                    {
                        "name": "counts",
                        "in": "query",
                        "type": "boolean",
                        "description": "patient and observation counts will be in the response if set to true"
                    },
                    {
                        "name": "tags",
                        "in": "query",
                        "type": "boolean",
                        "description": "tags will be in the response if set to true"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "tree_nodes": {
                                    "type": "array",
                                    "items": {
                                        "$ref": "#/definitions/treeNode"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "/v2/storage": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "storageSystems": {
                                    "type": "array",
                                    "items": {
                                        "$ref": "#/definitions/storageSystem"
                                    }
                                }
                            }
                        }
                    }
                }
            },
            "post": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "systemType": {
                                    "type": "string"
                                },
                                "url": {
                                    "type": "string"
                                },
                                "systemVersion": {
                                    "type": "string"
                                },
                                "singleFileCollections": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/storageSystem"
                        }
                    }
                }
            }
        },
        "/v2/storage/{id}": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/storageSystem"
                        }
                    }
                }
            },
            "put": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    },
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "systemType": {
                                    "type": "string"
                                },
                                "url": {
                                    "type": "string"
                                },
                                "systemVersion": {
                                    "type": "string"
                                },
                                "singleFileCollections": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/storageSystem"
                        }
                    }
                }
            },
            "delete": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "204": {
                        "description": "returns null"
                    }
                }
            }
        },
        "/v2/files": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "files": {
                                    "type": "array",
                                    "items": {
                                        "$ref": "#/definitions/fileLink"
                                    }
                                }
                            }
                        }
                    }
                }
            },
            "post": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "sourceSystem": {
                                    "type": "integer"
                                },
                                "study": {
                                    "type": "string"
                                },
                                "uuid": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "$ref": "#/definitions/fileLink"
                        }
                    }
                }
            }
        },
        "/v2/files/{id}": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/fileLink"
                        }
                    }
                }
            },
            "put": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    },
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "sourceSystem": {
                                    "type": "integer"
                                },
                                "study": {
                                    "type": "string"
                                },
                                "uuid": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/fileLink"
                        }
                    }
                }
            },
            "delete": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "204": {
                        "description": "returns null"
                    }
                }
            }
        },
        "/v2/arvados/workflows": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2",
                    "arvados"
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "type": "object",
                            "properties": {
                                "supportedWorkflows": {
                                    "type": "array",
                                    "items": {
                                        "$ref": "#/definitions/supportedWorkflow"
                                    }
                                }
                            }
                        }
                    }
                }
            },
            "post": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2",
                    "arvados"
                ],
                "parameters": [
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "id": {
                                    "type": "integer"
                                },
                                "name": {
                                    "type": "string"
                                },
                                "arvadosInstanceUrl": {
                                    "type": "string"
                                },
                                "uuid": {
                                    "type": "string"
                                },
                                "description": {
                                    "type": "string"
                                },
                                "arvadosVersion": {
                                    "type": "string"
                                },
                                "defaultParams": {
                                    "description": "a map of key value pairs",
                                    "type": "object"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "OK",
                        "schema": {
                            "$ref": "#/definitions/supportedWorkflow"
                        }
                    }
                }
            }
        },
        "/v2/arvados/workflows/{id}": {
            "get": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2",
                    "arvados"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/supportedWorkflow"
                        }
                    }
                }
            },
            "put": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2",
                    "arvados"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    },
                    {
                        "name": "body",
                        "in": "body",
                        "required": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                                "id": {
                                    "type": "integer"
                                },
                                "name": {
                                    "type": "string"
                                },
                                "arvadosInstanceUrl": {
                                    "type": "string"
                                },
                                "uuid": {
                                    "type": "string"
                                },
                                "description": {
                                    "type": "string"
                                },
                                "arvadosVersion": {
                                    "type": "string"
                                },
                                "defaultParams": {
                                    "description": "a map of key value pairs",
                                    "type": "object"
                                }
                            }
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "",
                        "schema": {
                            "$ref": "#/definitions/supportedWorkflow"
                        }
                    }
                }
            },
            "delete": {
                "summary": "",
                "description": "",
                "tags": [
                    "v2",
                    "arvados"
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "required": true,
                        "type": "integer"
                    }
                ],
                "responses": {
                    "204": {
                        "description": "returns null"
                    }
                }
            }
        }
    },
    "definitions": {
        "storageSystem": {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer"
                },
                "name": {
                    "type": "string"
                },
                "systemType": {
                    "type": "string"
                },
                "url": {
                    "type": "string"
                },
                "systemVersion": {
                    "type": "string"
                },
                "singleFileCollections": {
                    "type": "boolean"
                }
            }
        },
        "fileLink": {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer"
                },
                "name": {
                    "type": "string"
                },
                "sourceSystem": {
                    "type": "integer"
                },
                "study": {
                    "type": "string"
                },
                "uuid": {
                    "type": "string"
                }
            }
        },
        "supportedWorkflow": {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer"
                },
                "name": {
                    "type": "string"
                },
                "arvadosInstanceUrl": {
                    "type": "string"
                },
                "uuid": {
                    "type": "string"
                },
                "description": {
                    "type": "string"
                },
                "arvadosVersion": {
                    "type": "string"
                },
                "defaultParams": {
                    "description": "a map of key value pairs",
                    "type": "object"
                }
            }
        },
        "treeNode": {
            "type": "object",
            "properties": {
                "children": {
                    "type": "array",
                    "description": "A list of treeNodes if there are any children. ",
                    "items": {
                        "type": "object"
                    }
                },
                "fullName": {
                    "type": "string",
                    "description": "Example: \\Public Studies\\SHARED_CONCEPTS_STUDY_A\\ "
                },
                "name": {
                    "type": "string",
                    "description": "Example: SHARED_CONCEPTS_STUDY_A"
                },
                "type": {
                    "description": "Example: STUDY",
                    "type": "string"
                },
                "visualAttributes": {
                    "type": "array",
                    "items": {
                        "description": "Example: [FOLDER, ACTIVE, STUDY]",
                        "type": "string"
                    }
                },
                "observationCount": {
                    "description": "only available on ConceptNodes",
                    "type": "integer"
                },
                "patientCount": {
                    "description": "only available on ConceptNodes",
                    "type": "integer"
                },
                "constraint": {
                    "description": "only available on ConceptNodes",
                    "type": "object",
                    "properties": {
                        "type": {
                            "description": "Example: ConceptConstraint",
                            "type": "string"
                        },
                        "path": {
                            "description": "Example: \\Public Studies\\CLINICAL_TRIAL\\Demography\\Age\\ ",
                            "type": "string"
                        }
                    }
                }
            }
        },
        "patient": {
            "type": "object",
            "properties": {
                "age": {
                    "type": "integer"
                },
                "birthDate": {
                    "type": "string",
                    "format": "date"
                },
                "deathDate": {
                    "type": "string",
                    "format": "date"
                },
                "id": {
                    "type": "integer"
                },
                "inTrialId": {
                    "type": "integer"
                },
                "maritalStatus": {
                    "type": "string"
                },
                "race": {
                    "type": "string"
                },
                "religion": {
                    "type": "string"
                },
                "sex": {
                    "type": "string"
                },
                "trial": {
                    "type": "string"
                }
            }
        },
        "observations": {
            "type": "object",
            "properties": {
                "header": {
                    "type": "object",
                    "properties": {
                        "dimensionDeclarations": {
                            "type": "array",
                            "items": {
                                "$ref": "#/definitions/dimensionDeclaration"
                            }
                        }
                    }
                },
                "cells": {
                    "type": "array",
                    "items": {
                        "$ref": "#/definitions/cell"
                    }
                },
                "footer": {
                    "type": "object",
                    "properties": {
                        "dimensions": {
                            "type": "array",
                            "items": {
                                "type": "array",
                                "items": {
                                    "$ref": "#/definitions/dimensionValue"
                                }
                            }
                        }
                    }
                }
            }
        },
        "dimensionDeclaration": {
            "type": "object",
            "properties": {
                "inline": {
                    "description": "if true, this dimension will be inlined in the cell. only pressent if true.",
                    "type": "boolean"
                },
                "fields": {
                    "description": "fields is omitted if the dimension consists of one field",
                    "type": "array",
                    "items": {
                        "$ref": "#/definitions/field"
                    }
                },
                "name": {
                    "type": "string"
                },
                "type": {
                    "description": "STRING, INTEGER, DATE, OBJECT",
                    "type": "string"
                }
            }
        },
        "field": {
            "type": "object",
            "properties": {
                "name": {
                    "type": "string"
                },
                "type": {
                    "description": "STRING, INTEGER, DATE",
                    "type": "string"
                }
            }
        },
        "cell": {
            "type": "object",
            "description": "numericValue or stringValue, never both",
            "properties": {
                "dimensionIndexes": {
                    "description": "the index in the array is equal to the index of the dimension in the dimensions array in the footer. The number is the index of the dimensionValue in the dimension",
                    "type": "array",
                    "items": {
                        "type": "integer"
                    }
                },
                "inlineDimensions": {
                    "type": "array",
                    "items": {
                        "$ref": "#/definitions/dimensionValue"
                    }
                },
                "numericValue": {
                    "type": "number"
                },
                "stringValue": {
                    "type": "string"
                }
            }
        },
        "dimensionValue": {
            "type": "object",
            "description": "the structure of this value is described in the header. The order of the dimensionValues is determent by the order of the dimensionDeclaration in the header"
        },
        "StudyResponse": {
            "title": "StudiesResponse",
            "type": "object",
            "items": {
                "title": "StudyArray",
                "type": "array",
                "items": {
                    "title": "study",
                    "type": "object",
                    "properties": {
                        "id": {
                            "type": "string"
                        },
                        "accessibleByUser": {
                            "type": "object",
                            "properties": {
                                "view": {
                                    "type": "boolean"
                                },
                                "export": {
                                    "type": "boolean"
                                }
                            }
                        },
                        "_embedded": {
                            "type": "object",
                            "properties": {
                                "ontologyTerm": {
                                    "type": "object",
                                    "properties": {
                                        "name": {
                                            "type": "string"
                                        },
                                        "key": {
                                            "type": "string"
                                        },
                                        "fullName": {
                                            "type": "string"
                                        },
                                        "patientCount": {
                                            "type": "integer"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}