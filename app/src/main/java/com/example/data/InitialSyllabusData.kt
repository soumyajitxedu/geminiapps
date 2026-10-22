package com.example.data

import com.example.data.model.ChapterProgress

object InitialSyllabusData {
    val subjectsWithChapters: Map<String, List<String>> = linkedMapOf(
        "Physics" to listOf(
            "Force", "WPE", "Machines", "Refraction", "Lens", "Spectrum",
            "Sound", "Electricity", "Household", "Electromagnetism",
            "Calorimetry", "Radioactivity"
        ),
        "Chemistry" to listOf(
            "Periodic Table", "Chemical Bonding", "Acid Base Salt", "Analytical Chemistry",
            "Mole Concept", "Electrolysis", "Metallurgy", "HCl", "NH3", "HNO3",
            "H2SO4", "Organic Chemistry", "Practical"
        ),
        "Biology" to listOf(
            "Chromosome", "Genetics", "Absorption", "Transpiration", "Photosynthesis",
            "Chemical Coord", "Circulatory", "Excretory", "Nervous", "Sense organ",
            "Endocrine", "Reproductive", "Evolution", "Population", "Pollution"
        ),
        "Maths" to listOf(
            "Banking", "GST", "Shares and Dividends", "Quadratic equation", "Linear inequality",
            "Factor Theorem", "Matrices", "Ratio Proportion", "AP and GP", "Reflection",
            "Coordinate Geometry", "Loci", "Circles", "Similarity", "3D Shapes",
            "Trigonometry", "Heights and Distances", "Probability", "Mean Median Mode", "Histogram and Ogive"
        ),
        "History" to listOf(
            "Revolt of 1857", "Rise of Nationalism", "Early Nationalists", "Assertive Nationalists",
            "Muslim League", "Gandhian Era", "Quit India", "Netaji and Forward Bloc",
            "Partition of India", "First World War", "Rise of Dictatorship", "Second World War",
            "United Nations", "UN Agencies", "NAM", "Union Parliament", "Prime Minister",
            "President and VP", "Supreme Court", "High and Lower Courts"
        ),
        "Geography" to listOf(
            "Climate", "Soil", "Natural Vegetation", "Water Resources", "Mineral and Energy",
            "Mineral based Industries", "Agriculture in India I", "Agriculture in India II",
            "Agro Based Industries", "Transport", "Waste Management I", "Waste Management II",
            "Map", "Topo"
        ),
        "Literature" to listOf(
            "JC Act III Scene 1", "JC Act III Scene 2", "JC Act III Scene 3",
            "JC Act IV Scene 1", "JC Act IV Scene 2", "JC Act IV Scene 3",
            "JC Act V Scene 1", "JC Act V Scene 2", "JC Act V Scene 3", "JC Act V Scene 4", "JC Act V Scene 5",
            "With The Photographer", "The Pedestrian", "The Elevator", "The Girl Who Can",
            "The Last Lesson", "The Glove and the Lions", "A Considerable Speck",
            "Haunted Houses", "When Great Trees fall", "The Power of Music"
        ),
        "L2" to listOf(
            "Bama", "Naam", "Asohojogee", "Gandhota Khub Sandehojanok", "Lokti",
            "Bangabhoomir Proti", "Kuli-Mojur", "Aabar Asibo Phire", "Amalkanti", "Kho-Kho"
        ),
        "Computer Applications" to listOf(
            "Library Classes", "String Handling", "User defined Methods", "Constructor",
            "Arrays", "Class as the Basis of all Computation", "Encapsulation and Inheritance"
        )
    )

    fun createInitialChaptersList(): List<ChapterProgress> {
        val list = mutableListOf<ChapterProgress>()
        var globalIndex = 0
        subjectsWithChapters.forEach { (subject, chapters) ->
            chapters.forEach { chapterName ->
                list.add(
                    ChapterProgress(
                        id = "${subject}_${chapterName.replace(" ", "_")}",
                        subject = subject,
                        chapterName = chapterName,
                        isLearned = false,
                        isRevised = false,
                        isPyqsDone = false,
                        notes = "",
                        customUrl = "",
                        orderIndex = globalIndex++
                    )
                )
            }
        }
        return list
    }
}
