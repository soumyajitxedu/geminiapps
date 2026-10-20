package com.example.quiz

import com.example.data.model.QuizPayload
import com.example.data.model.QuizQuestion
import org.json.JSONObject

object QuizParser {

    const val SAMPLE_QUIZ_JSON = """{
  "chapter": "Union Parliament",
  "questions": [
    {
      "question": "What is the maximum strength of the Lok Sabha?",
      "options": ["545", "550", "552", "560"],
      "answer": "552",
      "explanation": "The maximum strength is 552. Option 545 is the current strength. 550 and 560 are incorrect."
    },
    {
      "question": "Who presides over the joint session of both Houses of Parliament?",
      "options": ["President of India", "Prime Minister", "Speaker of Lok Sabha", "Chairman of Rajya Sabha"],
      "answer": "Speaker of Lok Sabha",
      "explanation": "Under Article 118(4) of the Indian Constitution, the Speaker of the Lok Sabha presides over a joint sitting of both Houses."
    },
    {
      "question": "What is the minimum age required to become a member of the Rajya Sabha?",
      "options": ["21 years", "25 years", "30 years", "35 years"],
      "answer": "30 years",
      "explanation": "To qualify for election to the Rajya Sabha (Council of States), a candidate must not be less than 30 years of age, whereas for Lok Sabha it is 25 years."
    }
  ]
}"""

    const val AI_PROMPT_TEMPLATE = """Please generate a 10-question multiple-choice quiz based on the following chapter: [INSERT CHAPTER NAME HERE]. The output must be strictly valid JSON with no markdown formatting. Use this exact schema: {"chapter": "String", "questions": [{"question": "String", "options": ["String", "String", "String", "String"], "answer": "String", "explanation": "String explaining why the correct answer is right and why the other options are wrong"}]}."""

    fun parse(rawJson: String): Result<QuizPayload> {
        return try {
            // Clean markdown code blocks if present
            var cleaned = rawJson.trim()
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.removePrefix("```json").trim()
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.removePrefix("```").trim()
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.removeSuffix("```").trim()
            }

            val jsonObject = JSONObject(cleaned)
            val chapter = jsonObject.optString("chapter", "ICSE Study Quiz")
            val questionsArray = jsonObject.getJSONArray("questions")

            if (questionsArray.length() == 0) {
                return Result.failure(IllegalArgumentException("Quiz must contain at least one question."))
            }

            val questionList = mutableListOf<QuizQuestion>()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val questionText = qObj.getString("question")
                val answer = qObj.getString("answer")
                val explanation = qObj.optString("explanation", "No explanation provided.")

                val optionsArray = qObj.getJSONArray("options")
                val optionsList = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    optionsList.add(optionsArray.getString(j))
                }

                if (optionsList.isEmpty()) {
                    return Result.failure(IllegalArgumentException("Question ${i + 1} has no options."))
                }

                questionList.add(
                    QuizQuestion(
                        question = questionText,
                        options = optionsList,
                        answer = answer,
                        explanation = explanation
                    )
                )
            }

            Result.success(QuizPayload(chapter = chapter, questions = questionList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
