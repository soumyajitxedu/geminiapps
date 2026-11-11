package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class IcseExamPaper(
    val id: String,
    val subjectTitle: String,
    val paperCode: String,
    val dateStr: String, // "yyyy-MM-dd"
    val displayDate: String, // e.g. "Feb 15, 2027"
    val time: String, // e.g. "10:15 AM"
    val duration: String, // e.g. "2 hrs"
    val mappedSubjectTag: String, // e.g. "Physics", "Chemistry", "Biology", "Maths", "History", "Geography", "Literature", "English", "Computer Applications", "L2"
    val emoji: String,
    val keyCheckpoints: List<String>
)

data class IcseExamRule(
    val title: String,
    val description: String,
    val isBannedItem: Boolean = false,
    val isCrucialTiming: Boolean = false,
    val iconEmoji: String
)

object ExamCountdownUtil {

    // Official ICSE 2027 Examination Timetable Target Dates
    const val PRE_BOARD_DATE = "2026-12-15"
    const val BOARD_EXAM_DATE = "2027-02-15"
    const val BOARD_EXAM_DISPLAY = "Feb 15, 2027"

    val official2027Timetable: List<IcseExamPaper> = listOf(
        IcseExamPaper(
            id = "ENG_LANG",
            subjectTitle = "English Language",
            paperCode = "English Paper 1",
            dateStr = "2027-02-15",
            displayDate = "Feb 15, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "English",
            emoji = "📝",
            keyCheckpoints = listOf("Composition & Essays", "Letter Writing", "Notice & Email", "Unseen Comprehension", "Functional Grammar")
        ),
        IcseExamPaper(
            id = "ENG_LIT",
            subjectTitle = "Literature in English",
            paperCode = "English Paper 2",
            dateStr = "2027-02-18",
            displayDate = "Feb 18, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Literature",
            emoji = "📖",
            keyCheckpoints = listOf("Julius Caesar Act III-V", "Treasure Chest Prose", "Treasure Chest Poetry", "Character Motifs & Quotes")
        ),
        IcseExamPaper(
            id = "MATHS",
            subjectTitle = "Mathematics",
            paperCode = "Mathematics",
            dateStr = "2027-02-22",
            displayDate = "Feb 22, 2027",
            time = "10:15 AM",
            duration = "3 hrs",
            mappedSubjectTag = "Maths",
            emoji = "📐",
            keyCheckpoints = listOf("Commercial Maths (GST, Banking)", "Algebra & Matrices", "Coordinate Geometry", "Geometry & Circles", "Trigonometry & Heights", "Statistics & Ogive")
        ),
        IcseExamPaper(
            id = "HIST_CIVICS",
            subjectTitle = "History & Civics",
            paperCode = "H.C.G. Paper 1",
            dateStr = "2027-02-26",
            displayDate = "Feb 26, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "History",
            emoji = "🏛️",
            keyCheckpoints = listOf("Union Parliament & Judiciary", "First War of Independence 1857", "National Movement & Gandhian Era", "World Wars & United Nations", "NAM Movement")
        ),
        IcseExamPaper(
            id = "GEOGRAPHY",
            subjectTitle = "Geography",
            paperCode = "H.C.G. Paper 2",
            dateStr = "2027-03-01",
            displayDate = "Mar 01, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Geography",
            emoji = "🌍",
            keyCheckpoints = listOf("Topographical Map Survey", "Map of India", "Climate & Soils", "Water & Mineral Resources", "Agro & Mineral Industries", "Waste Management")
        ),
        IcseExamPaper(
            id = "PHYSICS",
            subjectTitle = "Physics",
            paperCode = "Science Paper 1",
            dateStr = "2027-03-08",
            displayDate = "Mar 08, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Physics",
            emoji = "⚡",
            keyCheckpoints = listOf("Force, Work, Power & Energy", "Light Refraction, Lens & Spectrum", "Sound & Resonance", "Current Electricity & Magnetism", "Calorimetry & Radioactivity")
        ),
        IcseExamPaper(
            id = "CHEMISTRY",
            subjectTitle = "Chemistry",
            paperCode = "Science Paper 2",
            dateStr = "2027-03-12",
            displayDate = "Mar 12, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Chemistry",
            emoji = "🧪",
            keyCheckpoints = listOf("Periodic Properties & Bonding", "Acids, Bases & Analytical Chem", "Mole Concept & Stoichiometry", "Electrolysis & Metallurgy", "Study of Compounds (HCl, NH3, HNO3, H2SO4)", "Organic Chemistry")
        ),
        IcseExamPaper(
            id = "BIOLOGY",
            subjectTitle = "Biology",
            paperCode = "Science Paper 3",
            dateStr = "2027-03-15",
            displayDate = "Mar 15, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Biology",
            emoji = "🧬",
            keyCheckpoints = listOf("Cell Division & Genetics", "Plant Physiology (Absorption, Transpiration, Photosynthesis)", "Human Anatomy & Physiology (Circulatory, Excretory, Nervous, Endocrine, Reproductive)", "Population & Pollution")
        ),
        IcseExamPaper(
            id = "COMP_APP",
            subjectTitle = "Computer Applications",
            paperCode = "Group III Paper",
            dateStr = "2027-03-17",
            displayDate = "Mar 17, 2027",
            time = "10:15 AM",
            duration = "2 hrs",
            mappedSubjectTag = "Computer Applications",
            emoji = "💻",
            keyCheckpoints = listOf("OOP Concepts & Class Architecture", "User Defined Methods & Constructors", "Library Classes & Wrapper Objects", "Arrays & Sorting Algorithms", "String Handling & Tokenizers")
        ),
        IcseExamPaper(
            id = "L2_LANG",
            subjectTitle = "Second Language (Hindi / Bengali / etc.)",
            paperCode = "Language Paper 2",
            dateStr = "2027-03-19",
            displayDate = "Mar 19, 2027",
            time = "10:15 AM",
            duration = "3 hrs",
            mappedSubjectTag = "L2",
            emoji = "📜",
            keyCheckpoints = listOf("Essay & Letter Writing", "Unseen Passage Comprehension", "Grammar & Idioms", "Sahitya Sagar / Specified Text Prose & Poetry")
        )
    )

