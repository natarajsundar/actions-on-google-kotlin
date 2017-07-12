import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.ticketmaster.apiai.ApiAiRequest
import com.ticketmaster.apiai.ApiAiResponse
import com.ticketmaster.apiai.apiAiRequest
import com.tmsdurham.actions.*
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import com.tmsdurham.actions.Handler

val gson = Gson()

class MockParameters

object ActionsTest : Spek({
    // ---------------------------------------------------------------------------
    //                   App helpers
    // ---------------------------------------------------------------------------
    /**
     * Describes the behavior for Assistant isNotApiVersionOne_ method.
     */
    describe("ApiAiApp#isNotApiVersionOne") {
        it("Should detect Proto2 when header is not present") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1"
            )
            val mockRequest = RequestWrapper(headers, ApiAiRequest<MockParameters>())
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(mockRequest, mockResponse)

            expect(app.isNotApiVersionOne()).to.equal(false)
        }

        it("Should detect v1 when header is present") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1",
                    "Google-Actions-API-Version" to "1"
            )
            val mockRequest = RequestWrapper(headers, ApiAiRequest<MockParameters>())
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(request = mockRequest, response = mockResponse);

            expect(app.isNotApiVersionOne()).to.equal(false);
        }
        it("Should detect v2 when version is present in APIAI req") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1",
                    "Google-Actions-API-Version" to "2"
            )
            val mockRequest = RequestWrapper(headers, apiAiRequest<MockParameters> {
                result {
                    originalRequest {
                        version = "1"
                    }
                }
            })

            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(request = mockRequest, response = mockResponse)
            app.debug(mockRequest.toString())

            expect(app.isNotApiVersionOne()).to.equal(false)
        }
        it("Should detect v2 when header is present") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1",
                    "Google-Actions-API-Version" to "2"
            )
            val mockRequest = RequestWrapper<ApiAiRequest<MockParameters>>(headers, ApiAiRequest())
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(request = mockRequest, response = mockResponse)

            expect(app.isNotApiVersionOne()).to.equal(true)
        }
        it("Should detect v2 when version is present in APIAI req") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1",
                    "Google-Actions-API-Version" to "2"
            )
            val mockRequest = RequestWrapper(headers, apiAiRequest<MockParameters> {
                originalRequest {
                    version = "2"
                }
            })
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(request = mockRequest, response = mockResponse)

            expect(app.isNotApiVersionOne()).to.equal(true)
        }
    }

    // ---------------------------------------------------------------------------
    //                   API.ai support
    // ---------------------------------------------------------------------------

    /**
     * Describes the behavior for ApiAiApp constructor method.
     */
    describe("ApiAiApp#constructor") {
        // Calls sessionStarted when provided
        it("Calls sessionStarted when new session") {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1"
            )

            val t = TypeToken.get(MockParameters::class.java).type
            val type = TypeToken.getParameterized(ApiAiRequest::class.java, t)
            val body = gson.fromJson<ApiAiRequest<MockParameters>>(
                    """{
	"id": "ce7295cc-b042-42d8-8d72-14b83597ac1e",
	"timestamp": "2016-10-28T03:05:34.288Z",
	"result": {
		"source": "agent",
		"resolvedQuery": "start guess a number game",
		"speech": "",
		"action": "generate_answer",
		"actionIncomplete": false,
		"parameters": {

		},
		"contexts": [{
			"name": "game",
			"lifespan": 5
		}],
		"metadata": {
			"intentId": "56da4637-0419-46b2-b851-d7bf726b1b1b",
			"webhookUsed": "true",
			"intentName": "start_game"
		},
		"fulfillment": {
			"speech": ""
		},
		"score": 1
	},
	"status": {
		"code": 200,
		"errorType": "success"
	},
	"sessionId": "e420f007-501d-4bc8-b551-5d97772bc50c",
	"originalRequest": {
		"data": {
			"conversation": {
				"type": 1
			}
		}
	}
}""", type.type)


            val mockRequest = RequestWrapper(headers, body)
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val sessionStartedSpy = mock<(() -> Unit)> {}

            val app = ApiAiApp(
                    request = mockRequest,
                    response = mockResponse,
                    sessionStarted = sessionStartedSpy
            )
            app.debug(mockRequest.toString())

            app.handleRequest(handler = {})

            verify(sessionStartedSpy).invoke()
        }
    }

    // Does not call sessionStarted when not new sessoin
    it("Does not call sessionStarted when not new session") {
        val headers = mapOf(
                "Content-Type" to "application/json",
                "google-assistant-api-version" to "v1"
        )

        val t = TypeToken.get(MockParameters::class.java).type
        val type = TypeToken.getParameterized(ApiAiRequest::class.java, t)
        val body = gson.fromJson<ApiAiRequest<MockParameters>>(
                """{
            "id": "ce7295cc-b042-42d8-8d72-14b83597ac1e",
            "timestamp": "2016-10-28T03:05:34.288Z",
            "result": {
            "source": "agent",
            "resolvedQuery": "start guess a number game",
            "speech": "",
            "action": "generate_answer",
            "actionIncomplete": false,
            "parameters": {

        },
            "contexts": [
            {
                "name": "game",
                "lifespan": 5
            }
            ],
            "metadata": {
            "intentId": "56da4637-0419-46b2-b851-d7bf726b1b1b",
            "webhookUsed": "true",
            "intentName": "start_game"
        },
            "fulfillment": {
            "speech": ""
        },
            "score": 1
        },
            "status": {
            "code": 200,
            "errorType": "success"
        },
            "sessionId": "e420f007-501d-4bc8-b551-5d97772bc50c",
            "originalRequest": {
            "data": {
            "conversation": {
            "type": 2
        }
        }
        }
        }""", type.type)
        val mockRequest = RequestWrapper <ApiAiRequest <MockParameters>>(headers, body)
        val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

        val sessionStartedSpy = mock<(() -> Unit)> {}

        val app = ApiAiApp(
                request = mockRequest,
                response = mockResponse,
                sessionStarted = sessionStartedSpy
        )

        app.handleRequest(handler = {})

        verifyZeroInteractions(sessionStartedSpy)
    }


    //TODO 2 tests

    /**
     * Describes the behavior for ApiAiApp tell method.
     */
    describe("ApiAiApp#tell") {
        // Success case test, when the API returns a valid 200 response with the response object
        it("Should return the valid JSON in the response object for the success case.", {
            val headers = mapOf(
                    "Content-Type" to "application/json",
                    "google-assistant-api-version" to "v1"
            )

            val t = TypeToken.get(MockParameters::class.java).type
            val type = TypeToken.getParameterized(ApiAiRequest::class.java, t)
            val body = gson.fromJson<ApiAiRequest<MockParameters>>(
                    """{
                "id": "ce7295cc-b042-42d8-8d72-14b83597ac1e",
                "timestamp": "2016-10-28T03:05:34.288Z",
                "result": {
                "source": "agent",
                "resolvedQuery": "start guess a number game",
                "speech": "",
                "action": "generate_answer",
                "actionIncomplete": false,
                "parameters": {

            },
                "contexts": [
                {
                    "name": "game",
                    "lifespan": 5
                }
                ],
                "metadata": {
                "intentId": "56da4637-0419-46b2-b851-d7bf726b1b1b",
                "webhookUsed": "true",
                "intentName": "start_game"
            },
                "fulfillment": {
                "speech": ""
            },
                "score": 1
            },
                "status": {
                "code": 200,
                "errorType": "success"
            },
                "sessionId": "e420f007-501d-4bc8-b551-5d97772bc50c",
                "originalRequest": {
                "data": {
                "conversation": {
                "type": 2
            }
            }
            }
            }""", type.type)
            val mockRequest = RequestWrapper<ApiAiRequest<MockParameters>>(headers, body)
            val mockResponse = ResponseWrapper<ApiAiResponse<MockParameters>>()

            val app = ApiAiApp(request = mockRequest, response = mockResponse)

            val handler: Handler<ApiAiRequest<MockParameters>, ApiAiResponse<MockParameters>, MockParameters> = {
                app.tell("hello")
            }

            val actionMap = mapOf("generate_answer" to handler)

            app.handleRequest(actionMap);

            // Validating the response object
            val expectedResponse = """{
                "speech": "hello",
                "data": {
                "google": {
                "expect_user_response": false,
                "is_ssml": false,
                "no_input_prompts": [

                ]
            }
            },
                contextOut: [

                ]
            }"""
            val responseType = TypeToken.getParameterized(ApiAiResponse::class.java, t)
            expect(mockResponse.body).to.equal(gson.fromJson(expectedResponse, responseType.type))
        })
    }
})