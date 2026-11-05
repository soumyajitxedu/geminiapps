package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class FlashcardItem(
    val front: String,
    val back: String
)

data class FlashcardDeck(
    val chapter: String,
    val cards: List<FlashcardItem>
)

data class MindMapNode(
    val title: String,
    val children: List<MindMapNode> = emptyList()
)

data class MindMapData(
    val chapter: String,
    val root: MindMapNode
)

object StudyToolParser {

    /**
     * Sanitizes raw text input to remove markdown code fences, e.g., ```json ... ```
     */
    fun sanitizeJson(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json", ignoreCase = true)) {
            clean = clean.substring(7)
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3)
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length - 3)
        }
        return clean.trim()
    }

    /**
     * Parses JSON string into FlashcardDeck. Throws exception if invalid.
     */
    fun parseFlashcards(rawJson: String): FlashcardDeck {
        val clean = sanitizeJson(rawJson)
        val json = JSONObject(clean)
        val chapter = json.optString("chapter", "ICSE Revision")
        val cardsArray = json.getJSONArray("cards")

        val cardList = mutableListOf<FlashcardItem>()
        for (i in 0 until cardsArray.length()) {
            val cardObj = cardsArray.getJSONObject(i)
            val front = cardObj.getString("front").trim()
            val back = cardObj.getString("back").trim()
            if (front.isNotEmpty() && back.isNotEmpty()) {
                cardList.add(FlashcardItem(front = front, back = back))
            }
        }

        if (cardList.isEmpty()) {
            throw IllegalArgumentException("No cards found in JSON")
        }

        return FlashcardDeck(chapter = chapter, cards = cardList)
    }

    /**
     * Parses JSON string into MindMapData. Throws exception if invalid.
     */
    fun parseMindMap(rawJson: String): MindMapData {
        val clean = sanitizeJson(rawJson)
        val json = JSONObject(clean)
        val chapter = json.optString("chapter", "Mind Map")
        val rootObj = json.getJSONObject("root")
        val rootNode = parseNode(rootObj)
        return MindMapData(chapter = chapter, root = rootNode)
    }

    private fun parseNode(nodeObj: JSONObject): MindMapNode {
        val title = (nodeObj.optString("title").ifBlank {
            nodeObj.optString("name").ifBlank {
                nodeObj.optString("topic", "Topic")
            }
        }).trim()
        val childrenList = mutableListOf<MindMapNode>()
        val arr = nodeObj.optJSONArray("children")
            ?: nodeObj.optJSONArray("subtopics")
            ?: nodeObj.optJSONArray("subTopics")
            ?: nodeObj.optJSONArray("items")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val childObj = arr.optJSONObject(i)
                if (childObj != null) {
                    childrenList.add(parseNode(childObj))
                }
            }
        }
        return MindMapNode(title = title, children = childrenList)
    }

    /**
     * Default Sample Deck for instant preview & study
     */
    fun getSampleFlashcards(): FlashcardDeck {
        return FlashcardDeck(
            chapter = "Physics - Force & Motion",
            cards = listOf(
                FlashcardItem(
                    front = "Define Moment of a Force (Torque).",
                    back = "The turning effect of a force about a pivot point. Torque = Force × Perpendicular Distance (τ = F × r)."
                ),
                FlashcardItem(
                    front = "What is the SI unit of Moment of Force?",
                    back = "Newton-metre (N m). Note: 1 N m = 10^7 dyne cm."
                ),
                FlashcardItem(
                    front = "What are the two conditions for complete mechanical equilibrium?",
                    back = "1. Resultant of all external forces must be zero (ΣF = 0).\n2. Algebraic sum of moments about any point must be zero (Στ = 0)."
                ),
                FlashcardItem(
                    front = "Where does the centre of gravity of a uniform ring lie?",
                    back = "At the centre of the ring, outside the physical material."
                ),
                FlashcardItem(
                    front = "Distinguish between Stable, Unstable, and Neutral Equilibrium.",
                    back = "• Stable: CG rises when tilted, returns to rest.\n• Unstable: CG lowers when tilted, topples.\n• Neutral: CG remains at constant height."
                ),
                FlashcardItem(
                    front = "State the Principle of Moments.",
                    back = "In equilibrium, the sum of clockwise moments about a pivot equals the sum of anticlockwise moments about the same pivot."
                )
            )
        )
    }

    /**
     * Default Sample Mind Map featuring a deep 3-layer hierarchy
     */
    fun getSampleMindMap(): MindMapData {
        return MindMapData(
            chapter = "Physics: Force & Rotational Motion",
            root = MindMapNode(
                title = "FORCE & ROTATIONAL MOTION",
                children = listOf(
                    MindMapNode(
                        title = "1. Moment of Force & Torque",
                        children = listOf(
                            MindMapNode(
                                title = "Definition & Formula",
                                children = listOf(
                                    MindMapNode("Torque (τ) = Force (F) × Perpendicular Distance (r)"),
                                    MindMapNode("SI Unit: N m (1 N m = 10⁷ dyne cm)"),
                                    MindMapNode("Pivoted body produces rotational turning effect")
                                )
                            ),
                            MindMapNode(
                                title = "Sign Conventions & Couple",
                                children = listOf(
                                    MindMapNode("Anticlockwise = Positive (+), Clockwise = Negative (−)"),
                                    MindMapNode("Couple: Pair of equal, opposite parallel forces"),
                                    MindMapNode("Principle of Moments: Σ Clockwise = Σ Anticlockwise")
                                )
                            )
                        )
                    ),
                    MindMapNode(
                        title = "2. Centre of Gravity (CG)",
                        children = listOf(
                            MindMapNode(
                                title = "Key Properties",
                                children = listOf(
                                    MindMapNode("Point where entire weight acts vertically downwards"),
                                    MindMapNode("Can lie outside body material (e.g. Ring, Hollow Sphere)"),
                                    MindMapNode("Independent of orientation; depends only on mass distribution")
                                )
                            ),
                            MindMapNode(
                                title = "Equilibrium Classes",
                                children = listOf(
                                    MindMapNode("Stable: CG rises when tilted, restores itself"),
                                    MindMapNode("Unstable: CG lowers when tilted, topples easily"),
                                    MindMapNode("Neutral: CG height remains constant on displacement")
                                )
                            )
                        )
                    ),
                    MindMapNode(
                        title = "3. Uniform Circular Motion",
                        children = listOf(
                            MindMapNode(
                                title = "Dynamics of Radial Force",
                                children = listOf(
                                    MindMapNode("Centripetal Force: Real force towards center (F = mv²/r)"),
                                    MindMapNode("Centrifugal Force: Fictitious inertial reaction"),
                                    MindMapNode("Work done by centripetal force is zero (W = F·d·cos 90° = 0)")
                                )
                            ),
                            MindMapNode(
                                title = "Kinematic Features",
                                children = listOf(
                                    MindMapNode("Constant scalar speed with variable directional velocity"),
                                    MindMapNode("Continuous radial acceleration: a = v²/r"),
                                    MindMapNode("Periodic motion with constant angular velocity (ω = 2π/T)")
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