    val official2027Rules: List<IcseExamRule> = listOf(
        // Strict Prohibitions (NEW FOR 2027)
        IcseExamRule(
            title = "Strict Ban: AI-enabled Smart Glasses",
            description = "AI-enabled smart spectacles, smart eyewear, and augmented-reality lenses are strictly prohibited. Possession leads to immediate disqualification and cancellation of examination candidatures.",
            isBannedItem = true,
            iconEmoji = "👓"
        ),
        IcseExamRule(
            title = "Strict Ban: Smart Rings & Wearable Trackers",
            description = "Smart rings (e.g. Ultrahuman, Oura), smartwatches, digital bands, and biometric health monitors are barred from all exam centres. Only ordinary analogue watches with standard needles are permitted.",
            isBannedItem = true,
            iconEmoji = "💍"
        ),
        IcseExamRule(
            title = "Strict Ban: Bluetooth Spectacles & Audio Devices",
            description = "Eyeglasses with bone-conduction transducers, hidden micro-Bluetooth receivers, concealed wireless earbuds, or electronic audio aids are totally prohibited.",
            isBannedItem = true,
            iconEmoji = "🎧"
        ),
        IcseExamRule(
            title = "Strict Ban: Smart Pens & Digital Transmitters",
            description = "Smart pens equipped with internal micro-cameras, optical scanners, recording chips, or wireless transmitting modules are strictly banned and will be confiscated.",
            isBannedItem = true,
            iconEmoji = "🖊️"
        ),
        // Timing & Seating Protocol
        IcseExamRule(
            title = "Seating Protocol: Seated by 9:45 AM",
            description = "Candidates must be seated by 9:45 AM inside the examination hall for all 10:15 AM morning exams. Late arrivals after question distribution will strictly not be entertained.",
            isCrucialTiming = true,
            iconEmoji = "⏰"
        ),
        IcseExamRule(
            title = "15-Minute Dedicated Reading Period",
            description = "Question papers are distributed at 10:00 AM. 10:00 AM to 10:15 AM is allocated solely for reading questions and planning answers. Writing on answer sheets during this time is prohibited.",
            isCrucialTiming = true,
            iconEmoji = "📖"
        ),
        // Dress Code & Permitted Items
        IcseExamRule(
            title = "Official School Uniform & ID",
            description = "Candidates must appear in their proper, complete school uniform with official school crest along with their student identification card.",
            isBannedItem = false,
            iconEmoji = "👔"
        ),
        IcseExamRule(
            title = "Mandatory Admit Card",
            description = "Carry the original, laminated or crisp ICSE 2027 Hall Ticket / Admit Card signed by the School Principal. Entry without the official hall ticket is disallowed.",
            isBannedItem = false,
            iconEmoji = "🪪"
        ),
        IcseExamRule(
            title = "Transparent Stationery Pouch Only",
            description = "Only see-through transparent pouches are allowed. Carry blue or black ballpoint/gel pens, HB pencils, eraser, sharpener, standard 15cm/30cm scale, and geometry box for Maths/Geography.",
            isBannedItem = false,
            iconEmoji = "✏️"
        ),
        IcseExamRule(
            title = "Transparent Water Bottle",
            description = "Clear transparent water bottles without any promotional labels, branding wraps, or stickers are permitted on the examination desk.",
            isBannedItem = false,
            iconEmoji = "💧"
        ),
        // Answer Sheet Protocol
        IcseExamRule(
            title = "Answer Booklet Identification",
            description = "Clearly write your Unique Identification (UID) Number, Index Number, and Subject on the cover of the main answer booklet and every supplementary continuation sheet used.",
            isBannedItem = false,
            iconEmoji = "📋"
        )
    )

    /**
     * Calculates days remaining between the device's current date and the target date (yyyy-MM-dd).
     * Normalizes both timestamps to midnight so partial days count accurately.
     */
    fun getDaysRemaining(targetDateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = sdf.parse(targetDateStr) ?: return 0L

            val currentCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = targetCal.timeInMillis - currentCal.timeInMillis
            if (diffMillis <= 0) return 0L

            TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun getPreBoardDaysLeft(): Long = getDaysRemaining(PRE_BOARD_DATE)

    fun getBoardExamDaysLeft(): Long = getDaysRemaining(BOARD_EXAM_DATE)

    /**
     * Finds the matching official timetable exam for a syllabus subject name.
     */
    fun getExamDateForSubject(subjectName: String): IcseExamPaper? {
        val s = subjectName.trim().lowercase()
        return official2027Timetable.firstOrNull { exam ->
            val mapped = exam.mappedSubjectTag.lowercase()
            when {
                s.contains("physic") -> mapped.contains("physic")
                s.contains("chem") -> mapped.contains("chem")
                s.contains("bio") -> mapped.contains("bio")
                s.contains("math") -> mapped.contains("math")
                s.contains("hist") || s.contains("civic") -> mapped.contains("hist")
                s.contains("geog") -> mapped.contains("geog")
                s.contains("comp") -> mapped.contains("comp")
                s.contains("lit") -> mapped.contains("lit")
                s.contains("eng") -> mapped.contains("eng")
                s.contains("l2") || s.contains("hindi") || s.contains("bengali") -> mapped.contains("l2")
                else -> false
            }
        }
    }

    /**
     * Returns the earliest upcoming exam relative to today's date.
     */
    fun getNextUpcomingExam(): IcseExamPaper {
        val now = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return official2027Timetable.firstOrNull { it.dateStr >= now } ?: official2027Timetable.first()
    }
}
